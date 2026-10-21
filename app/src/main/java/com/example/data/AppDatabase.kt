package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        BatchEntity::class,
        StudentEntity::class,
        TeacherEntity::class,
        AttendanceSessionEntity::class,
        AttendanceRecordEntity::class,
        AttendanceChangeLogEntity::class,
        DailyStudentAttendanceEntity::class,
        FeeInvoiceEntity::class,
        FeePaymentEntity::class,
        ExaminationEntity::class,
        ExamResultEntity::class,
        AnnouncementEntity::class,
        ActivityLogEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun coachingDao(): CoachingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "frontcraft_coaching.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.coachingDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(dao: CoachingDao) {
            // 1. Teachers
            dao.insertTeacher(
                TeacherEntity(
                    id = 1,
                    teacherCode = "TCH-01",
                    fullName = "Prof. Ramesh Kulkarni",
                    mobile = "9820011999",
                    email = "ramesh@frontcraft.edu.in",
                    subject = "Physics",
                    assignedCourses = "IIT-JEE Advanced",
                    assignedBatches = "JEE Alpha, NEET Achievers",
                    joiningDate = "2024-05-10"
                )
            )
            dao.insertTeacher(
                TeacherEntity(
                    id = 2,
                    teacherCode = "TCH-02",
                    fullName = "Dr. Sunita Deshpande",
                    mobile = "9820011888",
                    email = "sunita@frontcraft.edu.in",
                    subject = "Biology",
                    assignedCourses = "NEET Medical Target",
                    assignedBatches = "NEET Achievers",
                    joiningDate = "2024-06-15"
                )
            )
            dao.insertTeacher(
                TeacherEntity(
                    id = 3,
                    teacherCode = "TCH-03",
                    fullName = "Er. Vikram Singhania",
                    mobile = "9820011777",
                    email = "vikram@frontcraft.edu.in",
                    subject = "Mathematics",
                    assignedCourses = "IIT-JEE & CBSE 12",
                    assignedBatches = "JEE Alpha, Board Prep",
                    joiningDate = "2025-01-10"
                )
            )

            // 2. Batches (with Subject, Capacity, Teacher, Schedule)
            dao.insertBatch(
                BatchEntity(
                    id = 1,
                    batchName = "JEE Advanced Alpha",
                    courseName = "IIT-JEE Target 2026",
                    subject = "Physics",
                    teacherId = 1,
                    teacherName = "Prof. Ramesh Kulkarni",
                    scheduleTime = "07:30 AM - 10:00 AM",
                    capacity = 30,
                    startDate = "2026-04-01"
                )
            )
            dao.insertBatch(
                BatchEntity(
                    id = 2,
                    batchName = "NEET Medical Achievers",
                    courseName = "NEET Target 2026",
                    subject = "Biology",
                    teacherId = 2,
                    teacherName = "Dr. Sunita Deshpande",
                    scheduleTime = "10:30 AM - 01:00 PM",
                    capacity = 40,
                    startDate = "2026-04-05"
                )
            )
            dao.insertBatch(
                BatchEntity(
                    id = 3,
                    batchName = "Class 12 Board Prep",
                    courseName = "CBSE Board 2026",
                    subject = "Mathematics",
                    teacherId = 3,
                    teacherName = "Er. Vikram Singhania",
                    scheduleTime = "04:00 PM - 06:30 PM",
                    capacity = 25,
                    startDate = "2026-04-10"
                )
            )

            // 3. Students
            val students = listOf(
                StudentEntity(1, "STU-101", "Aarav Mehta", "9820011223", "Sunil Mehta", "9820011220", "aarav@gmail.com", "Andheri West, Mumbai", "2026-04-05", 1, "IIT-JEE Target 2026", 95000.0),
                StudentEntity(2, "STU-102", "Diya Sengupta", "9820011224", "Anirban Sengupta", "9820011221", "diya@gmail.com", "Bandra, Mumbai", "2026-04-08", 1, "IIT-JEE Target 2026", 95000.0),
                StudentEntity(3, "STU-103", "Rohan Sharma", "9820011225", "Vikram Sharma", "9820011222", "rohan@gmail.com", "Borivali, Mumbai", "2026-04-10", 1, "IIT-JEE Target 2026", 95000.0),
                StudentEntity(4, "STU-104", "Ananya Iyer", "9820011226", "Ramachandran Iyer", "9820011227", "ananya@gmail.com", "Powai, Mumbai", "2026-04-12", 1, "IIT-JEE Target 2026", 95000.0),
                StudentEntity(5, "STU-201", "Aditya Joshi", "9830022331", "Manish Joshi", "9830022330", "aditya@gmail.com", "Thane, Mumbai", "2026-05-02", 2, "NEET Target 2026", 90000.0),
                StudentEntity(6, "STU-202", "Kavya Nair", "9830022332", "Suresh Nair", "9830022333", "kavya@gmail.com", "Mulund, Mumbai", "2026-05-03", 2, "NEET Target 2026", 90000.0),
                StudentEntity(7, "STU-203", "Ishaan Verma", "9830022334", "Deepak Verma", "9830022335", "ishaan@gmail.com", "Goregaon, Mumbai", "2026-05-05", 2, "NEET Target 2026", 90000.0),
                StudentEntity(8, "STU-204", "Sneha Kulkarni", "9830022336", "Prof. Ramesh Kulkarni", "9820011999", "sneha@gmail.com", "Dadar, Mumbai", "2026-05-06", 2, "NEET Target 2026", 90000.0),
                StudentEntity(9, "STU-301", "Tanvi Deshmukh", "9840033441", "Prakash Deshmukh", "9840033440", "tanvi@gmail.com", "Vile Parle, Mumbai", "2026-06-01", 3, "CBSE Board 2026", 60000.0),
                StudentEntity(10, "STU-302", "Siddharth Patel", "9840033442", "Bhavesh Patel", "9840033443", "sid@gmail.com", "Kandivali, Mumbai", "2026-06-02", 3, "CBSE Board 2026", 60000.0),
                StudentEntity(11, "STU-303", "Pooja Reddy", "9840033444", "Narayana Reddy", "9840033445", "pooja@gmail.com", "Sion, Mumbai", "2026-06-03", 3, "CBSE Board 2026", 60000.0),
                StudentEntity(12, "STU-304", "Varun Kapoor", "9840033446", "Rajiv Kapoor", "9840033447", "varun@gmail.com", "Colaba, Mumbai", "2026-06-04", 3, "CBSE Board 2026", 60000.0)
            )
            for (st in students) {
                dao.insertStudent(st)
                // Invoices & Initial Payments
                dao.insertInvoice(
                    FeeInvoiceEntity(
                        id = st.id,
                        invoiceNumber = "INV-2026-${1000 + st.id}",
                        studentId = st.id,
                        totalFee = st.totalCourseFee,
                        paidAmount = 25000.0,
                        balanceAmount = st.totalCourseFee - 25000.0,
                        dueDate = "2026-11-01",
                        status = "PARTIAL"
                    )
                )
                dao.insertPayment(
                    FeePaymentEntity(
                        id = st.id,
                        receiptNumber = "REC-2026-${100 + st.id}",
                        invoiceId = st.id,
                        studentId = st.id,
                        amount = 25000.0,
                        paymentDate = "2026-10-10",
                        paymentMethod = "UPI",
                        remarks = "Quarterly Admission Fee",
                        receivedBy = "Administration"
                    )
                )
            }

            // 4. Initial Attendance Session
            dao.insertAttendanceSession(
                AttendanceSessionEntity(
                    id = 1,
                    batchId = 1,
                    attendanceDate = "2026-10-17",
                    subject = "Physics Mechanics",
                    createdBy = "Prof. Ramesh Kulkarni"
                )
            )
            dao.insertAttendanceRecords(
                listOf(
                    AttendanceRecordEntity(1, 1, 1, "PRESENT", "On time", "Prof. Ramesh Kulkarni"),
                    AttendanceRecordEntity(2, 1, 2, "PRESENT", "On time", "Prof. Ramesh Kulkarni"),
                    AttendanceRecordEntity(3, 1, 3, "LATE", "Traffic delay", "Prof. Ramesh Kulkarni"),
                    AttendanceRecordEntity(4, 1, 4, "PRESENT", "On time", "Prof. Ramesh Kulkarni")
                )
            )
            dao.insertDailyAttendance(
                listOf(
                    DailyStudentAttendanceEntity(id = 1, batchId = 1, studentId = 1, attendanceDate = "2026-10-17", status = "PRESENT", remarks = "On time", markedBy = "Prof. Ramesh Kulkarni", recordedAt = "2026-10-17 08:30:00"),
                    DailyStudentAttendanceEntity(id = 2, batchId = 1, studentId = 2, attendanceDate = "2026-10-17", status = "PRESENT", remarks = "On time", markedBy = "Prof. Ramesh Kulkarni", recordedAt = "2026-10-17 08:30:00"),
                    DailyStudentAttendanceEntity(id = 3, batchId = 1, studentId = 3, attendanceDate = "2026-10-17", status = "LATE", remarks = "Traffic delay", markedBy = "Prof. Ramesh Kulkarni", recordedAt = "2026-10-17 08:30:00"),
                    DailyStudentAttendanceEntity(id = 4, batchId = 1, studentId = 4, attendanceDate = "2026-10-17", status = "PRESENT", remarks = "On time", markedBy = "Prof. Ramesh Kulkarni", recordedAt = "2026-10-17 08:30:00")
                )
            )

            // 5. Initial Examinations & Historical Results
            dao.insertExamination(
                ExaminationEntity(
                    id = 1,
                    examTitle = "Rotational Mechanics Test 1",
                    batchId = 1,
                    subject = "Physics",
                    maxMarks = 100.0,
                    passingMarks = 40.0,
                    examDate = "2026-10-14",
                    status = "COMPLETED"
                )
            )
            dao.insertExamResults(
                listOf(
                    ExamResultEntity(id = 1, examId = 1, studentId = 1, marksObtained = 92.0, grade = "A+", status = "PASS", remarks = "Grade A+ (Topper) - Excellent mechanics grasp", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 2, examId = 1, studentId = 2, marksObtained = 85.5, grade = "A", status = "PASS", remarks = "Grade A - Accurate derivations", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 3, examId = 1, studentId = 3, marksObtained = 74.0, grade = "B+", status = "PASS", remarks = "Grade B+ - Good effort, revise inertia formulas", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 4, examId = 1, studentId = 4, marksObtained = 88.0, grade = "A", status = "PASS", remarks = "Grade A - Great rotational dynamics", submittedBy = "Prof. Ramesh Kulkarni")
                )
            )

            dao.insertExamination(
                ExaminationEntity(
                    id = 2,
                    examTitle = "Kinematics & Vectors Assessment",
                    batchId = 1,
                    subject = "Physics",
                    maxMarks = 50.0,
                    passingMarks = 20.0,
                    examDate = "2026-09-28",
                    status = "COMPLETED"
                )
            )
            dao.insertExamResults(
                listOf(
                    ExamResultEntity(id = 5, examId = 2, studentId = 1, marksObtained = 47.0, grade = "A+", status = "PASS", remarks = "Grade A+ - Flawless vector algebra", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 6, examId = 2, studentId = 2, marksObtained = 43.0, grade = "A", status = "PASS", remarks = "Grade A - Swift problem solving", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 7, examId = 2, studentId = 3, marksObtained = 35.0, grade = "B+", status = "PASS", remarks = "Grade B+ - Steady progress", submittedBy = "Prof. Ramesh Kulkarni"),
                    ExamResultEntity(id = 8, examId = 2, studentId = 4, marksObtained = 45.0, grade = "A+", status = "PASS", remarks = "Grade A+ - Very neat graph interpretations", submittedBy = "Prof. Ramesh Kulkarni")
                )
            )

            dao.insertExamination(
                ExaminationEntity(
                    id = 3,
                    examTitle = "Cell Biology & Genetics Unit Test",
                    batchId = 2,
                    subject = "Biology",
                    maxMarks = 100.0,
                    passingMarks = 40.0,
                    examDate = "2026-10-10",
                    status = "COMPLETED"
                )
            )
            dao.insertExamResults(
                listOf(
                    ExamResultEntity(id = 9, examId = 3, studentId = 5, marksObtained = 94.0, grade = "A+", status = "PASS", remarks = "Grade A+ (Topper) - High taxonomy precision", submittedBy = "Dr. Sunita Deshpande"),
                    ExamResultEntity(id = 10, examId = 3, studentId = 6, marksObtained = 82.0, grade = "A", status = "PASS", remarks = "Grade A - Good cellular organelles analysis", submittedBy = "Dr. Sunita Deshpande"),
                    ExamResultEntity(id = 11, examId = 3, studentId = 7, marksObtained = 68.0, grade = "B", status = "PASS", remarks = "Grade B - Revise meiosis diagrams", submittedBy = "Dr. Sunita Deshpande"),
                    ExamResultEntity(id = 12, examId = 3, studentId = 8, marksObtained = 90.0, grade = "A+", status = "PASS", remarks = "Grade A+ - Outstanding performance", submittedBy = "Dr. Sunita Deshpande")
                )
            )

            dao.insertExamination(
                ExaminationEntity(
                    id = 4,
                    examTitle = "Human Physiology Comprehensive Mock",
                    batchId = 2,
                    subject = "Biology",
                    maxMarks = 360.0,
                    passingMarks = 144.0,
                    examDate = "2026-10-28",
                    status = "SCHEDULED"
                )
            )

            dao.insertExamination(
                ExaminationEntity(
                    id = 5,
                    examTitle = "Class 12 Calculus & Matrices Pre-Board",
                    batchId = 3,
                    subject = "Mathematics",
                    maxMarks = 80.0,
                    passingMarks = 32.0,
                    examDate = "2026-11-02",
                    status = "SCHEDULED"
                )
            )

            // 6. Announcements
            dao.insertAnnouncement(
                AnnouncementEntity(
                    id = 1,
                    title = "Mandatory JEE Mock Test 3 Attendance",
                    content = "All enrolled JEE Alpha students must report at 7:00 AM with institute ID card. Formula sheets are prohibited.",
                    targetAudience = "STUDENTS",
                    publishedDate = "2026-10-17",
                    priority = "URGENT",
                    createdBy = "Academic Dean"
                )
            )

            // 7. Initial Activity Log
            dao.insertActivityLog(
                ActivityLogEntity(
                    id = 1,
                    userName = "Administrator",
                    userRole = "ADMIN",
                    action = "SYSTEM_INITIALIZED",
                    entityType = "System",
                    entityReference = "FrontCraft PRO",
                    details = "Institution academic year 2026-2027 configured with batches, faculty, and student directories",
                    timestamp = "2026-10-17 08:00:00"
                )
            )
        }
    }
}
