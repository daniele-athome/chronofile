package com.chaidarun.chronofile

enum class Metric {
  AVERAGE_DAY,
  AVERAGE_WEEK,
  TOTAL
}

data class GraphConfig(
  val grouped: Boolean = true,
  val stacked: Boolean = true,
  val metric: Metric = Metric.AVERAGE_DAY,
  val startTime: Long? = null,
  val endTime: Long? = null
)
