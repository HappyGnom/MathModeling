package lab2

typealias StateProbabilityMap = Map<Pair<Int, Int>, Double>
typealias StateProbabilitiesListMap = MutableMap<Pair<Int, Int>, MutableList<Double>>

interface QueuingSystemStats {
    val relativeThroughput: Double
    val absoluteHourlyThroughput: Double
    val rejectionProbability: Double

    val averageClientsInService: Double
    val averageClientsInQueue: Double
    val averageClientsInSystem: Double

    val averageSecondsInQueue: Long
    val averageSecondsInService: Long

    fun getStateProbabilities(): StateProbabilityMap
}
