package com.ryuta46.nemapitestandroid

import android.util.Log
import com.google.gson.Gson
import com.ryuta46.nemapitestandroid.entity.AccountMetaDataPair
import com.ryuta46.nemapitestandroid.entity.NemAnnounceResult
import com.ryuta46.nemapitestandroid.entity.RequestAnnounce
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody




class NemApiClient(host:String) {
    companion object {
        val NODE_PORT = 7890
        val JSON = MediaType.parse("application/json; charset=utf-8")
    }
    private val tag = "NemApiTestAndroid"

    private val okClient = OkHttpClient()

    private val urlBase = "http://$host:$NODE_PORT"




    private inline fun <reified T : Any>sendGetMessageToNis(path: String, query: Map<String, String> = emptyMap()): Observable<T> {
        val url = createUrl(path, query)

        Log.v(tag, "get request url = $url")

        val request = Request.Builder().url(url).get().build()

        return Observable.create { subscriber ->
            val response = okClient.newCall(request).execute()
            val responseString = response.body()?.string()

            if (responseString == null) {
                subscriber.onError(Exception("Failed to get response body"))
            } else {
                Log.v(tag, "response = $responseString")
                val semanticResponse = Gson().fromJson(responseString, T::class.java)
                subscriber.onNext(semanticResponse)
            }
            subscriber.onComplete()
        }
    }
    private inline fun <R,reified S: Any>sendPostMessageToNis(path: String, body: R, query: Map<String, String> = emptyMap()): Observable<S> {
        val url = createUrl(path, query)

        val requestBodyString = Gson().toJson(body)
        val requestBody = RequestBody.create(JSON, requestBodyString)

        Log.v(tag, "post request url = $url, body = $requestBodyString")

        val request = Request.Builder().url(url).post(requestBody).build()

        return Observable.create { subscriber ->
            val response = okClient.newCall(request).execute()
            val responseString = response.body()?.string()

            if (responseString == null) {
                subscriber.onError(Exception("Failed to get response body"))
            } else {
                Log.v(tag, "response = $responseString")
                val semanticResponse = Gson().fromJson(responseString, S::class.java)
                subscriber.onNext(semanticResponse)
            }
            subscriber.onComplete()
        }
    }

    private fun createUrl(path: String, query: Map<String, String>): String {
        val url = urlBase + path

        if (query.isEmpty()){
            return url
        }

        return url +  "?" + query.toList().joinToString(separator = "&") {
            it.first + "=" + it.second
        }
    }

    fun getAccountFromPublicKey(publicKey: String): Observable<AccountMetaDataPair> {
        return sendGetMessageToNis("/account/get/from-public-key", mapOf("publicKey" to publicKey))
    }

    fun transactionAnnounce(requestAnnounce: RequestAnnounce): Observable<NemAnnounceResult> {
        return sendPostMessageToNis("/transaction/announce", requestAnnounce)
    }

}