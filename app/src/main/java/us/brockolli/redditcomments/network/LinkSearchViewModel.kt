package us.brockolli.redditcomments.network

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import com.google.gson.JsonObject
import com.koushikdutta.async.future.FutureCallback
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.model.Link
import us.brockolli.redditcomments.model.LinkFactory
import us.brockolli.redditcomments.ui.LinkRecyclerViewAdapter
import us.brockolli.redditcomments.utils.RedditUtils

class LinkSearchViewModel: ViewModel() {
    private var mLinks: MutableLiveData<List<Link>>? = null
    private var mLastLoadedUrl: String? = null
    private var mResult: MutableLiveData<JsonResult>? = null
    private var mRedditSearch: RedditSearch? = null
    private var mSearch: RedditSearch? = null

    var linksData: MutableLiveData<List<Link>>? = null
        get() {
            if (field == null) {
                field = MutableLiveData()
            }
            return field!!
        }
        private set

    var searchData: MutableLiveData<RedditSearch>? = null
        get() {
            if (field == null) {
                field = MutableLiveData()
            }
            return field!!
        }
        private set

    fun getLinks(context: Context, url: String): MutableLiveData<List<Link>> {
        if(mLinks == null || !(mLastLoadedUrl == url)) {
            LogTag.d("mLinks is NULL, performing search on network")
            mLinks = MutableLiveData()
            loadLinks(context, url)
        }
        return mLinks!!
    }

    fun search(context: Context, query: String, params: Map<String, String>) {
        val newSearch = RedditSearch(query, params)
        var shouldSearch = false
        if (mSearch == null) {
            shouldSearch = true
        } else {
            shouldSearch = (mSearch!!.url != newSearch.url) || !mSearch!!.hasResult()
        }

        if (shouldSearch) {
            mSearch = newSearch
            mSearch!!.doSearch(context, object: RedditSearch.SearchCompletedListener {
                override fun onSearchCompleted(search: RedditSearch) {
                    searchData!!.value = search
                }
            })
        }
    }

    fun doLoadLinks(context: Context, query: String, params: Map<String, String>) {
        val newSearch = RedditSearch(query, params)
        var shouldSearch = false
        if (mSearch == null) {
            shouldSearch = true
        } else {
            shouldSearch = (mSearch!!.url != newSearch.url) || !mSearch!!.hasResult()
        }

        if (shouldSearch) {
            mSearch = newSearch
            loadLinks(context, newSearch.url)
        }

//        if (mRedditSearch != null && mRedditSearch!!.url == newSearch.url) {
//            if (mRedditSearch!!.hasResult()) {
//                loadLinks(context, mRedditSearch!!.url)
//            }
//        } else if (mRedditSearch == null) {
//            mRedditSearch = newSearch
//            loadLinks(context, mRedditSearch!!.url)
//        }
    }

    fun getJsonResult(context: Context, searchString: String, params: Map<String, String>,
                      forceReload: Boolean = false): LiveData<JsonResult> {
        val url = RedditUtils.createSearchUrl(searchString, params)
        LogTag.d("Performing JsonResult search for $url")
        if (mResult == null) {
            mResult = MutableLiveData()
        }
        if (!(mLastLoadedUrl == url) || forceReload) {
            LogTag.d("Performing search on network, forced: $forceReload")
            mRedditSearch = RedditSearch(searchString, params)
            loadResult(context, url)
        }
        return mResult!!
    }

    fun getLinksData(context: Context, query: String, params: Map<String, String>): MutableLiveData<List<Link>> {
        if (mLinks == null) {
            mLinks = MutableLiveData()
        }

        val newSearch = RedditSearch(query, params)
        if (mRedditSearch != null && mRedditSearch!!.url == newSearch.url) {
            if (mRedditSearch!!.hasResult()) {
                loadLinks(context, mRedditSearch!!.url)
            }
        } else if (mRedditSearch == null) {
            mRedditSearch = newSearch
            loadLinks(context, mRedditSearch!!.url)
        }

        return mLinks!!
    }

    private fun loadResult(context: Context, url: String) {
        val request = RequestFactory.createJsonRequest(url, object: RequestCallback<JsonObject> {
            val url = url
            override fun onCompleted(e: Exception?, result: JsonObject?) {
                var msg: String? = null
                if(e != null) {
                    LogTag.e("error: " + e.message);
                    msg = e.message
                }
                if(result == null) {
                    LogTag.e("Uh-oh: no result. URL: ${this.url}");
                    TODO("Localize")
                    msg = "No results"
                }
                mLastLoadedUrl = this.url
                val jsonResult = JsonResult(this.url, result, e, msg)
                mResult!!.value = jsonResult
                mRedditSearch!!.result = jsonResult
            }
        })
        request.load(context)
    }

    private fun loadLinks(context: Context, url: String) {
        val request = RequestFactory.createJsonRequest(url, object: RequestCallback<JsonObject> {
            val url = url
            override fun onCompleted(e: Exception?, result: JsonObject?) {
                var msg: String? = null
                if(e != null) {
                    LogTag.e("error: " + e.message);
                    msg = e.message
                } else if(result == null) {
                    LogTag.e("error: no result");
                    msg = "Result null"
                } else {
                    val listing = LinkFactory.createListOfLinksFromJsonObject(result)
                    val jsonResult = JsonResult(this.url, result, e, msg)
                    mSearch!!.result = jsonResult
                    linksData!!.value = listing
                }
//                mLinks!!.value = listing
            }
        })
        request.load(context)
    }
}