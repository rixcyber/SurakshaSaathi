package com.example.surakshasaathi.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import com.example.surakshasaathi.riskengine.RiskEngine
import com.example.surakshasaathi.riskengine.RiskEvidence
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Common prompt scenario for elderly users who want to quickly test or ask
 * about a suspicious situation without typing.
 */
data class QuickVoicePrompt(
    val title: String,
    val titleHi: String,
    val titleOr: String,
    val queryText: String,
    val emoji: String,
)

/**
 * Result of a voice safety inquiry.
 */
data class VoiceAssistantResult(
    val query: String,
    val riskResult: RiskResult,
    val spokenExplanation: String,
)

/**
 * VoiceAssistantManager enables the "Is This Safe?" voice inquiry feature.
 * Supports listening to the user's voice via Android SpeechRecognizer and
 * evaluating the inquiry against the local RiskEngine, then speaking the answer.
 */
class VoiceAssistantManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "VoiceAssistant"

        @Volatile
        private var INSTANCE: VoiceAssistantManager? = null

        fun getInstance(context: Context): VoiceAssistantManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceAssistantManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        val QUICK_PROMPTS = listOf(
            QuickVoicePrompt(
                title = "Caller asked for OTP",
                titleHi = "कॉल पर OTP माँगा गया",
                titleOr = "କଲ୍ ରେ ଓଟିପି ମଗାଗଲା",
                queryText = "Someone called saying they are from bank and asked for my OTP to update KYC immediately or account will be blocked.",
                emoji = "🔢",
            ),
            QuickVoicePrompt(
                title = "Electricity power cut SMS",
                titleHi = "बिजली कटने का मैसेज आया",
                titleOr = "ବିଦ୍ୟୁତ୍ କାଟ ମେସେଜ୍ ଆସିଛି",
                queryText = "Urgent: Your electricity bill is unpaid. Power will be disconnected tonight. Call this officer number immediately to pay.",
                emoji = "⚡",
            ),
            QuickVoicePrompt(
                title = "Install AnyDesk for KYC",
                titleHi = "AnyDesk ऐप डाउनलोड करने को कहा",
                titleOr = "AnyDesk ଆପ୍ ଡାଉନଲୋଡ୍ କରିବାକୁ କୁହାଗଲା",
                queryText = "Bank officer asked me to install AnyDesk or TeamViewer screen sharing app to complete KYC verification.",
                emoji = "📲",
            ),
            QuickVoicePrompt(
                title = "Lottery / Prize won link",
                titleHi = "लॉटरी जीतने का लिंक मिला",
                titleOr = "ଲଟେରୀ ଜିତିବା ଲିଙ୍କ୍ ମିଳିଛି",
                queryText = "Congratulations! You have won Rs 50,000 lottery reward. Click link bit.ly/claim-prize to claim your prize now.",
                emoji = "🎁",
            ),
            QuickVoicePrompt(
                title = "Part-time job YouTube like",
                titleHi = "घर बैठे यूट्यूब लाइक की नौकरी",
                titleOr = "ଘରେ ବସି ୟୁଟ୍ୟୁବ୍ ଲାଇକ୍ ଚାକିରି",
                queryText = "Earn Rs 3000 daily working from home just by liking YouTube videos. Deposit 500 rupees registration fee on Telegram.",
                emoji = "💼",
            ),
            QuickVoicePrompt(
                title = "Delivery package address update",
                titleHi = "पार्सल डिलीवरी पता अपडेट लिंक",
                titleOr = "ପାର୍ସଲ ଠିକଣା ଅପଡେଟ୍ ଲିଙ୍କ୍",
                queryText = "Your India Post parcel is pending due to wrong address. Pay Rs 5 re-delivery fee at post-parcel-update.com",
                emoji = "📦",
            ),
        )
    }

    private var speechRecognizer: SpeechRecognizer? = null

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _speechError = MutableStateFlow<String?>(null)
    val speechError: StateFlow<String?> = _speechError.asStateFlow()

    private val _currentInquiryResult = MutableStateFlow<VoiceAssistantResult?>(null)
    val currentInquiryResult: StateFlow<VoiceAssistantResult?> = _currentInquiryResult.asStateFlow()

    private val voiceGuidance = VoiceGuidanceManager.getInstance(context)

    fun isRecognitionAvailable(): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
    }

    /**
     * Start listening to voice input using Android SpeechRecognizer.
     */
    fun startListening(language: AppLanguage, onSpeechResult: (String) -> Unit) {
        if (!isRecognitionAvailable()) {
            _speechError.value = "Voice recognition not available on this device"
            return
        }

        stopListening()
        _speechError.value = null
        _isListening.value = true

        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        _isListening.value = false
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _speechError.value = "Could not hear clearly. Please try again or tap a scenario."
                        Log.w(TAG, "Speech recognition error code: $error")
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()
                        if (!text.isNullOrBlank()) {
                            onSpeechResult(text)
                            analyzeQuery(text, language)
                        } else {
                            _speechError.value = "No speech detected"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.primaryLocale.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Ask Suraksha Saathi: Is this safe?")
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _isListening.value = false
            _speechError.value = "Could not start microphone: ${e.message}"
            Log.e(TAG, "Error starting speech recognition", e)
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognizer", e)
        } finally {
            speechRecognizer = null
            _isListening.value = false
        }
    }

    /**
     * Evaluates a question or query text and speaks the safety advice.
     */
    fun analyzeQuery(query: String, language: AppLanguage): VoiceAssistantResult {
        // Run query through RiskEngine
        var result = RiskEngine.analyzeMessage(query)

        // Custom checks for screen-sharing requests in voice prompt
        val lower = query.lowercase(Locale.ROOT)
        if (lower.contains("anydesk") || lower.contains("teamviewer") || lower.contains("quicksupport") || lower.contains("screen share")) {
            val appEvidence = RiskEvidence("Remote access / screen sharing app requested", 85, "remote_access")
            result = RiskResult(
                score = maxOf(result.score, 85),
                verdict = RiskVerdict.STOP,
                evidenceList = listOf(appEvidence) + result.evidenceList,
            )
        }

        val spoken = voiceGuidance.buildSpokenMessage(result, "situation", language)
        val assistantResult = VoiceAssistantResult(
            query = query,
            riskResult = result,
            spokenExplanation = spoken,
        )

        _currentInquiryResult.value = assistantResult

        // Speak aloud automatically
        voiceGuidance.speakText(spoken)

        return assistantResult
    }

    fun clearResult() {
        _currentInquiryResult.value = null
        _speechError.value = null
        voiceGuidance.stop()
    }
}
