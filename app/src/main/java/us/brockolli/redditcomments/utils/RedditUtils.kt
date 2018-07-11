package us.brockolli.redditcomments.utils

import android.net.Uri
import android.text.TextUtils
import android.util.Patterns



class RedditUtils {
    companion object {
        private val REDDIT_URL = "https://www.reddit.com"
        //        private val SUBREDDITS_URL = "https://www.reddit.com/r/reddits.json?limit=100";
        private val SEARCH_URL = REDDIT_URL + "/search.json?sort=comments&q="

        fun createSearchUri(searchString: String): Uri {
            return Uri.parse(createSearchUrl(searchString))
        }

        fun createSearchUrl(searchString: String): String {
            val search = SearchUtils.createEncodedSearchString(searchString)
            return "${SEARCH_URL}${search}"
        }

        fun createCommentsUrl(permalink: String): String {
            return "${REDDIT_URL}${permalink}"
        }
    }
}