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
import android.os.CountDownTimer
import android.os.RemoteException
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
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
import kotlinx.coroutines.*
import kotlinx.coroutines.GlobalScope.coroutineContext
import java.io.IOException
import kotlin.coroutines.coroutineContext

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
            if (serverSharedViewModel.getConnectionStatus().value == ConnectionStatus.CONNECTION_ATTEMPT)
                connect()

            if (it != null) {
                binding.serverNameTv.text = it.countryLong
                binding.serverIp.text = it.ipAddress
                Glide.with(requireContext())
                    .load(FlagKit.getDrawable(requireContext(), it.countryShort))
                    .into(binding.serverImage)
            }
        }

        binding.connectBtn.setOnClickListener {
            if (serverSharedViewModel.getSelected().value == null) {
                Toast.makeText(requireContext(), "Please select a server", Toast.LENGTH_SHORT)
                    .show()
                navController.navigate(R.id.action_homeFragment_to_serversFragment)
            } else {
                when (serverSharedViewModel.getConnectionStatus().value) {
                    ConnectionStatus.CONNECTED -> disconnect()
                    ConnectionStatus.DISCONNECTED -> connect()
                    else -> {
                    }
                }
            }
        }

        serverSharedViewModel.getConnectionStatus().observe(viewLifecycleOwner) {
            when (it) {
                ConnectionStatus.CONNECTED -> {
                    binding.cpIndicator.visibility = View.VISIBLE
                    binding.cpIndicator.isIndeterminate = false
                    binding.ripple.startRippleAnimation()
                }
                ConnectionStatus.CONNECTING_SUCCESS -> {
                    serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTED)
                }
                ConnectionStatus.DISCONNECTED -> {
                    binding.connectBtn.text = getString(R.string.connect)
                    binding.cpIndicator.visibility = View.INVISIBLE
                    binding.ripple.stopRippleAnimation()
                }
                ConnectionStatus.CONNECTING -> {
                    binding.connectBtn.text = getString(R.string.connecting)
                    binding.cpIndicator.visibility = View.VISIBLE
                    binding.cpIndicator.isIndeterminate = true
                }
                ConnectionStatus.CONNECTION_ATTEMPT -> {
                    binding.connectBtn.text = getString(R.string.connecting)
                    binding.cpIndicator.visibility = View.VISIBLE
                    binding.cpIndicator.isIndeterminate = true
                }
                ConnectionStatus.DISCONNECTING -> {
                    binding.connectBtn.text = getString(R.string.disconnecting)
                    binding.cpIndicator.visibility = View.VISIBLE
                    binding.cpIndicator.isIndeterminate = true
                }
                else -> {
                    binding.connectBtn.text = getString(R.string.connect)
                    binding.cpIndicator.visibility = View.INVISIBLE
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
        serverSharedViewModel.getSelected().value.let { server ->
            if (server != null) {
                prepareConnection(requireContext(), server)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun setStatus(connectionState: String?) {
        Log.i("_OpenVPN_", "Connection state: $connectionState")
        if (connectionState != null) when (connectionState) {
            "DISCONNECTED" -> {
                serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
                OpenVPNService.setDefaultStatus()
            }
            "CONNECTED" -> {
                binding.cpIndicator.setProgressCompat(100, true)
                GlobalScope.launch {
                    delay(1000)
                    withContext(Dispatchers.Main) {
                        serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTING_SUCCESS)
                    }
                }
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
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            broadcastReceiver!!, IntentFilter("countdownTimer"))
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
        binding.downloadSpeedText.text = byteIn.substring(0, byteIn.indexOf(" "))
        binding.uploadSpeedText.text = byteOut.substring(0, byteOut.indexOf(" "))
        binding.measText.text = byteIn.substring(byteIn.indexOf(" ") + 1)
        binding.measDText.text = byteOut.substring(byteOut.indexOf(" ") + 1)

        val connDuration = 1
        val time = duration.split(":")
        val minutes = time[1].toInt()
        val seconds = time[2].toInt()
        val millis = (minutes * 60 + seconds) * 1000
        val millisUntilFinished = (connDuration * 60 * 1000) - millis

        binding.connectBtn.text = String.format("%02d:%02d", minutes, seconds)

        val progress = ((millisUntilFinished * 100) / (connDuration * 60 * 1000))
        binding.cpIndicator.setProgressCompat(progress, true)

        if (progress == 0) {
            disconnect()
        }
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

    private val vpnResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { vpnResult ->
            if (vpnResult.resultCode == Activity.RESULT_OK) {
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
    }
}