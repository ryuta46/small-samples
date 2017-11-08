package com.ryuta46.nemapitestandroid

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.ryuta46.nemapitestandroid.entity.AccountMetaDataPair
import com.ryuta46.nemapitestandroid.entity.NemAnnounceResult
import com.ryuta46.nemapitestandroid.entity.RequestAnnounce
import com.ryuta46.nemapitestandroid.entity.TransferTransaction
import com.ryuta46.nemapitestandroid.util.ConvertUtil
import com.ryuta46.nemapitestandroid.util.CryptUtil
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import net.i2p.crypto.eddsa.EdDSAPublicKey
import java.security.KeyPair


class MainActivity : AppCompatActivity() {
    private val tag = "NemApiTestAndroid"

    @BindView(R.id.textAddress) lateinit var textAddress: TextView
    @BindView(R.id.textMessage) lateinit var textMessage: TextView
    @BindView(R.id.buttonAccountInfo) lateinit var buttonAccountInfo: Button
    @BindView(R.id.buttonSendXem) lateinit var buttonSendXem: Button

    private lateinit var keyPair: KeyPair
    private var address: String = ""

    private val client = NemApiClient("62.75.251.134")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ButterKnife.bind(this)

        keyPair = CryptUtil.loadKeys(this)

        setupView()
        setupListeners()
    }

    private fun setupView() {
        fetchAccountInfo()
    }
    private fun setupListeners() {

        buttonAccountInfo.setOnClickListener {
            textMessage.text = ""
            fetchAccountInfo()
        }

        buttonSendXem.setOnClickListener {
            val view = layoutInflater.inflate(R.layout.dialog_send_xem, null)
            AlertDialog.Builder(this)
                    .setView(view)
                    .setPositiveButton("OK") { _, _ ->
                        textMessage.text = ""
                        val addressEdit: EditText = view.findViewById(R.id.editTextAddress)
                        val xemEdit: EditText =  view.findViewById(R.id.editTextMicroNem)

                        try {
                            val microNem = xemEdit.text.toString().toLong()
                            sendXem(addressEdit.text.toString(), microNem)
                        } catch (e: NumberFormatException) {
                            Log.e(tag, e.message)
                            return@setPositiveButton
                        }
                    }
                    .setNegativeButton("CANCEL") { _, _ -> }
                    .show()

        }
    }

    private fun fetchAccountInfo() {
        val publicKey = keyPair.public as? EdDSAPublicKey ?: throw Exception("Key pair is invalid")
        val publicKeyString = CryptUtil.publicKey2HexString(publicKey)

        client.getAccountFromPublicKey(publicKeyString)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext{ e: Throwable ->
                    Log.e(tag, "error occurred: ${e.message}")
                    Observable.empty<AccountMetaDataPair>()
                }
                .subscribe { response: AccountMetaDataPair ->
                    val accountInfo = response.account
                    address = accountInfo.address
                    Log.i(tag, "address = $address")

                    textAddress.text = address

                    val message = "balance: " + accountInfo.balance
                    textMessage.text = message
                }
    }

    private fun sendXem(receiverAddress: String, microNem: Long) {
        val kv = keyPair
        val publicKey = kv.public as? EdDSAPublicKey ?: throw Exception("Key pair is invalid")

        val transaction = TransferTransaction(
                publicKey = publicKey.abyte,
                receiverAddress = receiverAddress,
                ammount = microNem
        )

        // 転送データのバイト列を取得
        val transactionByteString = ConvertUtil.toHexString(transaction.transactionBytes)
        // データ列を署名
        val signature = CryptUtil.sign(kv.private, transaction.transactionBytes)

        // データ列と署名を合わせて 転送トランザクションを発行する
        val requestAnnounce = RequestAnnounce(
                transactionByteString,
                ConvertUtil.toHexString(signature))

        client.transactionAnnounce(requestAnnounce)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .onErrorResumeNext{ e: Throwable ->
                    Log.e(tag, "error occurred: ${e.message}")
                    Observable.empty<NemAnnounceResult>()
                }
                .subscribe { response: NemAnnounceResult ->
                    val message = "type = ${response.type}\n" +
                            "code = ${response.code}\n" +
                            "message = ${response.message}\n" +
                            "hash = ${response.transactionHash?.data}\n" +
                            "inner = ${response.innerTransactionHash?.data}\n"
                    Log.i(tag, message)
                    textMessage.text = message
                }
    }
}
