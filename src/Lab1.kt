import model.Matrix
import org.apache.commons.math3.distribution.ChiSquaredDistribution
import org.apache.commons.math3.distribution.TDistribution
import space.kscience.dataforge.values.Value
import space.kscience.plotly.*
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

private const val RANDOM_ELEMENTS_COUNT = 1000
private const val INTERVAL_RELIABILITY = 0.95f

fun main() {
    val reader = Scanner(System.`in`)

    print("Input first values group count: ")
    val firstSetSize = reader.nextInt()

    print("Input second values group count: ")
    val secondSetSize = reader.nextInt()

    val probabilities = Matrix(secondSetSize, firstSetSize)
    println("Input probabilities matrix values row by row: ")

    var probabilitiesSum = 0f
    for (row in 0.until(secondSetSize)) for (column in 0.until(firstSetSize)) {
        val probability = reader.nextFloat()
        if (probability >= 1f)
            throw IllegalArgumentException("Probability can't be higher than 1")

        probabilitiesSum += probability
        if (probabilitiesSum - EPSILON >= 1f)
            throw IllegalArgumentException("Probabilities sum can't be higher than 1. Sum is $probabilitiesSum already")

        probabilities[row][column] = probability
    }

    if (probabilitiesSum + EPSILON < 1f)
        throw IllegalArgumentException("Probabilities sum should be exactly 1. Sum is $probabilitiesSum")

    println("\nYour probabilities matrix:\n$probabilities\n")

    val randomIndices = getListOfRandomTwoDimensionalIndices(probabilities, RANDOM_ELEMENTS_COUNT)
    println("\n$RANDOM_ELEMENTS_COUNT elements generated")

    val empiricalDistribution = getEmpiricalDistribution(randomIndices, firstSetSize, secondSetSize)
    val setDistributions = getSetDistributions(empiricalDistribution)
    println("\nEmpirical distribution matrix:\n$empiricalDistribution\n")

    plotDistributionMatrixComponentsHistograms(setDistributions)

    val mathExpectations = getMathExpectations(setDistributions)
    println("Mathematical expectations: ${mathExpectations.formattedString()}")

    val dispersions = getDispersions(setDistributions, mathExpectations)
    println("\nDispersions: ${dispersions.formattedString()}")

    val standardDeviations = getStandardDeviations(dispersions)
    println("\nStandard deviations: ${standardDeviations.formattedString()}")

    val mathExpectationIntervals = getMathExpectationIntervals(dispersions, standardDeviations)
    println("\nMath expectation intervals with $INTERVAL_RELIABILITY reliability: ${mathExpectationIntervals.first.formattedString()} and ${mathExpectationIntervals.second.formattedString()}")

    val dispersionIntervals = getDispersionIntervals(dispersions)
    println("\nDispersion intervals with $INTERVAL_RELIABILITY reliability: ${dispersionIntervals.first.formattedString()} and ${dispersionIntervals.second.formattedString()}")

    val correlationCoefficient = getCorrelationCoefficient(randomIndices, standardDeviations)
    println("\nCorrelation coefficient: $correlationCoefficient")

    val isPearsonCompatible = checkPearsonCriteria(probabilities, empiricalDistribution)
    println("\nPearson criteria compatible: $isPearsonCompatible\n")
}

private fun getEmpiricalDistribution(
    randomIndices: List<Pair<Int, Int>>,
    firstSetSize: Int, secondSetSize: Int
): Matrix {
    val empiricalDistribution = Matrix(secondSetSize, firstSetSize)
    val randomIndicesCount = randomIndices.count()

    for (row in 0.until(secondSetSize)) for (column in 0.until(firstSetSize)) {
        val elementsCount = randomIndices.count { it.first == row && it.second == column }
        empiricalDistribution[row][column] = elementsCount.toFloat() / randomIndicesCount
    }

    return empiricalDistribution
}

private fun getSetDistributions(empiricalDistribution: Matrix): Pair<Array<Float>, Array<Float>> {
    val firstSetDistribution = Array(empiricalDistribution.columnsCount) { 0f }
    val secondSetDistribution = Array(empiricalDistribution.rowsCount) { 0f }

    empiricalDistribution.getColumnVectors().forEachIndexed { index, column ->
        firstSetDistribution[index] = column.transposed()[0].sum()
    }

    empiricalDistribution.getRowVectors().forEachIndexed { index, row ->
        secondSetDistribution[index] = row[0].sum()
    }

    return Pair(firstSetDistribution, secondSetDistribution)
}

private fun plotDistributionMatrixComponentsHistograms(setDistributions: Pair<Array<Float>, Array<Float>>) {
    val firstSetX = (1..setDistributions.first.size).toList().toTypedArray()
    val secondSetX = (1..setDistributions.second.size).toList().toTypedArray()

    showBarsPlot(firstSetX, setDistributions.first, "Distribution of the random values of the first set")
    showBarsPlot(secondSetX, setDistributions.second, "Distribution of the random values of the second set")
}

private fun <T : Number, Y : Number> showBarsPlot(x: Array<T>, y: Array<Y>, title: String = "") {
    val plot = Plotly.plot {
        bar {
            x(*x)
            y(*y)

            marker {
                width = 0.2
                color("#E84A5F")
            }
        }

        layout {
            title {
                text = title
                font {
                    family = "Raleway, sans-serif"
                }
            }

            yaxis {
                range(Value.of(0), Value.of(1))
                this.gridcolor("#FFFFFF")
            }

            width = 1000
            plot_bgcolor("#2A363B")
        }
    }

    plot.makeFile()
}

private fun getMathExpectations(setDistributions: Pair<Array<Float>, Array<Float>>): Pair<Float, Float> {
    val firstSetExpectation = getMathExpectation(setDistributions.first)
    val secondSetExpectation = getMathExpectation(setDistributions.second)

    return Pair(firstSetExpectation, secondSetExpectation)
}

private fun getMathExpectation(distributions: Array<Float>): Float {
    var expectation = 0f
    distributions.forEachIndexed { index, possibility ->
        expectation += (index + 1) * possibility
    }

    return expectation
}

private fun getDispersions(
    setDistributions: Pair<Array<Float>, Array<Float>>,
    mathExpectations: Pair<Float, Float>
): Pair<Float, Float> {
    val firstSetDispersion = getDispersion(setDistributions.first, mathExpectations.first)
    val secondSetDispersion = getDispersion(setDistributions.second, mathExpectations.second)

    return Pair(firstSetDispersion, secondSetDispersion)
}

private fun getDispersion(distributions: Array<Float>, mathExpectation: Float): Float {
    var dispersion = 0f
    distributions.forEachIndexed { index, possibility ->
        dispersion += (index + 1.0).pow(2).toFloat() * possibility
    }
    dispersion -= mathExpectation.pow(2)

    return dispersion
}

private fun getStandardDeviations(dispersions: Pair<Float, Float>): Pair<Float, Float> {
    return Pair(sqrt(dispersions.first), sqrt(dispersions.second))
}

private fun getMathExpectationIntervals(
    mathExpectations: Pair<Float, Float>,
    standardDeviations: Pair<Float, Float>
): Pair<Pair<Float, Float>, Pair<Float, Float>> {
    val firstSetMathExpectationInterval =
        getMathExpectationInterval(mathExpectations.first, standardDeviations.first)
    val secondSetMathExpectationInterval =
        getMathExpectationInterval(mathExpectations.second, standardDeviations.second)

    return Pair(firstSetMathExpectationInterval, secondSetMathExpectationInterval)
}

private fun getMathExpectationInterval(mathExpectation: Float, standardDeviation: Float): Pair<Float, Float> {
    val significanceLevel = (1.0 + INTERVAL_RELIABILITY) / 2

    val degreesOfFreedom = RANDOM_ELEMENTS_COUNT - 1.0
    val studentsDistribution = TDistribution(degreesOfFreedom)
    val students = studentsDistribution.inverseCumulativeProbability(significanceLevel)

    val delta = students * standardDeviation / sqrt(RANDOM_ELEMENTS_COUNT.toDouble())
    val intervalLeft = mathExpectation - delta
    val intervalRight = mathExpectation + delta

    return Pair(intervalLeft.toFloat(), intervalRight.toFloat())
}

private fun getDispersionIntervals(dispersions: Pair<Float, Float>): Pair<Pair<Float, Float>, Pair<Float, Float>> {
    val firstSetDispersionInterval = getDispersionInterval(dispersions.first)
    val secondSetDispersionInterval = getDispersionInterval(dispersions.second)

    return Pair(firstSetDispersionInterval, secondSetDispersionInterval)
}

private fun getDispersionInterval(dispersion: Float): Pair<Float, Float> {
    val leftSignificanceLevel = (1.0 + INTERVAL_RELIABILITY) / 2
    val rightSignificanceLevel = (1.0 - INTERVAL_RELIABILITY) / 2

    val degreesOfFreedom = RANDOM_ELEMENTS_COUNT - 1.0
    val chiSquaredDistribution = ChiSquaredDistribution(degreesOfFreedom)
    val leftChiSquared = chiSquaredDistribution.inverseCumulativeProbability(leftSignificanceLevel)
    val rightChiSquared = chiSquaredDistribution.inverseCumulativeProbability(rightSignificanceLevel)

    val intervalLeft = (RANDOM_ELEMENTS_COUNT * dispersion) / leftChiSquared
    val intervalRight = (RANDOM_ELEMENTS_COUNT * dispersion) / rightChiSquared

    return Pair(intervalLeft.toFloat(), intervalRight.toFloat())
}

private fun getCorrelationCoefficient(
    randomIndices: List<Pair<Int, Int>>,
    standardDeviation: Pair<Float, Float>
): Float {
    val averageFirst = randomIndices.map { it.first }.average().toFloat()
    val averageSecond = randomIndices.map { it.second }.average().toFloat()
    val averageMultiplication = randomIndices.map { it.first * it.second }.average().toFloat()

    return (averageMultiplication - averageFirst * averageSecond) / (standardDeviation.first * standardDeviation.second)
}

private fun checkPearsonCriteria(probabilities: Matrix, empiricalDistribution: Matrix) : Boolean{
    var chi2Sum = 0f
    for (row in 0.until(probabilities.rowsCount)) for (column in 0.until(probabilities.columnsCount))
        chi2Sum += (empiricalDistribution[row][column] - probabilities[row][column]).pow(2)/probabilities[row][column]

    val degreesOfFreedom = (probabilities.rowsCount - 1.0) * (probabilities.columnsCount - 1.0)
    val significanceLevel = abs(INTERVAL_RELIABILITY - 1.0)
    val chiSquaredDistribution = ChiSquaredDistribution(degreesOfFreedom)
    val chi2Theoretical = chiSquaredDistribution.inverseCumulativeProbability(significanceLevel)

    return chi2Sum <= chi2Theoretical
}

/* Task example
P =
0,05 0,20 0,30
0,10 0,20 0,15

P (Covid Ill/None-vaccinated) =
0,050 0,030 0,020 0,005
0,030 0,150 0,050 0,020
0,020 0,050 0,300 0,050
0,005 0,020 0,050 0,150

P =
0,090 0,030 0,020 0,005
0,030 0,170 0,030 0,020
0,020 0,030 0,300 0,030
0,005 0,020 0,030 0,170

P =
0,000 0,000 0,000 0,000
0,000 0,500 0,000 0,000
0,000 0,000 0,500 0,000
0,000 0,000 0,000 0,000

P (Male/Female / Height 160-180) = 
0,01 0,04 0,10 0,25 0,10
0,10 0,25 0,10 0,04 0,01

*/
