package lab2

import space.kscience.dataforge.values.Value
import space.kscience.plotly.*

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
