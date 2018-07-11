package us.brockolli.redditcomments.ui

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.fragment_link_search.view.*
import us.brockolli.redditcomments.R
import us.brockolli.redditcomments.model.Link

class LinkView: LinearLayout {
    constructor(context: Context?): super(context)
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int = 0):
            this(context, attrs)

    var link: Link? = null
        set(link) {
            field = link
            update(link!!)
        }

    private fun update(link: Link) {
        val spannableString = SpannableString("${link.title} (${link.domain})")
        val foregroundSpan = ForegroundColorSpan(resources.getColor(R.color.material_grey500))
        val end = spannableString.length
        val start = spannableString.lastIndexOf("(", end, true)
        spannableString.setSpan(foregroundSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//        title.text = spannableString
        title.text = "${link.title}"
        comments.text = "${link.numComments} comments"
        points.text = "${link.score}"
        subreddit.text = "${link.subreddit}"
        domain.text = "(${link.domain})"
    }
}