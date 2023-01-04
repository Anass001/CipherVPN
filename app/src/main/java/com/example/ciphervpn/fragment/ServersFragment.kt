package com.example.ciphervpn.fragment

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ciphervpn.R
import com.example.ciphervpn.adapter.ServersAdapter
import com.example.ciphervpn.databinding.FragmentServersBinding
import com.example.ciphervpn.viewmodel.ServersViewModel

class ServersFragment : Fragment() {

    companion object {
        fun newInstance() = ServersFragment()
    }

    private lateinit var viewModel: ServersViewModel
    private lateinit var binding: FragmentServersBinding
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

        serversRecyclerView.layoutManager = LinearLayoutManager(context)
//        serversAdapter = ServersAdapter()
        serversRecyclerView.adapter = serversAdapter

    }

}