package us.brockolli.redditcomments

import com.google.gson.JsonParser
import org.junit.Test
import us.brockolli.redditcomments.network.SearchResult
import us.brockolli.redditcomments.utils.RedditUtils

class SearchResultTest {
    @Test
    fun testSearchResult() {
        val params = RedditUtils.createParams()
        val jsonObject = JsonParser().parse("{ foo: bar }").asJsonObject
        var result = SearchResult("test", params, "test.url", jsonObject, null,  null)
        assert(result.success)
    }
}