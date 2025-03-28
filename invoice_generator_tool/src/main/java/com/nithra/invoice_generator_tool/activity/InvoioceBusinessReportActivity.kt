package com.nithra.invoice_generator_tool.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon.Companion.Text
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType.Companion.Text
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nithra.invoice_generator_tool.R
import com.nithra.invoice_generator_tool.databinding.ActivityInvoioceBusinessReportBinding

class InvoioceBusinessReportActivity : AppCompatActivity() {
    lateinit var binding : ActivityInvoioceBusinessReportBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //setContentView(R.layout.activity_invoioce_business_report)
        binding = ActivityInvoioceBusinessReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val composeView = findViewById<ComposeView>(R.id.pieChartComposeView)
        composeView.setContent {
            PieChartScreen() // Load the Compose Pie Chart inside XML view
        }

    }

    @Composable
    fun PieChartScreen() {
        val pieData = listOf(
            PieChartData(75f, Color(0xFF4CAF50), "Paid"),  // Green
            PieChartData(15f, Color(0xFFFF5722), "Expenses"), // Red
            PieChartData(10f, Color(0xFF2196F3), "Unpaid")   // Blue
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "This Month Report",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            PieChart(pieData)
        }
    }
    @Composable
    fun PieChart(data: List<PieChartData>) {
        val total = data.sumOf { it.percentage.toDouble() }
        val sweepAngles = data.map { 360 * (it.percentage / total.toFloat()) }

        Canvas(
            modifier = Modifier.size(250.dp)
        ) {
            var startAngle = 0f

            data.forEachIndexed { _, item ->
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngles[data.indexOf(item)],
                    useCenter = true
                )
                startAngle += sweepAngles[data.indexOf(item)]
            }
        }
    }
}
data class PieChartData(val percentage: Float, val color: Color, val label: String)
