package com.quickgstbillprint.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.quickgstbillprint.R
import com.quickgstbillprint.database.DatabaseHelper
import com.quickgstbillprint.databinding.RowBillItemBinding
import com.quickgstbillprint.listener.DeleteDataListener
import com.quickgstbillprint.listener.DetailListener
import com.quickgstbillprint.listener.UpdateDataListener
import com.quickgstbillprint.model.Bill
import java.text.DecimalFormat


class BillsAdapter(
    private val context: Context,
    private val activity: Activity,
    private val billsList: List<Bill>,
    private val detailsClickListener: DetailListener,
    private val deleteClickListener: DeleteDataListener,
    private val updateClickListener: UpdateDataListener,
) : RecyclerView.Adapter<BillsAdapter.CategoryViewHolder>() {
    private lateinit var dbHelper: DatabaseHelper

    inner class CategoryViewHolder(val binding: RowBillItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RowBillItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = billsList[position]

        dbHelper = DatabaseHelper(context)

        holder.binding.tvNumber.text = (position + 1).toString()
        holder.binding.tvQty.text = currentItem.quantity.toString()
        val df = DecimalFormat("#.##")
        val formattedNumber = df.format(currentItem.total)
        holder.binding.tvTotal.text = formattedNumber

        val formattedRate = df.format(currentItem.rate)
        holder.binding.tvRate.text = formattedRate

        holder.binding.ivEdit.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_update_data, null)
            val edtDetail = dialogView.findViewById<EditText>(R.id.edtDetail)
            val edtQty = dialogView.findViewById<EditText>(R.id.edtQty)
            val edtRate = dialogView.findViewById<EditText>(R.id.edtRate)
            val tvCancel = dialogView.findViewById<TextView>(R.id.tvCancel)
            val tvTotalU = dialogView.findViewById<TextView>(R.id.tvTotalU)
            val btnDelete = dialogView.findViewById<TextView>(R.id.btnDelete)
            val btnUpdate = dialogView.findViewById<TextView>(R.id.btnUpdate)

            val currentItem = billsList[holder.adapterPosition] // Get the current item

            val id = currentItem.id
            val totalQuantity = currentItem.quantity.toInt()
            val totalAmount = currentItem.total
            val totalRate = currentItem.rate
            val detail = currentItem.details

            if (billsList.isNotEmpty()) {
                val df = DecimalFormat("#.##")
                val formattedTotalAmount = df.format(totalAmount)

                tvTotalU.text = "Total: " + formattedTotalAmount
                edtQty.setText(totalQuantity.toString())
                edtRate.setText(totalRate.toString())
                edtDetail.setText(detail)
            } else {
                edtQty.setText("")
                edtRate.setText("")
                edtDetail.setText("")
                tvTotalU.text = ""
            }

            // TextWatcher to update total when quantity or rate changes
            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    val updatedQty = edtQty.text.toString().toDoubleOrNull() ?: 0.0
                    val updatedRate = edtRate.text.toString().toDoubleOrNull() ?: 0.0
                    val total = updatedQty * updatedRate
                    tvTotalU.text = "Total: " + DecimalFormat("#.##").format(total)
                }
            }

            edtQty.addTextChangedListener(textWatcher)
            edtRate.addTextChangedListener(textWatcher)

            val dialog = AlertDialog.Builder(context)
                .setView(dialogView)
                .create()

            tvCancel.setOnClickListener {
                dialog.dismiss()
            }

            btnDelete.setOnClickListener {
                deleteClickListener.onDeleteDataClick(id)
                dialog.dismiss()
            }

            btnUpdate.setOnClickListener() {
                val updatedDetail = edtDetail.text.toString().trim()
                val updatedQty = edtQty.text.toString().toDoubleOrNull() ?: 0.0
                val updatedRate = edtRate.text.toString().toDoubleOrNull() ?: 0.0
                val total = updatedQty * updatedRate

                updateClickListener.onUpdateDataClick(
                    id,
                    updatedQty,
                    updatedRate,
                    updatedDetail,
                    total
                )

                // Dismiss the dialog after updating
                dialog.dismiss()
            }
            dialog.show()
        }


        holder.binding.tvDetails.setOnClickListener {
            val dialogView =
                LayoutInflater.from(context).inflate(R.layout.dialog_edit_details, null)
            val dialogEditText = dialogView.findViewById<EditText>(R.id.dialog_edtDetails)

            val dialog = AlertDialog.Builder(context)
                .setTitle("Add Details")
                .setView(dialogView)
                .setPositiveButton("Add") { _, _ ->
                    // Get the value from dialog EditText and set it to currentItem
                    val details = dialogEditText.text.toString()
//                    val details = holder.binding.edtDetails.text.toString()
                    detailsClickListener.onDetailsClick(currentItem.id, details)
                    holder.binding.edtDetails.clearFocus()
                    // Hide EditText and show Details TextView
                    holder.binding.edtDetails.visibility = View.GONE
                    holder.binding.tvDetails.visibility = View.VISIBLE

                    // Hide keyboard after submitting

                    hideKeyboard(activity)
                }
                .setNegativeButton("Cancel", null)
                .create()
            dialog.show()
        }

        holder.binding.edtDetails.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                saveDetails(holder, currentItem)
                return@setOnEditorActionListener true
            }
            false
        }

        holder.binding.edtDetails.setText(currentItem.details)

        if (currentItem.details.isEmpty()) {
            holder.binding.edtDetails.visibility = android.view.View.GONE
            holder.binding.tvDetails.visibility = android.view.View.VISIBLE
        } else {
            holder.binding.tvDetails.visibility = android.view.View.GONE
            holder.binding.edtDetails.visibility = android.view.View.VISIBLE
            holder.binding.edtDetails.isFocusable = false
            holder.binding.edtDetails.isFocusableInTouchMode = false
        }
    }

    override fun getItemCount(): Int {
        return billsList.size
    }

    private fun saveDetails(holder: CategoryViewHolder, currentItem: Bill) {
        Log.d("BillsAdapter", "Saving details")
        val details = holder.binding.edtDetails.text.toString()
        detailsClickListener.onDetailsClick(currentItem.id, details)
        holder.binding.edtDetails.clearFocus()
        // Hide EditText and show Details TextView
        holder.binding.edtDetails.visibility = View.GONE
        holder.binding.tvDetails.visibility = View.VISIBLE

        // Hide keyboard after submitting

        hideKeyboard(activity)
    }

    fun hideKeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        var view = activity.currentFocus
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
