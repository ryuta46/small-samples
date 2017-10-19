package com.ryuta46.networkprofiler

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.gson.Gson
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response


class MainActivity : AppCompatActivity() {
    private val logger = Logger(javaClass.simpleName)

    val okClient = OkHttpClient()

    companion object {
        val A3RT_API_KEY = "CHANGE_THIS_WITH_YOUR_API_KEY"
        val A3RT_URL = "https://api.a3rt.recruit-tech.co.jp/talk/v1/smalltalk"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Logger.level = Logger.LOG_LEVEL_VERBOSE

        setupListeners()
    }

    private fun setupListeners() {
        val button = findViewById<Button>(R.id.buttonUpload)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val textView = findViewById<TextView>(R.id.textViewResponse)

        button.setOnClickListener {
            sendMessageToA3rt("おはよう", textView)
            downloadImage("https://ryuta46.com/wp-content/uploads/2017/10/IMG_8999_small-203x300.jpg", imageView)
        }
    }
    private fun sendMessageToA3rt(query: String, textView: TextView) {
        val body = FormBody.Builder()
                .add("apikey", A3RT_API_KEY)
                .add("query", query)
                .build()

        val request = Request.Builder().url(A3RT_URL).post(body).build()

        val requestObservable = Observable.create(ObservableOnSubscribe<Response> { subscriber ->
            val response = okClient.newCall(request).execute()
            subscriber.onNext(response)
            subscriber.onComplete()
        })

        requestObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { response ->
                    val a3rtResponse = Gson().fromJson(response.body()?.string(), A3RtResponse::class.java)
                    textView.text = a3rtResponse.results[0].reply
                }
    }

    private fun downloadImage(url: String, imageView: ImageView) {
        Picasso.Builder(this).downloader(OkHttp3Downloader(this)).build()
                .load(url)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .fit()
                .centerCrop()
                .into(imageView)
    }

    class A3RtResponse {
        class Entry {
            var perplexity = 0.0
            var reply = ""
        }
        var status  = 0
        var message = ""
        var results = listOf<Entry>()
    }
}
