package com.quickgstbillprint.model

import java.io.Serializable

data class Bill(
    val id: Int,
    val quantity: Double,
    val rate: Double,
    val total: Double,
    val details: String
): Serializable