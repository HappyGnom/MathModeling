package queuing_system

data class QueuingSystemStatsImpl(
    val stateProbabilityMap: StateProbabilityMap,

    override val relativeThroughput: Double,
    override val absoluteHourlyThroughput: Double,
    override val rejectionProbability: Double,

    override val averageClientsInService: Double,
    override val averageClientsInQueue: Double,
    override val averageClientsInSystem: Double,
    override val averageSecondsInQueue: Long,
    override val averageSecondsInService: Long
) : QueuingSystemStats {
    override fun getStateProbabilities() = stateProbabilityMap
}
