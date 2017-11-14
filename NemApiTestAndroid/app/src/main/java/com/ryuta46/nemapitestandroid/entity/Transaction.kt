package com.ryuta46.nemapitestandroid.entity

data class Transaction(
        val type: Int,
        val version: Int,
        val timestamp: Int,
        val signer: String,
        val fee: Long,
        val deadline: Int,
        val signature: String
// Transaction の種別ごとに入ってくる要素が違う気がするので、どうみるか？ as とか使う？
)
