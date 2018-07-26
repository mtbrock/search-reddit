package us.brockolli.redditcomments.ui

import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.AlphaAnimation
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import kotlinx.android.synthetic.main.widget_search_params.*
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.R
import us.brockolli.redditcomments.model.Link
import us.brockolli.redditcomments.model.LinkFactory
import us.brockolli.redditcomments.network.SearchResult
import us.brockolli.redditcomments.utils.RedditUtils
import us.brockolli.redditcomments.utils.ViewUtils


class MainActivity : AppCompatActivity(), LinkRecyclerViewAdapter.OnListInteractionListener {
    private var mShouldShowParams: Boolean = false
    private var mSubmittedQuery: String? = null
    private var mSearching = false
    private var mSearchHasFocus = false

    private var mSearchResult: SearchResult? = null


    override fun onListInteraction(item: Link) {
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(RedditUtils.createCommentsUrl(item.permalink))
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)



        mShouldShowParams = dropdown_layout.isExpanded

        with(link_list) {
            layoutManager = LinearLayoutManager(context)
        }

        search_fab.setOnClickListener {
            searchbar.clearFocus()
            searchbar.setQuery(searchbar.query, true)
        }

        search_params_widget.setOnParamsChangedListener(object: SearchParamsWidget.OnParamsChangedListener {
            var lastParams: Map<String, String> = getParams()
            override fun onParamsChanged(params: Map<String, String>) {
                lastParams = params
                hideOrShowSearchButton()
            }
        })

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchbar.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchbar.setIconifiedByDefault(false)
        searchbar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            mSearchHasFocus = hasFocus
            // Refill the search text if empty and we are displaying a valid result
            if (!hasFocus && hasValidSearchResult() && TextUtils.isEmpty(searchbar.query)) {
                searchbar.setQuery(mSearchResult!!.query, false)
            }

            // Show or hide prefs
            if(hasFocus) {
                showParams()
                if (!TextUtils.isEmpty(searchbar.query)) {
                    search_fab.show()
                }
            } else {
                val paramsWidgetHasFocus = search_params_widget.focusedChild != null ||
                        search_params_widget.hasFocus()
                if (!mShouldShowParams && !paramsWidgetHasFocus && hasValidSearchResult()) {
                    hideParams()
                }
                hideOrShowSearchButton()
            }
        }
        searchbar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                hideOrShowSearchButton()
                return false
            }
        })

        savedInstanceState?.let {
            mSubmittedQuery = it.getString("submitted_query")
            searchbar.setQuery(it.getString("query"), false)
        }

        val searchModel = ViewModelProviders.of(this)
                .get(SearchViewModel::class.java)
        searchModel.searchData!!.observe(this, Observer {
            it?.run {
                handleSearchResult(it)
            }
        })
        handleIntent()
    }

    override fun onResume() {
        super.onResume()
        if (!TextUtils.isEmpty(mSubmittedQuery)) {
            if(!hasValidSearchResult() && !mSearching) {
                search(mSubmittedQuery)
            }
        } else {
            searchbar.isIconified = false
            showEmptyText(R.string.search_tip_1)
        }
        hideOrShowSearchButton()
    }

    override fun onPause() {
        super.onPause()
//        searchbar.clearFocus()
        mSearching = false
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        handleIntent()
    }

    private fun handleIntent() {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                val query = intent.getStringExtra(Intent.EXTRA_TEXT)
                search(query, true)
            }
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                search(query, true)
            }
        }
        intent = null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            mShouldShowParams = !params_layout.isExpanded
            if(mShouldShowParams) {
                showParams()
            } else {
                hideParams()
            }
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("submitted_query", mSubmittedQuery)
        outState.putString("query", searchbar.query.toString())
    }

    private fun search(query: String?, forceReload: Boolean) {
        if(TextUtils.isEmpty(query)) {
            LogTag.w("Attempted to search for empty query.")
            return
        }
        mSubmittedQuery = query!!
        mSearching = true
        mShouldShowParams = false
        hideDropdown()
        hideParams()
        showProgress()
        search_fab.hide()
        val searchModel = ViewModelProviders.of(this)
                .get(SearchViewModel::class.java)
        searchModel.search(this, query, getParams(), forceReload)
    }

    private fun search(query: String?) {
        search(query, false)
    }

    private fun handleSearchResult(searchResult: SearchResult) {
        mSearchResult = searchResult
        mSearching = false

        if (searchResult.success) {
            val links = LinkFactory.createListOfLinksFromJsonObject(searchResult.jsonObject!!)
            updateLinks(links)
            updateDropdown(searchResult.query, searchResult.params, links.size)
            if (links.size == 0) {
                showEmptyText(":(")
            } else {
                searchbar.clearFocus()
                showList()
            }
        } else {
            // show error
            LogTag.d(searchResult.e.toString())
//            showEmptyText(searchResult.errorMsg)
            showEmptyText(R.string.search_error)
        }
    }

    private fun updateDropdown(query: String, params: Map<String, String>, numResults: Int) {
        val subreddit = params.get("subreddit")
        val subText = if (TextUtils.isEmpty(subreddit)) "" else " in r/$subreddit"
        dropdown_text?.text = "$numResults results for \"$query\"$subText"
        showDropdown()
    }

    private fun updateLinks(links: List<Link>) {
        link_list.adapter = LinkRecyclerViewAdapter(links, this)
    }

    /**
     * Return true if we have a successful search result for the last submitted query.
     */
    private fun hasValidSearchResult(): Boolean {
        mSearchResult?.let {
            return it.success && it.query == mSubmittedQuery
        }
        return false
    }

    private fun hideOrShowSearchButton() {
        if(shouldSearch()) {
            search_fab.show()
        } else {
            search_fab.hide()
        }
    }

    /**
     * Return true if we want to perform a search.
     */
    private fun shouldSearch(): Boolean {
        val query = searchbar.query.toString()
        val params = getParams()
        var shouldSearch = false
        if (TextUtils.isEmpty(query) || mSearching) {
            return false
        }
        if (mSearchHasFocus) {
            return true
        }
        // Query is non-empty

        if (hasValidSearchResult()) {
            if (query != mSearchResult!!.query || !params.equals(mSearchResult!!.params)) {
                shouldSearch = true
            }
        } else {
            shouldSearch = true
        }
        return shouldSearch
    }

    private fun getParams(): Map<String, String> {
        return search_params_widget.getParams()
    }

    private fun showParams(animate: Boolean = true) {
        params_layout.expand(animate)
    }

    private fun hideParams(animate: Boolean = true) {
        params_layout.collapse(animate)
        if (subreddit_field.hasFocus()) {
            subreddit_field.clearFocus()
            ViewUtils.setImeVisibility(subreddit_field, false)
        }
    }

    private fun showDropdown(animate: Boolean = true) {
        dropdown_layout.expand(animate)
    }

    private fun hideDropdown(animate: Boolean = true) {
        dropdown_layout.collapse(animate)
    }

    private fun showProgress() {
        link_list.visibility = View.GONE
        empty_text.visibility = View.GONE
        progress.alpha = 1f
        progress.visibility = View.VISIBLE
    }

    private fun showList() {
        ViewUtils.fadeIn(link_list)
        ViewUtils.fadeOut(progress)
        empty_text.visibility = View.GONE
    }

    private fun showEmptyText(text: String? = null) {
        if (text != null) {
            empty_text.text = text
        }

        ViewUtils.fadeIn(empty_text)
        ViewUtils.fadeOut(progress)
        ViewUtils.fadeOut(link_list)
    }

    private fun showEmptyText(resId: Int) {
        empty_text.setText(resId)
        showEmptyText()
    }
}
