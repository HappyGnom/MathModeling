package model

import EPSILON
import kotlin.math.abs

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Matrix(val rowsCount: Int, val columnsCount: Int, initialValue: Float = 0.0f) {

    constructor(size: Int) : this(size, size)

    val values = Array(rowsCount) { FloatArray(columnsCount) { initialValue } }

    companion object {
        fun identityMatrix(size: Int): Matrix {
            val matrix = Matrix(size)

            for (i in 0.until(size))
                matrix[i][i] = 1f

            return matrix
        }

        fun fromVectorRows(rows: List<Matrix>): Matrix {
            val matrix = Matrix(rows.size, rows.first().columnsCount)

            for (row in 0.until(matrix.rowsCount)) for (column in 0.until(matrix.columnsCount))
                matrix[row][column] = rows[row][0, column]

            return matrix
        }

        fun fromVectorColumns(columns: List<Matrix>): Matrix {
            val matrix = Matrix(columns.first().rowsCount, columns.size)

            for (column in 0.until(matrix.columnsCount)) for (row in 0.until(matrix.rowsCount))
                matrix[row][column] = columns[column][row, 0]

            return matrix
        }
    }

    fun getRowVectors(): List<Matrix> {
        val rows = List(rowsCount) { Matrix(1, columnsCount) }

        for (row in 0.until(rowsCount)) for (column in 0.until(columnsCount))
            rows[row][0, column] = this[row][column]

        return rows
    }

    fun getColumnVectors(): List<Matrix> {
        val columns = List(columnsCount) { Matrix(rowsCount, 1) }

        for (row in 0.until(rowsCount)) for (column in 0.until(columnsCount))
            columns[column][row, 0] = this[row][column]

        return columns
    }

    fun flatten(): FloatArray {
        val values = mutableListOf<Float>()

        for (row in 0.until(rowsCount)) for (column in 0.until(columnsCount))
            values.add(this[row][column])

        return values.toFloatArray()
    }

    fun transposed(): Matrix {
        val transposedMatrix = Matrix(columnsCount, rowsCount)

        for (row_index in 0.until(rowsCount)) for (column_index in 0.until(columnsCount))
            transposedMatrix[column_index][row_index] = this[row_index][column_index]

        return transposedMatrix
    }

    fun inverted(): Matrix {
        if (rowsCount != columnsCount) throw IllegalStateException("model.Matrix should be square for `inverted` function")
        val size = rowsCount

        val availableIndices = IntArray(size) { it }.toMutableList()
        val indexOrder = mutableListOf<Int>()
        val columns = this.getColumnVectors()

        var mixedResult = identityMatrix(size)
        val identity = identityMatrix(size).getColumnVectors()

        for (i in 0.until(size))
            for (availableIndex in availableIndices) {
                val alpha = (identity[i].transposed() * mixedResult * columns[availableIndex])[0, 0]

                if (abs(alpha) > EPSILON) {
                    val zVector = mixedResult * columns[availableIndex]
                    val zk = zVector[i][0]
                    zVector[i][0] = -1f

                    val dTransformationFunc = identityMatrix(size)
                    val dVector = (-1 / zk) * zVector
                    dTransformationFunc.replaceColumn(i, dVector.transposed()[0])

                    mixedResult = dTransformationFunc * mixedResult

                    availableIndices.remove(availableIndex)
                    indexOrder.add(availableIndex)

                    break
                }
            }

        if (availableIndices.size > 0) throw IllegalStateException("model.Matrix is not invertible")

        val result = identityMatrix(size)
        indexOrder.forEachIndexed { index, order ->
            result.replaceRow(order, mixedResult[index])
        }

        return result
    }

    fun clone(): Matrix {
        val copy = Matrix(this.rowsCount, this.columnsCount)

        for (row in 0.until(this.rowsCount)) for (column in 0.until(this.columnsCount))
            copy[row, column] = this[row, column]

        return copy
    }

    fun replaceRow(index: Int, rowVector: FloatArray) {
        if (rowVector.size != columnsCount) throw IllegalArgumentException("Vector length is not equal to the column count of the matrix")

        for (column in 0.until(columnsCount))
            this[index][column] = rowVector[column]
    }

    fun replaceColumn(index: Int, columnVector: FloatArray) {
        if (columnVector.size != rowsCount) throw IllegalArgumentException("Vector length is not equal to the column count of the matrix")

        for (row in 0.until(rowsCount))
            this[row][index] = columnVector[row]
    }

    //region Operator overrides

    operator fun get(row: Int) = values[row]

    operator fun get(row: Int, column: Int) = values[row][column]

    operator fun set(row: Int, column: Int, value: Float) {
        values[row][column] = value
    }

    operator fun plus(other: Matrix): Matrix {
        if (this.columnsCount != other.columnsCount || this.rowsCount != other.rowsCount) throw IllegalStateException("Matrices should be of the same shape")
        val result = Matrix(this.rowsCount, this.columnsCount)

        for (row in 0.until(this.rowsCount)) for (column in 0.until(this.columnsCount))
            result[row, column] = this[row, column] + other[row, column]

        return result
    }

    operator fun minus(other: Matrix): Matrix {
        if (this.columnsCount != other.columnsCount || this.rowsCount != other.rowsCount) throw IllegalStateException("Matrices should be of the same shape")
        val result = Matrix(this.rowsCount, this.columnsCount)

        for (row in 0.until(this.rowsCount)) for (column in 0.until(this.columnsCount))
            result[row, column] = this[row, column] - other[row, column]

        return result
    }

    operator fun unaryMinus(): Matrix {
        val result = Matrix(this.rowsCount, this.columnsCount)

        for (row in 0.until(this.rowsCount)) for (column in 0.until(this.columnsCount))
            result[row, column] = -this[row, column]

        return result
    }

    operator fun times(other: Matrix): Matrix {
        if (this.columnsCount != other.rowsCount) throw IllegalStateException("First matrix columns count should be equal to second matrix rows count")
        val result = Matrix(this.rowsCount, other.columnsCount)

        for (row in 0.until(this.rowsCount))
            for (column in 0.until(other.columnsCount)) {
                result[row][column] = 0f

                for (index in 0.until(other.rowsCount))
                    result[row][column] += this[row][index] * other[index][column]
            }

        return result
    }

    operator fun times(scalar: Float): Matrix {
        val result = Matrix(this.rowsCount, this.columnsCount)

        for (row in 0.until(this.rowsCount))
            for (column in 0.until(this.columnsCount))
                result[row][column] = this[row][column] * scalar

        return result
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        values.forEach { row ->
            row.forEach { value ->
                stringBuilder.append("%.3f".format(value).padEnd(10, ' '))
            }

            stringBuilder.append("\n")
        }

        return stringBuilder.trim().toString()
    }
    //endregion
}

operator fun Float.times(matrix: Matrix) = matrix * this
