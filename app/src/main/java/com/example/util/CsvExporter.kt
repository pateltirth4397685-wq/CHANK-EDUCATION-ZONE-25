package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.BatchEntity
import com.example.data.DailyStudentAttendanceEntity
import com.example.data.ExaminationEntity
import com.example.data.ExamResultEntity
import com.example.data.FeeInvoiceEntity
import com.example.data.StudentEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvExporter {

    /**
     * Sanitizes cell content against CSV injection vulnerabilities (OWASP guidelines).
     * If content begins with =, +, -, @, or tab/return, prepend a single quote (').
     * If content contains comma, quote, or newline, escape quotes as "" and wrap in double quotes.
     */
    fun sanitizeCsvCell(value: String?): String {
        if (value == null) return "\"\""
        var str = value.trim()

        // Formula injection protection
        if (str.isNotEmpty() && (str.startsWith("=") || str.startsWith("+") || str.startsWith("-") || str.startsWith("@") || str.startsWith("\t") || str.startsWith("\r"))) {
            str = "'$str"
        }

        // If contains special characters, escape inner quotes and wrap in quotes
        return if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            "\"${str.replace("\"", "\"\"")}\""
        } else {
            "\"$str\""
        }
    }

    /**
     * Exports Room Student records into formatted CSV data.
     */
    fun generateStudentsCsv(
        students: List<StudentEntity>,
        batches: List<BatchEntity>,
        invoices: List<FeeInvoiceEntity>
    ): String {
        return buildString {
            // Header
            appendLine(
                listOf(
                    "Student ID",
                    "Student Code",
                    "Full Name",
                    "Mobile",
                    "Email",
                    "Assigned Cohort",
                    "Course Program",
                    "Admission Date",
                    "Parent / Guardian",
                    "Parent Mobile",
                    "Address",
                    "Total Course Fee (INR)",
                    "Fee Paid (INR)",
                    "Balance Due (INR)",
                    "Fee Status",
                    "Enrollment Status"
                ).joinToString(",") { sanitizeCsvCell(it) }
            )

            // Rows
            students.forEach { st ->
                val batch = batches.find { it.id == st.assignedBatchId }
                val invoice = invoices.find { it.studentId == st.id }
                val paid = invoice?.paidAmount ?: 0.0
                val balance = invoice?.balanceAmount ?: (st.totalCourseFee - paid)
                val feeStatus = invoice?.status ?: (if (balance <= 0) "PAID" else "UNPAID")

                appendLine(
                    listOf(
                        st.id.toString(),
                        st.studentCode,
                        st.fullName,
                        st.mobile,
                        st.email ?: "N/A",
                        batch?.batchName ?: "Unassigned",
                        st.courseName,
                        st.admissionDate,
                        st.parentName,
                        st.parentMobile,
                        st.address,
                        String.format(Locale.getDefault(), "%.2f", st.totalCourseFee),
                        String.format(Locale.getDefault(), "%.2f", paid),
                        String.format(Locale.getDefault(), "%.2f", balance),
                        feeStatus,
                        st.status
                    ).joinToString(",") { sanitizeCsvCell(it) }
                )
            }
        }
    }

    /**
     * Exports Room Daily Student Attendance records into formatted CSV data.
     */
    fun generateAttendanceCsv(
        attendanceRecords: List<DailyStudentAttendanceEntity>,
        students: List<StudentEntity>,
        batches: List<BatchEntity>
    ): String {
        return buildString {
            // Header
            appendLine(
                listOf(
                    "Attendance Date",
                    "Batch / Cohort",
                    "Course Name",
                    "Subject",
                    "Student Code",
                    "Student Full Name",
                    "Contact Mobile",
                    "Attendance Status",
                    "Faculty Note / Remarks",
                    "Evaluator",
                    "Recorded Timestamp"
                ).joinToString(",") { sanitizeCsvCell(it) }
            )

            // Rows sorted by date descending, then batch
            attendanceRecords.sortedWith(compareByDescending<DailyStudentAttendanceEntity> { it.attendanceDate }.thenBy { it.batchId }).forEach { att ->
                val batch = batches.find { it.id == att.batchId }
                val student = students.find { it.id == att.studentId }

                appendLine(
                    listOf(
                        att.attendanceDate,
                        batch?.batchName ?: "Batch #${att.batchId}",
                        batch?.courseName ?: "Coaching Program",
                        batch?.subject ?: "Core Subject",
                        student?.studentCode ?: "STU-${att.studentId}",
                        student?.fullName ?: "Student #${att.studentId}",
                        student?.mobile ?: "N/A",
                        att.status,
                        att.remarks.ifBlank { "Regular Attendance" },
                        att.markedBy,
                        att.recordedAt.ifBlank { att.attendanceDate }
                    ).joinToString(",") { sanitizeCsvCell(it) }
                )
            }
        }
    }

    /**
     * Exports Master Administrative Report combining student demographics, attendance percentage, and test scores.
     */
    fun generateMasterAdministrativeReportCsv(
        students: List<StudentEntity>,
        batches: List<BatchEntity>,
        dailyAttendance: List<DailyStudentAttendanceEntity>,
        examResults: List<ExamResultEntity>,
        examinations: List<ExaminationEntity>,
        invoices: List<FeeInvoiceEntity>
    ): String {
        return buildString {
            // Header
            appendLine(
                listOf(
                    "Student Code",
                    "Full Name",
                    "Assigned Cohort",
                    "Course",
                    "Student Phone",
                    "Parent Phone",
                    "Total Sessions Evaluated",
                    "Sessions Present",
                    "Sessions Absent",
                    "Attendance Rate (%)",
                    "Tests Taken",
                    "Academic Average Score (%)",
                    "Cumulative Grade",
                    "Course Fee (INR)",
                    "Fee Paid (INR)",
                    "Balance Outstanding (INR)"
                ).joinToString(",") { sanitizeCsvCell(it) }
            )

            students.forEach { st ->
                val batch = batches.find { it.id == st.assignedBatchId }
                val studentAttendance = dailyAttendance.filter { it.studentId == st.id }
                val totalAtt = studentAttendance.size
                val presentCount = studentAttendance.count { it.status == "PRESENT" }
                val absentCount = studentAttendance.count { it.status == "ABSENT" }
                val attRate = if (totalAtt > 0) ((presentCount.toDouble() / totalAtt) * 100).toInt() else 0

                val sResults = examResults.filter { it.studentId == st.id }
                val testsTaken = sResults.size
                val sMax = sResults.mapNotNull { r -> examinations.find { it.id == r.examId }?.maxMarks }.sum()
                val sScore = sResults.sumOf { it.marksObtained }
                val acadAvg = if (sMax > 0) ((sScore / sMax) * 100).toInt() else 0
                val grade = when {
                    acadAvg >= 90 -> "A+"
                    acadAvg >= 80 -> "A"
                    acadAvg >= 70 -> "B+"
                    acadAvg >= 60 -> "B"
                    acadAvg >= 50 -> "C"
                    acadAvg >= 40 -> "D"
                    testsTaken == 0 -> "N/A"
                    else -> "F"
                }

                val invoice = invoices.find { it.studentId == st.id }
                val paid = invoice?.paidAmount ?: 0.0
                val balance = invoice?.balanceAmount ?: (st.totalCourseFee - paid)

                appendLine(
                    listOf(
                        st.studentCode,
                        st.fullName,
                        batch?.batchName ?: "Unassigned",
                        st.courseName,
                        st.mobile,
                        st.parentMobile,
                        totalAtt.toString(),
                        presentCount.toString(),
                        absentCount.toString(),
                        "$attRate%",
                        testsTaken.toString(),
                        if (testsTaken > 0) "$acadAvg%" else "N/A",
                        grade,
                        String.format(Locale.getDefault(), "%.2f", st.totalCourseFee),
                        String.format(Locale.getDefault(), "%.2f", paid),
                        String.format(Locale.getDefault(), "%.2f", balance)
                    ).joinToString(",") { sanitizeCsvCell(it) }
                )
            }
        }
    }

    /**
     * Writes CSV string to cache file and triggers Android system share chooser via FileProvider.
     */
    fun saveAndShareCsv(
        context: Context,
        fileName: String,
        csvContent: String,
        shareTitle: String = "Share Administrative CSV Export"
    ): File? {
        return try {
            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val cleanName = if (fileName.endsWith(".csv")) fileName else "$fileName.csv"
            val file = File(exportDir, cleanName)

            FileOutputStream(file).use { out ->
                out.write(csvContent.toByteArray(Charsets.UTF_8))
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, cleanName)
                putExtra(Intent.EXTRA_TEXT, "Attached is the FrontCraft Pro administrative export: $cleanName")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, shareTitle)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
