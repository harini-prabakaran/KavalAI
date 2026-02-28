package com.kavalai.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import android.content.Intent
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.kavalai.prototype.ui.theme.KavalAiTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)

        setContent {
            KavalAiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val displayMessage = if (sharedText != null) {
                        val risk = calculateRisk(sharedText)
                        val riskLevel = getRiskLevel(risk)

                        "$riskLevel\nRisk Score: $risk / 100\n\nMessage:\n$sharedText"
                    } else {
                        "Waiting for shared message..."
                    }

                    MessageScreen(
                        message = displayMessage,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageScreen(message: String, modifier: Modifier = Modifier) {
    Text(
        text = message,
        fontSize = 18.sp,
        modifier = modifier
    )
}
fun calculateRisk(message: String): Int {
    var score = 0

    if (message.contains("urgent", true)) score += 20
    if (message.contains("bank", true)) score += 20
    if (message.contains("http", true)) score += 25
    if (message.contains("verify", true)) score += 15
    if (message.contains("account", true)) score += 10

    return minOf(score, 100)
}
fun getRiskLevel(score: Int): String {
    return when {
        score >= 70 -> "🔴 HIGH RISK"
        score >= 40 -> "🟡 MEDIUM RISK"
        else -> "🟢 LOW RISK"
    }
}