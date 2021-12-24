package se.allco.githubbrowser.utils

import android.content.Context
import android.util.TypedValue

/**
 * Context related utils
 */

fun Context.dpToPx(dps: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dps, resources.displayMetrics).toInt()
