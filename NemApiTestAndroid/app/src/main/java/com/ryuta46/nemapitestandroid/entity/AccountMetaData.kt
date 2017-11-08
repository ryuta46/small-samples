package com.ryuta46.nemapitestandroid.entity

import java.io.Serializable


data class AccountMetaData (
        var status: String = "",
        var remoteStatus: String = "",
        var cosignatoryOf: List<AccountInfo> = listOf(),
        var cosignatories: List<AccountInfo> = listOf()
) : Serializable
