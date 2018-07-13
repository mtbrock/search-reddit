package us.brockolli.redditcomments.ui

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.gson.JsonObject
import kotlinx.android.synthetic.main.fragment_search.view.*
import net.cachapa.expandablelayout.ExpandableLayout
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.R
import us.brockolli.redditcomments.model.Link
import us.brockolli.redditcomments.model.LinkFactory
import us.brockolli.redditcomments.network.JsonResult
import us.brockolli.redditcomments.network.LinkSearchViewModel
import us.brockolli.redditcomments.network.RequestCallback
import us.brockolli.redditcomments.network.RequestFactory
import us.brockolli.redditcomments.utils.RedditUtils

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [LinkSearchFragment.OnLinkSearchInteractionListener] interface.
 */
class LinkSearchFragment : Fragment() {

    private var columnCount = 1

    private var listener: OnLinkSearchInteractionListener? = null

    private var mListView: RecyclerView? = null

    private var mSearchResult: JsonResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            columnCount = it.getInt(ARG_COLUMN_COUNT)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            mListView = view
            with(mListView!!) {
                layoutManager = when {
                    columnCount <= 1 -> LinearLayoutManager(context)
                    else -> GridLayoutManager(context, columnCount)
                }
            }
        }
        return view
    }

    override fun onResume() {
        super.onResume()
//        val searchText = searchProvider?.getSearchText() ?: return
//        val url = RedditUtils.createSearchUrl(searchText)
////        val url = "https://www.reddit.com/search.json?q=A4_auMe1HsY"
//        val request = RequestFactory.createJsonRequest(requireContext(), url, this)
//        request.load()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnLinkSearchInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnListFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    fun search(searchText: String?, params: Map<String, String>) {
        searchText ?: return
        val searchModel = ViewModelProviders.of(requireActivity())
                .get(LinkSearchViewModel::class.java)
        val resultData = searchModel.getJsonResult(requireContext(), searchText,
                params)
        resultData.observe(this, Observer {
            mSearchResult = it
            val jsonObject = it!!.jsonObject
            if (jsonObject == null || it!!.e != null) {
                // No result
                LogTag.e("Search result empty, error msg: ${it.errorMsg}")
            } else {
                val links = LinkFactory.createListOfLinksFromJsonObject(jsonObject!!)
                if (links.size > 0) {
                    mListView?.adapter = LinkRecyclerViewAdapter(links, listener)
                } else {
                    // Result is empty
                }
            }
        })
//        val linksData = searchModel.search(requireContext(), searchText,
//                params)
//        linksData.observe(this, Observer {
//            mListView?.adapter = LinkRecyclerViewAdapter(it!!, listener)
//        })
//        mQueryString = "\"$searchText\""
//        val url = RedditUtils.createSearchUrl(searchText)
////        val url = "https://www.reddit.com/search.json?q=A4_auMe1HsY"
//        val request = RequestFactory.createJsonRequest(requireContext(), url, this)
//        request.load()
    }

//    override fun onCompleted(e: Exception?, result: JsonObject?) {
//        if(e != null) {
//            LogTag.e("error: " + e.message);
//        }
//        if(result == null) {
//            LogTag.e("error: no result");
//            return;
//        }
//        val listing = LinkFactory.createListOfLinksFromJsonObject(result)
//        mListView!!.adapter = LinkRecyclerViewAdapter(listing, listener)
//        mQueryText?.text = mQueryString
//        mQueryTextLayout?.expand(true)
//    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson
     * [Communicating with Other Fragments](http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnLinkSearchInteractionListener {
        fun onListInteraction(item: Link?)
    }

    companion object {

        // TODO: Customize parameter argument names
        const val ARG_COLUMN_COUNT = "column-count"

        // TODO: Customize parameter initialization
        @JvmStatic
        fun newInstance(columnCount: Int) =
                LinkSearchFragment().apply {
                    arguments = Bundle().apply {
                        putInt(ARG_COLUMN_COUNT, columnCount)
                    }
                }
    }
}
