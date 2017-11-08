package com.ryuta46.nemapitestandroid.entity

import java.io.Serializable

data class AccountMetaDataPair(
    var account: AccountInfo = AccountInfo(),
    var meta: AccountMetaData = AccountMetaData()
) : Serializable