package com.billapp.model

data class Bill(
    val id: Int,
    val quantity: Double,
    val rate: Double,
    val total: Double,
    val details: String
)