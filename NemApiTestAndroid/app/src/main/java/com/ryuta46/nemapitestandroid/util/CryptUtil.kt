package com.ryuta46.nemapitestandroid.util

import android.content.Context
import android.util.Log
import net.i2p.crypto.eddsa.EdDSAEngine
import net.i2p.crypto.eddsa.EdDSAPrivateKey
import net.i2p.crypto.eddsa.EdDSAPublicKey
import net.i2p.crypto.eddsa.KeyPairGenerator
import net.i2p.crypto.eddsa.spec.EdDSAParameterSpec
import net.i2p.crypto.eddsa.spec.EdDSAPrivateKeySpec
import net.i2p.crypto.eddsa.spec.EdDSAPublicKeySpec
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.security.SecureRandom


class CryptUtil {
    companion object {
        private val tag = "NemApiTestAndroid"

        fun publicKey2HexString(key : EdDSAPublicKey): String {
            return ConvertUtil.toHexString(key.abyte)
        }


        fun privateKey2HexString(key : EdDSAPrivateKey): String {
            return ConvertUtil.toHexString(key.seed)
        }

        /**
         * キーペアを読み込む。キーが保存されていない場合はキーを新たに作成する
         */
        fun loadKeys(context: Context): KeyPair {
            val savedPrivateKey = PreferenceUtil.loadByteArray(context, PreferenceUtil.KEY_PRIVATE_KEY, ByteArray(0))
            val privateKeySeed =
                    if (savedPrivateKey.isNotEmpty()) {
                        savedPrivateKey
                    } else {
                        // 新しいキーを作成
                        val newKey = ByteArray(32)
                        SecureRandom().nextBytes(newKey)
                        // TODO: 本来は暗号化して保存すべき
                        PreferenceUtil.saveByteArray(context, PreferenceUtil.KEY_PRIVATE_KEY, newKey)
                        newKey
                    }


            // カーブ関係のパラメータを取得する用に、適当に一つ鍵ペアを作成する
            val tmpPrivateKey = KeyPairGenerator().generateKeyPair().private as EdDSAPrivateKey
            val param = tmpPrivateKey.params
            // ハッシュアルゴリズムは SHA3-512 を使う
            val paramSpec = EdDSAParameterSpec(param.curve, "SHA3-512", param.scalarOps, param.b)

            // シードとパラメータを使って秘密鍵と、公開鍵を作成
            val privateKey = EdDSAPrivateKey(EdDSAPrivateKeySpec(privateKeySeed, paramSpec))
            val publicKey = EdDSAPublicKey(EdDSAPublicKeySpec(privateKey.a, privateKey.params))

            //Log.i(tag, "Private Key:" + CryptUtil.privateKey2HexString(privateKey))
            Log.i(tag, "Public Key:" + CryptUtil.publicKey2HexString(publicKey))

            // キーペアを作成
            val kv = KeyPair(publicKey, privateKey)

            // 一応署名と検証ができるか検証しておく
            CryptUtil.validateKeyPair(kv)
            return kv
        }


        fun validateKeyPair(kv: KeyPair) {
            // 適当にランダムなバイト列を生成し、署名と検証を行う
            val message = ByteArray(256)
            SecureRandom().nextBytes(message)

            val signature = sign(kv.private, message)
            if (verify(kv.public, message, signature)) {
                Log.i(tag, "Verify OK")
            } else {
                Log.e(tag, "Verify NG")
                throw VerifyError("Invalid Key Pair.")
            }
        }

        /**
         * 署名バイト列を返す
         */
        fun sign(key: PrivateKey, message: ByteArray): ByteArray {
            val engine = EdDSAEngine()
            engine.initSign(key)

            return engine.signOneShot(message)
        }

        /**
         * 署名を検証する
         */
        fun verify(key: PublicKey, message: ByteArray, signature: ByteArray): Boolean {
            val engine = EdDSAEngine()
            engine.initVerify(key)

            return engine.verifyOneShot(message, signature)
        }

    }
}
