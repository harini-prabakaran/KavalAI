# Requirements Document: KavalAI 

## Introduction

KavalAI is an Android-based privacy-first AI safety layer designed to protect elderly users from scam messages on WhatsApp. Built for the AI for Bharat initiative as a national-level hackathon submission, KavalAI addresses the critical problem of digitally vulnerable senior citizens falling victim to sophisticated scam messages. The system operates entirely on-device, respecting WhatsApp's end-to-end encryption while providing intelligent risk assessment through a two-layer analysis model.

## Glossary

- **KavalAI_System**: The complete on-device AI safety layer application
- **Risk_Engine**: The on-device component that analyzes messages and calculates risk scores
- **Layer_1_Analyzer**: The passive notification-based analysis component using lightweight preview
- **Layer_2_Analyzer**: The active deep scan component triggered by user consent via share-to-app
- **Multi_Trigger_System**: The logic requiring 2+ risk signals before flagging a message
- **Template_Matcher**: The component that performs semantic similarity matching against scam archetypes
- **Explainability_Panel**: The UI component showing triggered signals and risk breakdown
- **Privacy_Dashboard**: The settings interface for user controls and transparency
- **Guardian**: A trusted family member who receives optional weekly summaries
- **Elderly_User**: The primary user, typically a senior citizen vulnerable to digital scams
- **Risk_Score**: A numerical value from 0-100 indicating scam likelihood
- **Scam_Template**: A curated archetype representing a known scam pattern category

## Requirements

### Requirement 1: Multi-Trigger Activation System

**User Story:** As an elderly user, I want the system to only flag messages when multiple suspicious signals are present, so that I am not overwhelmed with false alarms for legitimate messages.

#### Acceptance Criteria

1. WHEN a message is analyzed, THE Multi_Trigger_System SHALL evaluate all available risk signals
2. WHEN fewer than 2 risk signals are detected, THE Multi_Trigger_System SHALL classify the message as low risk
3. WHEN 2 or more risk signals are detected, THE Multi_Trigger_System SHALL proceed with full risk scoring
4. THE Multi_Trigger_System SHALL recognize forwarded message flags as a risk signal
5. THE Multi_Trigger_System SHALL recognize URL presence as a risk signal
6. THE Multi_Trigger_System SHALL recognize urgency keywords as a risk signal
7. THE Multi_Trigger_System SHALL recognize unknown sender status as a risk signal

### Requirement 2: On-Device Risk Scoring

**User Story:** As an elderly user, I want to see a clear risk level for suspicious messages, so that I can make informed decisions about whether to trust them.

#### Acceptance Criteria

1. WHEN a message passes the multi-trigger threshold, THE Risk_Engine SHALL calculate a weighted risk score from 0 to 100
2. WHEN the risk score is between 0 and 33, THE Risk_Engine SHALL classify the message as Low risk
3. WHEN the risk score is between 34 and 66, THE Risk_Engine SHALL classify the message as Medium risk
4. WHEN the risk score is between 67 and 100, THE Risk_Engine SHALL classify the message as High risk
5. THE Risk_Engine SHALL perform all computations on-device without network access
6. THE Risk_Engine SHALL complete risk scoring within 2 seconds for Layer 1 analysis
7. THE Risk_Engine SHALL complete risk scoring within 5 seconds for Layer 2 analysis

### Requirement 3: Scam Template Pattern Matching

**User Story:** As an elderly user, I want the system to recognize known scam patterns, so that I can be warned about messages similar to common fraud schemes.

#### Acceptance Criteria

1. THE Template_Matcher SHALL maintain embeddings for all curated scam template categories
2. WHEN analyzing a message, THE Template_Matcher SHALL compute semantic similarity using cosine similarity
3. WHEN similarity exceeds 0.7 threshold, THE Template_Matcher SHALL flag a template match
4. THE Template_Matcher SHALL support Fake KYC scam templates
5. THE Template_Matcher SHALL support Government subsidy scam templates
6. THE Template_Matcher SHALL support Lottery scam templates
7. THE Template_Matcher SHALL support Loan approval scam templates
8. THE Template_Matcher SHALL support Job offer scam templates
9. THE Template_Matcher SHALL support Impersonation scam templates

### Requirement 4: Layer 1 Passive Analysis

**User Story:** As an elderly user, I want to see risk hints on WhatsApp notifications, so that I can be alerted to potential scams without taking extra action.

#### Acceptance Criteria

1. WHEN a WhatsApp notification is received, THE Layer_1_Analyzer SHALL extract the message preview text
2. WHEN the preview text is available, THE Layer_1_Analyzer SHALL perform lightweight risk analysis
3. WHEN risk is detected in Layer 1, THE KavalAI_System SHALL display a risk hint overlay on the notification
4. THE Layer_1_Analyzer SHALL operate without requiring user interaction
5. THE Layer_1_Analyzer SHALL complete analysis before the notification is displayed to the user
6. WHEN notification access permission is not granted, THE Layer_1_Analyzer SHALL remain inactive

### Requirement 5: Layer 2 Active Deep Scan

**User Story:** As an elderly user, I want to manually share suspicious messages for detailed analysis, so that I can get comprehensive risk assessment when I'm uncertain.

#### Acceptance Criteria

1. WHEN a user shares a message to KavalAI, THE Layer_2_Analyzer SHALL receive the full message content
2. WHEN full message content is available, THE Layer_2_Analyzer SHALL perform comprehensive risk analysis
3. THE Layer_2_Analyzer SHALL analyze message metadata including sender information
4. THE Layer_2_Analyzer SHALL perform deep template matching across all categories
5. THE Layer_2_Analyzer SHALL extract and analyze all URLs in the message
6. WHEN analysis is complete, THE Layer_2_Analyzer SHALL display the Explainability_Panel
7. THE Layer_2_Analyzer SHALL operate only when explicitly invoked by user action

### Requirement 6: Explainable AI Panel

**User Story:** As an elderly user, I want to understand why a message was flagged as risky, so that I can learn to recognize scam patterns myself.

#### Acceptance Criteria

1. WHEN displaying risk results, THE Explainability_Panel SHALL show all triggered risk signals
2. WHEN a template match occurs, THE Explainability_Panel SHALL display the matched scam category
3. THE Explainability_Panel SHALL show the overall risk score and classification
4. THE Explainability_Panel SHALL present information in simple, non-technical language
5. THE Explainability_Panel SHALL use visual indicators for risk levels
6. THE Explainability_Panel SHALL display a breakdown of weighted scoring factors

### Requirement 7: Regional Language Support

**User Story:** As an elderly user who communicates in Hindi or mixed languages, I want the system to analyze messages in my language, so that I am protected regardless of the language scammers use.

#### Acceptance Criteria

1. THE KavalAI_System SHALL support English language message analysis
2. THE KavalAI_System SHALL support Hindi language message analysis
3. THE KavalAI_System SHALL detect and analyze code-mixed messages containing both English and Hindi
4. WHEN analyzing regional language content, THE Template_Matcher SHALL use language-appropriate embeddings
5. THE Explainability_Panel SHALL display results in the user's preferred language

### Requirement 8: Privacy-First Architecture

**User Story:** As an elderly user concerned about privacy, I want assurance that my messages are never stored or sent to the cloud, so that I can use the protection without compromising my private conversations.

#### Acceptance Criteria

1. THE KavalAI_System SHALL perform all AI processing on-device
2. THE KavalAI_System SHALL NOT transmit any message content to external servers
3. THE KavalAI_System SHALL NOT store message content persistently
4. THE KavalAI_System SHALL NOT store message metadata persistently
5. WHEN analysis is complete, THE KavalAI_System SHALL discard all analyzed message data from memory
6. THE KavalAI_System SHALL NOT implement any analytics tracking
7. THE KavalAI_System SHALL NOT access WhatsApp's encrypted message database
8. THE KavalAI_System SHALL operate without requiring backend or cloud services

### Requirement 9: Permission Management

**User Story:** As an elderly user, I want clear control over what permissions the app uses, so that I understand and consent to how it accesses my device.

#### Acceptance Criteria

1. WHEN first launched, THE KavalAI_System SHALL request notification access permission with clear explanation
2. THE KavalAI_System SHALL function in Layer 2 mode when notification access is denied
3. THE Privacy_Dashboard SHALL display the current status of all permissions
4. THE Privacy_Dashboard SHALL allow users to revoke notification access
5. WHEN permissions change, THE KavalAI_System SHALL adapt its functionality accordingly
6. THE KavalAI_System SHALL NOT request unnecessary Android permissions

### Requirement 10: Guardian Weekly Summary

**User Story:** As a guardian of an elderly user, I want to receive weekly summaries of scam exposure, so that I can monitor my loved one's safety without invading their privacy.

#### Acceptance Criteria

1. WHERE the guardian feature is enabled, THE KavalAI_System SHALL generate weekly summaries
2. THE KavalAI_System SHALL include only aggregate risk counts in summaries
3. THE KavalAI_System SHALL NOT include message content in summaries
4. THE KavalAI_System SHALL NOT include sender information in summaries
5. THE KavalAI_System SHALL display risk level distribution in summaries
6. WHEN the guardian feature is disabled, THE KavalAI_System SHALL NOT generate or transmit any summaries
7. THE Privacy_Dashboard SHALL allow users to enable or disable guardian summaries

### Requirement 11: Privacy Dashboard Transparency

**User Story:** As an elderly user, I want a clear dashboard showing what the app does and doesn't do with my data, so that I can trust the system.

#### Acceptance Criteria

1. THE Privacy_Dashboard SHALL display an explicit "No Storage" confirmation
2. THE Privacy_Dashboard SHALL display an explicit "No Cloud" confirmation
3. THE Privacy_Dashboard SHALL display an explicit "No Tracking" confirmation
4. THE Privacy_Dashboard SHALL provide toggle controls for all optional features
5. THE Privacy_Dashboard SHALL explain each feature in simple language
6. THE Privacy_Dashboard SHALL be accessible from the main app screen

### Requirement 12: WhatsApp Compliance

**User Story:** As a system architect, I want to ensure full compliance with WhatsApp's end-to-end encryption, so that the app operates within legal and technical boundaries.

#### Acceptance Criteria

1. THE KavalAI_System SHALL NOT attempt to decrypt WhatsApp messages
2. THE KavalAI_System SHALL NOT access WhatsApp's message database
3. THE KavalAI_System SHALL NOT intercept WhatsApp network traffic
4. THE KavalAI_System SHALL access only notification content provided by Android OS
5. THE KavalAI_System SHALL access only content explicitly shared by the user via share intent

### Requirement 13: False Positive Mitigation

**User Story:** As an elderly user, I want the system to minimize false alarms, so that I don't become desensitized to warnings or distrust legitimate messages.

#### Acceptance Criteria

1. WHEN calculating risk scores, THE Risk_Engine SHALL apply weighted scoring to balance sensitivity and specificity
2. THE Multi_Trigger_System SHALL require multiple signals to reduce false positives
3. WHEN template matching, THE Template_Matcher SHALL use a conservative similarity threshold
4. THE KavalAI_System SHALL provide clear explanations to help users understand false positives
5. THE Risk_Engine SHALL prioritize high-confidence signals over low-confidence signals

### Requirement 14: Android Platform Integration

**User Story:** As an elderly user, I want the app to work seamlessly with my Android phone, so that protection feels natural and unobtrusive.

#### Acceptance Criteria

1. THE KavalAI_System SHALL support Android API level 26 and above
2. THE KavalAI_System SHALL integrate with Android's notification listener service
3. THE KavalAI_System SHALL integrate with Android's share intent system
4. THE KavalAI_System SHALL follow Android material design guidelines
5. THE KavalAI_System SHALL operate efficiently on devices with limited RAM
6. THE KavalAI_System SHALL minimize battery consumption during background operation

### Requirement 15: Offline Operation

**User Story:** As an elderly user in areas with unreliable internet, I want the protection to work without network connectivity, so that I am always protected.

#### Acceptance Criteria

1. THE KavalAI_System SHALL function completely offline
2. THE Risk_Engine SHALL load all AI models during app initialization
3. THE Template_Matcher SHALL load all scam templates during app initialization
4. THE KavalAI_System SHALL NOT require network connectivity for any core functionality
5. WHEN network is unavailable, THE KavalAI_System SHALL continue operating without degradation
