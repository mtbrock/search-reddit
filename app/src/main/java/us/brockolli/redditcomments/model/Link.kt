package us.brockolli.redditcomments.model

data class Link(
    val title: String?,
    val score: String?,
    val numComments: String?,
    val url: String?,
    val permalink: String?,
    val id: String?,
    val author: String?,
    val subreddit: String?,
    val domain: String?,
    val thumbnail: String?,
    val created: Long = 0)
