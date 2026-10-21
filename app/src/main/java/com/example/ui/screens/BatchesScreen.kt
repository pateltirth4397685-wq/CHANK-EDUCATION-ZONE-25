package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatchEntity
import com.example.data.StudentEntity
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchesScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var showCreateDialog by remember { mutableStateOf(false) }
    var batchToEdit by remember { mutableStateOf<BatchEntity?>(null) }
    var batchToDelete by remember { mutableStateOf<BatchEntity?>(null) }
    var batchForAddStudent by remember { mutableStateOf<BatchEntity?>(null) }
    var batchForViewStudents by remember { mutableStateOf<BatchEntity?>(null) }

    // Calculated batch analytics
    val totalBatchesCount = allBatches.size
    val totalCapacityCount = allBatches.sumOf { it.capacity }
    val totalEnrolledCount = allStudents.count { it.status == "ACTIVE" }
    val totalAvailableSeats = (totalCapacityCount - totalEnrolledCount).coerceAtLeast(0)

    val filteredBatches = allBatches.filter { batch ->
        val matchesQuery = batch.batchName.contains(searchQuery, ignoreCase = true) ||
                batch.courseName.contains(searchQuery, ignoreCase = true) ||
                batch.subject.contains(searchQuery, ignoreCase = true) ||
                batch.teacherName.contains(searchQuery, ignoreCase = true)

        val enrolledInBatch = allStudents.count { it.assignedBatchId == batch.id && it.status == "ACTIVE" }
        val matchesFilter = when (selectedFilter) {
            "AVAILABLE" -> enrolledInBatch < batch.capacity
            "FULL" -> enrolledInBatch >= batch.capacity
            "PHYSICS" -> batch.subject.equals("Physics", ignoreCase = true)
            "BIOLOGY" -> batch.subject.equals("Biology", ignoreCase = true)
            "MATH" -> batch.subject.contains("Math", ignoreCase = true)
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == "ADMIN") {
                FloatingActionButton(
                    onClick = { showCreateDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyPrimary,
                    modifier = Modifier.testTag("create_batch_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create New Batch")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            // Header with title and Create Batch button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Batch Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Create batches, assign subjects to teachers, & set student capacity",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (currentUser.role == "ADMIN") {
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("create_batch_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Batch", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Summary Metrics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Total Batches",
                    value = "$totalBatchesCount",
                    icon = Icons.Default.Class,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "Active Cohorts"
                )
                StatsCard(
                    title = "Total Capacity",
                    value = "$totalCapacityCount Seats",
                    icon = Icons.Default.GroupWork,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Max Classroom Limit"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Enrolled Students",
                    value = "$totalEnrolledCount",
                    icon = Icons.Default.Group,
                    iconBgColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    subText = "Assigned to Batches"
                )
                StatsCard(
                    title = "Available Seats",
                    value = "$totalAvailableSeats Seats",
                    icon = Icons.Default.School,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Vacant Openings"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_search_input"),
                placeholder = { Text("Search by batch name, subject, or faculty...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All Batches",
                    "AVAILABLE" to "Has Seats",
                    "FULL" to "Full Capacity",
                    "PHYSICS" to "Physics",
                    "BIOLOGY" to "Biology",
                    "MATH" to "Math"
                ).forEach { (key, label) ->
                    val isSelected = selectedFilter == key
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedFilter = key },
                        color = if (isSelected) NavyPrimary else Color.White,
                        shape = RoundedCornerShape(16.dp),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else NavyPrimary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Batches List
            if (filteredBatches.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Class, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No batches found", color = Color.Gray, fontWeight = FontWeight.Medium)
                        if (currentUser.role == "ADMIN") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { showCreateDialog = true }) {
                                Text("Create First Batch")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBatches, key = { it.id }) { batch ->
                        val enrolledCount = allStudents.count { it.assignedBatchId == batch.id && it.status == "ACTIVE" }
                        val occupancyRatio = if (batch.capacity > 0) enrolledCount.toFloat() / batch.capacity else 0f
                        val occupancyPercent = (occupancyRatio * 100).toInt()
                        val isFull = enrolledCount >= batch.capacity

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("batch_card_${batch.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Title row: Batch name, Course, and Status badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = batch.batchName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = batch.courseName,
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }

                                    StatusBadge(status = batch.status)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Subject Tag & Assigned Faculty
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Subject Chip
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.MenuBook,
                                                contentDescription = null,
                                                tint = Color(0xFFB45309),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Subject: ${batch.subject}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFFB45309)
                                            )
                                        }
                                    }

                                    // Faculty Chip
                                    Surface(
                                        color = Color(0xFFE0E7FF),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Person,
                                                contentDescription = null,
                                                tint = Color(0xFF3730A3),
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = batch.teacherName,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF3730A3)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Timing
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Schedule: ${batch.scheduleTime} • Started: ${batch.startDate}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Student Capacity Progress Indicator
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Student Capacity: $enrolledCount / ${batch.capacity} Enrolled",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = NavyPrimary
                                        )
                                        Surface(
                                            color = if (isFull) Color(0xFFFEE2E2) else if (occupancyPercent >= 75) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = if (isFull) "FULL" else "$occupancyPercent% Filled",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isFull) Color(0xFFB91C1C) else if (occupancyPercent >= 75) Color(0xFFB45309) else Color(0xFF15803D),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { occupancyRatio.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (isFull) BurgundyAccent else if (occupancyPercent >= 75) GoldAccent else Color(0xFF16A34A),
                                        trackColor = Color(0xFFE2E8F0)
                                    )

                                    val remainingSeats = (batch.capacity - enrolledCount).coerceAtLeast(0)
                                    Text(
                                        text = if (isFull) "No seats remaining" else "$remainingSeats seat(s) available for enrollment",
                                        fontSize = 10.sp,
                                        color = if (isFull) Color(0xFFB91C1C) else Color(0xFF64748B),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }

                                // Action Buttons (View Roster, Add to Batch, Edit, Delete)
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = { batchForViewStudents = batch },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("view_students_batch_${batch.id}")
                                    ) {
                                        Icon(Icons.Default.Group, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Roster ($enrolledCount)", fontSize = 11.sp)
                                    }

                                    if (currentUser.role == "ADMIN") {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = { batchForAddStudent = batch },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("add_to_batch_${batch.id}")
                                            ) {
                                                Icon(Icons.Default.PersonAdd, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Add to Batch", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                            }

                                            OutlinedButton(
                                                onClick = { batchToEdit = batch },
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.testTag("edit_batch_${batch.id}")
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit Batch", modifier = Modifier.size(14.dp))
                                            }

                                            IconButton(
                                                onClick = { batchToDelete = batch },
                                                modifier = Modifier.testTag("delete_batch_${batch.id}")
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Delete Batch", tint = Color(0xFFDC2626))
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
    }

    // Modal: Create New Batch Dialog
    if (showCreateDialog) {
        CreateOrEditBatchDialog(
            existingBatch = null,
            teachers = allTeachers,
            onDismiss = { showCreateDialog = false },
            onSave = { name, course, subject, teacherId, teacherName, schedule, capacity, startDate ->
                viewModel.createBatch(
                    batchName = name,
                    courseName = course,
                    subject = subject,
                    teacherId = teacherId,
                    teacherName = teacherName,
                    scheduleTime = schedule,
                    capacity = capacity,
                    startDate = startDate
                )
                showCreateDialog = false
                Toast.makeText(context, "Batch '$name' created with capacity of $capacity students!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Edit Batch Dialog
    batchToEdit?.let { batch ->
        CreateOrEditBatchDialog(
            existingBatch = batch,
            teachers = allTeachers,
            onDismiss = { batchToEdit = null },
            onSave = { name, course, subject, teacherId, teacherName, schedule, capacity, startDate ->
                viewModel.updateBatch(
                    batch.copy(
                        batchName = name,
                        courseName = course,
                        subject = subject,
                        teacherId = teacherId,
                        teacherName = teacherName,
                        scheduleTime = schedule,
                        capacity = capacity,
                        startDate = startDate
                    )
                )
                batchToEdit = null
                Toast.makeText(context, "Batch '$name' updated successfully!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Confirmation: Delete Batch Dialog
    batchToDelete?.let { batch ->
        AlertDialog(
            onDismissRequest = { batchToDelete = null },
            title = { Text("Delete Batch") },
            text = { Text("Are you sure you want to remove batch '${batch.batchName}'? Students assigned to this batch will remain enrolled without batch assignment.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBatch(batch.id, batch.batchName)
                        batchToDelete = null
                        Toast.makeText(context, "Batch removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { batchToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Modal: Add Student to Batch Dialog ("add to batch add")
    batchForAddStudent?.let { batch ->
        AddToBatchDialog(
            batch = batch,
            allStudents = allStudents,
            allBatches = allBatches,
            onDismiss = { batchForAddStudent = null },
            onAssignExisting = { studentId ->
                viewModel.assignStudentToBatch(studentId, batch.id)
                batchForAddStudent = null
                val studentName = allStudents.find { it.id == studentId }?.fullName ?: "Student"
                Toast.makeText(context, "$studentName assigned to ${batch.batchName}!", Toast.LENGTH_SHORT).show()
            },
            onEnrollNew = { name, mobile, parentName, parentMobile, courseFee ->
                viewModel.enrollStudent(
                    fullName = name,
                    mobile = mobile,
                    parentName = parentName,
                    parentMobile = parentMobile,
                    admissionDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    assignedBatchId = batch.id,
                    courseName = batch.courseName,
                    totalCourseFee = courseFee
                )
                batchForAddStudent = null
                Toast.makeText(context, "$name enrolled directly into ${batch.batchName}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Batch Students Roster Dialog
    batchForViewStudents?.let { batch ->
        BatchStudentsRosterDialog(
            batch = batch,
            students = allStudents.filter { it.assignedBatchId == batch.id },
            isAdmin = currentUser.role == "ADMIN",
            onDismiss = { batchForViewStudents = null },
            onRemoveStudent = { studentId ->
                val studentName = allStudents.find { it.id == studentId }?.fullName ?: "Student"
                viewModel.removeStudentFromBatch(studentId)
                Toast.makeText(context, "$studentName removed from ${batch.batchName}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditBatchDialog(
    existingBatch: BatchEntity?,
    teachers: List<com.example.data.TeacherEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        course: String,
        subject: String,
        teacherId: Long?,
        teacherName: String,
        schedule: String,
        capacity: Int,
        startDate: String
    ) -> Unit
) {
    var batchName by remember { mutableStateOf(existingBatch?.batchName ?: "") }
    var courseName by remember { mutableStateOf(existingBatch?.courseName ?: "") }
    var subject by remember { mutableStateOf(existingBatch?.subject ?: "Physics") }
    var teacherId by remember { mutableStateOf<Long?>(existingBatch?.teacherId ?: teachers.firstOrNull()?.id) }
    var teacherName by remember { mutableStateOf(existingBatch?.teacherName ?: teachers.firstOrNull()?.fullName ?: "Faculty") }
    var scheduleTime by remember { mutableStateOf(existingBatch?.scheduleTime ?: "07:30 AM - 10:00 AM") }
    var capacityStr by remember { mutableStateOf(existingBatch?.capacity?.toString() ?: "30") }
    var startDate by remember { mutableStateOf(existingBatch?.startDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var teacherDropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existingBatch != null) "Edit Batch Settings" else "Create New Batch",
                fontWeight = FontWeight.Bold,
                color = NavyPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Batch Name
                item {
                    OutlinedTextField(
                        value = batchName,
                        onValueChange = {
                            batchName = it
                            errorMessage = null
                        },
                        label = { Text("Batch Name *") },
                        placeholder = { Text("e.g. JEE Advanced 2026 Batch B") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_batch_name"),
                        singleLine = true
                    )
                }

                // Course Program
                item {
                    OutlinedTextField(
                        value = courseName,
                        onValueChange = {
                            courseName = it
                            errorMessage = null
                        },
                        label = { Text("Course / Academic Program *") },
                        placeholder = { Text("e.g. IIT-JEE Target 2026") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_batch_course"),
                        singleLine = true
                    )
                }

                // Assign Subject (with quick selection chips)
                item {
                    Column {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Assigned Subject *") },
                            placeholder = { Text("e.g. Physics, Chemistry, Mathematics") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_batch_subject"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Physics", "Chemistry", "Mathematics", "Biology").forEach { sub ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { subject = sub },
                                    color = if (subject.equals(sub, ignoreCase = true)) GoldAccent else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = sub,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (subject.equals(sub, ignoreCase = true)) NavyPrimary else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Assign Teacher (Dropdown)
                item {
                    ExposedDropdownMenuBox(
                        expanded = teacherDropdownExpanded,
                        onExpandedChange = { teacherDropdownExpanded = !teacherDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = teacherName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Assigned Teacher (Faculty) *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                                .testTag("select_batch_teacher")
                        )

                        ExposedDropdownMenu(
                            expanded = teacherDropdownExpanded,
                            onDismissRequest = { teacherDropdownExpanded = false }
                        ) {
                            teachers.forEach { t ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(t.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${t.subject} Specialist • ${t.teacherCode}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        teacherId = t.id
                                        teacherName = t.fullName
                                        if (subject.isEmpty() || subject == "Physics") {
                                            subject = t.subject
                                        }
                                        teacherDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Total Student Capacity (with quick preset chips)
                item {
                    Column {
                        OutlinedTextField(
                            value = capacityStr,
                            onValueChange = {
                                if (it.all { char -> char.isDigit() }) capacityStr = it
                            },
                            label = { Text("Total Student Capacity *") },
                            placeholder = { Text("Max seats, e.g. 30") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_batch_capacity"),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(20, 30, 40, 50, 60).forEach { cap ->
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { capacityStr = cap.toString() },
                                    color = if (capacityStr == cap.toString()) NavySecondary else Color(0xFFF1F5F9),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "$cap Seats",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (capacityStr == cap.toString()) GoldAccent else Color(0xFF475569),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Schedule Timing
                item {
                    OutlinedTextField(
                        value = scheduleTime,
                        onValueChange = { scheduleTime = it },
                        label = { Text("Class Schedule Timing *") },
                        placeholder = { Text("e.g. 07:30 AM - 10:00 AM") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_batch_schedule"),
                        singleLine = true
                    )
                }

                // Start Date
                item {
                    OutlinedTextField(
                        value = startDate,
                        onValueChange = { startDate = it },
                        label = { Text("Academic Start Date *") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_batch_start_date"),
                        singleLine = true
                    )
                }

                if (errorMessage != null) {
                    item {
                        Text(
                            text = errorMessage ?: "",
                            color = Color(0xFFDC2626),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cap = capacityStr.toIntOrNull() ?: 0
                    if (batchName.isBlank() || courseName.isBlank() || subject.isBlank() || cap <= 0) {
                        errorMessage = "Please enter valid batch name, course, subject, and capacity > 0."
                        return@Button
                    }
                    onSave(
                        batchName.trim(),
                        courseName.trim(),
                        subject.trim(),
                        teacherId,
                        teacherName.trim(),
                        scheduleTime.trim(),
                        cap,
                        startDate.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("save_batch_button")
            ) {
                Text(
                    text = if (existingBatch != null) "Update Batch" else "Create Batch",
                    color = NavyPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToBatchDialog(
    batch: BatchEntity,
    allStudents: List<StudentEntity>,
    allBatches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onAssignExisting: (studentId: Long) -> Unit,
    onEnrollNew: (name: String, mobile: String, parentName: String, parentMobile: String, courseFee: Double) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }

    var newStudentName by remember { mutableStateOf("") }
    var newStudentMobile by remember { mutableStateOf("") }
    var newParentName by remember { mutableStateOf("") }
    var newParentMobile by remember { mutableStateOf("") }
    var newCourseFeeStr by remember { mutableStateOf("45000") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val enrolledCount = allStudents.count { it.assignedBatchId == batch.id && it.status == "ACTIVE" }
    val isAtCapacity = enrolledCount >= batch.capacity
    val remainingSeats = (batch.capacity - enrolledCount).coerceAtLeast(0)

    val candidateStudents = allStudents.filter { st ->
        st.fullName.contains(searchQuery, ignoreCase = true) ||
                st.studentCode.contains(searchQuery, ignoreCase = true) ||
                st.mobile.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add Student to Batch",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 18.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
                Text(
                    text = "${batch.batchName} (${batch.subject})",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Capacity status banner
                Surface(
                    color = if (isAtCapacity) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAtCapacity) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (isAtCapacity) Icons.Default.Warning else Icons.Default.School,
                            contentDescription = null,
                            tint = if (isAtCapacity) Color(0xFFDC2626) else Color(0xFF16A34A),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isAtCapacity) "Batch is at Maximum Capacity ($enrolledCount / ${batch.capacity} seats)"
                                else "$remainingSeats Seat(s) Available ($enrolledCount / ${batch.capacity} Enrolled)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (isAtCapacity) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                            Text(
                                text = "Faculty: ${batch.teacherName} • Schedule: ${batch.scheduleTime}",
                                fontSize = 10.sp,
                                color = if (isAtCapacity) Color(0xFFB91C1C) else Color(0xFF15803D)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tabs: Assign Existing vs Enroll New
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = NavyPrimary,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Assign Existing", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("tab_assign_existing")
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Quick Enroll New", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("tab_enroll_new")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search student name or roll code...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_student_for_batch"),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    if (candidateStudents.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No matching students found", color = Color.Gray, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(candidateStudents, key = { it.id }) { st ->
                                val isAlreadyInThisBatch = st.assignedBatchId == batch.id
                                val otherBatch = allBatches.find { it.id == st.assignedBatchId }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                            Text("${st.studentCode} • ${st.mobile}", fontSize = 10.sp, color = Color.Gray)
                                            Text(
                                                text = if (isAlreadyInThisBatch) "Currently Enrolled Here"
                                                else if (otherBatch != null) "In ${otherBatch.batchName}"
                                                else "Unassigned Cohort",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isAlreadyInThisBatch) Color(0xFF16A34A) else Color(0xFFB45309)
                                            )
                                        }

                                        if (isAlreadyInThisBatch) {
                                            Surface(
                                                color = Color(0xFFDCFCE7),
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text(
                                                    text = "Enrolled",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF16A34A),
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                                )
                                            }
                                        } else {
                                            Button(
                                                onClick = { onAssignExisting(st.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.testTag("assign_student_${st.id}")
                                            ) {
                                                Text(
                                                    text = if (otherBatch != null) "Transfer" else "Assign",
                                                    color = NavyPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            OutlinedTextField(
                                value = newStudentName,
                                onValueChange = { newStudentName = it; errorMessage = null },
                                label = { Text("Student Full Name *") },
                                placeholder = { Text("e.g. Yash Singhania") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_student_name"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = newStudentMobile,
                                onValueChange = { newStudentMobile = it; errorMessage = null },
                                label = { Text("Student Mobile *") },
                                placeholder = { Text("10-digit number") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_student_mobile"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = newParentName,
                                onValueChange = { newParentName = it },
                                label = { Text("Guardian / Parent Name *") },
                                placeholder = { Text("e.g. Rajesh Singhania") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_guardian_name"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = newParentMobile,
                                onValueChange = { newParentMobile = it },
                                label = { Text("Guardian Mobile *") },
                                placeholder = { Text("Emergency contact") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_guardian_mobile"),
                                singleLine = true
                            )
                        }
                        item {
                            OutlinedTextField(
                                value = newCourseFeeStr,
                                onValueChange = { if (it.all { c -> c.isDigit() }) newCourseFeeStr = it },
                                label = { Text("Course Fee (₹) *") },
                                placeholder = { Text("e.g. 45000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_quick_course_fee"),
                                singleLine = true
                            )
                        }

                        if (errorMessage != null) {
                            item {
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFDC2626),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    val fee = newCourseFeeStr.toDoubleOrNull() ?: 0.0
                                    if (newStudentName.isBlank() || newStudentMobile.isBlank() || newParentName.isBlank()) {
                                        errorMessage = "Please fill student name, mobile, and guardian details."
                                        return@Button
                                    }
                                    onEnrollNew(
                                        newStudentName.trim(),
                                        newStudentMobile.trim(),
                                        newParentName.trim(),
                                        newParentMobile.trim(),
                                        fee
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quick_enroll_button"),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Enroll & Add to ${batch.batchName}", color = NavyPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun BatchStudentsRosterDialog(
    batch: BatchEntity,
    students: List<StudentEntity>,
    isAdmin: Boolean,
    onDismiss: () -> Unit,
    onRemoveStudent: (studentId: Long) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = students.filter {
        it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.studentCode.contains(searchQuery, ignoreCase = true) ||
                it.mobile.contains(searchQuery)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Batch Student Roster",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${batch.batchName} • ${students.size} / ${batch.capacity} Students",
                        fontSize = 11.sp,
                        color = Color.Gray
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
                    .padding(vertical = 4.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter roster by student name...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_batch_roster_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("No enrolled students in this batch", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filtered, key = { it.id }) { st ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("roster_item_${st.id}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Surface(
                                            color = NavySecondary,
                                            shape = CircleShape,
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = st.fullName.take(1).uppercase(),
                                                    color = Color.White,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Text(st.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = NavyPrimary)
                                            Text("${st.studentCode} • 📞 ${st.mobile}", fontSize = 10.sp, color = Color.Gray)
                                            Text("Guardian: ${st.parentName} (${st.parentMobile})", fontSize = 10.sp, color = Color.Gray)
                                        }
                                    }

                                    if (isAdmin) {
                                        IconButton(
                                            onClick = { onRemoveStudent(st.id) },
                                            modifier = Modifier.testTag("remove_student_batch_${st.id}")
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove from Batch",
                                                tint = Color(0xFFDC2626),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
