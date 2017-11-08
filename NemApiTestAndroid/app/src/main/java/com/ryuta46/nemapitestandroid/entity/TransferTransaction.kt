package com.ryuta46.nemapitestandroid.entity

import com.ryuta46.nemapitestandroid.enum.MessageType
import com.ryuta46.nemapitestandroid.enum.TransactionType
import com.ryuta46.nemapitestandroid.enum.Version
import com.ryuta46.nemapitestandroid.util.ConvertUtil.Companion.toByteArrayWithLittleEndian

class TransferTransaction (
        // 設定必須
        publicKey: ByteArray,
        val receiverAddress: String,
        val ammount: Long, // マイクロNEM単位の数量

        // デフォルト設定のあるパラメータ
        version: Version = Version.Main,
        fee: Long = 0, // マイクロNEM単位の手数料
        val messagePayload: String = "",
        val messageType: MessageType = MessageType.Plain
        )
    : SignedTransaction(TransactionType.Transfer, version, publicKey, fee){

    val transactionBytes: ByteArray
        get() {

            return commonTransactionBytes +
                    toByteArrayWithLittleEndian(receiverAddress.length) +
                    receiverAddress.toByteArray(Charsets.UTF_8) +
                    toByteArrayWithLittleEndian(ammount) +
                    if (messagePayload.isNotEmpty()) {
                        // メッセージフィールド長は messageType長(4バイト) + messagePayload長(4バイト) + messagePayload
                        val payloadBytes = messagePayload.toByteArray(Charsets.UTF_8)
                        val fieldLength = 4 + 4 + payloadBytes.size

                        toByteArrayWithLittleEndian(fieldLength) +
                                toByteArrayWithLittleEndian(messageType.rawValue) +
                                toByteArrayWithLittleEndian(payloadBytes.size) +
                                payloadBytes
                    } else {
                        toByteArrayWithLittleEndian(0)

                    } +
                    toByteArrayWithLittleEndian( 0) // Mosaic 数
        }


    /**
     * 最小の転送トランザクション手数料を計算する
     */
    override fun calculateMinimumTransactionFee() : Long {
        // v0.6.93 ベース
        // 10000 XEM 毎に手数料 0.05 xem
        // 最低手数料は 0.05xem
        // 上限は 1.25 xem
        // メッセージがある場合、0.05 xem 開始で メッセージ長 32バイト毎に 0.05xem

        // 以降全てマイクロNEMで計算する
        // 10_000_000_000 毎に 50_000
        // 最低手数料は 50_000
        // 上限は 1_250_000
        // メッセージがある場合、50_000 開始で メッセージ長 32バイト毎に 50_000

        val xemTransferFee = (ammount / 10_000_000_000L) * 50_000L
        val messageTransferFee =
                if (messagePayload.isNotEmpty()) {
                    50_000L * (1L + messagePayload.toByteArray(Charsets.UTF_8).size / 32L)
                } else {
                    0L
                }
        return Math.max(50_000L, Math.min(xemTransferFee + messageTransferFee, 1_250_000L))
    }

}
