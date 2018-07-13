package us.brockolli.redditcomments.network

import android.content.Context
import com.google.gson.JsonObject

class RequestFactory {
    companion object {
        fun createJsonRequest(url: String,
                              callback: RequestCallback<JsonObject>): Request {
            return JsonRequest(url, callback)
        }
    }
}