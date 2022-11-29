package example.uob.chatbox.common

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.*

object Common {
    private const val SERVER_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"

    @SuppressLint("SimpleDateFormat")
    fun convertGmtToLocalFullTimeString(dateTimeString: String?): String {
        val fromFormat = SimpleDateFormat(SERVER_TIME_FORMAT)
        fromFormat.timeZone = TimeZone.getTimeZone("UTC")

        val date: Date
        try {
            date = dateTimeString?.let { fromFormat.parse(it) } as Date
        } catch (e: Exception) {
            return ""
        }

        val cal = Calendar.getInstance()
        cal.time = date

        var outputDateFormat = "HH:mm"

        val toFormat = SimpleDateFormat(outputDateFormat)
        return toFormat.format(date)
    }
}