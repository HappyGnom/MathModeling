import java.util.concurrent.TimeUnit

const val EPSILON = 0.001f
const val VERBOSE = true

const val HOUR_IN_SECONDS = 3600
const val MINUTE_IN_SECONDS = 60

fun log(message: String) {
    if (VERBOSE) println(message)
}

fun Pair<Float, Float>.formattedString(): String {
    return "(%.3f, %.3f)".format(this.first, this.second)
}

fun logWithSeconds(seconds: Long, message: String) {
    log("${secondsToTimeString(seconds)} - $message")
}

fun secondsToTimeString(seconds: Long): String {
    var leftSeconds = seconds

    val hours = TimeUnit.SECONDS.toHours(leftSeconds)
    leftSeconds -= TimeUnit.HOURS.toSeconds(hours)

    val minutes = TimeUnit.SECONDS.toMinutes(leftSeconds)
    leftSeconds -= TimeUnit.MINUTES.toSeconds(minutes)

    return "%02d:%02d:%02d".format(
        hours,
        minutes,
        leftSeconds
    )
}
