package queuing_system.multichannel

import HOUR_IN_SECONDS
import MINUTE_IN_SECONDS
import queuing_system.QueuingSystemStats
import queuing_system.StateProbabilitiesListMap
import queuing_system.StateProbabilityMap

data class MultichannelQueuingSystemSimulatorStats(
    private val simulationSeconds: Int,
    private val operatorsCount: Int,
    private val queueMaxSize: Int,

    var clientsInTotal: Long = 0,
    var clientsServed: Long = 0,
    var clientsNotQueued: Long = 0,
    var clientsLeftUnserved: Long = 0,
    var clientsLeftAfterClosure: Long = 0,

    val clientsInServiceEverySecond: MutableList<Int> = MutableList(simulationSeconds) { 0 },
    val clientsInQueueEverySecond: MutableList<Int> = MutableList(simulationSeconds) { 0 },

    var secondsInQueueInTotal: Long = 0,
    var secondsInServiceInTotal: Long = 0,
) : QueuingSystemStats {

    override val rejectionProbability get() = clientsNotQueued.toDouble() / clientsInTotal
    override val relativeThroughput get() = 1 - rejectionProbability
    override val absoluteHourlyThroughput get() = (HOUR_IN_SECONDS * clientsInTotal / simulationSeconds) * relativeThroughput

    override val averageClientsInService get() = clientsInServiceEverySecond.average()
    override val averageClientsInQueue get() = clientsInQueueEverySecond.average()
    override val averageClientsInSystem get() = averageClientsInService + averageClientsInQueue
    override val averageSecondsInQueue get() = secondsInQueueInTotal / (clientsInTotal - clientsNotQueued)
    override val averageSecondsInService get() = secondsInServiceInTotal / clientsServed

    private val stateEverySecond
        get() = List(simulationSeconds) {
            Pair(clientsInServiceEverySecond[it], clientsInQueueEverySecond[it])
        }

    override fun getStateProbabilities(): StateProbabilityMap {
        val stateProbabilities = mutableMapOf<Pair<Int, Int>, Double>()
        val stateEverySecond = this.stateEverySecond

        val possibleStates = 0.rangeTo(operatorsCount).map { Pair(it, 0) } +
                1.rangeTo(queueMaxSize).map { Pair(operatorsCount, it) }

        possibleStates.forEach { state ->
            val probability =
                stateEverySecond.count { it == state }.toDouble() / simulationSeconds
            stateProbabilities[state] = probability
        }

        return stateProbabilities
    }

    fun getStateProbabilitiesEveryMinute(): StateProbabilitiesListMap {
        val stateProbabilitiesEveryMinute = mutableMapOf<Pair<Int, Int>, MutableList<Double>>()
        val simulationMinutes = simulationSeconds / MINUTE_IN_SECONDS
        val stateEverySecond = this.stateEverySecond

        val possibleStates = 0.rangeTo(operatorsCount).map { Pair(it, 0) } +
                1.rangeTo(queueMaxSize).map { Pair(operatorsCount, it) }

        for (minute in 0..simulationMinutes) {
            val second = minute * MINUTE_IN_SECONDS
            val statesTillCurrentSecond = stateEverySecond.take(second)

            possibleStates.forEach { state ->
                val probability = statesTillCurrentSecond.count { it == state }.toDouble() / second

                if (stateProbabilitiesEveryMinute.contains(state))
                    stateProbabilitiesEveryMinute[state]!!.add(probability)
                else
                    stateProbabilitiesEveryMinute[state] = mutableListOf(probability)
            }
        }

        return stateProbabilitiesEveryMinute
    }
}
