package queuing_system

import logWithSeconds
import randomExponential
import kotlin.math.roundToInt

class SimulationClientsGenerator(private val clientsIntensity: Double) : SimulationUnit {

    private var isClientArriving: Boolean = false
    private var secondsTillNewClient: Int = randomExponential(clientsIntensity).roundToInt()

    override fun processPassedSecond(second: Long) {
        if (!isClientArriving)
            secondsTillNewClient--

        if (secondsTillNewClient <= 0 && !isClientArriving) {
            logWithSeconds(second, "New client is coming")
            isClientArriving = true
        }
    }

    fun isClientArriving() = isClientArriving

    fun generateNext() {
        isClientArriving = false
        secondsTillNewClient = randomExponential(clientsIntensity).roundToInt()
    }
}
