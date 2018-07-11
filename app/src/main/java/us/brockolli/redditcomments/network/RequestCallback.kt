package us.brockolli.redditcomments.network

interface RequestCallback<T: Any> {
    fun onCompleted(e: Exception?, result: T?)
}
