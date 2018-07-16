package us.brockolli.redditcomments.network

import android.content.Context
import com.google.gson.JsonObject
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.utils.RedditUtils
import java.io.Serializable

class RedditSearch(val query: String, val params: Map<String, String>): Serializable {
    private var mState = State.NONE

    val url = RedditUtils.createSearchUrl(query, params)

    private enum class State {
        NONE,
        RESULT_OK,
        RESULT_ERROR
    }

    fun hasResult(): Boolean {
        return mState == State.RESULT_OK
    }

    private fun handleResult(jsonObject: JsonObject?, e: Exception?) {
        if (jsonObject == null || e != null) {
            // No result
            LogTag.e("Search failed, error msg: ${e?.message}")
            mState = State.RESULT_ERROR
        } else {
            mState = State.RESULT_OK
        }
    }

    fun doSearch(context: Context, listener: SearchCompletedListener?) {
        val request = RequestFactory.createJsonRequest(url, object: RequestCallback<JsonObject> {
            override fun onCompleted(e: Exception?, result: JsonObject?) {
                handleResult(result, e)
                var msg: String? = null
                if (e != null) {
                    msg = e.message
                } else if(result == null) {
                    msg = "Result null"
                }
                listener?.onSearchCompleted(SearchResult(query, params, url, result, e, msg))
            }
        })
        request.load(context)
    }

    interface SearchCompletedListener {
        fun onSearchCompleted(searchResult: SearchResult)
    }
}