package com.ryuta46.nemapitestandroid.entity

import java.io.Serializable

data class AccountInfo(
        var address: String = "",
        var balance: Int = 0,
        var vestedBalance: Int = 0,
        var importance: Double = 0.0,
        var publicKey: String = "",
        var label: String? = null,
        var harvestedBlocks: Int  = 0
) : Serializable
