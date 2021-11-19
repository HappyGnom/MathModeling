package queuing_system.singlechannel

import HOUR_IN_SECONDS
import queuing_system.QueuingSystemStatsImpl
import queuing_system.StateProbabilityMap

object SingleChannelQueueingSystemUtils {

    fun calculateStats(params: SingleChanelQueuingSystemParams): QueuingSystemStatsImpl {
        val stateProbabilities = getStateProbabilities(params)
        val operatorFreeState = Pair(0, 0)
        val operatorBusyState = Pair(1, 0)

        val rejectionProbability = stateProbabilities[operatorBusyState]!!
        val relativeThroughput = stateProbabilities[operatorFreeState]!!
        val absoluteHourlyThroughput = HOUR_IN_SECONDS * relativeThroughput * params.clientsIntensity

        val averageSecondsInService = 1.0 / params.processingIntensity

        return QueuingSystemStatsImpl(
            stateProbabilities,
            relativeThroughput, absoluteHourlyThroughput,
            rejectionProbability, rejectionProbability,
            0.0, rejectionProbability,
            0L, averageSecondsInService.toLong()
        )
    }

    private fun getStateProbabilities(params: SingleChanelQueuingSystemParams): StateProbabilityMap {
        val stateProbabilities = mutableMapOf<Pair<Int, Int>, Double>()
        val utilizationIntensity = params.clientsIntensity / params.processingIntensity

        val operatorFreeState = Pair(0, 0)
        stateProbabilities[operatorFreeState] = 1 / (1 + utilizationIntensity)

        val operatorBusyState = Pair(1, 0)
        stateProbabilities[operatorBusyState] = utilizationIntensity / (1 + utilizationIntensity)

        return stateProbabilities
    }
}
