package com.billapp.listener
import com.billapp.model.Bill

interface DetailListener {
    fun onDetailsClick(billId: Int, details: String)
}