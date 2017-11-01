package com.ryuta46.binaryqrcodeandroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.google.zxing.ResultMetadataType
import com.google.zxing.ResultPoint
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class MainActivity : AppCompatActivity() {
    companion object {
        private val CAMERA_PERMISSION_REQUEST_CODE = 1
        private val TAG = MainActivity::class.java.simpleName
    }

    private var qrReaderView: DecoratedBarcodeView? = null
    private var startButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        qrReaderView = findViewById(R.id.decoratedBarcodeView)
        startButton = findViewById(R.id.buttonRestart)

        startButton?.setOnClickListener {
            startCapture()
        }

        if (checkCameraPermission()) {
            startCapture()
        }
    }

    override fun onPause() {
        stopCapture()
        super.onPause()
    }

    private fun startCapture() {
        startButton?.isEnabled = false

        qrReaderView?.decodeSingle(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                stopCapture()
                if (result == null) {
                    // no result
                    Log.w(TAG, "No result")
                    return
                }
                Log.i(TAG, "QRCode Result: ${result.text}")
                val bytes = result.resultMetadata[ResultMetadataType.BYTE_SEGMENTS] as? List<*>
                val data = bytes?.get(0) as? ByteArray ?: return

                // print result
                val resultString = StringBuffer()
                data.map { byte ->
                    resultString.append(String.format("0x%02X,", byte))
                }
                Log.i(TAG, resultString.toString())
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) { }
        })
        qrReaderView?.resume()
    }
    private fun stopCapture() {
        qrReaderView?.pause()
        startButton?.isEnabled = true
    }

    private fun checkCameraPermission(): Boolean {
        // check camera permission
        val permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_title_camera_permission))
                    .setMessage(getString(R.string.dialog_message_camera_permission))
                    .setPositiveButton(android.R.string.ok) { _, _ ->
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
                    }
                    .create()
                    .show()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when(requestCode) {
            CAMERA_PERMISSION_REQUEST_CODE -> {
                if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startCapture()
                }
            }
        }
    }

}
