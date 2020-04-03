package pl.garedgame.game.util

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import pl.garedgame.game.BuildConfig
import pl.garedgame.game.Vector2
import pl.garedgame.game.Vector3
import java.nio.FloatBuffer
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class Utils {
    companion object {
        val rand = Random(System.currentTimeMillis())
        val handler = Handler(Looper.getMainLooper())
    }
}

fun doOnMainThread(action: () -> Unit) {
    if (Thread.currentThread() == Looper.getMainLooper().thread) {
        action()
    } else Utils.handler.post(action)
}

const val BYTES_PER_FLOAT = 4
const val TAG = "Ergon"

fun Float.inRange(f1: Float, f2: Float): Boolean = this in min(f1 , f2)..max(f1 , f2)

fun clamp(value: Float, min: Float, max: Float): Float = min(max(min, value), max)

fun Float.squared() = this * this
fun Double.squared() = this * this

fun FloatBuffer.put(v: Vector2) {
    put(v.x).put(v.y).put(0f)
}

fun FloatBuffer.put(v: Vector3) {
    put(v.x).put(v.y).put(v.z)
}

fun IntRange.random(): Int = first + Utils.rand.nextInt(last - first)
fun LongRange.random(): Long = first + Utils.rand.nextLong(last - first)

val Int.px: Int
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            Resources.getSystem().displayMetrics
    ).toInt()

val Float.px: Float
    get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            Resources.getSystem().displayMetrics
    )

fun View.setOnSingleOnMeasured(callback: () -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            callback()
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        }
    })
}

fun resizeToWidth(image: Drawable, newWidth: Int): Drawable? {
    val b = (image as BitmapDrawable).bitmap
    val newHeight = ( b.height.toFloat() / b.width.toFloat() * newWidth ).toInt()
    if (newWidth == 0 || newHeight == 0) return image
    val bitmapResized = Bitmap.createScaledBitmap(b, newWidth, newHeight, false)
    return BitmapDrawable(Resources.getSystem(), bitmapResized)
}

fun resizeToHeight(image: Drawable, newHeight: Int): Drawable? {
    val b = (image as BitmapDrawable).bitmap
    val newWidth = ( b.width.toFloat() / b.height.toFloat() * newHeight ).toInt()
    if (newWidth == 0 || newHeight == 0) return image
    val bitmapResized = Bitmap.createScaledBitmap(b, newWidth, newHeight, false)
    return BitmapDrawable(Resources.getSystem(), bitmapResized)
}

inline fun verbose(tag: String = TAG, message: () -> String) {
    if (BuildConfig.DEBUG) Log.v(tag, message())
}

inline fun debug(tag: String = TAG, message: () -> String) {
    if (BuildConfig.DEBUG) Log.d(tag, message())
}

inline fun info(tag: String = TAG, message: () -> String) {
    if (BuildConfig.DEBUG) Log.i(tag, message())
}

inline fun warn(tag: String = TAG, message: () -> String) {
    if (BuildConfig.DEBUG) Log.w(tag, message())
}

inline fun error(tag: String = TAG, message: () -> String) {
    if (BuildConfig.DEBUG) Log.e(tag, message())
}

fun timeRelateRotation(millis: Long): Float {
    return ((System.currentTimeMillis() % millis) / millis.toFloat()) * 360f
}

fun timeRelateScale(millis: Long, vararg scales: Float): Float {
    if (scales.isEmpty()) return 1f
    val currMillis = System.currentTimeMillis() % millis
    val step = millis / scales.size
    val index = (currMillis / step).toInt()
    val currStep = (currMillis % step).toFloat() / step.toFloat()
    val from = scales[index % scales.size]
    val to = scales[(index + 1) % scales.size]
    return from + currStep * (to - from)
}
