package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FrontCraftViewModel
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary

@Composable
fun SettingsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("System Settings & Role Portal", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text("Manage active role permissions and institute preferences", fontSize = 11.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Simulate Role Perspective", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Text("Select role to test permissions and workspace capabilities:", fontSize = 11.sp, color = Color.Gray)

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ADMIN" to "Admin", "TEACHER" to "Faculty", "STUDENT" to "Student", "PARENT" to "Parent").forEach { (role, label) ->
                        val isSel = currentUser.role == role
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    viewModel.switchRole(role)
                                    Toast.makeText(context, "Switched to $label role", Toast.LENGTH_SHORT).show()
                                },
                            color = if (isSel) NavyPrimary else Color(0xFFF1F5F9),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSel) GoldAccent else Color(0xFF475569),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Institution Details", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text("Name: FrontCraft PRO Coaching Institute", fontSize = 12.sp, color = Color.DarkGray)
                Text("Academic Session: 2026-2027", fontSize = 12.sp, color = Color.DarkGray)
                Text("Timezone: Asia/Kolkata (IST)", fontSize = 12.sp, color = Color.DarkGray)
                Text("Database: Room SQLite (Local Persistence)", fontSize = 12.sp, color = Color.DarkGray)
            }
        }
    }
}
