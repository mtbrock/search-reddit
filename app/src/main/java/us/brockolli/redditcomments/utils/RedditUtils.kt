package us.brockolli.redditcomments.utils

import android.net.Uri
import android.text.TextUtils
import android.util.Patterns



class RedditUtils {
    companion object {
        private val REDDIT_URL = "https://www.reddit.com"
        //        private val SUBREDDITS_URL = "https://www.reddit.com/r/reddits.json?limit=100";
        private val SEARCH_PREFIX = "/search.json?type=link&restrict_sr=1&q="
        private val SEARCH_URL = REDDIT_URL + SEARCH_PREFIX

        fun createSearchUrl(searchString: String, params: Map<String, String>): String {
            val search = SearchUtils.createEncodedSearchString(searchString)
            val suffix = StringBuilder()
            var subreddit = params.get("subreddit")
            val searchUrl = searchUrl(subreddit)
            params.forEach {
                if(it.key != "subreddit") suffix.append("&${it.key}=${it.value}")
            }
            return "${searchUrl}${search}${suffix}"
        }

        fun createCommentsUrl(permalink: String): String {
            return "${REDDIT_URL}${permalink}"
        }

        fun createSubredditUrl(subreddit: String): String {
            return "${REDDIT_URL}/r/$subreddit"
        }

        fun createParams(subreddit: String = "", sort: String = "comments", from: String = "all",
                         limit: String = "25"): Map<String, String> {
            return mapOf(Pair("subreddit", subreddit), Pair("sort", sort), Pair("t", from),
                    Pair("limit", limit))
        }

        private fun searchUrl(subreddit: String?): String = when {
            TextUtils.isEmpty(subreddit) -> SEARCH_URL
            else -> "${REDDIT_URL}/r/$subreddit" + SEARCH_PREFIX
        }
    }
}