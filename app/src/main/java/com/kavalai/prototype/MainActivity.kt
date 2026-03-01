package com.kavalai.prototype

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kavalai.prototype.ui.theme.KavalAiTheme

// ---------------------------
// Screen State
// ---------------------------

sealed class AppScreen {
    object Chat : AppScreen()
    object Analysis : AppScreen()
}

// ---------------------------
// Scam Engine
// ---------------------------

object ScamEngine {

    private val regexLink =
        Regex("(?i)(https?://|bit\\.ly|tinyurl|t\\.co)")

    private val regexUrgent =
        Regex("(?i)(urgent|immediately|action required)")

    private val regexMoney =
        Regex("(?i)(payment|transfer|prize|win|bank|kyc)")

    fun analyze(message: String): AnalysisResult {

        var score = 0
        val reasons = mutableListOf<Pair<String, Float>>()

        val hasLink = regexLink.containsMatchIn(message)
        val hasUrgent = regexUrgent.containsMatchIn(message)
        val hasMoney = regexMoney.containsMatchIn(message)

        if (hasLink) {
            score += 35
            reasons.add("Suspicious Link Detected" to 0.35f)
        }

        if (hasUrgent) {
            score += 25
            reasons.add("Urgency Language Used" to 0.25f)
        }

        if (hasMoney) {
            score += 25
            reasons.add("Financial Trigger Words" to 0.25f)
        }

        if (hasLink && hasUrgent) {
            score += 15
            reasons.add("High‑Pressure Phishing Pattern" to 0.15f)
        }

        val level = when {
            score >= 75 -> "CRITICAL"
            score >= 45 -> "CAUTION"
            else -> "LOW RISK"
        }

        return AnalysisResult(
            score = score.coerceAtMost(100),
            level = level,
            reasons = reasons
        )
    }
}

data class AnalysisResult(
    val score: Int,
    val level: String,
    val reasons: List<Pair<String, Float>>
)

// ---------------------------
// Main Activity
// ---------------------------

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent?.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            KavalAiTheme {

                var screen by remember {
                    mutableStateOf(
                        if (sharedText != null) AppScreen.Analysis
                        else AppScreen.Chat
                    )
                }

                var selectedMessage by remember {
                    mutableStateOf(sharedText ?: "")
                }

                AnimatedContent(
                    targetState = screen,
                    transitionSpec = {
                        fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                    },
                    label = ""
                ) { target ->

                    when (target) {

                        AppScreen.Chat -> {
                            ChatScreen()
                        }

                        AppScreen.Analysis -> {
                            val result = remember(selectedMessage) {
                                ScamEngine.analyze(selectedMessage)
                            }

                            AnalysisScreen(
                                result = result,
                                originalMessage = selectedMessage,
                                onBack = { screen = AppScreen.Chat }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------
// WhatsApp‑Style Chat Screen
// ---------------------------

@Composable
fun ChatScreen() {

    val messages = listOf(
        "Hey, are we still meeting tomorrow at 5?",
        "Your package delivery failed. Reschedule here: bit.ly/update-delivery-info",
        "URGENT: Your bank account has been suspended. Verify now at http://secure-update-bank.com"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECE5DD))
    ) {

        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF075E54))
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Gray, CircleShape)
            )

            Spacer(Modifier.width(10.dp))

            Column {
                Text("+91 998283 XXXXXX", color = Color.White, fontWeight = FontWeight.Bold)
                Text("online", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
            }
        }

        // Chat Area
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                ReceivedBubble(msg)
            }
        }

        // Bottom Input Bar
        WhatsAppInputBar()
    }
}
@Composable
fun WhatsAppInputBar() {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF0F0F0))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        // Text Field Container
        Row(
            modifier = Modifier
                .weight(1f)
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text("😊", fontSize = 18.sp)

            Spacer(Modifier.width(8.dp))

            Text(
                "Message",
                color = Color.Gray,
                fontSize = 14.sp
            )

            Spacer(Modifier.weight(1f))

            Text("📎", fontSize = 16.sp)
        }

        Spacer(Modifier.width(8.dp))

        // Mic Button
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color(0xFF25D366), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("", fontSize = 18.sp)
        }
    }
}
// ---------------------------
// Message Bubble (Share Sheet)
// ---------------------------

@Composable
fun ReceivedBubble(message: String) {

    val context = LocalContext.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            shape = RoundedCornerShape(
                topStart = 4.dp,
                topEnd = 16.dp,
                bottomStart = 16.dp,
                bottomEnd = 16.dp
            ),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clickable {

                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, message)
                        type = "text/plain"
                    }

                    val chooser = Intent.createChooser(
                        sendIntent,
                        "Share message"
                    )

                    context.startActivity(chooser)
                }
        ) {
            Column(Modifier.padding(12.dp)) {

                Text(message)

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text("5:42 PM", fontSize = 10.sp, color = Color.Gray)
                }
            }
        }
    }
}

// ---------------------------
// Analysis Screen
// ---------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    result: AnalysisResult,
    originalMessage: String,
    onBack: () -> Unit
) {

    val riskColor = when (result.level) {
        "CRITICAL" -> Color(0xFFE53935)
        "CAUTION" -> Color(0xFFFFB300)
        else -> Color(0xFF43A047)
    }

    Column(Modifier.fillMaxSize()) {

        TopAppBar(
            title = { Text("KavalAI Insight") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            item {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color(0xFF232F3E)
                ) {
                    Text(
                        "AI Powered by Amazon Bedrock",
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }

            item {
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(riskColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {

                            Text(result.level, color = riskColor)

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
                Text("Risk Breakdown", fontWeight = FontWeight.Bold)

                result.reasons.forEach { (reason, weight) ->
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = weight,
                        modifier = Modifier.fillMaxWidth(),
                        color = riskColor,
                        trackColor = Color(0xFFEEEEEE),
                        strokeCap = StrokeCap.Round
                    )
                    Text(reason, fontSize = 13.sp)
                }
            }

            item {
                Text("Original Message", fontWeight = FontWeight.Bold)
                Card {
                    Text(
                        originalMessage,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}