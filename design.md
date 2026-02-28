# Design Document: KavalAI 

## Overview

KavalAI is an Android application that provides real-time scam detection for WhatsApp messages through a privacy-first, on-device AI architecture. The system operates in two distinct layers: Layer 1 provides passive, lightweight analysis of notification previews, while Layer 2 offers deep, comprehensive analysis when users explicitly share messages. All processing occurs entirely on-device using pre-loaded AI models, ensuring zero cloud dependency and complete privacy preservation.

The core innovation lies in the multi-trigger activation system that requires at least two risk signals before flagging a message, significantly reducing false positives while maintaining high detection accuracy. The system uses semantic similarity matching against curated scam templates combined with weighted risk scoring to provide explainable, actionable insights to elderly users.

## Architecture

### System Components

```
┌─────────────────────────────────────────────────────────────┐
│                     KavalAI Android App                      │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  Notification    │         │   Share Intent   │          │
│  │    Listener      │         │     Handler      │          │
│  └────────┬─────────┘         └────────┬─────────┘          │
│           │                            │                     │
│           ▼                            ▼                     │
│  ┌──────────────────┐         ┌──────────────────┐          │
│  │  Layer 1         │         │  Layer 2         │          │
│  │  Analyzer        │         │  Analyzer        │          │
│  │  (Lightweight)   │         │  (Deep Scan)     │          │
│  └────────┬─────────┘         └────────┬─────────┘          │
│           │                            │                     │
│           └────────────┬───────────────┘                     │
│                        ▼                                     │
│           ┌────────────────────────┐                         │
│           │   Multi-Trigger        │                         │
│           │   System               │                         │
│           └────────────┬───────────┘                         │
│                        ▼                                     │
│           ┌────────────────────────┐                         │
│           │   Risk Engine          │                         │
│           │   - Signal Detector    │                         │
│           │   - Weighted Scorer    │                         │
│           │   - Template Matcher   │                         │
│           └────────────┬───────────┘                         │
│                        ▼                                     │
│           ┌────────────────────────┐                         │
│           │   Explainability       │                         │
│           │   Panel                │                         │
│           └────────────────────────┘                         │
│                                                               │
│  ┌──────────────────────────────────────────────┐            │
│  │   Privacy Dashboard & Settings               │            │
│  └──────────────────────────────────────────────┘            │
│                                                               │
│  ┌──────────────────────────────────────────────┐            │
│  │   On-Device AI Models                        │            │
│  │   - Embedding Model (Multilingual)           │            │
│  │   - Scam Template Embeddings                 │            │
│  │   - Language Detector                        │            │
│  └──────────────────────────────────────────────┘            │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

### Two-Layer Analysis Flow

**Layer 1 (Passive - Notification-Based):**
1. Android notification listener intercepts WhatsApp notification
2. Extract preview text (typically first 100-150 characters)
3. Quick signal detection (forwarded flag, URL presence, urgency keywords)
4. If 2+ triggers detected, perform lightweight template matching
5. Display risk hint overlay on notification (if risky)
6. Total processing time: < 2 seconds

**Layer 2 (Active - Share-to-App):**
1. User explicitly shares message via WhatsApp share menu
2. Receive full message content and available metadata
3. Comprehensive signal detection (all triggers)
4. Full template matching across all categories
5. URL extraction and analysis
6. Weighted risk scoring
7. Display detailed explainability panel
8. Total processing time: < 5 seconds

### Data Flow

```
WhatsApp Message
    │
    ├─→ [Notification] ─→ Layer 1 ─→ Quick Analysis ─→ Risk Hint
    │                                                      │
    │                                                      ▼
    │                                                   [Discard]
    │
    └─→ [User Shares] ─→ Layer 2 ─→ Deep Analysis ─→ Explainability Panel
                                                          │
                                                          ▼
                                                       [Discard]
```

**Critical Privacy Guarantee:** No message content persists beyond the analysis session. All data is held in memory only during processing and immediately discarded.

## Components and Interfaces

### 1. Notification Listener Service

**Purpose:** Intercepts WhatsApp notifications to enable Layer 1 passive analysis.

**Interface:**
```kotlin
interface NotificationListener {
    fun onNotificationPosted(notification: StatusBarNotification)
    fun extractMessagePreview(notification: StatusBarNotification): MessagePreview?
    fun isWhatsAppNotification(notification: StatusBarNotification): Boolean
}

data class MessagePreview(
    val text: String,
    val sender: String?,
    val timestamp: Long,
    val isGroup: Boolean
)
```

**Behavior:**
- Filters for WhatsApp package notifications only
- Extracts preview text from notification extras
- Passes preview to Layer 1 Analyzer
- Operates only when notification permission is granted

### 2. Share Intent Handler

**Purpose:** Receives messages explicitly shared by users for Layer 2 deep analysis.

**Interface:**
```kotlin
interface ShareIntentHandler {
    fun handleSharedText(intent: Intent): SharedMessage?
    fun extractMessageContent(intent: Intent): String
    fun extractMetadata(intent: Intent): MessageMetadata?
}

data class SharedMessage(
    val content: String,
    val metadata: MessageMetadata?,
    val timestamp: Long
)

data class MessageMetadata(
    val sender: String?,
    val isForwarded: Boolean,
    val forwardCount: Int?
)
```

### 3. Multi-Trigger System

**Purpose:** Implements the 2+ signal requirement to reduce false positives.

**Interface:**
```kotlin
interface MultiTriggerSystem {
    fun detectSignals(message: String, metadata: MessageMetadata?): Set<RiskSignal>
    fun meetsThreshold(signals: Set<RiskSignal>): Boolean
}

enum class RiskSignal {
    FORWARDED_MESSAGE,
    CONTAINS_URL,
    URGENCY_KEYWORDS,
    UNKNOWN_SENDER,
    PHONE_NUMBER_REQUEST,
    FINANCIAL_KEYWORDS,
    AUTHORITY_IMPERSONATION
}
```

**Signal Detection Logic:**
- **FORWARDED_MESSAGE**: Check metadata forwarded flag or detect "Forwarded" text
- **CONTAINS_URL**: Regex pattern matching for URLs (http/https/shortened links)
- **URGENCY_KEYWORDS**: Match against curated list ("urgent", "immediately", "expire", "last chance", "तुरंत", "जल्दी")
- **UNKNOWN_SENDER**: Sender not in user's contacts (when available)
- **PHONE_NUMBER_REQUEST**: Detect patterns asking for phone/OTP ("send OTP", "verify number")
- **FINANCIAL_KEYWORDS**: Match financial terms ("bank", "account", "KYC", "payment", "खाता")
- **AUTHORITY_IMPERSONATION**: Match government/official terms ("government", "police", "सरकार")

**Threshold Logic:**
```
if (signals.size >= 2) {
    proceedWithRiskScoring()
} else {
    classifyAsLowRisk()
}
```

### 4. Risk Engine

**Purpose:** Calculates weighted risk scores and classifies messages.

**Interface:**
```kotlin
interface RiskEngine {
    fun calculateRiskScore(
        signals: Set<RiskSignal>,
        templateMatches: List<TemplateMatch>,
        message: String
    ): RiskScore
    
    fun classifyRisk(score: Int): RiskLevel
}

data class RiskScore(
    val value: Int, // 0-100
    val level: RiskLevel,
    val breakdown: Map<String, Int>
)

enum class RiskLevel {
    LOW,      // 0-33
    MEDIUM,   // 34-66
    HIGH      // 67-100
}
```

**Weighted Scoring Formula:**
```
Base Score = 0

Signal Weights:
- FORWARDED_MESSAGE: +15
- CONTAINS_URL: +20
- URGENCY_KEYWORDS: +15
- UNKNOWN_SENDER: +10
- PHONE_NUMBER_REQUEST: +25
- FINANCIAL_KEYWORDS: +20
- AUTHORITY_IMPERSONATION: +25

Template Match Bonus:
- High similarity (>0.85): +30
- Medium similarity (0.7-0.85): +20

Final Score = min(100, Base Score + Signal Weights + Template Bonus)
```

### 5. Template Matcher

**Purpose:** Performs semantic similarity matching against curated scam templates.

**Interface:**
```kotlin
interface TemplateMatcher {
    fun matchTemplates(message: String, language: Language): List<TemplateMatch>
    fun computeSimilarity(messageEmbedding: FloatArray, templateEmbedding: FloatArray): Float
    fun getTemplatesByCategory(category: ScamCategory): List<ScamTemplate>
}

data class TemplateMatch(
    val category: ScamCategory,
    val similarity: Float, // 0.0-1.0
    val templateId: String
)

enum class ScamCategory {
    FAKE_KYC,
    GOVERNMENT_SUBSIDY,
    LOTTERY,
    LOAN_APPROVAL,
    JOB_OFFER,
    IMPERSONATION
}

data class ScamTemplate(
    val id: String,
    val category: ScamCategory,
    val embedding: FloatArray,
    val exampleText: String,
    val language: Language
)
```

**Template Matching Pipeline:**
1. Detect message language (English/Hindi/Mixed)
2. Generate message embedding using on-device model
3. Compute cosine similarity with all template embeddings
4. Filter matches where similarity > 0.7
5. Return top 3 matches sorted by similarity

**Cosine Similarity:**
```
similarity = (A · B) / (||A|| × ||B||)

where:
A = message embedding vector
B = template embedding vector
```

**Scam Template Examples:**

*Fake KYC Scam:*
```
"Your bank account will be blocked. Update KYC immediately by clicking: [URL]"
"आपका खाता बंद हो जाएगा। तुरंत KYC अपडेट करें: [URL]"
```

*Government Subsidy Scam:*
```
"Congratulations! You are eligible for ₹5000 government subsidy. Claim now: [URL]"
"बधाई हो! आपको ₹5000 सरकारी सब्सिडी मिली है। अभी क्लेम करें: [URL]"
```

*Lottery Scam:*
```
"You won ₹25 lakhs in lucky draw! Send your details to claim prize."
```

*Loan Approval Scam:*
```
"Your loan of ₹2 lakhs is approved! Pay ₹5000 processing fee to receive amount."
```

*Job Offer Scam:*
```
"Earn ₹50,000/month from home! Register now with ₹2000 registration fee."
```

*Impersonation Scam:*
```
"This is police department. Your Aadhaar is used in illegal activity. Call immediately."
```

### 6. Embedding Model

**Purpose:** Generates semantic embeddings for messages and templates.

**Model Selection:** Multilingual sentence transformer (e.g., paraphrase-multilingual-MiniLM-L12-v2)
- Size: ~120MB
- Languages: English, Hindi, 50+ others
- Embedding dimension: 384
- Inference time: ~100-200ms on mobile

**Interface:**
```kotlin
interface EmbeddingModel {
    fun encode(text: String): FloatArray
    fun batchEncode(texts: List<String>): List<FloatArray>
    fun isLoaded(): Boolean
}
```

### 7. Language Detector

**Purpose:** Identifies message language for appropriate template selection.

**Interface:**
```kotlin
interface LanguageDetector {
    fun detect(text: String): Language
    fun isCodeMixed(text: String): Boolean
}

enum class Language {
    ENGLISH,
    HINDI,
    MIXED
}
```

**Detection Logic:**
- Check Unicode ranges (Devanagari: U+0900-U+097F)
- If >30% Devanagari characters: HINDI
- If >30% Latin characters and >10% Devanagari: MIXED
- Otherwise: ENGLISH

### 8. Explainability Panel

**Purpose:** Displays risk analysis results in user-friendly format.

**Interface:**
```kotlin
interface ExplainabilityPanel {
    fun displayRiskScore(score: RiskScore)
    fun displayTriggeredSignals(signals: Set<RiskSignal>)
    fun displayTemplateMatches(matches: List<TemplateMatch>)
    fun displayRecommendation(level: RiskLevel)
}
```

**UI Components:**
- Risk level indicator (color-coded: green/yellow/red)
- Risk score gauge (0-100)
- List of triggered signals with icons
- Matched scam category badges
- Plain language explanation
- Actionable recommendations

**Example Display:**
```
⚠️ HIGH RISK (Score: 85/100)

Suspicious Signals Detected:
✓ Message was forwarded
✓ Contains suspicious link
✓ Requests urgent action
✓ Mentions bank/KYC

Similar to Known Scams:
🎯 Fake KYC Scam (92% match)

Recommendation:
Do not click any links. Do not share personal information.
Banks never ask for KYC updates via WhatsApp.
```

### 9. Privacy Dashboard

**Purpose:** Provides transparency and user control over privacy settings.

**Interface:**
```kotlin
interface PrivacyDashboard {
    fun displayPrivacyStatus()
    fun toggleNotificationAccess()
    fun toggleGuardianSummary()
    fun displayPermissionStatus()
    fun showDataPolicy()
}
```

**Dashboard Sections:**
1. **Privacy Guarantees:**
   - ✓ No message storage
   - ✓ No cloud connection
   - ✓ No tracking
   - ✓ All processing on-device

2. **Active Permissions:**
   - Notification Access: [ON/OFF]
   - Share Intent: [Always Active]

3. **Optional Features:**
   - Guardian Weekly Summary: [ON/OFF]

4. **Data Policy:**
   - Clear statement of no data retention
   - Explanation of on-device processing
   - WhatsApp compliance statement

### 10. Guardian Summary Generator

**Purpose:** Creates privacy-safe weekly summaries for guardians.

**Interface:**
```kotlin
interface GuardianSummaryGenerator {
    fun generateWeeklySummary(): GuardianSummary
    fun shouldGenerateSummary(): Boolean
}

data class GuardianSummary(
    val weekStartDate: LocalDate,
    val weekEndDate: LocalDate,
    val totalMessagesAnalyzed: Int,
    val riskDistribution: Map<RiskLevel, Int>,
    val topScamCategories: List<ScamCategory>
)
```

**Summary Content (Privacy-Safe):**
```
Weekly Safety Summary
Jan 15-21, 2024

Messages Analyzed: 47
├─ Low Risk: 42
├─ Medium Risk: 3
└─ High Risk: 2

Common Scam Types Detected:
• Fake KYC attempts
• Lottery scams

No message content or sender information included.
```

## Data Models

### Message Analysis Session

```kotlin
data class AnalysisSession(
    val sessionId: UUID,
    val layer: AnalysisLayer,
    val messageText: String,
    val metadata: MessageMetadata?,
    val startTime: Long,
    val detectedSignals: Set<RiskSignal>,
    val templateMatches: List<TemplateMatch>,
    val riskScore: RiskScore
) {
    // Session exists only in memory during analysis
    // Automatically garbage collected after display
}

enum class AnalysisLayer {
    LAYER_1_PASSIVE,
    LAYER_2_ACTIVE
}
```

### Risk Signal Detection Result

```kotlin
data class SignalDetectionResult(
    val signal: RiskSignal,
    val confidence: Float, // 0.0-1.0
    val evidence: String // What triggered this signal
)
```

### Template Database

```kotlin
data class TemplateDatabase(
    val templates: Map<ScamCategory, List<ScamTemplate>>,
    val version: String,
    val lastUpdated: LocalDate
) {
    // Loaded once at app startup
    // Stored in memory for fast access
    // Can be updated via app update (not cloud)
}
```

## Correctness Properties


*A property is a characteristic or behavior that should hold true across all valid executions of a system—essentially, a formal statement about what the system should do. Properties serve as the bridge between human-readable specifications and machine-verifiable correctness guarantees.*

### Property 1: Multi-Trigger Signal Detection

*For any* message with embedded risk indicators (forwarded flags, URLs, urgency keywords, unknown sender markers, phone requests, financial terms, authority impersonation), the Multi_Trigger_System should correctly identify and return all present risk signals.

**Validates: Requirements 1.1, 1.4, 1.5, 1.6, 1.7**

### Property 2: Multi-Trigger Threshold Enforcement

*For any* message, if fewer than 2 risk signals are detected, the system should classify it as low risk without full scoring; if 2 or more signals are detected, the system should proceed with full risk scoring.

**Validates: Requirements 1.2, 1.3**

### Property 3: Risk Score Bounds and Classification

*For any* message that passes the multi-trigger threshold, the calculated risk score should be between 0 and 100 (inclusive), and the classification should be Low for scores 0-33, Medium for scores 34-66, and High for scores 67-100.

**Validates: Requirements 2.1, 2.2, 2.3, 2.4**

### Property 4: Template Match Threshold

*For any* message and scam template pair, if the computed cosine similarity exceeds 0.7, the Template_Matcher should flag a template match; if similarity is 0.7 or below, no match should be flagged.

**Validates: Requirements 3.3**

### Property 5: Layer 1 Analysis Flow

*For any* WhatsApp notification with extractable preview text, the Layer_1_Analyzer should extract the text, perform risk analysis, and display a risk hint overlay if and only if risk is detected.

**Validates: Requirements 4.1, 4.2, 4.3**

### Property 6: Layer 2 Analysis Completeness

*For any* message shared to KavalAI, the Layer_2_Analyzer should receive the full content, perform comprehensive analysis (including metadata analysis, template matching, and URL extraction), and display the Explainability_Panel with complete results.

**Validates: Requirements 5.1, 5.2, 5.3, 5.4, 5.5, 5.6**

### Property 7: Explainability Panel Completeness

*For any* risk analysis result, the Explainability_Panel should display all triggered risk signals, all matched scam categories (if any), and the overall risk score with classification.

**Validates: Requirements 6.1, 6.2, 6.3**

### Property 8: Code-Mixed Language Analysis

*For any* message containing both English and Hindi text (code-mixed), the system should successfully detect the mixed language and perform risk analysis without errors.

**Validates: Requirements 7.3**

### Property 9: Localized Output

*For any* user-selected language preference and analysis result, the Explainability_Panel should display all text content in the user's preferred language.

**Validates: Requirements 7.5**

### Property 10: Privacy Guarantee - No Network Transmission

*For any* message analysis session (Layer 1 or Layer 2), the system should complete the entire analysis without making any network calls or transmitting any data to external servers.

**Validates: Requirements 8.2**

### Property 11: Privacy Guarantee - No Persistent Storage

*For any* message analysis session, after the analysis is complete and results are displayed, no message content or metadata should be persistently stored in any database, file, or shared preferences.

**Validates: Requirements 8.3, 8.4, 8.5**

### Property 12: Permission-Based Functionality Adaptation

*For any* permission state change (notification access granted/revoked), the system should immediately adapt its functionality, enabling Layer 1 when granted and disabling Layer 1 (while maintaining Layer 2) when revoked.

**Validates: Requirements 9.6**

### Property 13: Guardian Summary Privacy

*For any* generated guardian weekly summary, the summary should contain only aggregate risk counts and scam category statistics, and should never contain message content, sender information, or any personally identifiable information.

**Validates: Requirements 10.1, 10.2, 10.3, 10.4, 10.5**

### Property 14: Guardian Summary Conditional Generation

*For any* week when the guardian feature is disabled, no summary should be generated or transmitted; when enabled, exactly one summary should be generated per week.

**Validates: Requirements 10.6**

### Property 15: WhatsApp Compliance

*For any* message analysis operation, the system should never attempt to decrypt WhatsApp messages, access WhatsApp's message database, or intercept WhatsApp network traffic—only accessing notification content provided by Android OS or content explicitly shared via share intent.

**Validates: Requirements 12.1, 12.2, 12.3, 12.4, 12.5**

### Property 16: Offline Operation Consistency

*For any* message analysis operation, the system should function identically whether network connectivity is available or unavailable, with no degradation in functionality or accuracy when offline.

**Validates: Requirements 15.1, 15.5**

## Error Handling

### Error Categories

**1. Model Loading Errors**
- **Scenario:** Embedding model fails to load at app startup
- **Handling:** Display error dialog, disable analysis features, prompt user to reinstall app
- **Recovery:** Retry model loading on next app launch

**2. Insufficient Memory Errors**
- **Scenario:** Device runs out of memory during embedding generation
- **Handling:** Catch OutOfMemoryError, gracefully degrade to rule-based analysis only (no template matching)
- **User Feedback:** "Analysis running in limited mode due to device memory constraints"

**3. Malformed Notification Errors**
- **Scenario:** Notification structure doesn't match expected WhatsApp format
- **Handling:** Log error silently, skip analysis for that notification
- **Impact:** No user-visible error, Layer 2 remains available

**4. Share Intent Parsing Errors**
- **Scenario:** Shared content cannot be extracted from intent
- **Handling:** Display error message: "Unable to analyze this message format"
- **Recovery:** User can try sharing again

**5. Language Detection Errors**
- **Scenario:** Message contains unsupported script or language
- **Handling:** Default to English analysis, log warning
- **Impact:** Analysis proceeds with potentially reduced accuracy

**6. Permission Revocation During Operation**
- **Scenario:** User revokes notification permission while app is running
- **Handling:** Detect permission change, disable Layer 1 immediately, show notification in app
- **Recovery:** Automatic when permission is re-granted

**7. Template Database Corruption**
- **Scenario:** Template embeddings file is corrupted
- **Handling:** Fall back to signal-based analysis only, disable template matching
- **User Feedback:** "Running in limited mode. Please update the app."

### Error Handling Principles

1. **Fail Gracefully:** Never crash the app; degrade functionality when necessary
2. **User Transparency:** Inform users when features are unavailable and why
3. **Privacy Preservation:** Never log message content in error logs
4. **Silent Failures:** For non-critical errors (e.g., malformed notifications), fail silently
5. **Recovery Paths:** Always provide clear recovery actions for users

### Error Logging

**What to Log:**
- Error type and timestamp
- Component where error occurred
- Device model and Android version
- Available memory at time of error

**What NOT to Log:**
- Message content
- Sender information
- User personal data
- Analysis results

## Testing Strategy

### Dual Testing Approach

KavalAI requires both unit testing and property-based testing to ensure comprehensive correctness:

**Unit Tests** focus on:
- Specific examples of known scam messages
- Edge cases (empty messages, very long messages, special characters)
- Error conditions (null inputs, malformed data)
- Integration points between components
- UI component rendering

**Property-Based Tests** focus on:
- Universal properties that hold for all inputs
- Comprehensive input coverage through randomization
- Invariants that must be maintained
- Privacy guarantees across all execution paths

### Property-Based Testing Configuration

**Framework:** Use Kotest Property Testing for Kotlin
- Minimum 100 iterations per property test
- Each test must reference its design document property
- Tag format: **Feature: kavalai-scam-protection, Property {number}: {property_text}**

**Example Property Test Structure:**
```kotlin
class MultiTriggerSystemPropertyTest : StringSpec({
    "Property 2: Multi-Trigger Threshold Enforcement" {
        checkAll(100, Arb.message()) { message ->
            // Feature: kavalai-scam-protection, Property 2
            val signals = multiTriggerSystem.detectSignals(message)
            val result = riskEngine.analyze(message, signals)
            
            if (signals.size < 2) {
                result.level shouldBe RiskLevel.LOW
                result.fullScoringPerformed shouldBe false
            } else {
                result.fullScoringPerformed shouldBe true
            }
        }
    }
})
```

### Test Data Generators

**Message Generators:**
- Random text with configurable signal presence
- Known scam message templates
- Legitimate message patterns
- Code-mixed English/Hindi text
- Edge cases (empty, very long, special characters)

**Notification Generators:**
- Valid WhatsApp notification structures
- Malformed notification structures
- Notifications with/without preview text

**Template Generators:**
- Scam templates with varying similarity scores
- Templates in different languages

### Unit Test Coverage

**Critical Unit Tests:**
1. Each scam category template matching (6 examples)
2. Language detection for English, Hindi, and mixed text
3. Risk score calculation with known signal combinations
4. Cosine similarity computation accuracy
5. UI component rendering with various risk levels
6. Permission state transitions
7. Guardian summary generation with privacy constraints
8. Error handling for each error category

### Integration Testing

**Key Integration Scenarios:**
1. End-to-end Layer 1 flow: Notification → Analysis → Risk Hint
2. End-to-end Layer 2 flow: Share → Analysis → Explainability Panel
3. Permission grant/revoke during active session
4. App restart with model reloading
5. Memory pressure scenarios
6. Offline operation verification

### Privacy Testing

**Privacy Verification Tests:**
1. Network traffic monitoring during analysis (should be zero)
2. File system inspection after analysis (no new files)
3. Database inspection after analysis (no new records)
4. Memory dump analysis after analysis (no message content retained)
5. Guardian summary content inspection (no PII present)

### Performance Testing

**Performance Benchmarks:**
- Layer 1 analysis: < 2 seconds (target: < 1 second)
- Layer 2 analysis: < 5 seconds (target: < 3 seconds)
- Model loading time: < 10 seconds on mid-range devices
- Memory usage: < 150MB during active analysis
- Battery impact: < 2% per hour of background operation

### False Positive/Negative Testing

**Test Dataset:**
- 100 known scam messages (ground truth: positive)
- 100 legitimate messages (ground truth: negative)
- 50 ambiguous messages (manual review)

**Target Metrics:**
- False Positive Rate: < 10%
- False Negative Rate: < 5%
- Precision: > 85%
- Recall: > 90%

### Accessibility Testing

**Requirements:**
- Screen reader compatibility for all UI elements
- Minimum touch target size: 48dp
- Color contrast ratio: 4.5:1 for text
- Support for large text sizes
- Keyboard navigation support

### Localization Testing

**Test Coverage:**
- English UI with English messages
- Hindi UI with Hindi messages
- English UI with Hindi messages
- Hindi UI with English messages
- Code-mixed messages in both UI languages

## Security Considerations

### Threat Model

**Threats In Scope:**
1. **Scam Message Detection Evasion:** Attackers crafting messages to bypass detection
2. **Privacy Leakage:** Unauthorized access to analyzed message content
3. **Model Poisoning:** Malicious app updates with compromised models
4. **Permission Abuse:** Misuse of notification access permission

**Threats Out of Scope:**
1. Device-level attacks (rooted devices, malware)
2. WhatsApp platform vulnerabilities
3. Physical device access attacks
4. Social engineering targeting the app itself

### Security Measures

**1. Model Integrity**
- Embed model checksums in app binary
- Verify model integrity on load
- Reject corrupted or modified models

**2. Permission Minimization**
- Request only essential permissions (notification access)
- Clearly explain permission usage
- Function in degraded mode when permissions denied

**3. Memory Security**
- Clear sensitive data from memory after use
- Avoid logging sensitive information
- Use secure memory allocation for embeddings

**4. Code Obfuscation**
- Apply ProGuard/R8 obfuscation
- Protect risk scoring algorithms from reverse engineering
- Obfuscate template matching logic

**5. Update Security**
- Sign app updates with secure key
- Verify update signatures before installation
- Use Google Play's app signing

### Privacy-by-Design Principles

1. **Data Minimization:** Collect and process only what's necessary for analysis
2. **Purpose Limitation:** Use data only for scam detection, nothing else
3. **Storage Limitation:** Retain no data beyond analysis session
4. **Transparency:** Clearly communicate all data practices
5. **User Control:** Provide granular controls over features

### Compliance Considerations

**WhatsApp Terms of Service:**
- No automated message sending
- No bulk message processing
- No unauthorized data collection
- Respect end-to-end encryption

**Android Policies:**
- Notification listener service used only for declared purpose
- No background data collection
- Clear privacy policy disclosure

**Indian Data Protection:**
- No cross-border data transfer (all on-device)
- User consent for all data processing
- Right to disable features

## Implementation Notes

### Technology Stack

**Language:** Kotlin
**Minimum SDK:** Android 8.0 (API 26)
**Target SDK:** Android 14 (API 34)

**Key Libraries:**
- TensorFlow Lite: On-device ML inference
- Sentence Transformers (ONNX): Embedding generation
- Kotest: Property-based testing
- Jetpack Compose: UI framework
- Room: Local database (for app settings only, not messages)
- WorkManager: Guardian summary scheduling

### Model Selection

**Embedding Model:** paraphrase-multilingual-MiniLM-L12-v2
- Converted to TensorFlow Lite format
- Quantized to reduce size (120MB → ~30MB)
- Supports 50+ languages including English and Hindi

### Performance Optimizations

1. **Model Quantization:** Use 8-bit quantization for faster inference
2. **Lazy Loading:** Load templates only when needed
3. **Caching:** Cache embeddings for frequently seen patterns
4. **Batch Processing:** Process multiple signals in parallel
5. **Background Threads:** Run analysis on background threads to avoid UI blocking

### Deployment Strategy

**MVP Scope:**
- Layer 1 and Layer 2 analysis
- 6 scam template categories
- English and Hindi support
- Basic explainability panel
- Privacy dashboard

**Post-MVP:**
- Additional regional languages
- More scam categories
- Enhanced explainability with examples
- Guardian app for summary viewing
- Community-reported scam patterns

### Monitoring and Improvement

**Anonymous Telemetry (Opt-in Only):**
- Risk level distribution (no message content)
- Feature usage statistics
- Error rates by category
- Performance metrics

**Continuous Improvement:**
- Regular template updates via app updates
- Model retraining with new scam patterns
- User feedback integration
- False positive/negative analysis

## Appendix: Scam Template Details

### Template Creation Process

1. Collect real scam messages from public sources
2. Anonymize and categorize by scam type
3. Generate embeddings using multilingual model
4. Store embeddings in app bundle
5. Update templates with each app release

### Template Storage Format

```kotlin
data class SerializedTemplate(
    val id: String,
    val category: String,
    val embedding: FloatArray,
    val language: String,
    val createdDate: String
)

// Stored as JSON in assets/scam_templates.json
```

### Template Update Mechanism

- Templates bundled with app (no cloud dependency)
- Updates delivered via Google Play app updates
- Version tracking to ensure compatibility
- Backward compatibility with older template versions
