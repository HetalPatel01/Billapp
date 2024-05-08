package com.billapp.adapter

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.billapp.R
import com.billapp.databinding.RowBillItemBinding
import com.billapp.listener.DetailListener
import com.billapp.model.Bill
import java.text.DecimalFormat


class BillsAdapter(
    private val context: Context,
    private val activity: Activity,
    private val billsList: List<Bill>,
    private val detailsClickListener: DetailListener
) : RecyclerView.Adapter<BillsAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: RowBillItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = RowBillItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val currentItem = billsList[position]

        holder.binding.tvNumber.text = (position + 1).toString()
        holder.binding.tvQty.text = currentItem.quantity.toString()
        val df = DecimalFormat("#.##")
        val formattedNumber = df.format(currentItem.total)
        holder.binding.tvTotal.text =formattedNumber

        val formattedRate = df.format(currentItem.rate)
        holder.binding.tvRate.text = formattedRate
      /*  holder.binding.tvDetails.setOnClickListener {
            holder.binding.tvDetails.visibility = android.view.View.GONE
            holder.binding.edtDetails.visibility = android.view.View.VISIBLE
            holder.binding.edtDetails.requestFocus()
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(holder.binding.edtDetails, InputMethodManager.SHOW_IMPLICIT)
        }*/
        holder.binding.tvDetails.setOnClickListener {
            val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_details, null)
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
