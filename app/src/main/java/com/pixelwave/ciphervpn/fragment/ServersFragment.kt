package com.pixelwave.ciphervpn.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixelwave.ciphervpn.CipherApplication
import com.pixelwave.ciphervpn.adapter.ServersAdapter
import com.pixelwave.ciphervpn.data.model.ConnectionStatus
import com.pixelwave.ciphervpn.databinding.FragmentServersBinding
import com.pixelwave.ciphervpn.viewmodel.ServerSharedViewModel
import com.pixelwave.ciphervpn.viewmodel.ServersViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ServersFragment : Fragment() {

    private val viewModel by viewModels<ServersViewModel>()

    private lateinit var serverSharedViewModel: ServerSharedViewModel

    private lateinit var binding: FragmentServersBinding
    private lateinit var navController: NavController

    private lateinit var serversRecyclerView: RecyclerView
    private lateinit var serversAdapter: ServersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentServersBinding.inflate(inflater, container, false)
        serversRecyclerView = binding.serversRv
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serverSharedViewModel =
            ViewModelProvider(requireActivity())[ServerSharedViewModel::class.java]
        navController = NavHostFragment.findNavController(this)

        binding.progressIndicator.setVisibilityAfterHide(GONE)

        serversRecyclerView.layoutManager = LinearLayoutManager(context)
        serversAdapter = ServersAdapter(serverSharedViewModel)
        serversRecyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
        serversRecyclerView.adapter = serversAdapter

        viewModel.getServers().observe(viewLifecycleOwner) {
            binding.progressIndicator.hide()
            if (!it.isNullOrEmpty()) {
                serversAdapter.setOnItemClickListener(object : ServersAdapter.ItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        if (serverSharedViewModel.getConnectionStatus().value == ConnectionStatus.CONNECTED) {
                            Toast.makeText(context, "Please disconnect first", Toast.LENGTH_SHORT)
                                .show()
                            return
                        }
                        serverSharedViewModel.updateConnectionStatus(ConnectionStatus.CONNECTION_ATTEMPT)
                        serverSharedViewModel.select(it[position])
                        navController.navigateUp()
                    }
                })
                serversAdapter.setData(it as MutableList)
                serversRecyclerView.scheduleLayoutAnimation()
            } else {
                binding.noServersFoundLl.visibility = View.VISIBLE
            }
        }

        binding.retryBtn.setOnClickListener {
            binding.progressIndicator.show()
            binding.noServersFoundLl.visibility = View.GONE
            viewModel.refreshServers()
        }
    }

}