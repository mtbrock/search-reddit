package us.brockolli.redditcomments.ui

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.AttributeSet
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.link_list_item.view.*
import us.brockolli.redditcomments.R
import us.brockolli.redditcomments.model.Link

class LinkView: LinearLayout {
    constructor(context: Context?): this(context, null)
    constructor(context: Context?, attrs: AttributeSet?): super(context, attrs)

    var link: Link? = null
        set(link) {
            field = link
            link?.let {
                update(it)
            }
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