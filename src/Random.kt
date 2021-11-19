import model.Matrix
import kotlin.math.ln
import kotlin.random.Random

fun getListOfRandomTwoDimensionalIndices(probabilities: Matrix, count: Int): List<Pair<Int, Int>> {
    val randomIndicesList = mutableListOf<Pair<Int, Int>>()
    for (i in 0.until(count)) {
        val randomIndices = getRandomTwoDimensionalIndices(probabilities)
        randomIndicesList.add(randomIndices)
    }

    return randomIndicesList
}

fun getRandomTwoDimensionalIndices(probabilities: Matrix): Pair<Int, Int> {
    val lot = castLots()
    val probabilitiesFlat = probabilities.flatten()

    if (lot < probabilitiesFlat.first())
        return Pair(0, 0)

    var probabilitiesSum = probabilitiesFlat.first()
    for (probabilityIndex in 1.until(probabilitiesFlat.size)) {
        val newProbabilitiesSum = probabilitiesSum + probabilitiesFlat[probabilityIndex]

        if (lot < newProbabilitiesSum && lot > probabilitiesSum)
            return Pair(probabilityIndex / probabilities.columnsCount, probabilityIndex % probabilities.columnsCount)

        probabilitiesSum = newProbabilitiesSum
    }

    return Pair(probabilities.rowsCount, probabilities.columnsCount)
}

fun castLots(): Float {
    return Random.nextFloat()
}

fun randomExponential(intensity: Double): Double {
    return ln(1 - Random.nextDouble()) / -intensity
}
