package queuing_system.multichannel

import HOUR_IN_SECONDS
import logWithSeconds
import queuing_system.SimulationClientsGenerator
import queuing_system.SimulationUnit
import randomExponential
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

object MultichannelQueuingSystemSimulator {

    private const val SIMULATION_SECONDS = 24L * HOUR_IN_SECONDS

    private lateinit var params: MultichannelQueuingSystemParams
    private lateinit var stats: MultichannelQueuingSystemSimulatorStats

    private lateinit var clientsQueue: Queue<Client>
    private lateinit var clientsGenerator: SimulationClientsGenerator
    private lateinit var operators: List<Operator>

    private class Client(waitingIntensity: Double) : SimulationUnit {

        val id = Random.nextLong(100, 10000)
        private var isLeaving: Boolean = false
        private var secondsTillLeaving: Int = randomExponential(waitingIntensity).roundToInt()

        override fun processPassedSecond(second: Long) {
            if (!isLeaving) {
                stats.secondsInQueueInTotal++
                secondsTillLeaving--
            }

            if (secondsTillLeaving <= 0 && !isLeaving) {
                logWithSeconds(second, "Client $id is leaving without being served")
                isLeaving = true
            }
        }

        fun isLeaving() = isLeaving
    }

    private class Operator(val id: Int, private val processingIntensity: Double) : SimulationUnit {

        private var isAvailable: Boolean = true
        private var secondsTillAvailable: Int = 0

        override fun processPassedSecond(second: Long) {
            if (!isAvailable) {
                stats.secondsInServiceInTotal++
                secondsTillAvailable--
            }

            if (secondsTillAvailable <= 0 && !isAvailable) {
                logWithSeconds(second, "Operator $id is now available")
                isAvailable = true
            }
        }

        fun isAvailable() = isAvailable

        fun loadWithWork() {
            isAvailable = false
            secondsTillAvailable = randomExponential(processingIntensity).roundToInt()
        }
    }

    fun simulateQueuingSystem(queuingSystemParams: MultichannelQueuingSystemParams): MultichannelQueuingSystemSimulatorStats {
        params = queuingSystemParams
        stats = MultichannelQueuingSystemSimulatorStats(
            SIMULATION_SECONDS.toInt(),
            params.operatorsCount,
            params.queueMaxSize
        )

        clientsQueue = LinkedList()
        clientsGenerator = SimulationClientsGenerator(params.clientsIntensity)
        operators = List(params.operatorsCount) { Operator(it + 1, params.processingIntensity) }

        logWithSeconds(0, "Simulation started")

        for (second in 0 until SIMULATION_SECONDS) {
            generateClients(second)
            removeLeavingClients(second)
            loadOperators(second)

            stats.clientsInServiceEverySecond[second.toInt()] = operators.count { !it.isAvailable() }
            stats.clientsInQueueEverySecond[second.toInt()] = clientsQueue.size
        }

        if (clientsQueue.isNotEmpty())
            logWithSeconds(SIMULATION_SECONDS, "${clientsQueue.size} clients are leaving because of closure")
        logWithSeconds(SIMULATION_SECONDS, "Simulation ending")
        stats.clientsLeftAfterClosure += clientsQueue.size

        return stats
    }

    private fun generateClients(second: Long) {
        clientsGenerator.processPassedSecond(second)

        if (clientsGenerator.isClientArriving()) {
            val client = Client(params.waitingIntensity)
            stats.clientsInTotal++
            val queueIsFull = clientsQueue.size >= params.queueMaxSize

            if (queueIsFull) {
                stats.clientsNotQueued++
                logWithSeconds(second, "Client ${client.id} is leaving because the queue is full")
            } else {
                clientsQueue.add(client)
                logWithSeconds(second, "Client ${client.id} added to queue")
            }

            clientsGenerator.generateNext()
        }
    }

    private fun removeLeavingClients(second: Long) {
        clientsQueue.forEach { it.processPassedSecond(second) }

        val leavingClients = clientsQueue.filter { it.isLeaving() }
        stats.clientsLeftUnserved += leavingClients.size
        clientsQueue.removeAll(leavingClients)
    }

    private fun loadOperators(second: Long) {
        operators.forEach { it.processPassedSecond(second) }

        val freeOperator = operators.find { it.isAvailable() }
        if (freeOperator != null && !clientsQueue.isEmpty()) {
            val nextClient = clientsQueue.peek()
            clientsQueue.remove()
            freeOperator.loadWithWork()

            stats.clientsServed++
            logWithSeconds(second, "Operator ${freeOperator.id} is now serving client ${nextClient.id}")
        }
    }
}
