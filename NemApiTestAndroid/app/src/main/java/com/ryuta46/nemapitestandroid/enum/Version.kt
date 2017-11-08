package com.ryuta46.nemapitestandroid.enum

enum class Version(val rawValue: Int) {
    // トランザクション種別による加算値(+1, +2) は rawValue として含めない。
    // メインネットワーク
    Main(0x68.shl(24)),
    // テストネットワーク
    Test(0x98.shl(24)),


}