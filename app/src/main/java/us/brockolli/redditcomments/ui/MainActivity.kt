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
import android.content.Context
import android.widget.Toast
import android.os.Parcelable
import android.support.v4.text.TextUtilsCompat
import android.support.v7.widget.SearchView
import android.text.TextUtils
import android.widget.TextView
import kotlinx.android.synthetic.main.content_main.*
import net.cachapa.expandablelayout.ExpandableLayout
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.utils.RedditUtils
import java.util.*


class MainActivity : AppCompatActivity(), LinkSearchFragment.OnLinkSearchInteractionListener {
    var mSearchString: String? = null
    var mDropdownText: TextView? = null
    var mDropdown: ExpandableLayout? = null


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

        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchbar.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchbar.setIconifiedByDefault(false)
        searchbar.setOnQueryTextFocusChangeListener { v, hasFocus ->
            if(!hasFocus && TextUtils.isEmpty(searchbar.query) && !TextUtils.isEmpty(mSearchString)) {
                searchbar.setQuery(mSearchString, false)
            }
        }
        searchbar.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if(newText == mSearchString || TextUtils.isEmpty(mSearchString)) {
                    hideDropdown()
                } else {
                    showDropdown()
                }
                return false
            }

        })

//        fab.setOnClickListener { view ->
//            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                    .setAction("Action", null).show()
//        }

        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
//        toast("onResume")
    }

    override fun onPause() {
        super.onPause()
        searchbar.clearFocus()
    }

    override fun onNewIntent(intent: Intent?) {
        setIntent(intent)
        handleIntent(intent)
    }

    private fun search(searchString: String?) {
        searchString ?: return
        mSearchString = searchString
        mDropdownText!!.text = "\"$searchString\""
        getLinkSearchFragment()?.search(searchString)
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
                searchbar.setQuery(searchString, false)
                search(searchString)
            }
            Intent.ACTION_SEARCH -> {
                val query = intent.getStringExtra(SearchManager.QUERY)
                search(query)
                LogTag.d("SEARch QUERY: $query")
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
