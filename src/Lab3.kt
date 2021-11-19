import queuing_system.*
import queuing_system.singlechannel.*
import java.util.*

fun main() {
    val reader = Scanner(System.`in`)

    print("Average clients' count per hour: ")
    val averageCallsPerHour = reader.nextDouble()

    print("Operator's average serving time in minutes: ")
    val averageServingMinutes = reader.nextDouble()

    val queuingSystemParams = SingleChanelQueuingSystemParams(
        averageCallsPerHour / HOUR_IN_SECONDS,
        1 / (MINUTE_IN_SECONDS * averageServingMinutes)
    )

    val simulationStats = SingleChannelQueuingSystemSimulator.simulateQueuingSystem(queuingSystemParams)
    outputQueuingSystemStats("Simulation", simulationStats)

    val theoreticalStats = SingleChannelQueueingSystemUtils.calculateStats(queuingSystemParams)
    outputQueuingSystemStats("Theoretical", theoreticalStats)

    val perfectStats = PerfectSingleChannelQueueingSystemUtils.calculateStats(queuingSystemParams)
    outputQueuingSystemStats("Theoretical Perfect", perfectStats)
}

private fun outputQueuingSystemStats(name: String, stats: QueuingSystemStats) {
    println("\n$name stats:\n")
    println("Relative throughput: %.4f".format(stats.relativeThroughput))
    println("Absolute throughput (per hour): %.4f".format(stats.absoluteHourlyThroughput))
    println("Rejection probability: %.4f\n".format(stats.rejectionProbability))

    println("State probabilities: ")
    val stateProbabilities = stats.getStateProbabilities()
    stateProbabilities.forEach { (state, probability) ->
        val stateName = if (state.first == 0) "idle" else "work"
        println("p($stateName) = %.4f".format(probability))
    }

    showStateProbabilitiesPlot("$name states", stateProbabilities)

    if (stats is SingleChannelQueuingSystemSimulatorStats) {
        val operatorIsAvailableEverySecond = stats.operatorIsAvailableEverySecond
        val rejectedClientsSeconds = stats.rejectedClientsSeconds
        showSingleChannelStateProgression("System progression", operatorIsAvailableEverySecond, rejectedClientsSeconds)
    }
}

/* Task example

A bit more then can handle
24 3

As much as can handle
30 6

Less then can handle
6 5

*/
