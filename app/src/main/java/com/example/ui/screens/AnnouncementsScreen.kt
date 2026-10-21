package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FrontCraftViewModel
import com.example.ui.theme.NavyPrimary

@Composable
fun AnnouncementsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val announcements by viewModel.allAnnouncements.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Notice Board & Announcements", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text("Broadcast notifications for faculty, students, and parents", fontSize = 11.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(announcements, key = { it.id }) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (notice.priority == "URGENT") Color(0xFFFEE2E2) else Color(0xFFDBEAFE),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = notice.priority,
                                    color = if (notice.priority == "URGENT") Color(0xFFB91C1C) else Color(0xFF1D4ED8),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text(notice.publishedDate, fontSize = 11.sp, color = Color.Gray)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(notice.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(notice.content, fontSize = 12.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Audience: ${notice.targetAudience} • By: ${notice.createdBy}", fontSize = 10.sp, color = Color.LightGray)
                    }
                }
            }
        }
    }
}
