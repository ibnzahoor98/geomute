package com.ibnzahoor98.geomute.fragment.contact

import android.R.id.message
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.google.firebase.auth.FirebaseAuth
import com.ibnzahoor98.geomute.Constants
import com.ibnzahoor98.geomute.R
import com.ibnzahoor98.geomute.databinding.FragmentContactBinding
import com.ibnzahoor98.geomute.databinding.FragmentStoreBinding


class Contact : Fragment() , View.OnClickListener{

    private lateinit var binding: FragmentContactBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var billingClient: BillingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentContactBinding.inflate(layoutInflater)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        binding = FragmentContactBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()

        binding.whatsapp.setOnClickListener(this)
        binding.email.setOnClickListener(this)


        return binding.root
    }



    override fun onClick(p0: View?) {
        if (p0?.id == R.id.whatsapp)
        {

            val url = "https://api.whatsapp.com/send?phone=${Constants.CONTACT_WHATSAPP}"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)

        }
        if (p0?.id == R.id.email)
        {
            val email = Intent(Intent.ACTION_SEND)
            email.putExtra(Intent.EXTRA_EMAIL, arrayOf<String>(Constants.CONTACT_EMAIL_ADDRESS))
            email.putExtra(Intent.EXTRA_SUBJECT, "Need assistance")
            email.putExtra(Intent.EXTRA_TEXT, message)

            //need this to prompts email client only

            //need this to prompts email client only
            email.type = "message/rfc822"

            startActivity(Intent.createChooser(email, "Choose an Email client :"))
        }

    }


}
