package us.brockolli.redditcomments.network

import android.content.Context
import com.google.gson.JsonObject
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.utils.RedditUtils
import java.io.Serializable

class RedditSearch(val query: String, val params: Map<String, String>): Serializable {
    private var mState = State.NONE

    var searchCompletedListener: SearchCompletedListener? = null

    val url = RedditUtils.createSearchUrl(query, params)
    var result: JsonResult? = null
        set(value) {
            field = value
            if (value == null) {
                mState = State.NONE
                return
            }

            if (value.jsonObject == null || value.e != null) {
                // No result
                LogTag.e("Search result empty, error msg: ${value.errorMsg}")
                mState = State.RESULT_ERROR
            } else {
                mState = State.RESULT_OK
            }
        }
    var hasResult: Boolean = false
        get() {
            return result != null && mState == State.RESULT_OK
        }

    private enum class State {
        NONE,
        RESULT_OK,
        RESULT_ERROR
    }

    fun hasResult(): Boolean {
        return result != null && mState == State.RESULT_OK
    }

    fun doSearch(context: Context, listener: SearchCompletedListener?) {
        val request = RequestFactory.createJsonRequest(url, object: RequestCallback<JsonObject> {
            override fun onCompleted(e: Exception?, result: JsonObject?) {
                var msg: String? = null
                if(e != null) {
                    LogTag.e("error: " + e.message);
                    msg = e.message
                } else if(result == null) {
                    LogTag.e("error: no result");
                    msg = "Result null"
                }
                val jsonResult = JsonResult(url, result, e, msg)
                this@RedditSearch.result = jsonResult
                listener?.onSearchCompleted(this@RedditSearch)
            }
        })
        request.load(context)
    }

    interface SearchCompletedListener {
        fun onSearchCompleted(search: RedditSearch)
    }
}