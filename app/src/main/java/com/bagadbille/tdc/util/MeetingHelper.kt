package com.bagadbille.tdc.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.bagadbille.tdc.data.model.Schedule
import java.util.Calendar

object MeetingHelper {

    fun parseTimeToMinutes(timeStr: String): Int? {
        val clean = timeStr.trim().uppercase()
        val isPm = clean.endsWith("PM")
        val isAm = clean.endsWith("AM")
        val stripped = clean.replace("AM", "").replace("PM", "").trim()
        val parts = stripped.split(":")
        if (parts.size >= 2) {
            val rawHour = parts[0].trim().toIntOrNull() ?: return null
            val minute = parts[1].trim().take(2).toIntOrNull() ?: return null
            var hour = rawHour
            if (isPm && hour < 12) hour += 12
            if (isAm && hour == 12) hour = 0
            return hour * 60 + minute
        }
        return null
    }

    fun isScheduleLive(schedule: Schedule): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sun, 1=Mon, ..., 6=Sat
        if (schedule.dayOfWeek != today) return false

        val currentMinutes = calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
        val startMin = parseTimeToMinutes(schedule.startTime) ?: return false
        val endMin = parseTimeToMinutes(schedule.endTime) ?: (startMin + 60)

        return currentMinutes in startMin..endMin
    }

    fun extractMeetingUrl(room: String?): String? {
        if (room.isNullOrBlank()) return null
        val clean = room.trim()

        val urlRegex = Regex("""https?://[^\s]+""")
        val match = urlRegex.find(clean)
        if (match != null) return match.value

        if (clean.contains("meet.google.com", ignoreCase = true)) {
            return if (!clean.startsWith("http")) "https://$clean" else clean
        }
        if (clean.contains("zoom.us", ignoreCase = true)) {
            return if (!clean.startsWith("http")) "https://$clean" else clean
        }
        if (clean.contains("teams.microsoft.com", ignoreCase = true)) {
            return if (!clean.startsWith("http")) "https://$clean" else clean
        }
        if (clean.contains("Google Meet", ignoreCase = true) || clean.equals("Meet", ignoreCase = true)) {
            return "https://meet.google.com"
        }
        if (clean.contains("Zoom", ignoreCase = true)) {
            return "https://zoom.us/join"
        }
        return null
    }

    fun isOnlineMeeting(room: String?): Boolean {
        return extractMeetingUrl(room) != null
    }

    fun launchMeeting(context: Context, room: String?) {
        val url = extractMeetingUrl(room) ?: "https://meet.google.com"
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open meeting: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
