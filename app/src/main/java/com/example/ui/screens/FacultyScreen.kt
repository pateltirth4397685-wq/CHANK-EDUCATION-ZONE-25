package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BatchEntity
import com.example.data.TeacherEntity
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacultyScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentUser by viewModel.currentUser.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedSubjectFilter by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var teacherToEdit by remember { mutableStateOf<TeacherEntity?>(null) }
    var teacherForBatchAssignment by remember { mutableStateOf<TeacherEntity?>(null) }
    var teacherToDelete by remember { mutableStateOf<TeacherEntity?>(null) }

    // Analytics
    val totalFacultyCount = allTeachers.size
    val totalAssignedBatches = allBatches.count { it.teacherId != null }
    val uniqueSubjectsCount = allTeachers.map { it.subject }.distinct().size
    val totalStudentsTaught = allStudents.count { it.assignedBatchId != null && it.assignedBatchId != 0L }

    val filteredTeachers = allTeachers.filter { teacher ->
        val matchesQuery = teacher.fullName.contains(searchQuery, ignoreCase = true) ||
                teacher.teacherCode.contains(searchQuery, ignoreCase = true) ||
                teacher.subject.contains(searchQuery, ignoreCase = true) ||
                teacher.mobile.contains(searchQuery) ||
                teacher.email.contains(searchQuery, ignoreCase = true) ||
                teacher.assignedBatches.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedSubjectFilter) {
            "PHYSICS" -> teacher.subject.equals("Physics", ignoreCase = true)
            "CHEMISTRY" -> teacher.subject.equals("Chemistry", ignoreCase = true)
            "MATH" -> teacher.subject.contains("Math", ignoreCase = true)
            "BIOLOGY" -> teacher.subject.equals("Biology", ignoreCase = true)
            else -> true
        }
        matchesQuery && matchesFilter
    }

    Scaffold(
        floatingActionButton = {
            if (currentUser.role == "ADMIN") {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = GoldAccent,
                    contentColor = NavyPrimary,
                    modifier = Modifier.testTag("add_faculty_fab")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Faculty Member")
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Faculty Management",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary
                    )
                    Text(
                        text = "Add teachers, assign to batches, and view contact profiles",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                if (currentUser.role == "ADMIN") {
                    Button(
                        onClick = { showAddDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("add_faculty_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Faculty", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Analytics Metrics Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Total Faculty",
                    value = "$totalFacultyCount",
                    icon = Icons.Default.School,
                    iconBgColor = NavySecondary,
                    modifier = Modifier.weight(1f),
                    subText = "Active Teachers"
                )
                StatsCard(
                    title = "Assigned Batches",
                    value = "$totalAssignedBatches / ${allBatches.size}",
                    icon = Icons.Default.Class,
                    iconBgColor = GoldAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Cohorts with Faculty"
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatsCard(
                    title = "Subjects",
                    value = "$uniqueSubjectsCount",
                    icon = Icons.Default.MenuBook,
                    iconBgColor = BurgundyAccent,
                    modifier = Modifier.weight(1f),
                    subText = "Disciplines Taught"
                )
                StatsCard(
                    title = "Students Taught",
                    value = "$totalStudentsTaught",
                    icon = Icons.Default.Group,
                    iconBgColor = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f),
                    subText = "In Assigned Classes"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_faculty_input"),
                placeholder = { Text("Search by teacher name, code, contact, subject, or batch...") },
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
                    "ALL" to "All Faculty",
                    "PHYSICS" to "Physics",
                    "CHEMISTRY" to "Chemistry",
                    "MATH" to "Mathematics",
                    "BIOLOGY" to "Biology"
                ).forEach { (key, label) ->
                    val isSelected = selectedSubjectFilter == key
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedSubjectFilter = key },
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

            // Faculty List
            if (filteredTeachers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.School, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No faculty members found", color = Color.Gray, fontWeight = FontWeight.Medium)
                        if (currentUser.role == "ADMIN") {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(onClick = { showAddDialog = true }) {
                                Text("Add First Faculty Member")
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
                    items(filteredTeachers, key = { it.id }) { teacher ->
                        val teacherBatches = allBatches.filter { it.teacherId == teacher.id }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("faculty_card_${teacher.id}"),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                // Top row: Avatar, Name, Code, and Subject Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = NavyPrimary,
                                        shape = CircleShape,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = teacher.fullName.take(1).uppercase(),
                                                color = GoldAccent,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = teacher.fullName,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NavyPrimary
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = teacher.teacherCode,
                                                fontSize = 11.sp,
                                                color = Color.Gray,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(text = "•", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                text = "Courses: ${teacher.assignedCourses}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }

                                    // Subject Chip
                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            text = teacher.subject,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Contact Information Cards
                                Surface(
                                    color = Color(0xFFF8FAFC),
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(
                                            text = "CONTACT DETAILS",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(teacher.mobile))
                                                        Toast.makeText(context, "Copied phone: ${teacher.mobile}", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .testTag("contact_mobile_${teacher.id}")
                                            ) {
                                                Icon(Icons.Default.Call, contentDescription = "Phone", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = teacher.mobile,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = NavyPrimary
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .clickable {
                                                        clipboardManager.setText(AnnotatedString(teacher.email))
                                                        Toast.makeText(context, "Copied email: ${teacher.email}", Toast.LENGTH_SHORT).show()
                                                    }
                                                    .testTag("contact_email_${teacher.id}")
                                            ) {
                                                Icon(Icons.Default.Email, contentDescription = "Email", tint = NavySecondary, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = teacher.email,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = NavyPrimary
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.DateRange, contentDescription = "Joined", tint = Color.Gray, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Joined Faculty: ${teacher.joiningDate}",
                                                fontSize = 11.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Assigned Batches Section
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "ASSIGNED BATCHES (${teacherBatches.size})",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )

                                        if (currentUser.role == "ADMIN") {
                                            Text(
                                                text = "+ Assign Batches",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BurgundyAccent,
                                                modifier = Modifier
                                                    .clickable { teacherForBatchAssignment = teacher }
                                                    .testTag("assign_batches_link_${teacher.id}")
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    if (teacherBatches.isEmpty()) {
                                        Surface(
                                            color = Color(0xFFFFFBEB),
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(
                                                text = "Currently not assigned to any batch. Tap 'Assign Batches' to allocate cohorts.",
                                                fontSize = 11.sp,
                                                color = Color(0xFFB45309),
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                    } else {
                                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                            teacherBatches.forEach { b ->
                                                val enrolled = allStudents.count { it.assignedBatchId == b.id }
                                                Surface(
                                                    color = Color(0xFFEEF2FF),
                                                    shape = RoundedCornerShape(6.dp),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column {
                                                            Text(
                                                                text = b.batchName,
                                                                fontWeight = FontWeight.Bold,
                                                                fontSize = 12.sp,
                                                                color = Color(0xFF3730A3)
                                                            )
                                                            Text(
                                                                text = "${b.scheduleTime} • ${b.courseName}",
                                                                fontSize = 10.sp,
                                                                color = Color(0xFF6366F1)
                                                            )
                                                        }

                                                        Surface(
                                                            color = Color.White,
                                                            shape = RoundedCornerShape(10.dp)
                                                        ) {
                                                            Text(
                                                                text = "$enrolled / ${b.capacity} Seats",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = NavyPrimary,
                                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Admin Action Buttons
                                if (currentUser.role == "ADMIN") {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            onClick = { teacherForBatchAssignment = teacher },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("assign_batches_${teacher.id}")
                                        ) {
                                            Icon(Icons.Default.Class, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Assign Batches", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        OutlinedButton(
                                            onClick = { teacherToEdit = teacher },
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier.testTag("edit_faculty_${teacher.id}")
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = "Edit Faculty", modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Edit", fontSize = 11.sp)
                                        }

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { teacherToDelete = teacher },
                                            modifier = Modifier.testTag("delete_faculty_${teacher.id}")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Faculty", tint = Color(0xFFDC2626))
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

    // Modal: Add / Edit Faculty Dialog
    if (showAddDialog) {
        AddOrEditFacultyDialog(
            existingTeacher = null,
            allBatches = allBatches,
            onDismiss = { showAddDialog = false },
            onSave = { name, mobile, email, subject, courses, batchIds, date ->
                viewModel.addTeacher(
                    fullName = name,
                    mobile = mobile,
                    email = email,
                    subject = subject,
                    assignedCourses = courses,
                    assignedBatchIds = batchIds,
                    joiningDate = date
                )
                showAddDialog = false
                Toast.makeText(context, "Faculty $name added with ${batchIds.size} assigned batches!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    teacherToEdit?.let { teacher ->
        val currentBatchIds = allBatches.filter { it.teacherId == teacher.id }.map { it.id }
        AddOrEditFacultyDialog(
            existingTeacher = teacher,
            allBatches = allBatches,
            onDismiss = { teacherToEdit = null },
            onSave = { name, mobile, email, subject, courses, batchIds, date ->
                viewModel.updateTeacher(
                    teacher = teacher.copy(
                        fullName = name,
                        mobile = mobile,
                        email = email,
                        subject = subject,
                        assignedCourses = courses,
                        joiningDate = date
                    ),
                    assignedBatchIds = batchIds
                )
                teacherToEdit = null
                Toast.makeText(context, "Faculty profile updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Assign Batches to Teacher Dialog
    teacherForBatchAssignment?.let { teacher ->
        val currentlyAssignedBatchIds = allBatches.filter { it.teacherId == teacher.id }.map { it.id }
        AssignBatchesToTeacherDialog(
            teacher = teacher,
            allBatches = allBatches,
            initiallySelectedBatchIds = currentlyAssignedBatchIds,
            onDismiss = { teacherForBatchAssignment = null },
            onSave = { selectedBatchIds ->
                viewModel.assignTeacherToBatches(
                    teacherId = teacher.id,
                    teacherName = teacher.fullName,
                    teacherSubject = teacher.subject,
                    batchIds = selectedBatchIds
                )
                teacherForBatchAssignment = null
                Toast.makeText(context, "Assigned ${selectedBatchIds.size} batch(es) to ${teacher.fullName}!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Modal: Delete Faculty Confirmation Dialog
    teacherToDelete?.let { teacher ->
        AlertDialog(
            onDismissRequest = { teacherToDelete = null },
            title = { Text("Remove Faculty Member", fontWeight = FontWeight.Bold, color = NavyPrimary) },
            text = { Text("Are you sure you want to remove ${teacher.fullName} (${teacher.teacherCode})? Any batches assigned to this teacher will be marked as unassigned.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTeacher(teacher.id, teacher.fullName)
                        teacherToDelete = null
                        Toast.makeText(context, "Faculty member removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { teacherToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditFacultyDialog(
    existingTeacher: TeacherEntity?,
    allBatches: List<BatchEntity>,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        mobile: String,
        email: String,
        subject: String,
        courses: String,
        assignedBatchIds: List<Long>,
        joiningDate: String
    ) -> Unit
) {
    var fullName by remember { mutableStateOf(existingTeacher?.fullName ?: "") }
    var mobile by remember { mutableStateOf(existingTeacher?.mobile ?: "") }
    var email by remember { mutableStateOf(existingTeacher?.email ?: "") }
    var subject by remember { mutableStateOf(existingTeacher?.subject ?: "Physics") }
    var courses by remember { mutableStateOf(existingTeacher?.assignedCourses ?: "JEE / NEET") }
    var joiningDate by remember { mutableStateOf(existingTeacher?.joiningDate ?: SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())) }
    var selectedBatchIds by remember {
        mutableStateOf(
            if (existingTeacher != null) allBatches.filter { it.teacherId == existingTeacher.id }.map { it.id }.toSet()
            else emptySet()
        )
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (existingTeacher != null) "Edit Faculty Member" else "Add New Faculty",
                    fontWeight = FontWeight.Bold,
                    color = NavyPrimary,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Full Name
                item {
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it; errorMessage = null },
                        label = { Text("Full Name (with Honorific) *") },
                        placeholder = { Text("e.g. Prof. Sandeep Sharma") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_teacher_name"),
                        singleLine = true
                    )
                }

                // Mobile
                item {
                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it; errorMessage = null },
                        label = { Text("Mobile Contact *") },
                        placeholder = { Text("10-digit phone number") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_teacher_mobile"),
                        singleLine = true
                    )
                }

                // Email
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Address *") },
                        placeholder = { Text("faculty@frontcraft.edu") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_teacher_email"),
                        singleLine = true
                    )
                }

                // Subject Specialization + Chips
                item {
                    Column {
                        OutlinedTextField(
                            value = subject,
                            onValueChange = { subject = it },
                            label = { Text("Subject Specialization *") },
                            placeholder = { Text("e.g. Physics, Mathematics") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_teacher_subject"),
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

                // Target Courses
                item {
                    OutlinedTextField(
                        value = courses,
                        onValueChange = { courses = it },
                        label = { Text("Target Academic Programs") },
                        placeholder = { Text("e.g. IIT-JEE Target, NEET Crash") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_teacher_courses"),
                        singleLine = true
                    )
                }

                // Joining Date
                item {
                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Joining Date (YYYY-MM-DD)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_teacher_joining_date"),
                        singleLine = true
                    )
                }

                // Assign to Specific Batches Multi-Select
                item {
                    Column {
                        Text(
                            text = "Assign to Specific Batches:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = NavyPrimary
                        )
                        Text(
                            text = "Select one or more cohorts this teacher will conduct",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        if (allBatches.isEmpty()) {
                            Text("No batches created yet.", fontSize = 11.sp, color = Color.Gray)
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                allBatches.forEach { batch ->
                                    val isChecked = selectedBatchIds.contains(batch.id)
                                    Surface(
                                        color = if (isChecked) Color(0xFFEEF2FF) else Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isChecked) NavySecondary else Color(0xFFE2E8F0)
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                selectedBatchIds = if (isChecked) {
                                                    selectedBatchIds - batch.id
                                                } else {
                                                    selectedBatchIds + batch.id
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Checkbox(
                                                checked = isChecked,
                                                onCheckedChange = { checked ->
                                                    selectedBatchIds = if (checked) {
                                                        selectedBatchIds + batch.id
                                                    } else {
                                                        selectedBatchIds - batch.id
                                                    }
                                                },
                                                colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                            )
                                            Column {
                                                Text(
                                                    text = batch.batchName,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = NavyPrimary
                                                )
                                                Text(
                                                    text = "${batch.courseName} • ${batch.scheduleTime}",
                                                    fontSize = 10.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                    if (fullName.isBlank() || mobile.isBlank() || email.isBlank() || subject.isBlank()) {
                        errorMessage = "Please enter teacher name, mobile, email, and subject."
                        return@Button
                    }
                    onSave(
                        fullName.trim(),
                        mobile.trim(),
                        email.trim(),
                        subject.trim(),
                        courses.trim(),
                        selectedBatchIds.toList(),
                        joiningDate.trim()
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("save_faculty_button")
            ) {
                Text(
                    text = if (existingTeacher != null) "Update Faculty" else "Save Faculty",
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

@Composable
fun AssignBatchesToTeacherDialog(
    teacher: TeacherEntity,
    allBatches: List<BatchEntity>,
    initiallySelectedBatchIds: List<Long>,
    onDismiss: () -> Unit,
    onSave: (List<Long>) -> Unit
) {
    var selectedBatchIds by remember { mutableStateOf(initiallySelectedBatchIds.toSet()) }

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
                        text = "Assign Batches",
                        fontWeight = FontWeight.Bold,
                        color = NavyPrimary,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${teacher.fullName} (${teacher.subject})",
                        fontSize = 12.sp,
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
                Text(
                    text = "Select all batches this faculty member is responsible for:",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (allBatches.isEmpty()) {
                    Text("No batches found in system", fontSize = 12.sp, color = Color.Gray)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(allBatches, key = { it.id }) { batch ->
                            val isChecked = selectedBatchIds.contains(batch.id)
                            val isAssignedToOther = batch.teacherId != null && batch.teacherId != teacher.id

                            Surface(
                                color = if (isChecked) Color(0xFFEEF2FF) else Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isChecked) NavySecondary else Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedBatchIds = if (isChecked) {
                                            selectedBatchIds - batch.id
                                        } else {
                                            selectedBatchIds + batch.id
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isChecked,
                                        onCheckedChange = { checked ->
                                            selectedBatchIds = if (checked) {
                                                selectedBatchIds + batch.id
                                            } else {
                                                selectedBatchIds - batch.id
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(checkedColor = NavyPrimary)
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = batch.batchName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = NavyPrimary
                                        )
                                        Text(
                                            text = "${batch.courseName} • ${batch.scheduleTime}",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                        if (isAssignedToOther) {
                                            Text(
                                                text = "Currently assigned to: ${batch.teacherName}",
                                                fontSize = 10.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Surface(
                                        color = Color(0xFFFEF3C7),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = batch.subject,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFB45309),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedBatchIds.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                modifier = Modifier.testTag("save_assigned_batches_button")
            ) {
                Text("Save Batch Assignments", color = NavyPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
