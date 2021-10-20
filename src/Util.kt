const val EPSILON = 0.001f
const val VERBOSE = false

fun log(message: String) {
    if (VERBOSE) println(message + "\n")
}

fun Pair<Float, Float>.formattedString(): String {
    return "(%.3f, %.3f)".format(this.first, this.second)
}
