package com.pixelwave.ciphervpn.fragment

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.VpnService
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.os.RemoteException
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
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

        binding.serversBtn.setOnClickListener {
            navController.navigate(R.id.action_homeFragment_to_serversFragment)
        }

        serverSharedViewModel.getSelected().observe(viewLifecycleOwner) {
            connect()
        }

        binding.connectBtn.setOnClickListener {
            when (serverSharedViewModel.getConnectionStatus().value) {
                ConnectionStatus.CONNECTED -> {
                    disconnect()
                }
                ConnectionStatus.DISCONNECTED -> {
                    connect()
                }
                else -> {}
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
        } else {
            navController.navigate(R.id.action_homeFragment_to_serversFragment)
            Toast.makeText(requireContext(), "Please select a server", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVPN(context: Context, server: Server) {
        try {
            val conf = server.openVpnConfigData
            startVpn(context, conf, server.countryShort, "vpn", "vpn")
            serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTED)
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
        serverSharedViewModel.updateConnectionStatus(ConnectionStatus.DISCONNECTED)
    }
}