package com.ryuta46.nemapitestandroid.entity

data class NemAnnounceResult(
        val type: Int = 0,
        val code: Int = 0,
        val message: String = "",
        val transactionHash: TransactionData? = null,
        val innerTransactionHash: TransactionData? = null
) {
    class TransactionData(val data: String = "")
}