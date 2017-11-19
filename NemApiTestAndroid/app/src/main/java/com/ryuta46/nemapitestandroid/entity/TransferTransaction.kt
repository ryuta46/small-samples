package com.ryuta46.nemapitestandroid.entity

import com.ryuta46.nemapitestandroid.enum.MessageType
import com.ryuta46.nemapitestandroid.enum.TransactionType
import com.ryuta46.nemapitestandroid.enum.Version
import com.ryuta46.nemapitestandroid.util.ConvertUtil.Companion.toByteArrayWithLittleEndian

class TransferTransaction (
        // 設定必須
        publicKey: ByteArray,
        val receiverAddress: String,
        val amount: Long, // マイクロNEM単位の数量

        // デフォルト設定のあるパラメータ
        version: Version = Version.Main,
        fee: Long = 0, // マイクロNEM単位の手数料
        val messagePayload: String = "",
        val messageType: MessageType = MessageType.Plain,
        var mosaics: MutableList<MosaicAttachment> = mutableListOf()
        )
    : SignedTransaction(TransactionType.Transfer, version, publicKey, fee){

    val transactionBytes: ByteArray
        get() {

            return commonTransactionBytes +
                    toByteArrayWithLittleEndian(receiverAddress.length) +
                    receiverAddress.toByteArray(Charsets.UTF_8) +
                    toByteArrayWithLittleEndian(amount) +
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
                    if (mosaics.isNotEmpty()) {
                        var mosaicBytes = ByteArray(0)
                        mosaics.forEach { mosaic ->
                            val mosaicNameSpaceIdBytes = mosaic.namespaceId.toByteArray(Charsets.UTF_8)
                            val mosaicNameBytes = mosaic.name.toByteArray(Charsets.UTF_8)
                            // モザイクID構造の長さは、
                            // モザイクネームスペース長(4バイト) + モザイクネームスペース + モザイク名長(4バイト) + モザイク名
                            val mosaicIdStructureLength =
                                    4 + mosaicNameSpaceIdBytes.size + 4 + mosaicNameBytes.size

                            // モザイク構造の長さは、
                            // モザイクID構造長(４バイト) + モザイクID構造 + 量(8バイト)
                            val mosaicStructureLength =
                                    4 + mosaicIdStructureLength + 8

                            mosaicBytes += toByteArrayWithLittleEndian(mosaicStructureLength) +
                                    toByteArrayWithLittleEndian(mosaicIdStructureLength) +
                                    toByteArrayWithLittleEndian(mosaicNameSpaceIdBytes.size) +
                                    mosaicNameSpaceIdBytes +
                                    toByteArrayWithLittleEndian(mosaicNameBytes.size) +
                                    mosaicNameBytes +
                                    toByteArrayWithLittleEndian(mosaic.quantity)
                        }
                        toByteArrayWithLittleEndian(mosaics.size) + mosaicBytes
                    } else {
                        toByteArrayWithLittleEndian( 0) // Mosaic 数
                    }

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
        val messageTransferFee = calculateMessageTransferFee(messagePayload)

        val transactionFee =
                if (mosaics.isEmpty()) {
                    calculateMicroNemTransferFee(amount) + messageTransferFee
                } else {
                    var mosaicTransferFeeTotal = 0L
                    mosaics.forEach {
                        mosaicTransferFeeTotal += calculateMosaicTransferFee(it)
                    }
                    //amount / 1_000_000 * mosaicTransferFeeTotal + messageTransferFee
                    mosaicTransferFeeTotal + messageTransferFee
                }
        return transactionFee
    }

    private fun calculateMicroNemTransferFee(xem: Long): Long {
        return Math.max(50_000L, Math.min(((xem / 10_000_000_000L) * 50_000L), 1_250_000L))
    }

    private fun calculateMessageTransferFee(message: String): Long {
        return if (message.isNotEmpty()) {
            50_000L * (1L + message.toByteArray(Charsets.UTF_8).size / 32L)
        } else {
            0L
        }
    }

    private fun calculateMosaicTransferFee(mosaic: MosaicAttachment): Long {
        val factor = 50_000L
        return if ( mosaic.divisibility == 0 && mosaic.supply < 10_000 ) {
            factor
        } else {
            val maxMosaicQuantity = 9_000_000_000_000_000L
            val totalMosaicQuantity = mosaic.supply * Math.pow(10.0, mosaic.divisibility.toDouble())

            val supplyRelatedAdjustment = Math.floor(0.8 * Math.log(maxMosaicQuantity / totalMosaicQuantity)).toLong()
            val xemEquivalent = (8_999_999_999L * mosaic.quantity) / ( mosaic.supply * Math.pow(10.0, mosaic.divisibility.toDouble()) )
            val microNemEquivalentFee = calculateMicroNemTransferFee((xemEquivalent * Math.pow(10.0, 6.0)).toLong())

            Math.max(factor, microNemEquivalentFee - factor * supplyRelatedAdjustment)
        }
    }


}
