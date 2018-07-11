package us.brockolli.redditcomments.network

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

    fun getLinks(context: Context, url: String): MutableLiveData<List<Link>> {
        if(mLinks == null || !(mLastLoadedUrl == url)) {
            LogTag.d("mLinks is NULL, performing search on network")
            mLinks = MutableLiveData()
            loadLinks(context, url)
        }
        return mLinks!!
    }

    fun search(context: Context, searchString: String): MutableLiveData<List<Link>> {
        val url = RedditUtils.createSearchUrl(searchString)
//        val url = "https://www.reddit.com/search.json?q=A4_auMe1HsY"
        return getLinks(context, url)
    }

    private fun loadLinks(context: Context, url: String) {
        val request = RequestFactory.createJsonRequest(context, url, object: RequestCallback<JsonObject> {
            override fun onCompleted(e: Exception?, result: JsonObject?) {
                if(e != null) {
                    LogTag.e("error: " + e.message);
                }
                if(result == null) {
                    LogTag.e("error: no result");
                    return;
                }
                val listing = LinkFactory.createListOfLinksFromJsonObject(result)
                mLastLoadedUrl = url
                mLinks!!.value = listing
            }
        })
        request.load()
    }
}