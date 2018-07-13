package us.brockolli.redditcomments.ui

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.widget_search_params.view.*
import us.brockolli.redditcomments.LogTag
import us.brockolli.redditcomments.R

class SearchParamsWidget: RelativeLayout {
    private var mOnParamsChangedListener: OnParamsChangedListener? = null

    private var mItemSelectedListener = object: AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) {
            LogTag.e("onItemSelectedListener:onNothingSelected ${parent.toString()}")
        }

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            onParamsChanged()
        }
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet?): super(context, attrs) {
        init(context)
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0):
            this(context, attrs)

    private fun init(context: Context) {
        View.inflate(context, R.layout.widget_search_params, this)

        var adapter = ArrayAdapter<String>(context, R.layout.simple_spinner_item,
                mutableListOf("relevance", "top", "new", "comments"))
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        sort_spinner.adapter = adapter
        sort_spinner.setSelection(3)
        sort_spinner.onItemSelectedListener = mItemSelectedListener

        adapter = ArrayAdapter<String>(context, R.layout.simple_spinner_item,
                mutableListOf("past hour", "past day", "past week", "past month", "past year", "all time"))
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        from_spinner.adapter = adapter
        from_spinner.setSelection(5)
        from_spinner.onItemSelectedListener = mItemSelectedListener

        adapter = ArrayAdapter<String>(context, R.layout.simple_spinner_item,
                mutableListOf("25", "50", "100"))
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        limit_spinner.adapter = adapter
        limit_spinner.setSelection(0)
        limit_spinner.onItemSelectedListener = mItemSelectedListener

        adapter = ArrayAdapter<String>(context, R.layout.autocomplete_dropdown_item,
                mutableListOf("sports", "nsfw", "WTF", "gifs", "videos", "pics", "funny", "PS4", "png"))
        subreddit_field.setAdapter(adapter)
        subreddit_field.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                onParamsChanged()
            }
        })
    }

    fun getParams(): Map<String, String> {
        val sort = sort_spinner.selectedItem?.toString() ?: "comments"
        var from = from_spinner.selectedItem?.toString() ?: "all"
        from = from.replace("past ", "").replace(" time", "")
        val limit = limit_spinner.selectedItem?.toString() ?: "25"
        val subreddit = subreddit_field.text.toString()

        return mapOf(Pair("sort", sort), Pair("t", from), Pair("limit", limit),
                Pair("subreddit", subreddit))
    }

    private fun onParamsChanged() {
        mOnParamsChangedListener?.onParamsChanged(getParams())
    }

    fun setOnParamsChangedListener(listener: OnParamsChangedListener) {
        mOnParamsChangedListener = listener
    }

    interface OnParamsChangedListener {
        fun onParamsChanged(params: Map<String, String>)
    }
}