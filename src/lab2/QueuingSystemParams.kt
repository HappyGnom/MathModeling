package lab2

data class QueuingSystemParams(
    val operatorsCount: Int,
    val queueMaxSize: Int,
    val waitingIntensity: Double,
    val clientsIntensity: Double,
    val processingIntensity: Double,
)
