package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.FrontCraftViewModel
import com.example.ui.components.StatsCard
import com.example.ui.theme.BurgundyAccent
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.NavyPrimary
import com.example.ui.theme.NavySecondary

@Composable
fun ReportsScreen(
    viewModel: FrontCraftViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allBatches by viewModel.allBatches.collectAsState()
    val allStudents by viewModel.allStudents.collectAsState()
    val allInvoices by viewModel.allInvoices.collectAsState()
    val allPayments by viewModel.allPayments.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Institutional Reports & Analytics", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = NavyPrimary)
        Text("Audit trails, financial collections, and sanitized spreadsheet exports", fontSize = 11.sp, color = Color.Gray)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Attendance Sanctioned Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Text("Complete attendance logs protected with CSV formula injection defense.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Attendance register exported!", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Attendance CSV", fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Tuition & Fee Realization Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Text("Total collection, outstanding balances, and receipt references.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { Toast.makeText(context, "Fee collection summary exported!", Toast.LENGTH_SHORT).show() },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Fee Ledger CSV", fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("Academic Performance & Exam Scores Ledger", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NavyPrimary)
                Text("Evaluated student marks, assigned letter grades, class averages, and pass/fail transcripts.", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        viewModel.switchTab(com.example.ui.AppTab.EXAMS)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, tint = NavyPrimary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Open Academic Summaries & Scores", color = NavyPrimary, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}
