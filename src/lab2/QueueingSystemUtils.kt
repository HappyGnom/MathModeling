package lab2

import HOUR_IN_SECONDS
import org.apache.commons.math3.util.CombinatoricsUtils
import kotlin.math.pow
import kotlin.math.roundToLong

object QueueingSystemUtils {

    fun calculateStats(params: QueuingSystemParams): QueuingSystemStatsImpl {
        val stateProbabilities = getStateProbabilities(params)
        val fullSystemState = Pair(params.operatorsCount, params.queueMaxSize)

        val rejectionProbability = stateProbabilities[fullSystemState]!!
        val relativeThroughput = 1 - rejectionProbability
        val absoluteHourlyThroughput = HOUR_IN_SECONDS * relativeThroughput * params.clientsIntensity

        var averageClientsInQueue = 0.0
        for (i in 1..params.queueMaxSize) {
            val state = Pair(params.operatorsCount, i)
            averageClientsInQueue += i * stateProbabilities[state]!!
        }

        var averageClientsInService = 0.0
        stateProbabilities.forEach { (state, probability) ->
            averageClientsInService += state.first * probability
        }

        val averageClientsInSystem = averageClientsInQueue + averageClientsInService

        val averageSecondsInQueue = (averageClientsInQueue / params.clientsIntensity).roundToLong()
        val averageSecondsInService = (relativeThroughput / params.processingIntensity).roundToLong()

        return QueuingSystemStatsImpl(
            stateProbabilities,
            relativeThroughput, absoluteHourlyThroughput,
            rejectionProbability, averageClientsInService,
            averageClientsInQueue, averageClientsInSystem,
            averageSecondsInQueue, averageSecondsInService
        )
    }

    private fun getStateProbabilities(params: QueuingSystemParams): StateProbabilityMap {
        val stateProbabilities = mutableMapOf<Pair<Int, Int>, Double>()

        val utilizationIntensity = params.clientsIntensity / params.processingIntensity
        val leavingIntensity = params.waitingIntensity / params.processingIntensity

        val emptySystemState = Pair(0, 0)
        stateProbabilities[emptySystemState] = getEmptyStateProbability(params, utilizationIntensity, leavingIntensity)

        val otherStates = 1.rangeTo(params.operatorsCount).map { Pair(it, 0) } +
                1.rangeTo(params.queueMaxSize).map { Pair(params.operatorsCount, it) }

        otherStates.forEach { state ->
            val beingServedProbability = stateProbabilities[emptySystemState]!! *
                    utilizationIntensity.pow(state.first) /
                    CombinatoricsUtils.factorial(state.first)

            var leavingProd = 1.0
            for (y in 1..state.second)
                leavingProd *= params.operatorsCount + y * leavingIntensity

            val stillInSystemProbability = utilizationIntensity.pow(state.second) / leavingProd

            stateProbabilities[state] = beingServedProbability * stillInSystemProbability
        }

        return stateProbabilities
    }

    private fun getEmptyStateProbability(
        params: QueuingSystemParams,
        utilizationIntensity: Double,
        leavingIntensity: Double
    ): Double {
        val operators = params.operatorsCount
        val queuePlaces = params.queueMaxSize

        var notQueuedSum = 0.0
        for (i in 0..operators) {
            notQueuedSum += (utilizationIntensity).pow(i) / CombinatoricsUtils.factorial(i)
        }

        var queuedSum = 0.0
        for (i in 1..queuePlaces) {
            var leavingProd = 1.0
            for (y in 1..i)
                leavingProd *= operators + y * leavingIntensity

            queuedSum += (utilizationIntensity).pow(i) / leavingProd
        }
        queuedSum *= (utilizationIntensity).pow(operators) / CombinatoricsUtils.factorial(operators)

        return 1.0 / (notQueuedSum + queuedSum)
    }
}
