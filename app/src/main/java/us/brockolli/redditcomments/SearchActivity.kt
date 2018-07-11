package us.brockolli.redditcomments

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

       // val tv = findViewById<TextView>(R.id.tv)
    }

    override fun onResume() {
        super.onResume()
        var s: String = ""
        var sharedText: String? = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return

        s += sharedText

        if (sharedText!!.contains("youtube") || sharedText.contains("youtu.be")) {
            sharedText = sharedText.replace("http://", "")
            sharedText = sharedText.replace("https://", "")
            sharedText = sharedText.replace("www.youtube.com/watch?v=", "")
            sharedText = sharedText.replace("www.youtu.be/", "")
            sharedText = sharedText.replace("youtube.com/watch?v=", "")
            sharedText = sharedText.replace("youtu.be/", "")
        }

        s += "\n" + sharedText
        tv.text = s
    }
}
