package us.brockolli.redditcomments.model

import com.google.gson.JsonObject
import us.brockolli.redditcomments.network.JsonRequest
import us.brockolli.redditcomments.network.JsonResult
import us.brockolli.redditcomments.network.RequestFactory
import us.brockolli.redditcomments.utils.RedditUtils

class RedditSearch(query: String, val params: MutableMap<String, String> = mutableMapOf()) {
    var query = query
    var url: String = ""
        get() = RedditUtils.createSearchUrl(query, params)
        private set
}