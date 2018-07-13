package us.brockolli.redditcomments.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import us.brockolli.redditcomments.R

import kotlinx.android.synthetic.main.activity_main.*
import us.brockolli.redditcomments.model.Link
import android.app.SearchManager
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.widget.Toast
import android.os.Parcelable
import android.support.v4.text.TextUtilsCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.view.Menu
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.content_main.*
import net.cachapa.expandablelayout.ExpandableLayout
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.R.id.searchbar
import us.brockolli.redditcomments.utils.RedditUtils
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import kotlinx.android.synthetic.main.widget_search_params.*
import us.brockolli.redditcomments.model.LinkFactory
import us.brockolli.redditcomments.network.JsonResult
import us.brockolli.redditcomments.network.LinkSearchViewModel
import us.brockolli.redditcomments.network.RedditSearch
import java.io.Serializable


class MainActivity : AppCompatActivity(), LinkSearchFragment.OnLinkSearchInteractionListener {
    var mSearchString: String? = null
    var mDropdownText: TextView? = null
    var mDropdown: ExpandableLayout? = null
    var mPrefsLayout: ExpandableLayout? = null
    var mShouldShowPrefs: Boolean = false
    var mSearchResult: JsonResult? = null
    var mRedditSearch: RedditSearch? = null
    var mLastQuery: String? = null
    var mLastParams: Map<String, String>? = null
    private var mSearching = false
    private var mParamsChanged = false

    private var mState = State.NO_SEARCH
    private enum class State {
        NO_SEARCH,
        RESULT_OK,
        RESULT_OK_EMPTY,
        RESULT_ERROR
    }

    override fun onListInteraction(item: Link?) {
        val i = Intent(Intent.ACTION_VIEW)
        i.setData(Uri.parse(RedditUtils.createCommentsUrl(item!!.permalink!!)))
        startActivity(i)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mDropdown = this.dropdown_layout
        mDropdownText = this.dropdown_text
        mPrefsLayout = this.prefs_layout

        with(link_list) {
            layoutManager = LinearLayoutManager(context)
        }

        search_fab.setOnClickListener {
            searchbar.clearFocus()
            search(searchbar.query.toString())
        }

        search_params_widget.setOnParamsChangedListener(object: SearchParamsWidget.OnParamsChangedListener {
            override fun onParamsChanged(params: Map<String, String>) {
//                mParamsChanged = params != mLastParams
                hideOrShowSearchButton()
            }
        })

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchbar.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchbar.setIconifiedByDefault(false)
        searchbar.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(!hasFocus && TextUtils.isEmpty(searchbar.query) && !TextUtils.isEmpty(mSearchString)) {
                searchbar.setQuery(mSearchString, false)
            }

            // Show or hide prefs
            if(hasFocus) {
                showPrefs()
                if (!TextUtils.isEmpty(searchbar.query)) {
                    search_fab.show()
                }
            } else {
                if (!mShouldShowPrefs) {
                    hidePrefs()
                }
                hideOrShowSearchButton()
            }
        }
        searchbar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText != mSearchString) {
                    LogTag.d("hideorshow from querytextchange")
                    hideOrShowSearchButton()
                }
                if(newText == mSearchString || TextUtils.isEmpty(mSearchString)) {
//                    hideDropdown()
                } else if (mState != State.NO_SEARCH) {
//                    mDropdownText!!.text = "\"$mSearchString\""
//                    showDropdown()
                }
                return false
            }
        })

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }


        val searchModel = ViewModelProviders.of(this)
                .get(LinkSearchViewModel::class.java)
//        val linksData = searchModel.getLinksData(this,
//                searchbar.query.toString(), getParams())
        searchModel.linksData!!.observe(this, Observer {
            it?.run {
                bindSearchResult(it)
            }
        })
        searchModel.searchData!!.observe(this, Observer {
            it?.run {
                handleSearchResult(it)
            }
        })
//        linksData.observe(this, Observer {
//            mSearching = false
//            mSearchResult = it
//            mRedditSearch?.result = it
//            handleSearchResult(it)
//        })
        handleIntent(intent)
    }

    fun bindSearchResult(result: List<Link>) {
        LogTag.d("bindSearchResult we have a result! size: ${result.size}")
    }

    override fun onResume() {
        super.onResume()
        LogTag.d("hideorshow from onResume")
        hideOrShowSearchButton()
        if (mRedditSearch != null && mRedditSearch!!.hasResult()) {
            LogTag.d("gonna reload result")
            reloadResult()
        } else {
            LogTag.d("no result so gonna search")
            search(searchbar.query.toString())
        }
//        toast("onResume")
    }

    override fun onPause() {
        super.onPause()
        searchbar.clearFocus()
        mSearching = false
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_settings -> {
            // User chose the "Settings" item, show the app settings UI...
            mShouldShowPrefs = !mPrefsLayout!!.isExpanded
            if(mShouldShowPrefs) {
                showPrefs()
            } else {
                hidePrefs()
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("state", mState)
//        if(mSearchResult != null) {
//            LogTag.d("onSaveInstanceState: ${mSearchResult?.toString()}")
//            outState.putSerializable("result", mSearchResult)
//        }
//        if(mRedditSearch != null) {
//            LogTag.d("onSaveInstanceState: ${mRedditSearch?.toString()}, hasResult? ${mRedditSearch?.hasResult()}")
//            outState.putSerializable("search", mRedditSearch)
//        }
//        outState!!.putString("searchString", mSearchString)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        mState = savedInstanceState.get("state") as State
//        val result = savedInstanceState.get("result")
//        var search = savedInstanceState.get("search")
//        if (search != null) {
//            mRedditSearch = search as RedditSearch
//            search = search as RedditSearch
//            LogTag.d("onRestoreInstanceState: ${search?.toString()}, hasResult? ${search?.hasResult()}")
//        }
//        LogTag.d("onRestoreInstanceState: ${mRedditSearch?.toString()}, hasResult? ${mRedditSearch?.hasResult()}")
//        if (result != null) {
//            mSearchResult = result as JsonResult
//        }
//        LogTag.d("onRestoreInstanceState: ${mSearchResult?.toString()}")
//        search(savedInstanceState.getString("searchString"))
    }

    private fun hideOrShowSearchButton() {
        LogTag.d("Calling hideorshow")
        if(shouldSearch()) {
            search_fab.show()
        } else {
            search_fab.hide()
        }
    }

    /**
     * Return true if we want to perform a search.
     *
     * If there is a result in memory, we want to search if the query text changes
     * or if the parameters change.
     *
     * If there is no result in memory, we want to search if the query text is not empty
     */
    private fun shouldSearch(): Boolean {
        val query = searchbar.query.toString()
        val params = getParams()
        var shouldSearch = false
        if (TextUtils.isEmpty(query) || mSearching) {
            return false
        }
        // Query is non-empty

        if (mState == State.RESULT_OK || mState == State.RESULT_OK_EMPTY) {
            if (query != mRedditSearch?.query || params != mRedditSearch?.params ) {
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

    private fun search(searchString: String?) {
        if(TextUtils.isEmpty(searchString)) {
            return
        }
//        mSearchString = searchString
        mLastQuery = searchString
        mLastParams = getParams()
        mSearching = true
        mRedditSearch = RedditSearch(searchString!!, mLastParams!!)
        hideDropdown()
        mShouldShowPrefs = false
        hidePrefs()
        search_fab.hide()
//        loadLinks(searchString, mLastParams!!)
        val searchModel = ViewModelProviders.of(this)
                .get(LinkSearchViewModel::class.java)
//        searchModel.doLoadLinks(this, searchString, mLastParams!!)
        searchModel.search(this, searchString, mLastParams!!)
//        getLinkSearchFragment()?.search(searchString, getParams())
    }

    private fun reloadResult() {
//        handleSearchResult(mRedditSearch?.result)
    }

    private fun handleSearchResult(search: RedditSearch) {
        val result = search.result
        if (search == null || result == null) {
            LogTag.d("handleResult: RedditSearch or result is null!!!")
            return
        }

        result.let {
            val searchString = search!!.query
            var numResults = 0
            var from = from_spinner.selectedItem.toString()
            var subreddit = subreddit_field.text.toString()
            val jsonObject = it!!.jsonObject
            if (jsonObject == null || it!!.e != null) {
                // No result
                LogTag.e("Search result empty, error msg: ${it.errorMsg}")
                mState = State.RESULT_ERROR
//                mState = if (it.e == null) State.RESULT_OK_EMPTY else State.RESULT_ERROR
            } else {
                val links = LinkFactory.createListOfLinksFromJsonObject(jsonObject!!)
                numResults = links.size
                val subText = if (TextUtils.isEmpty(subreddit)) "" else " in r/$subreddit"
                mDropdownText?.text = "$numResults results for \"$searchString\"$subText from $from"
                showDropdown()
                if (links.size > 0) {
                    mState = State.RESULT_OK
                    link_list.adapter = LinkRecyclerViewAdapter(links, this)
                } else {
                    // Result is empty
                    mState = State.RESULT_OK_EMPTY
                }
            }
        }
    }

    private fun loadLinks(searchString: String?, params: Map<String, String>) {
        if (searchString == null) {
            link_list.adapter = null
            return
        }
        val searchModel = ViewModelProviders.of(this)
                .get(LinkSearchViewModel::class.java)
        val resultData = searchModel.getJsonResult(this, searchString,
                params, mState == State.NO_SEARCH || mState == State.RESULT_ERROR)
        resultData.observe(this, Observer {
            mSearching = false
            mSearchResult = it
            mRedditSearch?.result = it
//            handleSearchResult(it)
        })
    }

    private fun showPrefs(animate: Boolean = true) {
        mPrefsLayout!!.expand(animate)
    }

    private fun hidePrefs(animate: Boolean = true) {
        mPrefsLayout!!.collapse(animate)
        if (subreddit_field.hasFocus()) {
            subreddit_field.clearFocus()
            setImeVisibility(subreddit_field, false)
        }
    }

    private fun showDropdown(animate: Boolean = true) {
        mDropdown?.expand(animate)
    }

    private fun hideDropdown(animate: Boolean = true) {
        mDropdown?.collapse(true)
    }

    private fun handleIntent(intent: Intent?) {
        intent ?: return
        when (intent.action) {
            Intent.ACTION_SEND -> {
                var searchString = intent.getStringExtra(Intent.EXTRA_TEXT)
                searchbar.setQuery(searchString, true)
                LogTag.d("searching from ACTION_SEND")
//                search(searchString)
            }
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                LogTag.d("searching from ACTION_SEARCH: $query")
                search(query)
            }
        }
//        if (Intent.ACTION_SEND == intent.action) {
//            searchString = intent.getStringExtra(Intent.EXTRA_TEXT)
//            getLinkSearchFragment()?.search(searchString)
//        }
//
//        toast(intentToString(intent))
//        LogTag.d(intentToString(intent))
//        if (Intent.ACTION_SEARCH == intent.action) {
//            val query = intent.getStringExtra(SearchManager.QUERY)
//            toast(query)
//        }
    }

    private fun setImeVisibility(view: View, visible: Boolean) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        if (!visible) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            return
        }
        if (imm.isActive(view)) {
            imm.showSoftInput(view, 0)
            return
        }
    }

    fun intentToString(intent: Intent?): String {
        if (intent == null)
            return ""

        val stringBuilder = StringBuilder("action: ")
                .append(intent.action)
                .append(" data: ")
                .append(intent.dataString)
                .append(" extras: ")
        if (intent.extras != null) {
            for (key in intent.extras.keySet())
                stringBuilder.append(key).append("=").append(intent.extras!!.get(key)).append(" ")
        }

        return stringBuilder.toString()
    }

    private fun toast(msg: String?) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun getLinkSearchFragment(): LinkSearchFragment {
        var fragment: Fragment? = supportFragmentManager.findFragmentById(R.id.link_search_fragment)
        return fragment as LinkSearchFragment
    }
}
