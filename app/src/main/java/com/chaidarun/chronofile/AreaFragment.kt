package com.chaidarun.chronofile

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_area.areaChart
import kotlinx.android.synthetic.main.fragment_area.areaIsGrouped
import kotlinx.android.synthetic.main.fragment_area.areaIsStacked

class AreaFragment : GraphFragment() {

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View = inflater.inflate(R.layout.fragment_area, container, false)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    with(areaChart) {
      with(axisLeft) {
        axisMinimum = 0f
        setDrawAxisLine(false)
        setDrawGridLines(false)
        setDrawLabels(false)
      }
      axisRight.isEnabled = false
      description.isEnabled = false
      with(legend) {
        isWordWrapEnabled = true
        textColor = LABEL_COLOR
        textSize = LABEL_FONT_SIZE
        typeface = App.instance.typeface
        xEntrySpace = 15f
      }

      setDrawBorders(false)
      setDrawGridBackground(false)
      with(xAxis) {
        setDrawAxisLine(false)
        setDrawGridLines(false)
        setDrawLabels(false)
      }

      setTouchEnabled(false)
    }

    disposables =
      CompositeDisposable().apply {
        add(
          Store.observable
            .filter { it.config != null && it.history != null }
            .map { Triple(it.config!!, it.history!!, it.graphConfig) }
            .distinctUntilChanged()
            .subscribe {
              render(it)
              val visibleStart = it.third.startTime?.toFloat()
              if (visibleStart != null) areaChart.moveViewToX(visibleStart)
            }
        )
        add(
          Store.observable.map { it.graphConfig.grouped }.distinctUntilChanged().subscribe {
            areaIsGrouped.isChecked = it
          }
        )
        add(
          Store.observable.map { it.graphConfig.stacked }.distinctUntilChanged().subscribe {
            areaIsStacked.isChecked = it
          }
        )
      }
  }

  private fun render(state: Triple<Config, History, GraphConfig>) {
    val start = System.currentTimeMillis()
    val (config, history, graphConfig) = state

    // Get top groups
    val (rangeStart, rangeEnd) = getChartRange(history, graphConfig)
    val (buckets, sliceList) =
      aggregateEntries(
        config,
        history,
        graphConfig,
        getPreviousMidnight(rangeStart),
        rangeEnd - 1,
        Aggregation.DAY
      )
    val groups = sliceList.map { it.first }

    // Convert into data set lists
    val lines = groups.associateWith { mutableListOf<Entry>() }.toMutableMap()
    val groupsReversed = groups.reversed()
    val stacked = graphConfig.stacked
    var maxEntrySeconds = 0L
    for ((dayStart, dayGroups) in buckets.toList().sortedBy { it.first }) {
      var seenSecondsToday = 0L
      for (group in groupsReversed) {
        val seconds = dayGroups.getOrDefault(group, 0L)
        seenSecondsToday += seconds
        maxEntrySeconds = Math.max(maxEntrySeconds, seconds)
        val entrySeconds = if (stacked) seenSecondsToday else seconds
        lines[group]?.add(Entry(dayStart.toFloat(), entrySeconds.toFloat()))
          ?: error("$group missing from area chart data sets")
      }
    }

    val dataSets =
      groups.mapIndexed { i, group ->
        LineDataSet(lines[group], group).apply {
          val mColor = COLORS[i % COLORS.size].apply { setCircleColor(this) }
          axisDependency = YAxis.AxisDependency.LEFT
          color = mColor
          lineWidth = if (stacked) 0f else 1f
          fillAlpha = if (stacked) 255 else 0
          fillColor = mColor
          fillFormatter = IFillFormatter { _, _ -> areaChart.axisLeft.axisMinimum }
          setDrawCircles(false)
          setDrawCircleHole(false)
          setDrawFilled(true)
          setDrawHorizontalHighlightIndicator(false)
          setDrawValues(false)
          setDrawVerticalHighlightIndicator(false)
        }
      }

    with(areaChart) {
      with(axisLeft) {
        axisMaximum = if (stacked) DAY_SECONDS.toFloat() else maxEntrySeconds.toFloat()
        removeAllLimitLines()
        if (!stacked) addLimitLine(limitLine)
      }
      data = LineData(dataSets)
      isScaleYEnabled = !stacked
      invalidate()
    }

    val elapsed = System.currentTimeMillis() - start
    Log.i(TAG, "Rendered area chart in $elapsed ms")
  }

  companion object {
    val limitLine =
      LimitLine(28800f).apply {
        lineColor = Color.WHITE
        lineWidth = 2f
        enableDashedLine(5f, 5f, 0f)
      }
  }
}
