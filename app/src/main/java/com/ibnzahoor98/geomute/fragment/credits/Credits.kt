package com.ibnzahoor98.geomute.fragment.credits

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.ibnzahoor98.geomute.Constants
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.databinding.FragmentAboutBinding
import com.ibnzahoor98.geomute.databinding.FragmentCreditsBinding

class Credits : Fragment() {

    private lateinit var binding: FragmentCreditsBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentCreditsBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentCreditsBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()




        return binding.root
    }


}