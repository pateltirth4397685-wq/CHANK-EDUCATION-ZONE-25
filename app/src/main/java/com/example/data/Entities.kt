package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "batches")
data class BatchEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchName: String,
    val courseName: String,
    val subject: String = "Physics", // Assigned subject for this batch
    val teacherId: Long? = null,
    val teacherName: String = "Prof. Ramesh Kulkarni",
    val scheduleTime: String = "07:30 AM - 10:00 AM",
    val capacity: Int = 30, // Total student capacity per batch
    val startDate: String = "2026-04-01",
    val endDate: String = "2027-03-31",
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "students",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedBatchId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["studentCode"], unique = true), Index(value = ["assignedBatchId"])]
)
data class StudentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentCode: String,
    val fullName: String,
    val mobile: String,
    val parentName: String,
    val parentMobile: String,
    val email: String? = null,
    val address: String? = null,
    val admissionDate: String,
    val assignedBatchId: Long? = null,
    val courseName: String = "Standard Course",
    val totalCourseFee: Double = 90000.0,
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "teachers",
    indices = [Index(value = ["teacherCode"], unique = true)]
)
data class TeacherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teacherCode: String,
    val fullName: String,
    val mobile: String,
    val email: String,
    val subject: String,
    val assignedCourses: String = "JEE / NEET",
    val assignedBatches: String = "Alpha, Achievers",
    val joiningDate: String = "2025-06-01",
    val status: String = "ACTIVE"
)

@Entity(
    tableName = "attendance_sessions",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["batchId", "attendanceDate"], unique = true)]
)
data class AttendanceSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val batchId: Long,
    val attendanceDate: String,
    val subject: String = "Physics Mechanics",
    val sessionStatus: String = "COMPLETED",
    val createdBy: String = "Admin User",
    val createdAt: String = "2026-10-17"
)

@Entity(
    tableName = "attendance_records",
    foreignKeys = [
        ForeignKey(
            entity = AttendanceSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId", "studentId"], unique = true), Index(value = ["studentId"])]
)
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val studentId: Long,
    val status: String, // PRESENT, ABSENT, LATE, LEAVE
    val remarks: String = "",
    val markedBy: String = "Faculty"
)

@Entity(tableName = "attendance_change_logs")
data class AttendanceChangeLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val recordId: Long,
    val studentId: Long,
    val oldStatus: String,
    val newStatus: String,
    val oldRemarks: String? = null,
    val newRemarks: String? = null,
    val changedBy: String,
    val changeReason: String,
    val changedAt: String
)

@Entity(
    tableName = "fee_invoices",
    foreignKeys = [
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["invoiceNumber"], unique = true), Index(value = ["studentId"])]
)
data class FeeInvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val studentId: Long,
    val totalFee: Double,
    val paidAmount: Double = 0.0,
    val balanceAmount: Double,
    val dueDate: String,
    val status: String = "UNPAID" // PAID, PARTIAL, UNPAID
)

@Entity(
    tableName = "fee_payments",
    foreignKeys = [
        ForeignKey(
            entity = FeeInvoiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["invoiceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["receiptNumber"], unique = true), Index(value = ["invoiceId"]), Index(value = ["studentId"])]
)
data class FeePaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val invoiceId: Long,
    val studentId: Long,
    val amount: Double,
    val paymentDate: String,
    val paymentMethod: String, // UPI, Cash, Bank Transfer, Cheque
    val remarks: String = "Tuition installment",
    val receivedBy: String = "Administration"
)

@Entity(
    tableName = "examinations",
    foreignKeys = [
        ForeignKey(
            entity = BatchEntity::class,
            parentColumns = ["id"],
            childColumns = ["batchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["batchId"])]
)
data class ExaminationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examTitle: String,
    val batchId: Long,
    val subject: String,
    val maxMarks: Double = 100.0,
    val passingMarks: Double = 40.0,
    val examDate: String,
    val status: String = "SCHEDULED" // SCHEDULED, COMPLETED, DRAFT
)

@Entity(
    tableName = "exam_results",
    foreignKeys = [
        ForeignKey(
            entity = ExaminationEntity::class,
            parentColumns = ["id"],
            childColumns = ["examId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = StudentEntity::class,
            parentColumns = ["id"],
            childColumns = ["studentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["examId", "studentId"], unique = true), Index(value = ["studentId"])]
)
data class ExamResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val examId: Long,
    val studentId: Long,
    val marksObtained: Double,
    val grade: String = "N/A",
    val status: String, // PASS, FAIL
    val remarks: String = "",
    val submittedBy: String = "Faculty"
)

@Entity(tableName = "announcements")
data class AnnouncementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val targetAudience: String = "ALL", // ALL, STUDENTS, FACULTY
    val publishedDate: String,
    val priority: String = "NORMAL", // URGENT, NORMAL
    val createdBy: String = "Principal Office"
)

@Entity(tableName = "activity_logs")
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userName: String,
    val userRole: String,
    val action: String,
    val entityType: String,
    val entityReference: String,
    val details: String,
    val timestamp: String
)

data class UserProfile(
    val username: String = "admin",
    val fullName: String = "Principal Administrator",
    val role: String = "ADMIN", // ADMIN, TEACHER, STUDENT, PARENT
    val linkedRefId: Long? = 1
)
