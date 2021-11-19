package queuing_system

import HOUR_IN_SECONDS
import MINUTE_IN_SECONDS
import space.kscience.dataforge.values.Value
import space.kscience.plotly.*
import space.kscience.plotly.models.ScatterMode
import space.kscience.plotly.models.Ticks
import space.kscience.plotly.models.color

fun showStateProbabilitiesPlot(plotTitle: String, stateProbabilities: StateProbabilityMap) {
    val clientsInSystem = stateProbabilities.keys.map { "p(${it.first}, ${it.second})" }
    val probabilities = stateProbabilities.values

    val operatorsCount = stateProbabilities.keys.maxByOrNull { it.first }!!.first
    val barColors = List(clientsInSystem.size) {
        when {
            it == 0 -> "#E84A5F"
            it <= operatorsCount -> "#8CBA80"
            else -> "#F2B880"
        }
    }

    val plot = Plotly.plot {
        bar {
            x(*clientsInSystem.toTypedArray())
            y(*probabilities.toTypedArray())
            name = "System state"

            marker {
                colors(barColors)
                width = 0.5
            }
        }

        layout {
            title {
                text = plotTitle
                font {
                    family = "Raleway, sans-serif"
                }
            }

            yaxis {
                range(Value.of(0), Value.of(1))
                gridcolor("#FFFFFF")
                title = "Probability"
            }

            xaxis {
                title = "State (in service, in queue)"
            }

            width = 1000
            plot_bgcolor("#2A363B")
        }
    }

    plot.makeFile()
}

fun showStateProbabilitiesEveryMinutePlot(
    plotTitle: String,
    stateProbabilitiesEveryMinute: StateProbabilitiesListMap,
) {
    val plot = Plotly.plot {
        stateProbabilitiesEveryMinute.forEach { (state, probabilities) ->
            val minutes = probabilities.indices.toList()

            scatter {
                x(*minutes.toTypedArray())
                y(*probabilities.toTypedArray())
                name = "p(${state.first}, ${state.second})"
            }
        }

        layout {
            title {
                text = plotTitle
                font {
                    family = "Raleway, sans-serif"
                }
            }

            legend {
                font {
                    family = "Raleway, sans-serif"
                }
            }

            yaxis {
                range(Value.of(0), Value.of(1))
                gridcolor("#FFFFFF")
                title = "Probability"
            }

            xaxis {
                title = "Time, min"
            }

            width = 1000
            plot_bgcolor("#2A363B")
        }
    }

    plot.makeFile()
}

fun showSingleChannelStateProgression(
    plotTitle: String,
    operatorIsAvailableEverySecond: List<Boolean>,
    rejectedClientsSeconds: MutableList<Long>
) {
    val multiplier = 45
    val valueEachSecond = operatorIsAvailableEverySecond.map { if (it) multiplier else 0 }
    val secondsList = operatorIsAvailableEverySecond.indices.toList()

    val plot = Plotly.plot {
        scatter {
            x(*secondsList.toTypedArray())
            y(*valueEachSecond.toTypedArray())
            name = "Operator state"

            line {
                color("#8CBA80")
            }
        }

        scatter {
            x(*rejectedClientsSeconds.toTypedArray())
            y(*IntArray(rejectedClientsSeconds.size) { multiplier }.toTypedArray())

            mode = ScatterMode.markers
            name = "Rejected clients"

            marker {
                color("#E84A5F")
            }
        }

        layout {
            title {
                text = plotTitle
                font {
                    family = "Raleway, sans-serif"
                }
            }

            legend {
                font {
                    family = "Raleway, sans-serif"
                }
            }

            yaxis {
                range(Value.of(0 - multiplier * 0.1), Value.of(multiplier + multiplier * 0.1))
                title = "State"

                tickvals(listOf(0, multiplier))
                ticktext(listOf("Idle", "Work"))
            }

            xaxis {
                title = "Time, sec"
            }

            width = 3000
            plot_bgcolor("#2A363B")
        }
    }

    plot.makeFile()
}
