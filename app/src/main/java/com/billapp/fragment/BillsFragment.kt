package com.billapp.fragment

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.AppCompatButton
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.billapp.R
import com.billapp.adapter.BillsAdapter
import com.billapp.database.DatabaseHelper
import com.billapp.databinding.FragmentBillsBinding
import com.billapp.listener.DetailListener
import com.billapp.model.Bill
import java.text.DecimalFormat

class BillsFragment : Fragment(), DetailListener {

    private lateinit var binding: FragmentBillsBinding
    private lateinit var dbHelper: DatabaseHelper
    private var quantityValue = ""
    private var rateValue = ""
    private var clearButtonClickCount = 0
    private var isFirstClick = true

    var quantity: Double = 0.0
    var rate: Double = 0.0
    var result: Double = 0.0
    private lateinit var fragmentContext: Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bills, container, false)
        dbHelper = DatabaseHelper(requireContext())
        /*hideKeyboard(requireActivity())
        binding.edtQuantity.requestFocus()*/
        hideKeyboardAndFocusOnEditText(requireContext(), binding.edtQuantity)
        init()
        // Hide the keyboard

        return binding.root
    }

    private fun init() {
        binding.edtRate.setShowSoftInputOnFocus(false)
        binding.edtQuantity.setShowSoftInputOnFocus(false)
        binding.edtQuantity.setSelection(binding.edtQuantity.text!!.length)
        binding.edtRate.setSelection(binding.edtRate.text!!.length)

        val numberButtons = listOf<AppCompatButton>(
            binding.btn0, binding.btn1, binding.btn2, binding.btn3, binding.btn4,
            binding.btn5, binding.btn6, binding.btn7, binding.btn8, binding.btn9,
            binding.btnDot
        )
        numberButtons.forEach { button ->
            button.setOnClickListener {
                if (binding.edtQuantity.isFocused) {
                    quantityValue += button.text
                    binding.edtQuantity.setText(quantityValue)
                    binding.edtQuantity.setSelection(binding.edtQuantity.text!!.length)
                } else if (binding.edtRate.isFocused) {
                    rateValue += button.text
                    binding.edtRate.setText(rateValue)
                    binding.edtRate.setSelection(binding.edtRate.text!!.length)
                }
            }
        }

        /* binding.btnSave.setOnClickListener {
             saveAndShowBillsList()
         }*/

        binding.btnClear.setOnClickListener {
            /*   clearButtonClickCount++

               if (clearButtonClickCount in 4..5) {


               }*/

            if (binding.edtQuantity.text!!.isEmpty() && binding.edtRate.text!!.isEmpty()) {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.app_name)
                builder.setMessage(R.string.deletedb)
                builder.setIcon(android.R.drawable.ic_dialog_alert)

                builder.setPositiveButton("Yes") { dialogInterface, which ->
                    dialogInterface.dismiss()
                    val dbHelper = DatabaseHelper(requireContext())
                    dbHelper.clearDatabase()
                    binding.tvFQty.text = ""
                    binding.tvFTotal.text = ""
                    showBillsList()
                }
                builder.setNegativeButton("No") { dialogInterface, which ->
                    dialogInterface.dismiss()
                }
                val alertDialog: AlertDialog = builder.create()
                alertDialog.setCancelable(false)
                alertDialog.show()
            } else {
                quantityValue = ""
                binding.edtQuantity.setText("")

                rateValue = ""
                binding.edtRate.setText("")
            }


        }

        binding.btnBack.setOnClickListener {
            if (binding.edtQuantity.isFocused && quantityValue.isNotEmpty()) {
                quantityValue = quantityValue.dropLast(1)
                binding.edtQuantity.setText(quantityValue)
                binding.edtQuantity.setSelection(binding.edtQuantity.text!!.length)
            } else if (binding.edtRate.isFocused && rateValue.isNotEmpty()) {
                rateValue = rateValue.dropLast(1)
                binding.edtRate.setText(rateValue)
                binding.edtRate.setSelection(binding.edtRate.text!!.length)
            }
        }

        binding.btnMultiply.setOnClickListener {
            if (binding.edtRate.text!!.isEmpty() && isFirstClick) {
                binding.edtRate.requestFocus()
                isFirstClick = false
            } else {
                calculateMultiplication()
            }
        }

        showBillsList()
    }

    private fun calculateMultiplication() {
        val quantityStr = binding.edtQuantity.text.toString()
        val rateStr = binding.edtRate.text.toString()
        if (quantityStr.isNotEmpty() && rateStr.isNotEmpty()) {
            quantity = quantityStr.toDouble()
            rate = rateStr.toDouble()
            result = quantity * rate
            val bill = Bill(0, quantity, rate, result, "")
            dbHelper.addBill(bill)
            showBillsList()
            clearFields() // Clear fields after calculation
        } else {
            Toast.makeText(
                requireContext(),
                "Please enter both quantity and rate",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun clearFields() {
        quantityValue = ""
        binding.edtQuantity.setText("")

        rateValue = ""
        binding.edtRate.setText("")

        // Clear focus from both fields
        binding.edtQuantity.clearFocus()
        binding.edtRate.clearFocus()

        // Hide the keyboard
        hideKeyboard(requireActivity())

        isFirstClick = true // Reset isFirstClick flag
    }

    private fun saveAndShowBillsList() {
        calculateMultiplication()
    }

    private fun showBillsList() {
        val billsList = dbHelper.getAllBills()
        for (bill in billsList) {
            val quantity = bill.quantity
            val total = bill.total
            val df = DecimalFormat("#.##")
            val formattedNumber = df.format(total)
            binding.tvFTotal.text = formattedNumber
            binding.tvFQty.text = quantity.toString()

        }
        val adapter = BillsAdapter(requireContext(), requireActivity(), billsList, this)
        binding.rvBillItems.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBillItems.adapter = adapter
    }

    override fun onDetailsClick(billId: Int, details: String) {
        dbHelper.updateBillDetails(billId, details)
        showBillsList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContext = context
        val callback: OnBackPressedCallback =
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(R.string.app_name)
                    builder.setMessage(R.string.exitapp)
                    builder.setIcon(android.R.drawable.ic_dialog_alert)

                    builder.setPositiveButton("Yes") { dialogInterface, which ->
                        dialogInterface.dismiss()
                        requireActivity().finish()
                    }
                    builder.setNegativeButton("No") { dialogInterface, which ->
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

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
    fun hideKeyboardAndFocusOnEditText(context: Context, editText: EditText) {
        // Get the InputMethodManager
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Hide the keyboard
        val currentFocusedView = (context as? Activity)?.currentFocus
        currentFocusedView?.let { view ->
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        editText.setCursorVisible(true)
        editText.requestFocus()
    }

    private fun clearFocusAndHideKeyboard() {
        binding.edtQuantity.clearFocus()
        binding.edtRate.clearFocus()
        hideKeyboard(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        clearFocusAndHideKeyboard()
    }
}
