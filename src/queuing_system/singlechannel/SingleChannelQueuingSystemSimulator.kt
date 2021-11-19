package queuing_system.singlechannel

import HOUR_IN_SECONDS
import logWithSeconds
import queuing_system.SimulationClientsGenerator
import queuing_system.SimulationUnit
import randomExponential
import kotlin.math.roundToInt
import kotlin.random.Random


object SingleChannelQueuingSystemSimulator {

    private const val SIMULATION_SECONDS = 24L * HOUR_IN_SECONDS

    private lateinit var params: SingleChanelQueuingSystemParams
    private lateinit var stats: SingleChannelQueuingSystemSimulatorStats

    private lateinit var clientsGenerator: SimulationClientsGenerator
    private lateinit var operator: Operator

    private class Operator(private val processingIntensity: Double) : SimulationUnit {

        private var isAvailable: Boolean = true
        private var secondsTillAvailable: Int = 0

        override fun processPassedSecond(second: Long) {
            if (!isAvailable)
                secondsTillAvailable--

            if (secondsTillAvailable <= 0 && !isAvailable) {
                logWithSeconds(second, "Operator is now available")
                isAvailable = true
            }
        }

        fun isAvailable() = isAvailable

        fun loadWithWork() {
            isAvailable = false
            secondsTillAvailable = randomExponential(processingIntensity).roundToInt()
        }
    }

    fun simulateQueuingSystem(queuingSystemParams: SingleChanelQueuingSystemParams): SingleChannelQueuingSystemSimulatorStats {
        params = queuingSystemParams
        stats = SingleChannelQueuingSystemSimulatorStats(SIMULATION_SECONDS.toInt())

        clientsGenerator = SimulationClientsGenerator(params.clientsIntensity)
        operator = Operator(params.processingIntensity)

        logWithSeconds(0, "Simulation started")

        for (second in 0 until SIMULATION_SECONDS) {
            operator.processPassedSecond(second)
            val newClientId = getNewClientId(second)

            if (newClientId != null) {
                if (operator.isAvailable())
                    serveClient(second, newClientId)
                else
                    rejectClient(second, newClientId)
            }

            stats.operatorIsAvailableEverySecond[second.toInt()] = operator.isAvailable()
        }

        logWithSeconds(SIMULATION_SECONDS, "Simulation ending")

        return stats
    }


    private fun getNewClientId(second: Long): Long? {
        clientsGenerator.processPassedSecond(second)

        if (clientsGenerator.isClientArriving()) {
            val clientId = Random.nextLong(100, 10000)
            logWithSeconds(second, "Client $clientId is arriving")
            stats.clientsInTotal++

            clientsGenerator.generateNext()
            return clientId
        }

        return null
    }

    private fun serveClient(second: Long, clientId: Long) {
        operator.loadWithWork()

        stats.clientsServed++
        logWithSeconds(second, "Operator is now serving client $clientId")
    }

    private fun rejectClient(second: Long, clientId: Long) {
        stats.clientsRejected++
        stats.rejectedClientsSeconds.add(second)
        logWithSeconds(second, "Client $clientId is rejected because operator is busy")
    }
}
