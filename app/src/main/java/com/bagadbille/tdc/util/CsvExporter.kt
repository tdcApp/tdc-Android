package com.bagadbille.tdc.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bagadbille.tdc.data.model.QuizSubmissionDetail
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CsvExporter {

    fun exportQuizResults(
        context: Context,
        quizTitle: String,
        filterLabel: String,
        submissions: List<QuizSubmissionDetail>,
        totalQuestions: Int
    ) {
        if (submissions.isEmpty()) {
            Toast.makeText(context, "No candidates to export", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val safeQuizTitle = quizTitle.replace(Regex("[^a-zA-Z0-9_-]"), "_").take(30)
            val safeFilter = filterLabel.replace(Regex("[^a-zA-Z0-9_-]"), "_")
            val fileName = "Quiz_${safeQuizTitle}_${safeFilter}_$dateStamp.csv"

            val csvContent = buildString {
                // UTF-8 BOM so Excel opens with proper character encoding
                append("\uFEFF")
                append("Rank,Student Name,Email,Enrollment,Score,Total Questions,Percentage,Status,Entering Time,Exit Time,Total Time Taken\n")
                submissions.forEachIndexed { index, item ->
                    val rank = index + 1
                    val name = escapeCsv(item.studentName.ifBlank { "N/A" })
                    val email = escapeCsv(item.studentEmail.ifBlank { "N/A" })
                    val enrollment = escapeCsv(item.studentEnrollment ?: "N/A")
                    val isDisqualified = item.submission.score < 0
                    val score = if (isDisqualified) "Disqualified" else item.submission.score.toString()
                    val total = totalQuestions.toString()
                    val percentage = if (!isDisqualified && totalQuestions > 0) {
                        String.format(Locale.US, "%.1f%%", (item.submission.score.toDouble() / totalQuestions) * 100)
                    } else "0.0%"
                    val status = if (isDisqualified) "Disqualified" else "Qualified"
                    val enteringTime = escapeCsv(formatTimestamp(item.submission.startedAt))
                    val exitTime = escapeCsv(formatTimestamp(item.submission.submittedAt))
                    val totalTimeTaken = escapeCsv(formatDuration(item.submission.timeTakenSeconds, item.submission.startedAt, item.submission.submittedAt))

                    append("$rank,$name,$email,$enrollment,$score,$total,$percentage,$status,$enteringTime,$exitTime,$totalTimeTaken\n")
                }
            }

            // Save in cache directory for FileProvider sharing
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val sharedFile = File(exportDir, fileName)
            sharedFile.writeText(csvContent, Charsets.UTF_8)

            // Save to device Downloads folder so the user has the physical file
            saveToDownloads(context, fileName, csvContent)

            // Open Android Share / View Sheet
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                sharedFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "$quizTitle - Results ($filterLabel)")
                putExtra(Intent.EXTRA_TEXT, "Exported results for $quizTitle (${submissions.size} candidates).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Quiz CSV Export"))
            Toast.makeText(context, "Exported ${submissions.size} candidates & saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToDownloads(context: Context, fileName: String, content: String) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir.exists() || downloadsDir.mkdirs()) {
                    val file = File(downloadsDir, fileName)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(content.toByteArray(Charsets.UTF_8))
                    }
                }
            }
        } catch (_: Exception) {
            // Non-critical: the cache file is still available for sharing
        }
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun formatTimestamp(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        return try {
            val parseFormats = listOf(
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
                SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
            )
            val outFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            for (pf in parseFormats) {
                try {
                    val d = pf.parse(isoString)
                    if (d != null) return outFormat.format(d)
                } catch (_: Exception) {}
            }
            isoString
        } catch (_: Exception) {
            isoString
        }
    }

    private fun parseDateMillis(isoString: String?): Long? {
        if (isoString.isNullOrBlank()) return null
        val parseFormats = listOf(
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US),
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
        )
        for (pf in parseFormats) {
            try {
                val d = pf.parse(isoString)
                if (d != null) return d.time
            } catch (_: Exception) {}
        }
        return null
    }

    private fun formatDuration(seconds: Int?, startedAt: String?, submittedAt: String?): String {
        val totalSec = if (seconds != null && seconds > 0) {
            seconds
        } else {
            val startMs = parseDateMillis(startedAt)
            val subMs = parseDateMillis(submittedAt)
            if (startMs != null && subMs != null && subMs >= startMs) {
                ((subMs - startMs) / 1000).toInt()
            } else null
        }

        if (totalSec == null || totalSec <= 0) return "N/A"
        val m = totalSec / 60
        val s = totalSec % 60
        return if (m > 0) "${m}m ${s}s" else "${s}s"
    }
}
