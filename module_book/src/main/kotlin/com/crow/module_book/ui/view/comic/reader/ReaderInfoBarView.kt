package com.crow.module_book.ui.view.comic.reader

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Shader
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.WindowInsets
import androidx.annotation.AttrRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.use
import androidx.core.graphics.ColorUtils
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.crow.base.tools.extensions.error
import com.crow.base.tools.extensions.measureDimension
import com.crow.base.tools.extensions.resolveDp
import com.crow.module_book.R
import com.crow.module_book.model.entity.comic.reader.ReaderInfo
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.android.material.R as materialR
import com.crow.base.R as baseR

class ReaderInfoBarView @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	@AttrRes defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

	private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
	private val textBounds = Rect()
	private val timeFormat = SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
	private val timeReceiver = TimeReceiver()
	private var insetLeft: Int = 0
	private var insetRight: Int = 0
	private var insetTop: Int = 0
	private var cutoutInsetLeft = 0
	private var cutoutInsetRight = 0
	private val colorText = ColorUtils.setAlphaComponent(
		context.obtainStyledAttributes(intArrayOf(materialR.attr.colorOnSurface)).use {
			it.getColor(0, Color.BLACK)
		},
		200,
	)
	private val colorOutline = ColorUtils.setAlphaComponent(
		context.obtainStyledAttributes(intArrayOf(materialR.attr.colorSurface)).use {
			it.getColor(0, Color.WHITE)
		},
		200,
	)

	private var timeText = timeFormat.format(Date())
	private var text: String = ""

	private val innerHeight
		get() = height - paddingTop - paddingBottom - insetTop

	private val innerWidth
		get() = width - paddingLeft - paddingRight - insetLeft - insetRight

	init {
		val insetCorner = getSystemUiDimensionOffset("rounded_corner_content_padding")
		val fallbackInset = resources.getDimensionPixelOffset(baseR.dimen.base_dp8)
		val insetStart = getSystemUiDimensionOffset("status_bar_padding_start", fallbackInset) + insetCorner
		val insetEnd = getSystemUiDimensionOffset("status_bar_padding_end", fallbackInset) + insetCorner
		val isRtl = layoutDirection == LAYOUT_DIRECTION_RTL
		paint.strokeWidth = context.resources.resolveDp(2f)
		paint.setShadowLayer(2f, 1f, 1f, Color.GRAY)
		insetLeft = if (isRtl) insetEnd else insetStart
		insetRight = if (isRtl) insetStart else insetEnd
		insetTop = minOf(insetLeft, insetRight)
	}

	override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
		val desiredWidth = suggestedMinimumWidth + paddingLeft + paddingRight + insetLeft + insetRight
		val desiredHeight = suggestedMinimumHeight + paddingTop + paddingBottom + insetTop
		setMeasuredDimension(
			measureDimension(desiredWidth, widthMeasureSpec),
			measureDimension(desiredHeight, heightMeasureSpec),
		)
	}

	override fun onDraw(canvas: Canvas) {
		super.onDraw(canvas)
		val ty = innerHeight / 2f + textBounds.height() / 2f - textBounds.bottom
		paint.textAlign = Paint.Align.LEFT
		canvas.drawTextOutline(
			text,
			(paddingLeft + insetLeft + cutoutInsetLeft).toFloat(),
			paddingTop + insetTop + ty,
		)
		paint.textAlign = Paint.Align.RIGHT
		canvas.drawTextOutline(
			timeText,
			(width - paddingRight - insetRight - cutoutInsetRight).toFloat(),
			paddingTop + insetTop + ty,
		)
	}

	override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
		super.onSizeChanged(w, h, oldw, oldh)
		updateCutoutInsets(ViewCompat.getRootWindowInsets(this))
		updateTextSize()
	}

	override fun onApplyWindowInsets(insets: WindowInsets): WindowInsets {
		updateCutoutInsets(WindowInsetsCompat.toWindowInsetsCompat(insets))
		return super.onApplyWindowInsets(insets)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		ContextCompat.registerReceiver(
			context,
			timeReceiver,
			IntentFilter(Intent.ACTION_TIME_TICK),
			ContextCompat.RECEIVER_EXPORTED,
		)
		updateCutoutInsets(ViewCompat.getRootWindowInsets(this))
	}

	override fun onDetachedFromWindow() {
		super.onDetachedFromWindow()
		context.unregisterReceiver(timeReceiver)
	}

	fun update(currentPage: Int, totalPage: Int, percent: Float, info: ReaderInfo) {
		text = context.getString(
			R.string.book_reader_info_bar,
			info.mChapterIndex + 1,
			info.mChapterCount,
			currentPage,
			totalPage,
			if(percent in 0f..1f) (percent * 100).format() else ""
		)
		updateTextSize()
		invalidate()
	}

	private fun Number.format(decimals: Int = 0, decPoint: Char = '.', thousandsSep: Char? = ' '): String {
		val formatter = NumberFormat.getInstance(Locale.US) as DecimalFormat
		val symbols = formatter.decimalFormatSymbols
		if (thousandsSep != null) {
			symbols.groupingSeparator = thousandsSep
			formatter.isGroupingUsed = true
		} else {
			formatter.isGroupingUsed = false
		}
		symbols.decimalSeparator = decPoint
		formatter.decimalFormatSymbols = symbols
		formatter.minimumFractionDigits = decimals
		formatter.maximumFractionDigits = decimals
		return when (this) {
			is Float,
			is Double, -> formatter.format(this.toDouble())
			else -> formatter.format(this.toLong())
		}
	}


	private fun updateTextSize() {
		val str = text + timeText
		val testTextSize = 48f
		paint.textSize = testTextSize
		paint.getTextBounds(str, 0, str.length, textBounds)
		paint.textSize = testTextSize * innerHeight / textBounds.height()
		paint.getTextBounds(str, 0, str.length, textBounds)
	}

	private fun Canvas.drawTextOutline(text: String, x: Float, y: Float) {
		paint.color = colorOutline
		paint.style = Paint.Style.STROKE
		drawText(text, x, y, paint)
		paint.color = colorText
		paint.style = Paint.Style.FILL
		drawText(text, x, y, paint)
	}

	private fun updateCutoutInsets(insetsCompat: WindowInsetsCompat?) {
		val cutouts = (insetsCompat ?: return).displayCutout?.boundingRects.orEmpty()
		cutoutInsetLeft = 0
		cutoutInsetRight = 0
		for (rect in cutouts) {
			if (rect.left <= paddingLeft) {
				cutoutInsetLeft += rect.width()
			}
			if (rect.right >= width - paddingRight) {
				cutoutInsetRight += rect.width()
			}
		}
	}

	private inner class TimeReceiver : BroadcastReceiver() {

		override fun onReceive(context: Context?, intent: Intent?) {
			timeText = timeFormat.format(Date())
			invalidate()
		}
	}

	@SuppressLint("DiscouragedApi")
	private fun getSystemUiDimensionOffset(name: String, fallback: Int = 0): Int = runCatching {
		val manager = context.packageManager
		val resources = manager.getResourcesForApplication("com.android.systemui")
		val resId = resources.getIdentifier(name, "dimen", "com.android.systemui")
		resources.getDimensionPixelOffset(resId)
	}.onFailure {
		it.stackTraceToString().error()
	}.getOrDefault(fallback)
}
