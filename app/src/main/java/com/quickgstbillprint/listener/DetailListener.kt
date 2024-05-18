package com.quickgstbillprint.listener

interface DetailListener {
    fun onDetailsClick(billId: Int, details: String)
}