package com.example.blood

import android.content.Context
import android.widget.Toast

object ToastHelper {
    fun show(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}