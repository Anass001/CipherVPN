package com.pixelwave.ciphervpn.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.VpnService
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.RemoteException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.bumptech.glide.Glide
import com.murgupluoglu.flagkit.FlagKit
import com.pixelwave.ciphervpn.R
import com.pixelwave.ciphervpn.data.model.ConnectionStatus
import com.pixelwave.ciphervpn.databinding.FragmentHomeBinding
import com.pixelwave.ciphervpn.data.model.Server
import com.pixelwave.ciphervpn.viewmodel.HomeViewModel
import com.pixelwave.ciphervpn.viewmodel.ServerSharedViewModel
import de.blinkt.openvpn.OpenVpnApi.startVpn
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import java.io.IOException

class HomeFragment : Fragment() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var serverSharedViewModel: ServerSharedViewModel

    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController

    private lateinit var vpnThread: OpenVPNThread
    private lateinit var vpnService: OpenVPNService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vpnThread = OpenVPNThread()
        vpnService = OpenVPNService()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        serverSharedViewModel =
            ViewModelProvider(requireActivity())[ServerSharedViewModel::class.java]
        navController = NavHostFragment.findNavController(this)

        binding.serversCard.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_serversFragment)
        }

        serverSharedViewModel.getSelected().observe(viewLifecycleOwner) {
            connect()
        }

        binding.connectBtn.setOnClickListener {
            if (serverSharedViewModel.getConnectionStatus().value == ConnectionStatus.CONNECTED) {
                disconnect()
            } else {
                if (serverSharedViewModel.getSelected().value == null) {
                    Toast.makeText(requireContext(), "Please select a server", Toast.LENGTH_SHORT)
                        .show()
                    navController.navigate(R.id.action_homeFragment_to_serversFragment)
                } else {
                    connect()
                }
            }
        }

        serverSharedViewModel.getConnectionStatus().observe(viewLifecycleOwner) {
            when (it) {
                ConnectionStatus.CONNECTED -> {
                    binding.connectBtn.text = getString(R.string.disconnect)
                }
                ConnectionStatus.DISCONNECTED -> {
                    binding.connectBtn.text = getString(R.string.connect)
                }
                ConnectionStatus.CONNECTING -> {
                    binding.connectBtn.text = getString(R.string.connecting)
                }
                ConnectionStatus.DISCONNECTING -> {
                    binding.connectBtn.text = getString(R.string.disconnecting)
                }
                else -> {
                    binding.connectBtn.text = getString(R.string.connect)
                }
            }
        }
    }

    private fun connect() {
        if (!isNetworkAvailable(requireContext())) {
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT).show()
            return
        }
        establishConnection()
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }


    private fun establishConnection() {
        val server = serverSharedViewModel.getSelected().value
        if (server != null) {
            prepareConnection(requireContext(), server)
            binding.serverNameTv.text = server.countryLong
            binding.serverIp.text = server.ipAddress
            Glide.with(requireContext())
                .load(FlagKit.getDrawable(requireContext(), server.countryShort))
                .into(binding.serverImage)
        }
    }

    fun setStatus(connectionState: String?) {
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
                OpenVPNService.setDefaultStatus()
            }
            "CONNECTED" -> {
                binding.ripple.startRippleAnimation()
                serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTED)
            }
            "WAIT", "AUTH", "RECONNECTING", "TCP_CONNECT" -> {
                serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTING)
            }
            "NONETWORK" -> {
                serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
            }
        }
    }

    private var broadcastReceiver: BroadcastReceiver? = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                setStatus(intent.getStringExtra("state"))
                intent.getStringExtra("state")?.let { Log.i("FUCK_VPN", it) }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            try {
                var duration = intent.getStringExtra("duration")
                var lastPacketReceive = intent.getStringExtra("lastPacketReceive")
                var byteIn = intent.getStringExtra("byteIn")
                var byteOut = intent.getStringExtra("byteOut")
                if (duration == null) duration = "00:00:00"
                if (lastPacketReceive == null) lastPacketReceive = "0"
                if (byteIn == null) byteIn = "0.0"
                if (byteOut == null) byteOut = "0.0"
                updateConnectionStatus(duration, lastPacketReceive, byteIn, byteOut)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onPause() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(
            broadcastReceiver!!
        )
        super.onPause()
    }

    override fun onResume() {
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver!!, IntentFilter("connectionState")
        )
        super.onResume()
    }

    /**
     * Update status UI
     * @param duration: running time
     * @param lastPacketReceive: last packet receive time
     * @param byteIn: incoming data
     * @param byteOut: outgoing data
     */
    fun updateConnectionStatus(
        duration: String,
        lastPacketReceive: String,
        byteIn: String,
        byteOut: String
    ) {
        binding.downloadSpeedText.text = byteIn
        binding.uploadSpeedText.text = byteOut
    }

    private fun startVPN(context: Context, server: Server) {
        try {
            val conf = server.openVpnConfigData
            startVpn(context, conf, server.countryShort, "vpn", "vpn")
        } catch (exception: IOException) {
            serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
            exception.printStackTrace()
        } catch (exception: RemoteException) {
            serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
            exception.printStackTrace()
        }
    }

    private val vpnResult by lazy {
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { vpnResult ->
            if (vpnResult.resultCode == Activity.RESULT_OK) {
                //Permission granted, start the VPN
                serverSharedViewModel.getSelected().value?.let { server ->
                    startVPN(requireContext(), server)
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "For a successful VPN connection, permission must be granted.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun prepareConnection(context: Context, server: Server) {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnResult.launch(intent)
        } else {
            startVPN(context, server)
        }
    }

    private fun disconnect() {
        OpenVPNThread.stop()
        binding.ripple.stopRippleAnimation()
    }
}