package com.ryuta46.nemapitestandroid.entity

data class MosaicAttachment(
        val namespaceId: String,
        val name: String,
        val quantity: Long,
        val supply: Long, // 供給量。手数料計算に必要
        val divisibility: Int // 可分性。手数料計算に必要
)