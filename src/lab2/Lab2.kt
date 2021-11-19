package lab2

import HOUR_IN_SECONDS
import MINUTE_IN_SECONDS
import secondsToTimeString
import java.util.*

fun main() {
    val reader = Scanner(System.`in`)

    print("Operators in the system: ")
    val operatorsCount = reader.nextInt()

    print("Maximum amount of clients in the queue: ")
    val queueMaxSize = reader.nextInt()

    print("Clients' average waiting time (before leaving) in minutes: ")
    val averageWaitingMinutes = reader.nextDouble()

    print("Average clients' count per hour: ")
    val averageCallsPerHour = reader.nextDouble()

    print("Operator's average serving time in minutes: ")
    val averageServingMinutes = reader.nextDouble()

    val queuingSystemParams = QueuingSystemParams(
        operatorsCount, queueMaxSize,
        1 / (MINUTE_IN_SECONDS * averageWaitingMinutes),
        averageCallsPerHour / HOUR_IN_SECONDS,
        1 / (MINUTE_IN_SECONDS * averageServingMinutes)
    )

    val simulationStats = QueuingSystemSimulator.simulateQueuingSystem(queuingSystemParams)
    printQueuingSystemStats("'High load booth' simulation", simulationStats)

    val theoreticalStats = QueueingSystemUtils.calculateStats(queuingSystemParams)
    printQueuingSystemStats("'High load booth' theoretical", theoreticalStats)
}

private fun printQueuingSystemStats(name: String, stats: QueuingSystemStats) {
    println("\n$name stats:\n")
    println("Relative throughput: %.4f".format(stats.relativeThroughput))
    println("Absolute throughput (per hour): %.4f".format(stats.absoluteHourlyThroughput))
    println("Rejection probability: %.4f".format(stats.rejectionProbability))
    println("Average clients in service count: %.4f".format(stats.averageClientsInService))
    println("Average clients in queue count: %.4f".format(stats.averageClientsInQueue))
    println("Average clients in the system: %.4f".format(stats.averageClientsInSystem))
    println("Average time in the queue: ${secondsToTimeString(stats.averageSecondsInQueue)}")
    println("Average time in the service: ${secondsToTimeString(stats.averageSecondsInService)}\n")

    println("State probabilities p(in service; in queue) : ")
    val stateProbabilities = stats.getStateProbabilities()
    stateProbabilities.forEach { (state, probability) ->
        val clientsInService = state.first
        val clientsInQueue = state.second

        println("p($clientsInService; $clientsInQueue) = %.4f".format(probability))
    }

    showStateProbabilitiesPlot("$name states", stateProbabilities)

    if (stats is QueuingSystemSimulatorStats) {
        val stateProbabilitiesEveryMinute = stats.getStateProbabilitiesEveryMinute()
        showStateProbabilitiesEveryMinutePlot("$name states progression", stateProbabilitiesEveryMinute)
    }
}

/* Task example

Good balance shop
3 6 15 30 4,5

Typical shop
5 20 20 150 3

Small gas station
2 4 15 12 5

Booth with too high load
1 10 10 60 1,5

*/
