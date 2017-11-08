package com.ryuta46.nemapitestandroid.enum

enum class TransactionType(val rawValue: Int, val versionOffset: Int) {
    //転送トランザクション
    Transfer(0x0101, 2),
    // 重要度転送トランザクション)
    ImportanceTransfer(0x0801, 1),
    // マルチシグ集計変更転送トランザクション
    MultisigAggregateModificationTransfer(0x1001, 2),
    // マルチシグ署名トランザクション)
    MultisigSignature(0x1002, 1),
    // マルチシグトランザクション
    Multisig(0x1004, 1),
    // プロキシネームスペーストランザクション
    ProvisionNamespace(0x2001, 1),
    // モザイク定義作成トランザクション
    MosaicDefinitionCreation(0x4001, 1),
    // モザイク供給変更トランザクション
    MosaicSupplyChange(0x4002, 1)
}