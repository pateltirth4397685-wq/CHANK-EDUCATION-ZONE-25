package com.example.data

import kotlinx.coroutines.flow.Flow

class Repository(private val dao: CoachingDao) {

    // Batches
    val allBatches: Flow<List<BatchEntity>> = dao.getAllBatches()
    suspend fun getBatchById(id: Long): BatchEntity? = dao.getBatchById(id)
    suspend fun insertBatch(batch: BatchEntity): Long = dao.insertBatch(batch)
    suspend fun updateBatch(batch: BatchEntity) = dao.updateBatch(batch)
    suspend fun deleteBatch(id: Long) = dao.deleteBatchById(id)

    // Students
    val allStudents: Flow<List<StudentEntity>> = dao.getAllStudents()
    fun getStudentsByBatch(batchId: Long): Flow<List<StudentEntity>> = dao.getStudentsByBatch(batchId)
    suspend fun insertStudent(student: StudentEntity): Long = dao.insertStudent(student)
    suspend fun updateStudent(student: StudentEntity) = dao.updateStudent(student)
    suspend fun deleteStudent(id: Long) = dao.deleteStudentById(id)

    // Teachers
    val allTeachers: Flow<List<TeacherEntity>> = dao.getAllTeachers()
    suspend fun insertTeacher(teacher: TeacherEntity): Long = dao.insertTeacher(teacher)
    suspend fun updateTeacher(teacher: TeacherEntity) = dao.updateTeacher(teacher)
    suspend fun deleteTeacher(id: Long) = dao.deleteTeacherById(id)

    // Attendance
    val allAttendanceSessions: Flow<List<AttendanceSessionEntity>> = dao.getAllAttendanceSessions()
    suspend fun getSessionByBatchAndDate(batchId: Long, date: String) = dao.getSessionByBatchAndDate(batchId, date)
    suspend fun insertAttendanceSession(session: AttendanceSessionEntity): Long = dao.insertAttendanceSession(session)
    val allAttendanceRecords: Flow<List<AttendanceRecordEntity>> = dao.getAllAttendanceRecords()
    fun getRecordsBySession(sessionId: Long): Flow<List<AttendanceRecordEntity>> = dao.getRecordsBySession(sessionId)
    suspend fun insertAttendanceRecords(records: List<AttendanceRecordEntity>) = dao.insertAttendanceRecords(records)
    suspend fun deleteRecordsBySessionId(sessionId: Long) = dao.deleteRecordsBySessionId(sessionId)
    suspend fun insertChangeLog(log: AttendanceChangeLogEntity) = dao.insertChangeLog(log)

    // Fees
    val allInvoices: Flow<List<FeeInvoiceEntity>> = dao.getAllInvoices()
    fun getInvoicesByStudent(studentId: Long): Flow<List<FeeInvoiceEntity>> = dao.getInvoicesByStudent(studentId)
    suspend fun insertInvoice(invoice: FeeInvoiceEntity): Long = dao.insertInvoice(invoice)
    suspend fun updateInvoice(invoice: FeeInvoiceEntity) = dao.updateInvoice(invoice)
    val allPayments: Flow<List<FeePaymentEntity>> = dao.getAllPayments()
    suspend fun insertPayment(payment: FeePaymentEntity): Long = dao.insertPayment(payment)

    // Examinations
    val allExaminations: Flow<List<ExaminationEntity>> = dao.getAllExaminations()
    fun getExaminationsByBatch(batchId: Long): Flow<List<ExaminationEntity>> = dao.getExaminationsByBatch(batchId)
    suspend fun insertExamination(exam: ExaminationEntity): Long = dao.insertExamination(exam)
    suspend fun updateExamination(exam: ExaminationEntity) = dao.updateExamination(exam)
    suspend fun deleteExamination(examId: Long) {
        dao.deleteResultsByExamId(examId)
        dao.deleteExaminationById(examId)
    }
    val allExamResults: Flow<List<ExamResultEntity>> = dao.getAllExamResults()
    fun getResultsByExam(examId: Long): Flow<List<ExamResultEntity>> = dao.getResultsByExam(examId)
    fun getResultsByStudent(studentId: Long): Flow<List<ExamResultEntity>> = dao.getResultsByStudent(studentId)
    suspend fun insertExamResults(results: List<ExamResultEntity>) = dao.insertExamResults(results)
    suspend fun deleteResultsByExamId(examId: Long) = dao.deleteResultsByExamId(examId)

    // Announcements & Logs
    val allAnnouncements: Flow<List<AnnouncementEntity>> = dao.getAllAnnouncements()
    suspend fun insertAnnouncement(announcement: AnnouncementEntity): Long = dao.insertAnnouncement(announcement)
    val recentActivityLogs: Flow<List<ActivityLogEntity>> = dao.getRecentActivityLogs()
    suspend fun insertActivityLog(log: ActivityLogEntity) = dao.insertActivityLog(log)
}
