package us.brockolli.redditcomments.network

import android.content.Context
import com.google.gson.JsonObject
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion
import java.lang.Exception

class JsonRequest(val context: Context,
                  val url: String,
                  val callback: RequestCallback<JsonObject>): Request {
    override fun load() {
        Ion.with(context)
                .load(url)
                .asJsonObject()
                .setCallback(object: FutureCallback<JsonObject> {
                    override fun onCompleted(e: Exception?, result: JsonObject?) {
                        callback.onCompleted(e, result)
                    }
                });
    }
}