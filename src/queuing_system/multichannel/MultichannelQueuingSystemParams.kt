package queuing_system.multichannel

data class MultichannelQueuingSystemParams(
    val operatorsCount: Int,
    val queueMaxSize: Int,
    val waitingIntensity: Double,
    val clientsIntensity: Double,
    val processingIntensity: Double,
)

