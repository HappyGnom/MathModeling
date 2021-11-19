package queuing_system.singlechannel

import HOUR_IN_SECONDS
import queuing_system.QueuingSystemStats
import queuing_system.StateProbabilityMap

data class SingleChannelQueuingSystemSimulatorStats(
    private val simulationSeconds: Int,

    var clientsInTotal: Long = 0,
    var clientsServed: Long = 0,
    var clientsRejected: Long = 0,

    val rejectedClientsSeconds: MutableList<Long> = mutableListOf(),
    val operatorIsAvailableEverySecond: MutableList<Boolean> = MutableList(simulationSeconds) { false }
) : QueuingSystemStats {

    override val rejectionProbability get() = clientsRejected.toDouble() / clientsInTotal
    override val relativeThroughput get() = 1 - rejectionProbability
    override val absoluteHourlyThroughput get() = (HOUR_IN_SECONDS * clientsInTotal / simulationSeconds) * relativeThroughput

    override val averageClientsInService get() = operatorIsAvailableEverySecond.count { available -> !available }.toDouble() / simulationSeconds
    override val averageClientsInQueue get() = 0.0
    override val averageClientsInSystem get() = averageClientsInService + averageClientsInQueue
    override val averageSecondsInQueue get() = 0L
    override val averageSecondsInService get() = operatorIsAvailableEverySecond.count { available -> !available } / clientsServed

    override fun getStateProbabilities(): StateProbabilityMap {
        val stateProbabilities = mutableMapOf<Pair<Int, Int>, Double>()

        val operatorFreeState = Pair(0, 0)
        stateProbabilities[operatorFreeState] =
            operatorIsAvailableEverySecond.count { available -> available }.toDouble() / simulationSeconds

        val operatorBusyState = Pair(1, 0)
        stateProbabilities[operatorBusyState] =
            operatorIsAvailableEverySecond.count { available -> !available }.toDouble() / simulationSeconds

        return stateProbabilities
    }
}
