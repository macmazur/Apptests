package www.fokus.techbytes.utils

import android.content.res.Resources
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import kotlin.math.roundToInt

private val displayMetrics: DisplayMetrics by lazy { Resources.getSystem().displayMetrics }

/**
 * Returns boundary of the screen in pixels (px).
 */
val screenRectPx: Rect
    get() = displayMetrics.run { Rect(0, 0, widthPixels, heightPixels) }

/**
 * Returns boundary of the screen in density independent pixels (dp).
 */
val screenRectDp: RectF
    get() = screenRectPx.run { RectF(0f, 0f, right.px2dp, bottom.px2dp) }

/**
 * Converts any given number from pixels (px) into density independent pixels (dp).
 */
val Number.px2dp: Float
    get() = this.toFloat() / displayMetrics.density

/**
 * Converts any given number from density independent pixels (dp) into pixels (px).
 */
val Number.dp2px: Int
    get() = (this.toFloat() * displayMetrics.density).roundToInt()
