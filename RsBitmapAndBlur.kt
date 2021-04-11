package com.task.fab_bottom_nav


import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.renderscript.*
import android.view.View
import androidx.annotation.Nullable
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.roundToInt

class RsBitmapAndBlur(private val renderScript: RenderScript) {
    @Nullable
    fun blur1(bitmap: Bitmap, radius: Float, repeat: Int): Bitmap? {
        var radius = radius
        if (!IS_BLUR_SUPPORTED) {
            return null
        }
        if (radius > MAX_RADIUS) {
            radius = MAX_RADIUS.toFloat()
        }
        val width = bitmap.width
        val height = bitmap.height

        // Create allocation type
        val bitmapType: Type = Type.Builder(renderScript, Element.RGBA_8888(renderScript))
                .setX(width)
                .setY(height)
                .setMipmaps(false) // We are using MipmapControl.MIPMAP_NONE
                .create()

        // Create allocation
        var allocation = Allocation.createTyped(renderScript, bitmapType)

        // Create blur script
        var blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript))
        blurScript!!.setRadius(radius)

        // Copy data to allocation
        allocation!!.copyFrom(bitmap)

        // set blur script input
        blurScript.setInput(allocation)

        // invoke the script to blur
        blurScript.forEach(allocation)

        // Repeat the blur for extra effect
        for (i in 0 until repeat) {
            blurScript.forEach(allocation)
        }

        // copy data back to the bitmap
        allocation.copyTo(bitmap)

        // release memory
        allocation.destroy()
        blurScript.destroy()
        return bitmap
    }

     private val bitmapScale = 0.4f
     private val bitmapRadius = 7.5f
    fun blur2(context: Context?, image: Bitmap): Bitmap? {
        val width = (image.width * bitmapScale).roundToInt()
        val height = (image.height * bitmapScale).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(bitmapRadius)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        tmpOut.destroy()
        tmpIn.destroy()
        theIntrinsic.destroy()
        return outputBitmap
    }

     fun createBitmapFromView1(view: View): Bitmap? {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val c = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(c)
        return bitmap
    }

    fun createBitmapFromView2(context: Context, view: View): Bitmap {

        view.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        val dm = context.resources.displayMetrics
        view.measure(
                View.MeasureSpec.makeMeasureSpec(dm.widthPixels, View.MeasureSpec.EXACTLY),
                View.MeasureSpec.makeMeasureSpec(dm.heightPixels, View.MeasureSpec.EXACTLY)
        )
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val bitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight,
                Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        view.layout(view.left, view.top, view.right, view.bottom)
        view.draw(canvas)
        return bitmap
    }

    companion object {
        private val IS_BLUR_SUPPORTED = Build.VERSION.SDK_INT >= 17
        private const val MAX_RADIUS = 25
    }

}