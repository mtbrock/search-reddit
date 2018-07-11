package us.brockolli.redditcomments

import android.util.Log

class LogTag {
    companion object {
        val TAG: String = "MattBrock"

        fun d(msg: String) {
            Log.d(TAG, msg)
        }

        fun i(msg: String) {
            Log.i(TAG, msg)
        }

        fun v(msg: String) {
            Log.v(TAG, msg)
        }

        fun w(msg: String) {
            Log.w(TAG, msg)
        }

        fun e(msg: String) {
            Log.e(TAG, msg)
        }
    }
}
