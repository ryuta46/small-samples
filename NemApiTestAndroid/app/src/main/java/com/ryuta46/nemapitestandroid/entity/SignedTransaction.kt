package com.ryuta46.nemapitestandroid.entity

import com.ryuta46.nemapitestandroid.enum.TransactionType
import com.ryuta46.nemapitestandroid.enum.Version
import com.ryuta46.nemapitestandroid.util.ConvertUtil.Companion.toByteArrayWithLittleEndian
import com.ryuta46.nemapitestandroid.util.TimeUtil


abstract class SignedTransaction(
        val type: TransactionType,
        val version: Version,
        val publicKey: ByteArray,
        val fee: Long // マイクロNEM単位の手数料
) {

    protected val commonTransactionBytes: ByteArray
    get() {
        // タイムスタンプ計算
        val timestamp = TimeUtil.currentTimeFromOrigin()
        // deadline はそこから 1 時間後にする
        val deadline = timestamp + 60 * 60

        val transactionFee = Math.max(calculateMinimumTransactionFee(), fee)
        return toByteArrayWithLittleEndian(type.rawValue) +
                toByteArrayWithLittleEndian(version.rawValue + type.versionOffset) +
                toByteArrayWithLittleEndian(timestamp) +
                toByteArrayWithLittleEndian(publicKey.size) +
                publicKey +
                toByteArrayWithLittleEndian(transactionFee) +
                toByteArrayWithLittleEndian(deadline)
    }

    /**
     * 最小の転送トランザクション手数料を計算する
     */
    abstract fun calculateMinimumTransactionFee() : Long

}
