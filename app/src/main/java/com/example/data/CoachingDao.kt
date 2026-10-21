package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachingDao {

    // --- Batches ---
    @Query("SELECT * FROM batches ORDER BY id ASC")
    fun getAllBatches(): Flow<List<BatchEntity>>

    @Query("SELECT * FROM batches WHERE id = :id")
    suspend fun getBatchById(id: Long): BatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBatch(batch: BatchEntity): Long

    @Update
    suspend fun updateBatch(batch: BatchEntity)

    @Query("DELETE FROM batches WHERE id = :id")
    suspend fun deleteBatchById(id: Long)

    // --- Students ---
    @Query("SELECT * FROM students WHERE status != 'ARCHIVED' ORDER BY fullName ASC")
    fun getAllStudents(): Flow<List<StudentEntity>>

    @Query("SELECT * FROM students WHERE assignedBatchId = :batchId AND status != 'ARCHIVED'")
    fun getStudentsByBatch(batchId: Long): Flow<List<StudentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentEntity): Long

    @Update
    suspend fun updateStudent(student: StudentEntity)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    // --- Teachers ---
    @Query("SELECT * FROM teachers ORDER BY fullName ASC")
    fun getAllTeachers(): Flow<List<TeacherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTeacher(teacher: TeacherEntity): Long

    @Update
    suspend fun updateTeacher(teacher: TeacherEntity)

    @Query("DELETE FROM teachers WHERE id = :id")
    suspend fun deleteTeacherById(id: Long)

    // --- Attendance Sessions & Records ---
    @Query("SELECT * FROM attendance_sessions ORDER BY attendanceDate DESC")
    fun getAllAttendanceSessions(): Flow<List<AttendanceSessionEntity>>

    @Query("SELECT * FROM attendance_sessions WHERE batchId = :batchId AND attendanceDate = :date LIMIT 1")
    suspend fun getSessionByBatchAndDate(batchId: Long, date: String): AttendanceSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceSession(session: AttendanceSessionEntity): Long

    @Query("SELECT * FROM attendance_records")
    fun getAllAttendanceRecords(): Flow<List<AttendanceRecordEntity>>

    @Query("SELECT * FROM attendance_records WHERE sessionId = :sessionId")
    fun getRecordsBySession(sessionId: Long): Flow<List<AttendanceRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecordEntity>)

    @Query("DELETE FROM attendance_records WHERE sessionId = :sessionId")
    suspend fun deleteRecordsBySessionId(sessionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChangeLog(log: AttendanceChangeLogEntity)

    // --- Daily Student Attendance ---
    @Query("SELECT * FROM daily_student_attendance WHERE batchId = :batchId AND attendanceDate = :date")
    fun getDailyAttendanceByBatchAndDate(batchId: Long, date: String): Flow<List<DailyStudentAttendanceEntity>>

    @Query("SELECT * FROM daily_student_attendance WHERE studentId = :studentId ORDER BY attendanceDate DESC")
    fun getDailyAttendanceByStudent(studentId: Long): Flow<List<DailyStudentAttendanceEntity>>

    @Query("SELECT * FROM daily_student_attendance ORDER BY attendanceDate DESC")
    fun getAllDailyAttendance(): Flow<List<DailyStudentAttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyAttendance(records: List<DailyStudentAttendanceEntity>)

    @Query("DELETE FROM daily_student_attendance WHERE batchId = :batchId AND attendanceDate = :date")
    suspend fun deleteDailyAttendanceByBatchAndDate(batchId: Long, date: String)

    // --- Fee Invoices & Payments ---
    @Query("SELECT * FROM fee_invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<FeeInvoiceEntity>>

    @Query("SELECT * FROM fee_invoices WHERE studentId = :studentId ORDER BY id DESC")
    fun getInvoicesByStudent(studentId: Long): Flow<List<FeeInvoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: FeeInvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: FeeInvoiceEntity)

    @Query("SELECT * FROM fee_payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<FeePaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: FeePaymentEntity): Long

    // --- Examinations ---
    @Query("SELECT * FROM examinations ORDER BY examDate DESC")
    fun getAllExaminations(): Flow<List<ExaminationEntity>>

    @Query("SELECT * FROM examinations WHERE batchId = :batchId ORDER BY examDate DESC")
    fun getExaminationsByBatch(batchId: Long): Flow<List<ExaminationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamination(exam: ExaminationEntity): Long

    @Update
    suspend fun updateExamination(exam: ExaminationEntity)

    @Query("DELETE FROM examinations WHERE id = :id")
    suspend fun deleteExaminationById(id: Long)

    @Query("DELETE FROM exam_results WHERE examId = :examId")
    suspend fun deleteResultsByExamId(examId: Long)

    @Query("SELECT * FROM exam_results WHERE examId = :examId")
    fun getResultsByExam(examId: Long): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results")
    fun getAllExamResults(): Flow<List<ExamResultEntity>>

    @Query("SELECT * FROM exam_results WHERE studentId = :studentId")
    fun getResultsByStudent(studentId: Long): Flow<List<ExamResultEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamResults(results: List<ExamResultEntity>)

    // --- Announcements ---
    @Query("SELECT * FROM announcements ORDER BY id DESC")
    fun getAllAnnouncements(): Flow<List<AnnouncementEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long

    // --- Activity Logs ---
    @Query("SELECT * FROM activity_logs ORDER BY id DESC LIMIT 50")
    fun getRecentActivityLogs(): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityLog(log: ActivityLogEntity)
}
