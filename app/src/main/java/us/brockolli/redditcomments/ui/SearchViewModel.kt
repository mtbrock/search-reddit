package us.brockolli.redditcomments.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import us.brockolli.redditcomments.network.RedditSearch
import us.brockolli.redditcomments.network.SearchResult

class SearchViewModel: ViewModel() {
    var searchData: MutableLiveData<SearchResult>? = null
        get() {
            if (field == null) {
                field = MutableLiveData()
            }
            return field!!
        }
        private set

    fun search(context: Context, query: String, params: Map<String, String>, forceReload: Boolean) {
        val search = RedditSearch(query, params)
        val shouldSearch: Boolean
        if (searchData!!.value == null) {
            shouldSearch = true
        } else {
            val queryMatches = search.url == searchData!!.value?.url
            val success = searchData!!.value?.success ?: false
            shouldSearch = (queryMatches) || !success
        }

        if (shouldSearch || forceReload) {
            search!!.doSearch(context, object: RedditSearch.SearchCompletedListener {
                override fun onSearchCompleted(searchResult: SearchResult) {
                    searchData!!.value = searchResult
                }
            })
        }
    }
}