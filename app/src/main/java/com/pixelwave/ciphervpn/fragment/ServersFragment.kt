package com.pixelwave.ciphervpn.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pixelwave.ciphervpn.adapter.ServersAdapter
import com.pixelwave.ciphervpn.databinding.FragmentServersBinding
import com.pixelwave.ciphervpn.viewmodel.ServerSharedViewModel
import com.pixelwave.ciphervpn.viewmodel.ServersViewModel

class ServersFragment : Fragment() {

    companion object {
        fun newInstance() = ServersFragment()
    }

    private lateinit var viewModel: ServersViewModel
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
        viewModel = ViewModelProvider(this)[ServersViewModel::class.java]
        serverSharedViewModel =
            ViewModelProvider(requireActivity())[ServerSharedViewModel::class.java]
        navController = NavHostFragment.findNavController(this)

        serversRecyclerView.layoutManager = LinearLayoutManager(context)
        serversAdapter = ServersAdapter()
        serversRecyclerView.adapter = serversAdapter

        viewModel.getServers().observe(viewLifecycleOwner) {
            serversAdapter.setOnItemClickListener(object : ServersAdapter.ItemClickListener {
                override fun onItemClick(view: View, position: Int) {
                    serverSharedViewModel.select(it[position])
                    navController.navigateUp()
                }
            })
            serversAdapter.setData(it as MutableList)
        }
    }

}