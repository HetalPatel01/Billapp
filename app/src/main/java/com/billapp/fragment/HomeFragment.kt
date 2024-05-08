package com.billapp.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatTextView
import com.billapp.R

class HomeFragment : Fragment() {
    private lateinit var fragmentContext: Context
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true)
            {
                override fun handleOnBackPressed() {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.app_name)
                    builder.setMessage(R.string.exitapp)
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    builder.setPositiveButton("Yes"){dialogInterface, which ->
                        dialogInterface.dismiss()
                        requireActivity().finish()
                    }
                    builder.setNegativeButton("No"){dialogInterface, which ->
                        dialogInterface.dismiss()
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.setCancelable(false)
                    alertDialog.show()
                }
            }
        requireActivity().onBackPressedDispatcher.addCallback(
            this,
            callback
        )
    }
}