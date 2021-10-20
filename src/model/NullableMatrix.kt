package model

class NullableMatrix(val matrix: Matrix, val notNullMap: Matrix) {

    operator fun get(row: Int, column: Int) = if (notNullMap[row, column] == 1f)
        matrix[row][column]
    else
        null

    operator fun set(row: Int, column: Int, value: Float?) = if (value == null) {
        matrix[row][column] = 0f
        notNullMap[row][column] = 0f
    } else {
        matrix[row][column] = value
        notNullMap[row][column] = 1f
    }

    override fun toString(): String {
        val stringBuilder = StringBuilder()

        matrix.values.forEachIndexed { row, vector ->
            vector.forEachIndexed { column, value ->
                if (notNullMap[row, column] == 1f)
                    stringBuilder.append("%.3f".format(value).padEnd(10))
                else
                    stringBuilder.append("–".format(value).padEnd(10))
            }

            stringBuilder.append("\n")
        }

        return stringBuilder.trim().toString()
    }
}
