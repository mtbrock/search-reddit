package us.brockolli.redditcomments.ui

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import us.brockolli.redditcomments.R


import kotlinx.android.synthetic.main.fragment_link_search.view.*
import us.brockolli.redditcomments.model.Link

/**
 * [RecyclerView.Adapter] that can display a [Link] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 */
class LinkRecyclerViewAdapter(
        private val mValues: List<Link>,
        private val mListener: LinkSearchFragment.OnLinkSearchInteractionListener?)
    : RecyclerView.Adapter<LinkRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as Link
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_link_search, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        holder.linkView.link = item

        with(holder.view) {
            tag = item
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val linkView: LinkView = view as LinkView

        override fun toString(): String {
            return super.toString() + " '" + linkView.title.text + "'"
        }
    }
}
