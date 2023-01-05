package com.pixelwave.ciphervpn.fragment

import android.app.Activity
import android.content.Context
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
import com.pixelwave.ciphervpn.databinding.FragmentHomeBinding
import com.pixelwave.ciphervpn.model.Server
import com.pixelwave.ciphervpn.viewmodel.HomeViewModel
import com.pixelwave.ciphervpn.viewmodel.ServerSharedViewModel
import de.blinkt.openvpn.OpenVpnApi.startVpn
import de.blinkt.openvpn.core.OpenVPNService
import de.blinkt.openvpn.core.OpenVPNThread
import java.io.IOException

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var serverSharedViewModel: ServerSharedViewModel

    private lateinit var binding: FragmentHomeBinding
    private lateinit var navController: NavController
    private var isConnected: MutableLiveData<Boolean> = MutableLiveData()

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
        isConnected.value = false
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

        binding.connectBtn.setOnClickListener {
            binding.connectBtn.text = getString(R.string.connecting)
            val server = serverSharedViewModel.getSelected().value
            prepareVpn(requireContext(), server!!)
//            getServerResult.launch(
//                Intent(context, ChangeServerActivity::class.java)
//            )
        }

        isConnected.observe(viewLifecycleOwner) {
            if (it) {
                binding.connectBtn.text = getString(R.string.connected)
            }
        }
    }

    private fun startVPN(context: Context, server: Server) {
        try {
            val conf = server.openVpnConfigData
            startVpn(context, conf, server.countryShort, "vpn", "vpn")
            isConnected.value = true
        } catch (exception: IOException) {
            exception.printStackTrace()
        } catch (exception: RemoteException) {
            exception.printStackTrace()
        }
    }

    private val vpnResult =
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

    private fun prepareVpn(context: Context, server: Server) {
//        if (!isConnected.value!!) {
//            if (getInternetStatus()) {
        val intent = VpnService.prepare(context)
        if (intent != null) {
            vpnResult.launch(intent)
        } else {
            startVPN(context, server)
        }
//                status("Connecting")
//            } else {
//                mContext.toast("No Internet Connection")
//            }
//        } else if (stopVpn()) {
//            mContext.toast("Disconnect Successfully")
//        }
    }
}