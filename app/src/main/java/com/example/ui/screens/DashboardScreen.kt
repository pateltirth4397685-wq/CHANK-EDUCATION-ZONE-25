package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.GroupWork
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FeePaymentEntity
import com.example.ui.AppTab
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.QuickActionTile
import com.example.ui.components.SectionTitle
import com.example.ui.components.StatsCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyCard
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allBatches by viewModel.allBatches.collectAsState()
    val allRecords by viewModel.allAttendanceRecords.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()
    val activityLogs by viewModel.recentActivityLogs.collectAsState()

    val totalStudentsCount = allStudents.size
    val activeStudentsCount = allStudents.count { it.status == "ACTIVE" }
    val totalTeachersCount = allTeachers.size
    val totalBatchesCount = allBatches.size
    val totalCapacityCount = allBatches.sumOf { it.capacity }

    val totalFeesBilled = allInvoices.sumOf { it.totalFee }
    val totalFeesCollected = allPayments.sumOf { it.amount }
    val totalFeesPending = (totalFeesBilled - totalFeesCollected).coerceAtLeast(0.0)

    val totalAttendanceEligible = allRecords.size
    val presentCount = allRecords.count { it.status == "PRESENT" || it.status == "LATE" }
    val instituteAttendancePct = if (totalAttendanceEligible > 0) {
        ((presentCount.toDouble() / totalAttendanceEligible) * 100).toInt()
    } else 0

    val avgStudentsPerBatch = if (totalBatchesCount > 0) {
        String.format(Locale.getDefault(), "%.1f", activeStudentsCount.toDouble() / totalBatchesCount)
    } else "0"

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Welcome Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = NavyCard),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Welcome back,",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                            Text(
                                text = currentUser.fullName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Surface(
                            color = GoldAccent,
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = currentUser.role,
                                color = NavyPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Institute Performance Summary (Asia/Kolkata)",
                        fontSize = 12.sp,
                        color = GoldAccent
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Fees Collected", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("₹${String.format("%,.0f", totalFeesCollected)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Column {
                            Text("Overall Attendance", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("$instituteAttendancePct%", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GoldAccent)
                        }
                        Column {
                            Text("Total Batches", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                            Text("$totalBatchesCount Batches", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Metrics Grid
        item {
            SectionTitle("Key Institutional Metrics")
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Active Students",
                        value = "$activeStudentsCount / $totalStudentsCount",
                        icon = Icons.Default.Groups,
                        iconBgColor = NavySecondary,
                        modifier = Modifier.weight(1f),
                        subText = "Enrolled Across Batches"
                    )
                    StatsCard(
                        title = "Faculty Teachers",
                        value = "$totalTeachersCount",
                        icon = Icons.Default.Person,
                        iconBgColor = BurgundyAccent,
                        modifier = Modifier.weight(1f),
                        subText = "Assigned to Batches"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Total Batches",
                        value = "$totalBatchesCount",
                        icon = Icons.Default.Class,
                        iconBgColor = GoldAccent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_total_batches"),
                        subText = "$totalCapacityCount Seats Max Limit"
                    )
                    StatsCard(
                        title = "Students per Batch",
                        value = "$avgStudentsPerBatch avg",
                        icon = Icons.Default.GroupWork,
                        iconBgColor = NavySecondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("card_students_per_batch"),
                        subText = "$activeStudentsCount across $totalBatchesCount batches"
                    )
                }
            }
        }

        // Fee Collection Trend Line Chart
        item {
            FeeCollectionTrendChart(allPayments = allPayments)
        }

        // Quick Operations
        item {
            SectionTitle("Quick Operations")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionTile(
                    title = "Batches",
                    icon = Icons.Default.Class,
                    color = GoldAccent,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.switchTab(AppTab.BATCHES)
                }
                QuickActionTile(
                    title = "Faculty",
                    icon = Icons.Default.School,
                    color = NavyPrimary,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.switchTab(AppTab.FACULTY)
                }
                QuickActionTile(
                    title = "Attendance",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF16A34A),
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.switchTab(AppTab.ATTENDANCE)
                }
                QuickActionTile(
                    title = "Fees",
                    icon = Icons.Default.CurrencyRupee,
                    color = BurgundyAccent,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.switchTab(AppTab.FEES)
                }
                QuickActionTile(
                    title = "Exams",
                    icon = Icons.Default.MenuBook,
                    color = NavySecondary,
                    modifier = Modifier.weight(1f)
                ) {
                    viewModel.switchTab(AppTab.EXAMS)
                }
            }
        }

        // Recent Audit Trail
        item {
            SectionTitle("Recent Institutional Activity")
        }

        if (activityLogs.isEmpty()) {
            item {
                Text("No recent activity recorded", color = Color.Gray, fontSize = 12.sp)
            }
        } else {
            items(activityLogs.take(5), key = { it.id }) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(log.action.replace('_', ' '), fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NavyPrimary)
                            Text(log.details, fontSize = 11.sp, color = Color.Gray)
                        }
                        Text(log.timestamp.takeLast(8), fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}

@Composable
fun FeeCollectionTrendChart(allPayments: List<FeePaymentEntity>) {
    val months = listOf("May", "Jun", "Jul", "Aug", "Sep", "Oct")
    val dataPoints = listOf(240000f, 310000f, 290000f, 360000f, 340000f, 420000f)
    val maxVal = 450000f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_trend_chart_card"),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Fee Collection Trends", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
                    }
                    Text("Last 6 Months • Tuition Realization (INR)", fontSize = 11.sp, color = Color.Gray)
                }

                Surface(
                    color = Color(0xFFDCFCE7),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("+18.2% Growth", color = Color(0xFF15803D), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height - 30.dp.toPx()
                    val stepX = width / (dataPoints.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    val points = dataPoints.mapIndexed { index, value ->
                        val x = index * stepX
                        val y = height - (value / maxVal) * height
                        Offset(x, y)
                    }

                    if (points.isNotEmpty()) {
                        path.moveTo(points[0].x, points[0].y)
                        fillPath.moveTo(points[0].x, height)
                        fillPath.lineTo(points[0].x, points[0].y)

                        for (i in 1 until points.size) {
                            val p0 = points[i - 1]
                            val p1 = points[i]
                            val cx = (p0.x + p1.x) / 2
                            path.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                            fillPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                        }

                        fillPath.lineTo(points.last().x, height)
                        fillPath.close()

                        // Draw gradient fill under line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(GoldAccent.copy(alpha = 0.35f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // Draw stroke
                        drawPath(
                            path = path,
                            color = NavyPrimary,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Draw points
                        points.forEachIndexed { idx, pt ->
                            drawCircle(color = GoldAccent, radius = 5.dp.toPx(), center = pt)
                            drawCircle(color = NavyPrimary, radius = 2.5.dp.toPx(), center = pt)
                        }
                    }
                }
            }

            // Month Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                months.forEach { m ->
                    Text(text = m, fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
