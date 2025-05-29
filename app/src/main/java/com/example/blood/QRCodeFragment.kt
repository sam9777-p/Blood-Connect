package com.example.blood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.blood.viewmodel.QRCodeViewModel

class QRCodeFragment : Fragment() {

    private lateinit var qrImageView: ImageView
    private val viewModel: QRCodeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_q_r_code, container, false)
        qrImageView = view.findViewById(R.id.qrCodeImage)

        viewModel.qrBitmap.observe(viewLifecycleOwner, Observer { bitmap ->
            qrImageView.setImageBitmap(bitmap)
        })

        viewModel.fetchQRCode()

        return view
    }
}
