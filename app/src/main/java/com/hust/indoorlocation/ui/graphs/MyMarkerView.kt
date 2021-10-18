package com.hust.indoorlocation.ui.graphs

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.hust.indoorlocation.R


/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
class MyMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById<TextView>(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        if (e is CandleEntry) {
            val ce: CandleEntry = e as CandleEntry
            tvContent.setText(Utils.formatNumber(ce.getHigh(), 0, true))
        } else {
            tvContent.setText(Utils.formatNumber(e.y, 0, true))
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(getWidth() / 2).toFloat(), -getHeight().toFloat())
    }


}