package us.brockolli.redditcomments.network

import com.google.gson.JsonObject

class SearchResult(val query: String,
                   val params: Map<String, String>,
                   val url: String,
                   val jsonObject: JsonObject?,
                   val e: Exception?,
                   val errorMsg: String?) {
    val success = jsonObject != null && e == null && errorMsg == null
}
