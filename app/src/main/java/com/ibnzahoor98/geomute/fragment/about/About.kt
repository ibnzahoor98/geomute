package com.ibnzahoor98.geomute.fragment.about

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.ibnzahoor98.geomute.Constants
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.databinding.FragmentAboutBinding


class About : Fragment()  , View.OnClickListener {

    private lateinit var binding: FragmentAboutBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentAboutBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAboutBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.facebook.setOnClickListener(this)
        binding.linkedin.setOnClickListener(this)


        return binding.root
    }


    override fun onClick(p0: View?) {
        if (p0?.id == R.id.linkedin) {
            var intent =
                Intent(Intent.ACTION_VIEW, Uri.parse("linkedin://${Constants.SOCIAL_LINKEDIN}"))
            val packageManager = requireContext()!!.packageManager
            val list =
                packageManager.queryIntentActivities(intent!!, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.isEmpty()) {
                intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(Constants.SOCIAL_LINKEDIN)
                )
            }
            startActivity(intent!!)
        }
        if (p0?.id == R.id.facebook) {

            try {
                requireContext()!!.packageManager.getPackageInfo("com.facebook.katana", 0)
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("fb://"+Constants.SOCIAL_FACEBOOK)))
            } catch (e: Exception) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SOCIAL_FACEBOOK)))
            }
        }

    }
}