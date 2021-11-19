package queuing_system.singlechannel

import HOUR_IN_SECONDS
import queuing_system.QueuingSystemStatsImpl
import queuing_system.StateProbabilityMap

object PerfectSingleChannelQueueingSystemUtils {

    fun calculateStats(params: SingleChanelQueuingSystemParams): QueuingSystemStatsImpl {
        val stateProbabilities = getPerfectStateProbabilities()
        val operatorFreeState = Pair(0, 0)
        val operatorBusyState = Pair(1, 0)

        val rejectionProbability = stateProbabilities[operatorFreeState]!!
        val relativeThroughput = stateProbabilities[operatorBusyState]!!
        val absoluteHourlyThroughput = HOUR_IN_SECONDS * relativeThroughput * params.processingIntensity

        val averageSecondsInService = 1.0 / params.processingIntensity

        return QueuingSystemStatsImpl(
            stateProbabilities,
            relativeThroughput, absoluteHourlyThroughput,
            rejectionProbability, rejectionProbability,
            0.0, rejectionProbability,
            0L, averageSecondsInService.toLong()
        )
    }

    /**
     * In order to get the best results we need to make sure we always have a client on the line.
     * If we assume, that we get a call right after finishing the previous one, we can say that
     * our utilization intensity tends to infinity:
     * utilizationIntensity -> ∞
     *
     * This way we bring our system's "busy" state to maximum and reducing "idle" state to minimum:
     * p(0) -> 0
     * p(1) -> 1
     */
    private fun getPerfectStateProbabilities(): StateProbabilityMap {
        val stateProbabilities = mutableMapOf<Pair<Int, Int>, Double>()

        // p(0) = 1 / (1 + utilizationIntensity)
        val operatorFreeState = Pair(0, 0)
        stateProbabilities[operatorFreeState] = 0.0

        // p(1) = utilizationIntensity / (1 + utilizationIntensity)
        val operatorBusyState = Pair(1, 0)
        stateProbabilities[operatorBusyState] = 1.0

        return stateProbabilities
    }
}
