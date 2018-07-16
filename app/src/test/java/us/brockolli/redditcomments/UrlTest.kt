package us.brockolli.redditcomments

import org.junit.Assert.assertEquals
import org.junit.Test
import us.brockolli.redditcomments.utils.RedditUtils

class UrlTest {

    @Test
    fun testSearchUrl() {
        var params = RedditUtils.createParams("", "comments",
                "all", "50")
        var url = RedditUtils.createSearchUrl("google.com", params)
        assertEquals("https://www.reddit.com/search.json?type=link&restrict_sr=1&q=google.com&sort=comments&t=all&limit=50", url)
        url = RedditUtils.createSearchUrl("https://google.com", params)
        assertEquals("https://www.reddit.com/search.json?type=link&restrict_sr=1&q=url:https://google.com&sort=comments&t=all&limit=50", url)
    }
}