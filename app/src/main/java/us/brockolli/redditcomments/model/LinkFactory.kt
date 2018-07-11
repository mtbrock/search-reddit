package us.brockolli.redditcomments.model

import com.google.gson.JsonObject
import java.util.ArrayList

class LinkFactory {
    companion object {
        fun createLinkFromJsonObject(jsonObject: JsonObject): Link {
            return Link(id=jsonObject.get("id").getAsString(),
                    title=jsonObject.get("title").getAsString(),
                    url=jsonObject.get("url").getAsString(),
                    permalink=jsonObject.get("permalink").getAsString(),
                    numComments=jsonObject.get("num_comments").getAsString(),
                    subreddit=jsonObject.get("subreddit").getAsString(),
                    score=jsonObject.get("score").getAsString(),
                    author=jsonObject.get("author").getAsString(),
                    domain=jsonObject.get("domain").getAsString(),
                    thumbnail=jsonObject.get("thumbnail").getAsString(),
                    created=jsonObject.get("created_utc").getAsLong())
        }

        fun createListOfLinksFromJsonObject(jsonObject: JsonObject): List<Link> {
            val links = ArrayList<Link>()
            val children = jsonObject.getAsJsonObject("data")
                    .getAsJsonArray("children")
            for (i in 0 until children.size()) {
                val linkData = children.get(i)
                        .getAsJsonObject().get("data")
                        .getAsJsonObject()
                links.add(createLinkFromJsonObject(linkData))
            }
            return links
        }
    }
}