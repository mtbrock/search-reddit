package us.brockolli.redditcomments.network

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context

class LinkSearchViewModel: ViewModel() {
    private var mSearch: RedditSearch? = null

    var searchData: MutableLiveData<SearchResult>? = null
        get() {
            if (field == null) {
                field = MutableLiveData()
            }
            return field!!
        }
        private set

    fun search(context: Context, query: String, params: Map<String, String>, forceReload: Boolean) {
        val newSearch = RedditSearch(query, params)
        val shouldSearch: Boolean
        if (mSearch == null) {
            shouldSearch = true
        } else {
            shouldSearch = (mSearch!!.url != newSearch.url) || !mSearch!!.hasResult()
        }

        if (shouldSearch || forceReload) {
            mSearch = newSearch
            mSearch!!.doSearch(context, object: RedditSearch.SearchCompletedListener {
                override fun onSearchCompleted(searchResult: SearchResult) {
                    searchData!!.value = searchResult
                }
            })
        }
    }
}