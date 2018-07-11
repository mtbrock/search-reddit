package us.brockolli.redditcomments.utils

import android.text.TextUtils
import android.util.Patterns

class SearchUtils {
    companion object {
        fun isValidUrl(url: String): Boolean {
            val p = Patterns.WEB_URL
            val m = p.matcher(url.toLowerCase())
            return m.matches()
        }

        fun isWebUrl(url: String): Boolean {
            return url.startsWith("http://") ||
                    url.startsWith("https://")
        }

        fun isYoutubeUrl(url: String): Boolean {
            return url.startsWith("http://youtube.com") ||
                    url.startsWith("https://youtu.be") ||
                    url.startsWith("youtube.com") ||
                    url.startsWith("youtu.be")
        }

        fun filterYoutube(url: String): String {
            var newString = url
            if(url.contains("youtube") || url.contains("youtu.be")) {
                newString = url.replace("http://", "")
                        .replace("https://", "")
                        .replace("/watch?v=", "")
                        .replace("www.", "")
                        .replace("youtube.com/", "")
                        .replace("youtu.be/", "")
            }
            return newString
        }

        fun createEncodedSearchString(search: String): String {
            var prefix = ""
            var searchString = search
            if (isWebUrl(search) || isYoutubeUrl(search)) {
                prefix = "url:"
                searchString = filterYoutube(search)
            }
            searchString = "${prefix}${searchString}"
            return TextUtils.htmlEncode(searchString).replace(" ", "%20")
        }
    }
}