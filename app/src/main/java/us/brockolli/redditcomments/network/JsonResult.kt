package us.brockolli.redditcomments.network

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.Serializable

data class JsonResult(val url: String, val jsonObject: JsonObject?,
                      val e: Exception? = null, val errorMsg: String? = null)
