package us.brockolli.redditcomments

import org.junit.Test
import org.junit.Assert.*
import us.brockolli.redditcomments.utils.RedditUtils

class UrlTest {
    @Test
    fun testSearchUrl() {
        var url = RedditUtils.createSearchUrl("ign.com")
        assertEquals("https://www.reddit.com/search.json?q=ign.com", url)
        url = RedditUtils.createSearchUrl("https://ign.com")
        assertEquals("https://www.reddit.com/search.json?q=url:https://ign.com", url)
    }
}