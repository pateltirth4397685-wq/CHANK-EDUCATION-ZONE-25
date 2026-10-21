package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ActivityLogEntity
import com.example.data.AnnouncementEntity
import com.example.data.AppDatabase
import com.example.data.AttendanceChangeLogEntity
import com.example.data.AttendanceRecordEntity
import com.example.data.AttendanceSessionEntity
import com.example.data.BatchEntity
import com.example.data.ExaminationEntity
import com.example.data.ExamResultEntity
import com.example.data.FeeInvoiceEntity
import com.example.data.FeePaymentEntity
import com.example.data.Repository
import com.example.data.StudentEntity
import com.example.data.TeacherEntity
import com.example.data.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppTab(val title: String) {
    DASHBOARD("Dashboard"),
    BATCHES("Batches"),
    ATTENDANCE("Attendance"),
    STUDENTS("Students"),
    FACULTY("Faculty Management"),
    FEES("Fees & Payments"),
    EXAMS("Exams & Results"),
    REPORTS("Reports"),
    ANNOUNCEMENTS("Notice Board"),
    SETTINGS("Settings")
}

class FrontCraftViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: Repository

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = Repository(database.coachingDao())
    }

    // --- Current User State ---
    private val _currentUser = MutableStateFlow(
        UserProfile("admin", "Principal Administrator", "ADMIN", 1)
    )
    val currentUser: StateFlow<UserProfile> = _currentUser.asStateFlow()

    fun switchRole(newRole: String) {
        val profile = when (newRole) {
            "TEACHER" -> UserProfile("teacher1", "Prof. Ramesh Kulkarni", "TEACHER", 1)
            "STUDENT" -> UserProfile("student1", "Aarav Mehta", "STUDENT", 1)
            "PARENT" -> UserProfile("parent1", "Sunil Mehta (Guardian)", "PARENT", 1)
            else -> UserProfile("admin", "Principal Administrator", "ADMIN", 1)
        }
        _currentUser.value = profile
    }

    // --- Navigation Tab State ---
    private val _currentTab = MutableStateFlow(AppTab.DASHBOARD)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    fun switchTab(tab: AppTab) {
        _currentTab.value = tab
    }

    // --- Data Streams ---
    val allBatches = repository.allBatches.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allStudents = repository.allStudents.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allTeachers = repository.allTeachers.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allAttendanceSessions = repository.allAttendanceSessions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allAttendanceRecords = repository.allAttendanceRecords.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allDailyAttendance = repository.allDailyAttendance.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allInvoices = repository.allInvoices.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allPayments = repository.allPayments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allExaminations = repository.allExaminations.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allExamResults = repository.allExamResults.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val allAnnouncements = repository.allAnnouncements.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val recentActivityLogs = repository.recentActivityLogs.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
    }

    // --- Batch Management Actions ---
    fun createBatch(
        batchName: String,
        courseName: String,
        subject: String,
        teacherId: Long?,
        teacherName: String,
        scheduleTime: String,
        capacity: Int,
        startDate: String
    ) {
        viewModelScope.launch {
            val batch = BatchEntity(
                batchName = batchName,
                courseName = courseName,
                subject = subject,
                teacherId = teacherId,
                teacherName = teacherName,
                scheduleTime = scheduleTime,
                capacity = capacity,
                startDate = startDate
            )
            repository.insertBatch(batch)

            // Synchronize teacher assignedBatches
            if (teacherId != null && teacherId > 0) {
                val teacher = allTeachers.value.find { it.id == teacherId }
                if (teacher != null) {
                    val currentBatches = teacher.assignedBatches.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it != "Unassigned" }
                        .toMutableList()
                    if (!currentBatches.contains(batchName)) {
                        currentBatches.add(batchName)
                        repository.updateTeacher(teacher.copy(assignedBatches = currentBatches.joinToString(", ")))
                    }
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "BATCH_CREATED",
                    entityType = "Batch",
                    entityReference = batchName,
                    details = "Created $batchName for $courseName ($subject, Capacity: $capacity, Faculty: $teacherName)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun updateBatch(batch: BatchEntity) {
        viewModelScope.launch {
            repository.updateBatch(batch)

            // Synchronize teacher assignedBatches
            if (batch.teacherId != null && batch.teacherId > 0) {
                val teacher = allTeachers.value.find { it.id == batch.teacherId }
                if (teacher != null) {
                    val currentBatches = teacher.assignedBatches.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it != "Unassigned" }
                        .toMutableList()
                    if (!currentBatches.contains(batch.batchName)) {
                        currentBatches.add(batch.batchName)
                        repository.updateTeacher(teacher.copy(assignedBatches = currentBatches.joinToString(", ")))
                    }
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "BATCH_UPDATED",
                    entityType = "Batch",
                    entityReference = batch.batchName,
                    details = "Updated batch configuration: Capacity ${batch.capacity}, Subject ${batch.subject}, Teacher: ${batch.teacherName}",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun deleteBatch(batchId: Long, batchName: String) {
        viewModelScope.launch {
            repository.deleteBatch(batchId)

            // Clean up deleted batch from any teachers' assignedBatches
            allTeachers.value.forEach { teacher ->
                if (teacher.assignedBatches.contains(batchName)) {
                    val updatedBatches = teacher.assignedBatches.split(",")
                        .map { it.trim() }
                        .filter { it.isNotBlank() && it != batchName }
                    val newBatchStr = if (updatedBatches.isEmpty()) "Unassigned" else updatedBatches.joinToString(", ")
                    repository.updateTeacher(teacher.copy(assignedBatches = newBatchStr))
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "BATCH_DELETED",
                    entityType = "Batch",
                    entityReference = batchName,
                    details = "Removed batch ID #$batchId ($batchName)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    // --- Student Actions ---
    fun enrollStudent(
        fullName: String,
        mobile: String,
        email: String? = null,
        parentName: String,
        parentMobile: String,
        address: String? = null,
        admissionDate: String,
        assignedBatchId: Long?,
        courseName: String,
        totalCourseFee: Double
    ) {
        viewModelScope.launch {
            val count = allStudents.value.size + 1
            val code = "STU-${100 + count}"
            val student = StudentEntity(
                studentCode = code,
                fullName = fullName,
                mobile = mobile,
                email = email?.takeIf { it.isNotBlank() },
                parentName = parentName,
                parentMobile = parentMobile,
                address = address?.takeIf { it.isNotBlank() },
                admissionDate = admissionDate,
                assignedBatchId = assignedBatchId,
                courseName = courseName,
                totalCourseFee = totalCourseFee
            )
            val sid = repository.insertStudent(student)

            // Auto create fee invoice
            repository.insertInvoice(
                FeeInvoiceEntity(
                    invoiceNumber = "INV-2026-${1000 + sid}",
                    studentId = sid,
                    totalFee = totalCourseFee,
                    paidAmount = 0.0,
                    balanceAmount = totalCourseFee,
                    dueDate = admissionDate,
                    status = "UNPAID"
                )
            )

            val batchName = allBatches.value.find { it.id == assignedBatchId }?.batchName ?: "Unassigned"
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "STUDENT_ENROLLED",
                    entityType = "Student",
                    entityReference = fullName,
                    details = "Enrolled $fullName ($code) into $batchName (Course: $courseName, Fee: ₹$totalCourseFee)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun updateStudent(student: StudentEntity) {
        viewModelScope.launch {
            repository.updateStudent(student)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "STUDENT_UPDATED",
                    entityType = "Student",
                    entityReference = student.fullName,
                    details = "Updated personal details for ${student.fullName} (${student.studentCode})",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun deleteStudent(studentId: Long, studentName: String) {
        viewModelScope.launch {
            repository.deleteStudent(studentId)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "STUDENT_DELETED",
                    entityType = "Student",
                    entityReference = studentName,
                    details = "Removed student $studentName (#$studentId)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun assignStudentToBatch(studentId: Long, newBatchId: Long) {
        viewModelScope.launch {
            val student = allStudents.value.find { it.id == studentId } ?: return@launch
            val batch = allBatches.value.find { it.id == newBatchId }
            val batchName = batch?.batchName ?: "Batch #$newBatchId"
            val updated = student.copy(assignedBatchId = newBatchId)
            repository.updateStudent(updated)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "STUDENT_BATCH_ASSIGNED",
                    entityType = "Student",
                    entityReference = student.fullName,
                    details = "Assigned ${student.fullName} (${student.studentCode}) to $batchName",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun removeStudentFromBatch(studentId: Long) {
        viewModelScope.launch {
            val student = allStudents.value.find { it.id == studentId } ?: return@launch
            val oldBatch = allBatches.value.find { it.id == student.assignedBatchId }
            val updated = student.copy(assignedBatchId = 0L)
            repository.updateStudent(updated)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "STUDENT_BATCH_REMOVED",
                    entityType = "Student",
                    entityReference = student.fullName,
                    details = "Removed ${student.fullName} from ${oldBatch?.batchName ?: "batch"}",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    // --- Daily Attendance Actions ---
    fun recordAttendance(
        batchId: Long,
        attendanceDate: String,
        subject: String,
        statusMap: Map<Long, String>,
        remarksMap: Map<Long, String> = emptyMap()
    ) {
        viewModelScope.launch {
            val batch = allBatches.value.find { it.id == batchId }
            val batchName = batch?.batchName ?: "Batch #$batchId"

            val existingSession = repository.getSessionByBatchAndDate(batchId, attendanceDate)
            val sessionId = if (existingSession != null) {
                repository.deleteRecordsBySessionId(existingSession.id)
                existingSession.id
            } else {
                repository.insertAttendanceSession(
                    AttendanceSessionEntity(
                        batchId = batchId,
                        attendanceDate = attendanceDate,
                        subject = subject,
                        sessionStatus = "COMPLETED",
                        createdBy = _currentUser.value.fullName,
                        createdAt = attendanceDate
                    )
                )
            }

            val records = statusMap.map { (studentId, status) ->
                AttendanceRecordEntity(
                    sessionId = sessionId,
                    studentId = studentId,
                    status = status,
                    remarks = remarksMap[studentId] ?: "",
                    markedBy = _currentUser.value.fullName
                )
            }
            repository.insertAttendanceRecords(records)

            val dailyRecords = statusMap.map { (studentId, status) ->
                com.example.data.DailyStudentAttendanceEntity(
                    batchId = batchId,
                    studentId = studentId,
                    attendanceDate = attendanceDate,
                    status = status,
                    remarks = remarksMap[studentId] ?: "",
                    markedBy = _currentUser.value.fullName,
                    recordedAt = getCurrentTimestamp()
                )
            }
            repository.deleteDailyAttendanceByBatchAndDate(batchId, attendanceDate)
            repository.insertDailyAttendance(dailyRecords)

            val presentCount = statusMap.values.count { it == "PRESENT" }
            val absentCount = statusMap.values.count { it == "ABSENT" }
            val totalMarked = statusMap.size

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "ATTENDANCE_RECORDED",
                    entityType = "Attendance",
                    entityReference = "$batchName - $attendanceDate",
                    details = "Marked attendance for $totalMarked students in $batchName ($presentCount Present, $absentCount Absent)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    // --- Fee Payment Actions ---
    fun recordPayment(
        studentId: Long,
        amount: Double,
        paymentDate: String,
        paymentMethod: String,
        remarks: String
    ) {
        viewModelScope.launch {
            val student = allStudents.value.find { it.id == studentId }
            val invoice = allInvoices.value.find { it.studentId == studentId }
            val invId = invoice?.id ?: 1L
            val recNo = "REC-2026-${(100..999).random()}"

            repository.insertPayment(
                FeePaymentEntity(
                    receiptNumber = recNo,
                    invoiceId = invId,
                    studentId = studentId,
                    amount = amount,
                    paymentDate = paymentDate,
                    paymentMethod = paymentMethod,
                    remarks = remarks,
                    receivedBy = _currentUser.value.fullName
                )
            )

            if (invoice != null) {
                val newPaid = invoice.paidAmount + amount
                val newBal = (invoice.totalFee - newPaid).coerceAtLeast(0.0)
                val newStatus = if (newBal == 0.0) "PAID" else if (newPaid > 0.0) "PARTIAL" else "UNPAID"
                repository.updateInvoice(
                    invoice.copy(paidAmount = newPaid, balanceAmount = newBal, status = newStatus)
                )
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "PAYMENT_RECORDED",
                    entityType = "FeePayment",
                    entityReference = recNo,
                    details = "Collected ₹${String.format("%,.0f", amount)} from ${student?.fullName ?: "Student"} via $paymentMethod",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun generateStudentFeeReminder(student: StudentEntity, batch: BatchEntity?, invoice: FeeInvoiceEntity): String {
        val batchName = batch?.batchName ?: "FrontCraft Cohort"
        val dueAmount = String.format(Locale.getDefault(), "%,.0f", invoice.balanceAmount)
        val totalFee = String.format(Locale.getDefault(), "%,.0f", invoice.totalFee)
        val paidAmount = String.format(Locale.getDefault(), "%,.0f", invoice.paidAmount)
        val guardian = student.parentName.ifBlank { "Parent/Guardian" }

        return """
*FEE PAYMENT REMINDER - FRONTCRAFT PRO*
Dear $guardian / ${student.fullName},

This is a courtesy reminder that tuition fees for cohort '$batchName' (${student.courseName}) have a pending balance:

• Student Code: ${student.studentCode}
• Invoice Number: ${invoice.invoiceNumber}
• Total Course Fee: ₹$totalFee
• Total Paid to Date: ₹$paidAmount
• Pending Balance Due: ₹$dueAmount
• Due Date: ${invoice.dueDate}

Payment Options:
1. UPI: frontcraft.pro@upi
2. Net Banking: FrontCraft Academy (A/C: 9820011999, IFSC: FCAC0001234)
3. Accounts Desk: Mon-Sat, 8 AM - 6 PM

Please ignore if already paid. For receipts or fee queries, contact 9820011999.
        """.trimIndent()
    }

    fun broadcastBatchFeeReminder(batchId: Long, customNote: String = "") {
        viewModelScope.launch {
            val batch = allBatches.value.find { it.id == batchId } ?: return@launch
            val studentsInBatch = allStudents.value.filter { it.assignedBatchId == batchId }
            val invoicesInBatch = allInvoices.value.filter { inv ->
                studentsInBatch.any { it.id == inv.studentId } && inv.balanceAmount > 0
            }
            val totalPendingAmount = invoicesInBatch.sumOf { it.balanceAmount }
            val pendingCount = invoicesInBatch.size

            val announcementTitle = "Fee Payment Reminder: ${batch.batchName}"
            val content = buildString {
                append("Attention ${batch.batchName} Students & Guardians: ")
                append("A scheduled tuition installment is pending for $pendingCount students (Total Outstanding: ₹${String.format(Locale.getDefault(), "%,.0f", totalPendingAmount)}). ")
                append("Please clear the outstanding dues through UPI (frontcraft.pro@upi) or at the administration accounts counter. ")
                if (customNote.isNotBlank()) {
                    append("Note: ${customNote.trim()} ")
                }
                append("Timely fee clearance ensures uninterrupted classroom sessions and mock test eligibility.")
            }

            repository.insertAnnouncement(
                AnnouncementEntity(
                    title = announcementTitle,
                    content = content,
                    targetAudience = "ALL",
                    publishedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    priority = "URGENT",
                    createdBy = _currentUser.value.fullName
                )
            )

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "BATCH_FEE_REMINDER_BROADCAST",
                    entityType = "Batch",
                    entityReference = batch.batchName,
                    details = "Broadcasted fee reminder for ${batch.batchName} ($pendingCount students, ₹${String.format(Locale.getDefault(), "%,.0f", totalPendingAmount)} pending)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    // --- Exam Actions ---
    fun scheduleExam(
        title: String,
        batchId: Long,
        subject: String,
        maxMarks: Double,
        passingMarks: Double = maxMarks * 0.4,
        date: String
    ) {
        viewModelScope.launch {
            val exam = ExaminationEntity(
                examTitle = title,
                batchId = batchId,
                subject = subject,
                maxMarks = maxMarks,
                passingMarks = passingMarks,
                examDate = date,
                status = "SCHEDULED"
            )
            repository.insertExamination(exam)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "EXAM_SCHEDULED",
                    entityType = "Examination",
                    entityReference = title,
                    details = "Scheduled $title for Batch #$batchId on $date (Max $maxMarks)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun deleteExam(examId: Long, title: String) {
        viewModelScope.launch {
            repository.deleteExamination(examId)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "EXAM_DELETED",
                    entityType = "Examination",
                    entityReference = title,
                    details = "Deleted exam #$examId ($title)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun calculateGrade(marks: Double, maxMarks: Double): String {
        if (maxMarks <= 0.0) return "N/A"
        val pct = (marks / maxMarks) * 100.0
        return when {
            pct >= 90.0 -> "A+"
            pct >= 80.0 -> "A"
            pct >= 70.0 -> "B+"
            pct >= 60.0 -> "B"
            pct >= 50.0 -> "C"
            pct >= 40.0 -> "D"
            else -> "F"
        }
    }

    fun recordExamScores(
        examId: Long,
        scores: Map<Long, Double>,
        remarksMap: Map<Long, String> = emptyMap()
    ) {
        viewModelScope.launch {
            val exam = allExaminations.value.find { it.id == examId } ?: return@launch
            val resultEntities = scores.map { (studentId, marks) ->
                val grade = calculateGrade(marks, exam.maxMarks)
                val status = if (marks >= exam.passingMarks) "PASS" else "FAIL"
                val defaultRemark = "Grade $grade"
                val customRemark = remarksMap[studentId]?.trim()
                val finalRemarks = if (!customRemark.isNullOrBlank()) customRemark else defaultRemark
                ExamResultEntity(
                    examId = examId,
                    studentId = studentId,
                    marksObtained = marks,
                    grade = grade,
                    status = status,
                    remarks = finalRemarks,
                    submittedBy = _currentUser.value.fullName
                )
            }

            repository.deleteResultsByExamId(examId)
            repository.insertExamResults(resultEntities)
            repository.updateExamination(exam.copy(status = "COMPLETED"))

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "EXAM_RESULTS_RECORDED",
                    entityType = "Examination",
                    entityReference = exam.examTitle,
                    details = "Recorded test scores and assigned grades for ${resultEntities.size} students in ${exam.examTitle}",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    // --- Faculty / Teacher Management Actions ---
    fun addTeacher(
        fullName: String,
        mobile: String,
        email: String,
        subject: String,
        assignedCourses: String,
        assignedBatchIds: List<Long>,
        joiningDate: String
    ) {
        viewModelScope.launch {
            val count = allTeachers.value.size + 1
            val code = "FAC-${100 + count}"
            val selectedBatches = allBatches.value.filter { assignedBatchIds.contains(it.id) }
            val batchNames = selectedBatches.joinToString(", ") { it.batchName }
            val teacher = TeacherEntity(
                teacherCode = code,
                fullName = fullName,
                mobile = mobile,
                email = email,
                subject = subject,
                assignedCourses = assignedCourses.ifBlank { "JEE / NEET" },
                assignedBatches = if (batchNames.isNotBlank()) batchNames else "Unassigned",
                joiningDate = joiningDate.ifBlank { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
            )
            val tid = repository.insertTeacher(teacher)

            // Update assigned batches with this teacher
            assignedBatchIds.forEach { bid ->
                val b = allBatches.value.find { it.id == bid }
                if (b != null) {
                    repository.updateBatch(b.copy(teacherId = tid, teacherName = fullName, subject = subject))
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "FACULTY_ADDED",
                    entityType = "Faculty",
                    entityReference = fullName,
                    details = "Added faculty $fullName ($code) for $subject with ${assignedBatchIds.size} assigned batches",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun updateTeacher(
        teacher: TeacherEntity,
        assignedBatchIds: List<Long>
    ) {
        viewModelScope.launch {
            val selectedBatches = allBatches.value.filter { assignedBatchIds.contains(it.id) }
            val batchNames = selectedBatches.joinToString(", ") { it.batchName }
            val updated = teacher.copy(
                assignedBatches = if (batchNames.isNotBlank()) batchNames else "Unassigned"
            )
            repository.updateTeacher(updated)

            // Sync assigned batches
            allBatches.value.forEach { b ->
                if (assignedBatchIds.contains(b.id)) {
                    repository.updateBatch(b.copy(teacherId = teacher.id, teacherName = teacher.fullName, subject = teacher.subject))
                } else if (b.teacherId == teacher.id) {
                    repository.updateBatch(b.copy(teacherId = null, teacherName = "Unassigned Faculty"))
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "FACULTY_UPDATED",
                    entityType = "Faculty",
                    entityReference = teacher.fullName,
                    details = "Updated profile for ${teacher.fullName} (${teacher.teacherCode})",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun assignTeacherToBatches(teacherId: Long, teacherName: String, teacherSubject: String, batchIds: List<Long>) {
        viewModelScope.launch {
            val teacher = allTeachers.value.find { it.id == teacherId } ?: return@launch
            val selectedBatches = allBatches.value.filter { batchIds.contains(it.id) }
            val batchNames = selectedBatches.joinToString(", ") { it.batchName }

            val updatedTeacher = teacher.copy(
                assignedBatches = if (batchNames.isNotBlank()) batchNames else "Unassigned"
            )
            repository.updateTeacher(updatedTeacher)

            // Update all batches
            allBatches.value.forEach { b ->
                if (batchIds.contains(b.id)) {
                    repository.updateBatch(b.copy(teacherId = teacherId, teacherName = teacherName, subject = teacherSubject))
                } else if (b.teacherId == teacherId) {
                    repository.updateBatch(b.copy(teacherId = null, teacherName = "Unassigned Faculty"))
                }
            }

            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "FACULTY_BATCH_ASSIGNED",
                    entityType = "Faculty",
                    entityReference = teacherName,
                    details = "Assigned $teacherName to batches: ${if (batchNames.isNotBlank()) batchNames else "None"}",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }

    fun deleteTeacher(teacherId: Long, teacherName: String) {
        viewModelScope.launch {
            allBatches.value.filter { it.teacherId == teacherId }.forEach { b ->
                repository.updateBatch(b.copy(teacherId = null, teacherName = "Unassigned Faculty"))
            }
            repository.deleteTeacher(teacherId)
            repository.insertActivityLog(
                ActivityLogEntity(
                    userName = _currentUser.value.fullName,
                    userRole = _currentUser.value.role,
                    action = "FACULTY_REMOVED",
                    entityType = "Faculty",
                    entityReference = teacherName,
                    details = "Removed faculty $teacherName (ID #$teacherId)",
                    timestamp = getCurrentTimestamp()
                )
            )
        }
    }
}
