package us.brockolli.redditcomments.network

import android.content.Context

interface Request {
    fun load(context: Context)
}