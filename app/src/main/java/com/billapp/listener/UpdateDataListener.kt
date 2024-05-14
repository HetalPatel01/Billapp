package com.billapp.listener

interface UpdateDataListener {
    fun onUpdateDataClick(billId: Int,qty:Double,rate:Double, detail:String, total:Double)
}