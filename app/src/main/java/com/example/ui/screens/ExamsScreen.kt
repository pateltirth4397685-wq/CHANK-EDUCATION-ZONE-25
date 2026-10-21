package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatchEntity
import com.example.data.ExaminationEntity
import com.example.data.ExamResultEntity
import com.example.data.StudentEntity
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allExaminations by viewModel.allExaminations.collectAsState()
    val allExamResults by viewModel.allExamResults.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Exams & Tests", "Input Scores", "Performance Summaries")

    // State for selected exam in Input Scores tab
    var inputScoresSelectedExamId by remember { mutableStateOf<Long?>(null) }

    // Dialog states
    var showScheduleDialog by remember { mutableStateOf(false) }
    var examToEnterMarksDialog by remember { mutableStateOf<ExaminationEntity?>(null) }
    var examForScoreboard by remember { mutableStateOf<ExaminationEntity?>(null) }
    var examToDelete by remember { mutableStateOf<ExaminationEntity?>(null) }
    var summaryReportDialogText by remember { mutableStateOf<String?>(null) }

    // Metrics for overview
    val totalExamsCount = allExaminations.size
    val completedExamsCount = allExaminations.count { it.status == "COMPLETED" }
    val scheduledExamsCount = allExaminations.count { it.status == "SCHEDULED" }
    val totalScoresRecorded = allExamResults.size
    val totalPassingScores = allExamResults.count { it.status == "PASS" }
    val overallPassRate = if (totalScoresRecorded > 0) {
        ((totalPassingScores.toDouble() / totalScoresRecorded) * 100).toInt()
    } else 0

    Scaffold(
        floatingActionButton = {
            if ((currentUser.role == "ADMIN" || currentUser.role == "TEACHER") && selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showScheduleDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyPrimary,
                    modifier = Modifier.testTag("schedule_exam_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Schedule Exam")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Main Top Bar
            Surface(
                color = NavyPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exams & Academic Performance",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Track examinations, input student scores & generate performance summaries",
                                fontSize = 11.sp,
                                color = GoldAccent
                            )
                        }

                        if (currentUser.role == "ADMIN" || currentUser.role == "TEACHER") {
                            Button(
                                onClick = { showScheduleDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("schedule_exam_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Test", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Sub-Navigation Tabs
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = NavySecondary,
                        contentColor = Color.White,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = GoldAccent,
                                height = 3.dp
                            )
                        }
                    ) {
                        tabTitles.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = title,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = if (selectedTabIndex == index) GoldAccent else Color.White.copy(alpha = 0.8f)
                                    )
                                },
                                modifier = Modifier.testTag("exam_tab_$index")
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // TAB 1: Exams & Tests Overview
                    ExamsOverviewTabContent(
                        allExaminations = allExaminations,
                        allExamResults = allExamResults,
                        allBatches = allBatches,
                        allStudents = allStudents,
                        currentUserRole = currentUser.role,
                        totalExamsCount = totalExamsCount,
                        completedExamsCount = completedExamsCount,
                        scheduledExamsCount = scheduledExamsCount,
                        overallPassRate = overallPassRate,
                        onEnterScores = { exam ->
                            // Switch to Input Scores tab with this exam pre-selected
                            inputScoresSelectedExamId = exam.id
                            selectedTabIndex = 1
                        },
                        onViewScoreboard = { exam -> examForScoreboard = exam },
                        onDeleteExam = { exam -> examToDelete = exam },
                        onScheduleClick = { showScheduleDialog = true }
                    )
                }

                1 -> {
                    // TAB 2: Dedicated Screen to Input Scores
                    InputScoresWorkspaceTabContent(
                        allExaminations = allExaminations,
                        allExamResults = allExamResults,
                        allBatches = allBatches,
                        allStudents = allStudents,
                        initialExamId = inputScoresSelectedExamId,
                        viewModel = viewModel,
                        onScoresSaved = { examTitle ->
                            Toast.makeText(context, "Scores & grades recorded for '$examTitle'!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                2 -> {
                    // TAB 3: Academic Performance Summaries
                    AcademicPerformanceSummariesTabContent(
                        allExaminations = allExaminations,
                        allExamResults = allExamResults,
                        allBatches = allBatches,
                        allStudents = allStudents,
                        viewModel = viewModel,
                        onGenerateReport = { reportText ->
                            summaryReportDialogText = reportText
                        }
                    )
                }
            }
        }
    }

    // Modal: Schedule Exam Dialog
    if (showScheduleDialog) {
        var selectedBatchId by remember { mutableStateOf<Long?>(allBatches.firstOrNull()?.id ?: 1L) }
        var subject by remember { mutableStateOf(allBatches.firstOrNull()?.subject ?: "Physics") }
        var examDate by remember { mutableStateOf("2026-11-05") }
        var maxMarksStr by remember { mutableStateOf("100") }
        var passMarksStr by remember { mutableStateOf("40") }
        var examTitle by remember { mutableStateOf("$subject Unit Assessment - 2026-11-05") }
        var batchDropdownOpen by remember { mutableStateOf(false) }

        val selBatch = allBatches.find { it.id == selectedBatchId }

        AlertDialog(
            onDismissRequest = { showScheduleDialog = false },
            title = { Text("Schedule New Examination", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Define test parameters and link to target cohort batch.", fontSize = 11.sp, color = Color.Gray)

                    // Target Cohort Dropdown
                    ExposedDropdownMenuBox(
                        expanded = batchDropdownOpen,
                        onExpandedChange = { batchDropdownOpen = !batchDropdownOpen }
                    ) {
                        OutlinedTextField(
                            value = selBatch?.let { "${it.batchName} (${it.subject})" } ?: "Select Cohort",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Batch *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = batchDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = batchDropdownOpen,
                            onDismissRequest = { batchDropdownOpen = false }
                        ) {
                            allBatches.forEach { b ->
                                androidx.compose.material3.DropdownMenuItem(
                                    text = { Text("${b.batchName} • ${b.courseName} (${b.subject})") },
                                    onClick = {
                                        selectedBatchId = b.id
                                        subject = b.subject
                                        examTitle = "${b.subject} Unit Test - $examDate"
                                        batchDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = examTitle,
                        onValueChange = { examTitle = it },
                        label = { Text("Exam Title *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = subject,
                        onValueChange = { subject = it },
                        label = { Text("Subject *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = examDate,
                        onValueChange = { examDate = it },
                        label = { Text("Exam Date (YYYY-MM-DD) *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = maxMarksStr,
                            onValueChange = {
                                maxMarksStr = it
                                val m = it.toDoubleOrNull() ?: 100.0
                                passMarksStr = (m * 0.4).toInt().toString()
                            },
                            label = { Text("Max Marks *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = passMarksStr,
                            onValueChange = { passMarksStr = it },
                            label = { Text("Pass Marks *") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val maxM = maxMarksStr.toDoubleOrNull() ?: 100.0
                        val passM = passMarksStr.toDoubleOrNull() ?: 40.0
                        if (selectedBatchId != null && subject.isNotBlank() && examDate.isNotBlank()) {
                            viewModel.scheduleExam(
                                title = examTitle.ifBlank { "$subject Assessment - $examDate" },
                                batchId = selectedBatchId!!,
                                subject = subject.trim(),
                                maxMarks = maxM,
                                passingMarks = passM,
                                date = examDate.trim()
                            )
                            showScheduleDialog = false
                            Toast.makeText(context, "Examination scheduled successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Text("Schedule Exam", color = NavyPrimary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showScheduleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Scoreboard Dialog
    examForScoreboard?.let { exam ->
        val batch = allBatches.find { it.id == exam.batchId }
        val examResults = allExamResults.filter { it.examId == exam.id }

        ExamResultsScoreboardDialog(
            exam = exam,
            batch = batch,
            results = examResults,
            allStudents = allStudents,
            viewModel = viewModel,
            onDismiss = { examForScoreboard = null },
            onEditScores = {
                examForScoreboard = null
                inputScoresSelectedExamId = exam.id
                selectedTabIndex = 1
            }
        )
    }

    // Modal: Delete Exam Confirmation
    examToDelete?.let { exam ->
        AlertDialog(
            onDismissRequest = { examToDelete = null },
            title = { Text("Delete Examination", fontWeight = FontWeight.Bold, color = Color(0xFFDC2626)) },
            text = {
                Text("Are you sure you want to delete '${exam.examTitle}'? All associated student marks will be permanently removed.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteExam(exam.id, exam.examTitle)
                        examToDelete = null
                        Toast.makeText(context, "Exam deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete Exam", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { examToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Official Academic Performance Summary Report Viewer
    summaryReportDialogText?.let { report ->
        AlertDialog(
            onDismissRequest = { summaryReportDialogText = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Academic Performance Summary", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 16.sp)
                }
            },
            text = {
                Surface(
                    color = Color(0xFFF8FAFC),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = report,
                                fontSize = 11.sp,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        clipboardManager.setText(AnnotatedString(report))
                        Toast.makeText(context, "Academic summary copied to clipboard!", Toast.LENGTH_SHORT).show()
                        summaryReportDialogText = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Transcript", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { summaryReportDialogText = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * TAB 1: Exams Overview List
 */
@Composable
private fun ExamsOverviewTabContent(
    allExaminations: List<ExaminationEntity>,
    allExamResults: List<ExamResultEntity>,
    allBatches: List<BatchEntity>,
    allStudents: List<StudentEntity>,
    currentUserRole: String,
    totalExamsCount: Int,
    completedExamsCount: Int,
    scheduledExamsCount: Int,
    overallPassRate: Int,
    onEnterScores: (ExaminationEntity) -> Unit,
    onViewScoreboard: (ExaminationEntity) -> Unit,
    onDeleteExam: (ExaminationEntity) -> Unit,
    onScheduleClick: () -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") } // ALL, COMPLETED, SCHEDULED
    var selectedBatchFilter by remember { mutableStateOf<Long?>(null) }

    val filteredExams = allExaminations.filter { exam ->
        val matchesStatus = when (statusFilter) {
            "COMPLETED" -> exam.status == "COMPLETED"
            "SCHEDULED" -> exam.status == "SCHEDULED"
            else -> true
        }
        val matchesBatch = selectedBatchFilter == null || exam.batchId == selectedBatchFilter
        matchesStatus && matchesBatch
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Analytics Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Total Tests",
                    value = "$totalExamsCount",
                    icon = Icons.Default.MenuBook,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "$completedExamsCount Evaluated"
                )
                StatsCard(
                    title = "Upcoming",
                    value = "$scheduledExamsCount",
                    icon = Icons.Default.CalendarToday,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Scheduled"
                )
                StatsCard(
                    title = "Overall Pass Rate",
                    value = "$overallPassRate%",
                    icon = Icons.Default.Grade,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "${allExamResults.size} Submissions"
                )
            }
        }

        // Filters: Status & Cohort
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Status Filter Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL" to "All Tests", "SCHEDULED" to "Upcoming", "COMPLETED" to "Evaluated").forEach { (key, label) ->
                        val isSelected = statusFilter == key
                        Surface(
                            color = if (isSelected) NavyPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .clickable { statusFilter = key }
                                .testTag("filter_exam_status_$key")
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) Color.White else NavyPrimary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Batch Filter Carousel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            color = if (selectedBatchFilter == null) GoldAccent else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { selectedBatchFilter = null }
                        ) {
                            Text(
                                text = "All Batches",
                                color = if (selectedBatchFilter == null) NavyPrimary else Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                    items(allBatches, key = { it.id }) { b ->
                        val isSel = selectedBatchFilter == b.id
                        Surface(
                            color = if (isSel) NavyPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.clickable { selectedBatchFilter = b.id }
                        ) {
                            Text(
                                text = b.batchName,
                                color = if (isSel) Color.White else NavyPrimary,
                                fontSize = 10.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }
        }

        // Exam Cards List
        if (filteredExams.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Examinations Found", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 14.sp)
                        Text("Schedule a test for any active batch to track scores and academic growth.", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        if (currentUserRole == "ADMIN" || currentUserRole == "TEACHER") {
                            Button(
                                onClick = onScheduleClick,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                            ) {
                                Text("Schedule First Exam", color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            items(filteredExams, key = { it.id }) { exam ->
                val batch = allBatches.find { it.id == exam.batchId }
                val resultsForExam = allExamResults.filter { it.examId == exam.id }
                val hasResults = resultsForExam.isNotEmpty()
                val gradedCount = resultsForExam.size
                val passCount = resultsForExam.count { it.status == "PASS" }
                val passRate = if (gradedCount > 0) ((passCount.toDouble() / gradedCount) * 100).toInt() else 0
                val avgScore = if (gradedCount > 0) resultsForExam.map { it.marksObtained }.average() else 0.0
                val avgPct = if (exam.maxMarks > 0) ((avgScore / exam.maxMarks) * 100).toInt() else 0
                val highestScore = resultsForExam.maxOfOrNull { it.marksObtained } ?: 0.0
                val topperResult = resultsForExam.maxByOrNull { it.marksObtained }
                val topperStudent = allStudents.find { it.id == topperResult?.studentId }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exam_card_${exam.id}"),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title & Status Badge
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exam.examTitle,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                                Text(
                                    text = "Cohort: ${batch?.batchName ?: "Batch"} • ${batch?.courseName ?: "Course"}",
                                    fontSize = 11.sp,
                                    color = GoldAccent,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            StatusBadge(status = exam.status)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Test Specifications
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Date: ${exam.examDate} • Subject: ${exam.subject}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                                Text(
                                    text = "Max: ${exam.maxMarks.toInt()} | Pass: ${exam.passingMarks.toInt()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NavyPrimary
                                )
                            }
                        }

                        // Evaluated Performance Preview (if graded)
                        if (hasResults) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$gradedCount Students Graded",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF15803D)
                                            )
                                        }
                                        Text(
                                            text = "Class Avg: ${String.format(Locale.getDefault(), "%.1f", avgScore)} ($avgPct%)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Topper: ${topperStudent?.fullName ?: "Student"} (${highestScore.toInt()}/${exam.maxMarks.toInt()})",
                                            fontSize = 10.sp,
                                            color = Color(0xFF166534)
                                        )
                                        Text(
                                            text = "Pass Rate: $passRate%",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (passRate >= 75) Color(0xFF16A34A) else BurgundyAccent
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (hasResults) {
                                    Button(
                                        onClick = { onViewScoreboard(exam) },
                                        colors = ButtonDefaults.buttonColors(containerColor = NavyPrimary),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("view_scoreboard_${exam.id}")
                                    ) {
                                        Icon(Icons.Default.Leaderboard, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Scoreboard", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }

                                    OutlinedButton(
                                        onClick = { onEnterScores(exam) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("edit_scores_${exam.id}")
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit Scores", fontSize = 11.sp)
                                    }
                                } else {
                                    Button(
                                        onClick = { onEnterScores(exam) },
                                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("record_scores_${exam.id}")
                                    ) {
                                        Icon(Icons.Default.Grade, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Input Scores & Grade", color = NavyPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            if (currentUserRole == "ADMIN") {
                                IconButton(
                                    onClick = { onDeleteExam(exam) },
                                    modifier = Modifier.testTag("delete_exam_${exam.id}")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Exam", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * TAB 2: Dedicated Screen to Input Scores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InputScoresWorkspaceTabContent(
    allExaminations: List<ExaminationEntity>,
    allExamResults: List<ExamResultEntity>,
    allBatches: List<BatchEntity>,
    allStudents: List<StudentEntity>,
    initialExamId: Long?,
    viewModel: FrontCraftViewModel,
    onScoresSaved: (String) -> Unit
) {
    var selectedExamId by remember {
        mutableStateOf<Long?>(initialExamId ?: allExaminations.firstOrNull()?.id)
    }
    var examDropdownOpen by remember { mutableStateOf(false) }

    val activeExam = allExaminations.find { it.id == selectedExamId }
    val activeBatch = allBatches.find { it.id == activeExam?.batchId }
    val enrolledStudents = allStudents.filter { it.assignedBatchId == activeExam?.batchId }
    val existingResults = allExamResults.filter { it.examId == selectedExamId }

    val scoreInputs = remember(selectedExamId, existingResults) {
        mutableStateMapOf<Long, String>().apply {
            enrolledStudents.forEach { s ->
                val existing = existingResults.find { it.studentId == s.id }?.marksObtained
                put(s.id, existing?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
            }
        }
    }

    val remarksInputs = remember(selectedExamId, existingResults) {
        mutableStateMapOf<Long, String>().apply {
            enrolledStudents.forEach { s ->
                val existing = existingResults.find { it.studentId == s.id }?.remarks
                put(s.id, existing ?: "")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Exam Selector Header
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Score Entry Workspace",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Select an examination to input marks, assign letter grades, and publish results to Room DB.",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dropdown for Exam Selection
                    ExposedDropdownMenuBox(
                        expanded = examDropdownOpen,
                        onExpandedChange = { examDropdownOpen = !examDropdownOpen }
                    ) {
                        OutlinedTextField(
                            value = activeExam?.let { "${it.examTitle} (${it.subject} • Max: ${it.maxMarks.toInt()})" } ?: "Select Examination",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Select Target Examination") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = examDropdownOpen) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )

                        ExposedDropdownMenu(
                            expanded = examDropdownOpen,
                            onDismissRequest = { examDropdownOpen = false }
                        ) {
                            allExaminations.forEach { ex ->
                                val b = allBatches.find { it.id == ex.batchId }
                                androidx.compose.material3.DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(ex.examTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${b?.batchName ?: "Batch"} • ${ex.subject} • ${ex.examDate} [${ex.status}]", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedExamId = ex.id
                                        examDropdownOpen = false
                                    }
                                )
                            }
                        }
                    }

                    if (activeExam != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Cohort: ${activeBatch?.batchName ?: "Batch"} • Students Enrolled: ${enrolledStudents.size}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = NavyPrimary
                            )
                            Surface(
                                color = if (activeExam.status == "COMPLETED") Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (activeExam.status == "COMPLETED") "Status: EVALUATED" else "Status: PENDING MARKS",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeExam.status == "COMPLETED") Color(0xFF15803D) else Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (activeExam != null) {
            // Live Summary & Quick-Fill Actions
            val gradedCount = scoreInputs.values.count { it.toDoubleOrNull() != null }
            val enteredScores = scoreInputs.mapNotNull { it.value.toDoubleOrNull() }
            val classAvg = if (enteredScores.isNotEmpty()) enteredScores.average() else 0.0
            val passingCount = enteredScores.count { it >= activeExam.passingMarks }

            item {
                Surface(
                    color = Color(0xFFEEF2FF),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Graded: $gradedCount / ${enrolledStudents.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF3730A3))
                            Text("Class Avg: ${String.format(Locale.getDefault(), "%.1f", classAvg)} / ${activeExam.maxMarks.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                            Text("Passing: $passingCount", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF15803D))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick-Fill Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    enrolledStudents.forEach { s ->
                                        scoreInputs[s.id] = activeExam.maxMarks.toInt().toString()
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("Fill Max (${activeExam.maxMarks.toInt()})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    enrolledStudents.forEach { s ->
                                        scoreInputs[s.id] = activeExam.passingMarks.toInt().toString()
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("Fill Pass (${activeExam.passingMarks.toInt()})", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    enrolledStudents.forEach { s ->
                                        scoreInputs[s.id] = ""
                                    }
                                },
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                            ) {
                                Text("Clear Marks", fontSize = 10.sp, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }
            }

            // Student Score Input Rows
            if (enrolledStudents.isEmpty()) {
                item {
                    Text(
                        text = "No students are currently allocated to '${activeBatch?.batchName}'. Assign students via the Students section.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 12.dp)
                    )
                }
            } else {
                items(enrolledStudents, key = { it.id }) { student ->
                    val scoreStr = scoreInputs[student.id] ?: ""
                    val markVal = scoreStr.toDoubleOrNull()
                    val isValid = markVal != null && markVal in 0.0..activeExam.maxMarks
                    val calculatedGrade = if (isValid) viewModel.calculateGrade(markVal!!, activeExam.maxMarks) else "--"
                    val isPassing = isValid && markVal!! >= activeExam.passingMarks

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("score_input_card_${student.id}"),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(NavySecondary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = student.fullName.take(2).uppercase(),
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(student.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                        Text(student.studentCode, fontSize = 10.sp, color = Color.Gray)
                                    }
                                }

                                // Live Grade & Status Badges
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = when {
                                            calculatedGrade.startsWith("A") -> Color(0xFFDCFCE7)
                                            calculatedGrade.startsWith("B") -> Color(0xFFE0E7FF)
                                            calculatedGrade.startsWith("C") || calculatedGrade.startsWith("D") -> Color(0xFFFEF3C7)
                                            calculatedGrade == "F" -> Color(0xFFFEE2E2)
                                            else -> Color(0xFFF1F5F9)
                                        },
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "Grade $calculatedGrade",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                calculatedGrade.startsWith("A") -> Color(0xFF15803D)
                                                calculatedGrade.startsWith("B") -> Color(0xFF3730A3)
                                                calculatedGrade.startsWith("C") || calculatedGrade.startsWith("D") -> Color(0xFFB45309)
                                                calculatedGrade == "F" -> Color(0xFFB91C1C)
                                                else -> Color.Gray
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (isValid) {
                                        Surface(
                                            color = if (isPassing) Color(0xFF16A34A) else Color(0xFFDC2626),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = if (isPassing) "PASS" else "FAIL",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Inputs Row: Marks and Optional Faculty Remark
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = scoreStr,
                                    onValueChange = { newVal ->
                                        if (newVal.isEmpty() || newVal.matches(Regex("""^\d*\.?\d*$"""))) {
                                            scoreInputs[student.id] = newVal
                                        }
                                    },
                                    label = { Text("Score (/ ${activeExam.maxMarks.toInt()})") },
                                    isError = scoreStr.isNotBlank() && (markVal == null || markVal !in 0.0..activeExam.maxMarks),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("score_field_${student.id}"),
                                    singleLine = true
                                )

                                OutlinedTextField(
                                    value = remarksInputs[student.id] ?: "",
                                    onValueChange = { remarksInputs[student.id] = it },
                                    label = { Text("Teacher Note (Optional)") },
                                    modifier = Modifier.weight(1.4f),
                                    singleLine = true
                                )
                            }
                        }
                    }
                }

                // Submit Button
                item {
                    Button(
                        onClick = {
                            val finalScores = mutableMapOf<Long, Double>()
                            val finalRemarks = mutableMapOf<Long, String>()

                            scoreInputs.forEach { (sid, str) ->
                                val m = str.toDoubleOrNull()
                                if (m != null && m in 0.0..activeExam.maxMarks) {
                                    finalScores[sid] = m
                                    val r = remarksInputs[sid]?.trim()
                                    if (!r.isNullOrBlank()) {
                                        finalRemarks[sid] = r
                                    }
                                }
                            }

                            if (finalScores.isNotEmpty()) {
                                viewModel.recordExamScores(
                                    examId = activeExam.id,
                                    scores = finalScores,
                                    remarksMap = finalRemarks
                                )
                                onScoresSaved(activeExam.examTitle)
                            }
                        },
                        enabled = scoreInputs.values.any { it.toDoubleOrNull() != null },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_and_publish_scores_button")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NavyPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Publish Exam Scores to Database", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/**
 * TAB 3: Academic Performance Summaries Generator
 */
@Composable
private fun AcademicPerformanceSummariesTabContent(
    allExaminations: List<ExaminationEntity>,
    allExamResults: List<ExamResultEntity>,
    allBatches: List<BatchEntity>,
    allStudents: List<StudentEntity>,
    viewModel: FrontCraftViewModel,
    onGenerateReport: (String) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var selectedBatchId by remember { mutableStateOf<Long?>(null) } // null = All Batches

    val targetExams = if (selectedBatchId == null) {
        allExaminations
    } else {
        allExaminations.filter { it.batchId == selectedBatchId }
    }

    val targetExamIds = targetExams.map { it.id }.toSet()
    val targetResults = allExamResults.filter { targetExamIds.contains(it.examId) }
    val targetStudents = if (selectedBatchId == null) {
        allStudents
    } else {
        allStudents.filter { it.assignedBatchId == selectedBatchId }
    }

    // Analytics calculations
    val totalSubmissions = targetResults.size
    val totalPassed = targetResults.count { it.status == "PASS" }
    val passRate = if (totalSubmissions > 0) ((totalPassed.toDouble() / totalSubmissions) * 100).toInt() else 0

    val totalMaxPossible = targetResults.mapNotNull { res ->
        targetExams.find { it.id == res.examId }?.maxMarks
    }.sum()
    val totalObtained = targetResults.sumOf { it.marksObtained }
    val cohortAvgPct = if (totalMaxPossible > 0) ((totalObtained / totalMaxPossible) * 100).toInt() else 0
    val overallGrade = viewModel.calculateGrade(totalObtained, totalMaxPossible)

    // Grade distribution counts
    val gradeCounts = mutableMapOf("A+" to 0, "A" to 0, "B+" to 0, "B" to 0, "C" to 0, "D" to 0, "F" to 0)
    targetResults.forEach { res ->
        val ex = targetExams.find { it.id == res.examId }
        val grade = if (ex != null) viewModel.calculateGrade(res.marksObtained, ex.maxMarks) else res.grade
        val bucket = when {
            grade == "A+" -> "A+"
            grade == "A" -> "A"
            grade == "B+" -> "B+"
            grade == "B" -> "B"
            grade == "C" -> "C"
            grade == "D" -> "D"
            else -> "F"
        }
        gradeCounts[bucket] = (gradeCounts[bucket] ?: 0) + 1
    }

    // Student Merit Rankings across evaluated exams
    val studentPerformanceList = targetStudents.mapNotNull { st ->
        val sResults = targetResults.filter { it.studentId == st.id }
        if (sResults.isEmpty()) null
        else {
            val sMax = sResults.mapNotNull { r -> targetExams.find { it.id == r.examId }?.maxMarks }.sum()
            val sScore = sResults.sumOf { it.marksObtained }
            val sPct = if (sMax > 0) ((sScore / sMax) * 100).toInt() else 0
            val sGrade = viewModel.calculateGrade(sScore, sMax)
            Triple(st, sPct, sGrade)
        }
    }.sortedByDescending { it.second }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Cohort Selector
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Institutional Academic Summary", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = NavyPrimary)
                            Text("Aggregated performance intelligence, grade distribution & merit analysis", fontSize = 11.sp, color = Color.Gray)
                        }

                        Button(
                            onClick = {
                                val report = buildAcademicSummaryReport(
                                    selectedBatchName = allBatches.find { it.id == selectedBatchId }?.batchName ?: "All Institute Batches",
                                    totalExams = targetExams.size,
                                    totalSubmissions = totalSubmissions,
                                    cohortAvgPct = cohortAvgPct,
                                    overallGrade = overallGrade,
                                    passRate = passRate,
                                    gradeCounts = gradeCounts,
                                    rankings = studentPerformanceList
                                )
                                onGenerateReport(report)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("generate_academic_summary_button")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Generate Report", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cohort Pills
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            Surface(
                                color = if (selectedBatchId == null) NavyPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { selectedBatchId = null }
                            ) {
                                Text(
                                    text = "All Cohorts Combined",
                                    color = if (selectedBatchId == null) Color.White else NavyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                        items(allBatches, key = { it.id }) { b ->
                            val isSel = selectedBatchId == b.id
                            Surface(
                                color = if (isSel) NavyPrimary else Color(0xFFF1F5F9),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.clickable { selectedBatchId = b.id }
                            ) {
                                Text(
                                    text = b.batchName,
                                    color = if (isSel) Color.White else NavyPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Summary Metric Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatsCard(
                    title = "Cohort Average",
                    value = "$cohortAvgPct%",
                    icon = Icons.Default.Analytics,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "Grade $overallGrade"
                )
                StatsCard(
                    title = "Pass Rate",
                    value = "$passRate%",
                    icon = Icons.Default.CheckCircle,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "$totalPassed / $totalSubmissions"
                )
                StatsCard(
                    title = "Distinctions",
                    value = "${(gradeCounts["A+"] ?: 0) + (gradeCounts["A"] ?: 0)}",
                    icon = Icons.Default.Star,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "A+ / A Grades"
                )
            }
        }

        // Grade Distribution Chart Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Grade Distribution Matrix", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                    Text("Frequency analysis of letter grades across all recorded tests", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(12.dp))

                    listOf("A+", "A", "B+", "B", "C", "D", "F").forEach { grade ->
                        val count = gradeCounts[grade] ?: 0
                        val pctOfTotal = if (totalSubmissions > 0) (count.toFloat() / totalSubmissions) else 0f

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Grade $grade",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary,
                                modifier = Modifier.width(60.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(14.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFF1F5F9))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction = pctOfTotal.coerceIn(0f, 1f))
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(
                                            when {
                                                grade.startsWith("A") -> Color(0xFF16A34A)
                                                grade.startsWith("B") -> NavySecondary
                                                grade.startsWith("C") || grade.startsWith("D") -> GoldAccent
                                                else -> BurgundyAccent
                                            }
                                        )
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$count (${(pctOfTotal * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray,
                                modifier = Modifier.width(60.dp)
                            )
                        }
                    }
                }
            }
        }

        // Merit Rankings Table
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Top Academic Performers (Merit Ranks)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                    Text("Ranked by cumulative percentage score across all evaluated assessments", fontSize = 11.sp, color = Color.Gray)

                    Spacer(modifier = Modifier.height(10.dp))

                    if (studentPerformanceList.isEmpty()) {
                        Text("No evaluated students available in this cohort.", fontSize = 11.sp, color = Color.Gray)
                    } else {
                        studentPerformanceList.take(6).forEachIndexed { index, (student, pct, grade) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (index) {
                                                    0 -> GoldAccent
                                                    1 -> Color(0xFFE2E8F0)
                                                    2 -> Color(0xFFFED7AA)
                                                    else -> Color(0xFFF1F5F9)
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("#${index + 1}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(student.fullName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                                        Text(student.studentCode, fontSize = 9.sp, color = Color.Gray)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$pct%", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = if (grade.startsWith("A")) Color(0xFFDCFCE7) else Color(0xFFEEF2FF),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = grade,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (grade.startsWith("A")) Color(0xFF15803D) else NavyPrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to build an official academic performance summary report transcript.
 */
private fun buildAcademicSummaryReport(
    selectedBatchName: String,
    totalExams: Int,
    totalSubmissions: Int,
    cohortAvgPct: Int,
    overallGrade: String,
    passRate: Int,
    gradeCounts: Map<String, Int>,
    rankings: List<Triple<StudentEntity, Int, String>>
): String {
    return buildString {
        appendLine("==================================================")
        appendLine("    FRONTCRAFT PRO COACHING ACADEMY")
        appendLine("    OFFICIAL ACADEMIC PERFORMANCE SUMMARY")
        appendLine("==================================================")
        appendLine("Cohort/Target: $selectedBatchName")
        appendLine("Total Examinations Conducted: $totalExams")
        appendLine("Total Student Submissions: $totalSubmissions")
        appendLine("Cohort Mean Proficiency: $cohortAvgPct% (Grade: $overallGrade)")
        appendLine("Overall Pass Rate: $passRate%")
        appendLine("--------------------------------------------------")
        appendLine("GRADE DISTRIBUTION FREQUENCY:")
        gradeCounts.forEach { (g, count) ->
            val pct = if (totalSubmissions > 0) ((count.toDouble() / totalSubmissions) * 100).toInt() else 0
            appendLine(" • Grade $g: $count students ($pct%)")
        }
        appendLine("--------------------------------------------------")
        appendLine("ACADEMIC HONOR ROLL & MERIT RANKINGS:")
        if (rankings.isEmpty()) {
            appendLine(" No evaluated records found.")
        } else {
            rankings.forEachIndexed { i, (st, pct, gr) ->
                appendLine(" #${i + 1} ${st.fullName} [${st.studentCode}]: $pct% (Grade $gr)")
            }
        }
        appendLine("==================================================")
        appendLine("Generated electronically by FrontCraft Pro Exam Module")
    }
}

/**
 * Scoreboard Leaderboard Modal for viewing individual exam results and merit order.
 */
@Composable
fun ExamResultsScoreboardDialog(
    exam: ExaminationEntity,
    batch: BatchEntity?,
    results: List<ExamResultEntity>,
    allStudents: List<StudentEntity>,
    viewModel: FrontCraftViewModel,
    onDismiss: () -> Unit,
    onEditScores: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val sortedResults = results.sortedByDescending { it.marksObtained }
    val totalAppeared = results.size
    val totalPassed = results.count { it.status == "PASS" }
    val passRate = if (totalAppeared > 0) ((totalPassed.toDouble() / totalAppeared) * 100).toInt() else 0
    val avgScore = if (totalAppeared > 0) results.map { it.marksObtained }.average() else 0.0
    val highestScore = results.maxOfOrNull { it.marksObtained } ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Assessment Scoreboard", fontWeight = FontWeight.Bold, color = NavyPrimary, fontSize = 17.sp)
                    Text(
                        text = "${exam.examTitle} • ${batch?.batchName ?: "Batch"}",
                        fontSize = 11.sp,
                        color = GoldAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(480.dp)
            ) {
                // Key Performance Metric Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CLASS AVG", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Text(
                                text = "${String.format(Locale.getDefault(), "%.1f", avgScore)} / ${exam.maxMarks.toInt()}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = NavyPrimary
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFF0FDF4),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("HIGHEST", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                            Text(
                                text = "${highestScore.toInt()} (${((highestScore / exam.maxMarks) * 100).toInt()}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFFEEF2FF),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC7D2FE)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("PASS RATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3730A3))
                            Text(
                                text = "$passRate%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3730A3)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MERIT RANKINGS & STUDENT GRADES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Merit List
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sortedResults.mapIndexed { idx, res -> idx + 1 to res }, key = { it.second.id }) { (rank, res) ->
                        val student = allStudents.find { it.id == res.studentId }
                        val pct = if (exam.maxMarks > 0) ((res.marksObtained / exam.maxMarks) * 100).toInt() else 0
                        val grade = viewModel.calculateGrade(res.marksObtained, exam.maxMarks)

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(30.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when (rank) {
                                                1 -> GoldAccent
                                                2 -> Color(0xFFE2E8F0)
                                                3 -> Color(0xFFFED7AA)
                                                else -> NavySecondary
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "#$rank",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (rank <= 3) NavyPrimary else Color.White
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = student?.fullName ?: "Student #${res.studentId}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = NavyPrimary
                                        )
                                        if (rank == 1) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(Icons.Default.Star, contentDescription = "Topper", tint = GoldAccent, modifier = Modifier.size(13.dp))
                                        }
                                    }
                                    Text(
                                        text = "${student?.studentCode ?: ""} • ${res.remarks.ifBlank { "Evaluated" }}",
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${res.marksObtained.toInt()} / ${exam.maxMarks.toInt()}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = NavyPrimary
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Surface(
                                            color = when {
                                                grade.startsWith("A") -> Color(0xFFDCFCE7)
                                                grade.startsWith("B") -> Color(0xFFE0E7FF)
                                                grade.startsWith("C") || grade.startsWith("D") -> Color(0xFFFEF3C7)
                                                else -> Color(0xFFFEE2E2)
                                            },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = grade,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when {
                                                    grade.startsWith("A") -> Color(0xFF15803D)
                                                    grade.startsWith("B") -> Color(0xFF3730A3)
                                                    grade.startsWith("C") || grade.startsWith("D") -> Color(0xFFB45309)
                                                    else -> Color(0xFFB91C1C)
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = "$pct% • ${res.status}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (res.status == "PASS") Color(0xFF16A34A) else Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        val transcript = buildString {
                            appendLine("=== ASSESSMENT RESULT SUMMARY ===")
                            appendLine("Test: ${exam.examTitle}")
                            appendLine("Cohort: ${batch?.batchName ?: ""}")
                            appendLine("Subject: ${exam.subject}")
                            appendLine("Date: ${exam.examDate} | Max Marks: ${exam.maxMarks.toInt()}")
                            appendLine("Class Average: ${String.format(Locale.getDefault(), "%.1f", avgScore)} | Pass Rate: $passRate%")
                            appendLine("---------------------------------")
                            sortedResults.forEachIndexed { i, r ->
                                val st = allStudents.find { it.id == r.studentId }
                                appendLine("#${i + 1} ${st?.fullName ?: "Student"} (${st?.studentCode ?: ""}): ${r.marksObtained.toInt()}/${exam.maxMarks.toInt()} - Grade ${viewModel.calculateGrade(r.marksObtained, exam.maxMarks)} [${r.status}]")
                            }
                        }
                        clipboardManager.setText(AnnotatedString(transcript))
                        Toast.makeText(context, "Result transcript copied to clipboard!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy Scoreboard", fontSize = 11.sp)
                }

                Button(
                    onClick = onEditScores,
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit Scores", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
