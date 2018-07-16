package us.brockolli.redditcomments.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager

class ViewUtils {
    companion object {
        fun setImeVisibility(view: View, visible: Boolean) {
            val imm = view.context.getSystemService(
                    Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (!visible) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0)
            } else if (imm.isActive(view)) {
                imm.showSoftInput(view, 0)
            }
        }

        fun fadeIn(view: View, duration: Long = 300) {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                    .alpha(1f)
                    .setDuration(duration)
                    .setListener(null)
        }

        fun fadeOut(view: View, duration: Long = 300) {
            view.animate()
                    .alpha(0f)
                    .setDuration(duration)
                    .setListener(object: AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator?) {
                            view.visibility = View.GONE
                        }
                    })
        }
    }
}