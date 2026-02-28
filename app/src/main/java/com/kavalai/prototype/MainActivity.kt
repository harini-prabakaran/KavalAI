package com.kavalai.prototype

import android.content.Intent
import android.os.Bundle
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavalai.prototype.ui.theme.KavalAiTheme

// =====================
// 🧠 SCAM ENGINE
// =====================

object ScamEngine {
    private val regexLogistics =
        Regex("(?i)(delivery|package|courier|shipment|reschedule)")

    private val regexUrgency =
        Regex("(?i)u[.\\s-]*r[.\\s-]*g[.\\s-]*e[.\\s-]*n[.\\s-]*t|immediately|action required")

    private val regexLink =
        Regex("(?i)(https?://|bit\\.ly|t\\.co|tinyurl|click\\.here)")

    private val regexMoney =
        Regex("(?i)(\\$|rs|dollars|amount|payment|transfer|win|won|prize)")

    fun analyzeMessage(message: String): AnalysisResult {

        val reasons = mutableListOf<Pair<String, Float>>()
        var score = 0

        val hasLink = regexLink.containsMatchIn(message)
        val hasUrgency = regexUrgency.containsMatchIn(message)
        val hasMoney = regexMoney.containsMatchIn(message)
        val hasLogistics = regexLogistics.containsMatchIn(message)

        if (hasLogistics && hasLink) {
            score += 25
            reasons.add("Fake Delivery Phishing Pattern" to 0.25f)
        }

        if (hasLink) {
            score += 35
            reasons.add("Suspicious Link Detected" to 0.35f)
        }

        if (hasUrgency) {
            score += 25
            reasons.add("Artificial Urgency Language" to 0.25f)
        }

        if (hasMoney) {
            score += 20
            reasons.add("Financial/Payment Trigger" to 0.20f)
        }

        if (hasLink && hasUrgency) {
            score += 20
            reasons.add("High‑Pressure Phishing Combo" to 0.20f)
        }

        val pattern = when {
            message.contains("kyc", true) -> "Bank KYC Phishing"
            message.contains("otp", true) -> "OTP Credential Theft"
            message.contains("delivery", true) || message.contains("package", true) ->
                "Fake Logistics Scam"
            message.contains("lottery", true) || message.contains("prize", true) ->
                "Advance Fee Fraud"
            else -> "Social Engineering Attempt"
        }

        return AnalysisResult(
            score = minOf(score, 100),
            level = when {
                score >= 75 -> "CRITICAL"
                score >= 45 -> "CAUTION"
                else -> "LOW RISK"
            },
            reasons = reasons,
            pattern = pattern
        )
    }
}

data class AnalysisResult(
    val score: Int,
    val level: String,
    val reasons: List<Pair<String, Float>>,
    val pattern: String
)

// =====================
// 📱 MAIN ACTIVITY
// =====================

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            KavalAiTheme {

                Scaffold(modifier = Modifier.fillMaxSize()) { padding ->

                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        color = Color(0xFFF8F9FB)
                    ) {

                        if (sharedText != null) {

                            val result = remember(sharedText) {
                                ScamEngine.analyzeMessage(sharedText)
                            }

                            MessageScreen(result, sharedText)

                        } else {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Waiting for a message to analyze...",
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

// =====================
// 🎨 UI
// =====================

@Composable
fun MessageScreen(result: AnalysisResult, originalMessage: String) {

    val riskColor = when (result.level) {
        "CRITICAL" -> Color(0xFFE53935)
        "CAUTION" -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        item {
            Text(
                "KavalAI Insight",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }

        item {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {

                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .background(riskColor.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text(
                            result.level,
                            color = riskColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        val animatedScore by animateFloatAsState(
                            targetValue = result.score.toFloat(),
                            animationSpec = tween(1000),
                            label = ""
                        )

                        Text(
                            "${animatedScore.toInt()}%",
                            color = riskColor,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        item {
            InfoCard(
                title = "Detected Strategy",
                content = result.pattern,
                color = riskColor
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(Modifier.padding(20.dp)) {

                    Text(
                        "Why is this risky?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )

                    Spacer(Modifier.height(16.dp))

                    result.reasons.forEach { (reason, weight) ->
                        ReasonItem(reason, weight, riskColor)
                    }
                }
            }
        }

        item {
            Text(
                "Input Text",
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(
                    originalMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        item {
            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
fun ReasonItem(label: String, progress: Float, color: Color) {

    Column(Modifier.padding(bottom = 12.dp)) {

        Text(label, fontSize = 14.sp)

        Spacer(Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = color,
            trackColor = Color(0xFFEEEEEE),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun InfoCard(title: String, content: String, color: Color) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(title, fontWeight = FontWeight.Bold, color = color, fontSize = 12.sp)
            Text(content, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}