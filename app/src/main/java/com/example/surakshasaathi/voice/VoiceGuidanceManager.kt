package com.example.surakshasaathi.voice

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.surakshasaathi.riskengine.RiskResult
import com.example.surakshasaathi.riskengine.RiskVerdict
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Voice Guidance Manager — handles Text-to-Speech warnings and safety guidance
 * in English, Hindi, and Odia for senior citizens.
 *
 * Designed to provide clear, calm, large-print-equivalent spoken explanations
 * so elderly users don't have to strain their eyes or decipher complex terms.
 */
class VoiceGuidanceManager private constructor(private val appContext: Context) :
    TextToSpeech.OnInitListener {

    companion object {
        private const val TAG = "VoiceGuidance"
        private const val PREFS_NAME = "suraksha_voice_prefs"
        private const val KEY_LANGUAGE = "pref_voice_language"
        private const val KEY_AUTO_SPEAK = "pref_auto_speak_warnings"

        @Volatile
        private var INSTANCE: VoiceGuidanceManager? = null

        fun getInstance(context: Context): VoiceGuidanceManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VoiceGuidanceManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _currentLanguage = MutableStateFlow(loadSavedLanguage())
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _isAutoSpeakEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_SPEAK, true)
    )
    val isAutoSpeakEnabled: StateFlow<Boolean> = _isAutoSpeakEnabled.asStateFlow()

    private val _lastSpokenText = MutableStateFlow<String?>(null)
    val lastSpokenText: StateFlow<String?> = _lastSpokenText.asStateFlow()

    init {
        initializeTts()
    }

    private fun initializeTts() {
        try {
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            setupProgressListener()
            applyLanguage(_currentLanguage.value)
            Log.i(TAG, "TextToSpeech initialized successfully")
        } else {
            isTtsReady = false
            Log.w(TAG, "TextToSpeech initialization failed with status $status")
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
                Log.w(TAG, "Speech utterance error for id $utteranceId")
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                _isSpeaking.value = false
                Log.w(TAG, "Speech utterance error $errorCode for id $utteranceId")
            }
        })
    }

    private fun loadSavedLanguage(): AppLanguage {
        val code = prefs.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
        return AppLanguage.fromCode(code)
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
        prefs.edit().putString(KEY_LANGUAGE, language.code).apply()
        applyLanguage(language)
    }

    fun toggleAutoSpeak(enabled: Boolean) {
        _isAutoSpeakEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_SPEAK, enabled).apply()
    }

    private fun applyLanguage(language: AppLanguage) {
        val engine = tts ?: return
        if (!isTtsReady) return

        val targetLocale = language.primaryLocale
        val availability = engine.isLanguageAvailable(targetLocale)

        if (availability >= TextToSpeech.LANG_AVAILABLE) {
            engine.language = targetLocale
            Log.d(TAG, "TTS language set to ${language.displayName} (${targetLocale})")
        } else {
            // Fallback: If Odia voice pack is missing on phone, fallback to Hindi or English
            val fallbackLocale = if (language == AppLanguage.ODIA) {
                val hindiLocale = Locale.forLanguageTag("hi-IN")
                if (engine.isLanguageAvailable(hindiLocale) >= TextToSpeech.LANG_AVAILABLE) {
                    hindiLocale
                } else {
                    Locale.ENGLISH
                }
            } else {
                Locale.ENGLISH
            }
            engine.language = fallbackLocale
            Log.w(TAG, "Language ${language.code} unavailable (code $availability), falling back to $fallbackLocale")
        }

        // Slightly slower rate for elderly clarity
        engine.setSpeechRate(0.88f)
        engine.setPitch(1.0f)
    }

    /**
     * Speaks safety guidance for a specific RiskResult verdict and context.
     */
    fun speakRiskResult(
        result: RiskResult,
        contextType: String = "Item",
        forceSpeak: Boolean = false,
    ) {
        if (!forceSpeak && !_isAutoSpeakEnabled.value) {
            return
        }

        val speechText = buildSpokenMessage(result, contextType, _currentLanguage.value)
        speakText(speechText)
    }

    /**
     * Speaks any provided text aloud in the current language.
     */
    fun speakText(text: String) {
        if (!isTtsReady || tts == null) {
            Log.w(TAG, "Cannot speak: TTS not ready")
            return
        }

        applyLanguage(_currentLanguage.value)
        _lastSpokenText.value = text

        val utteranceId = "suraksha_voice_${System.currentTimeMillis()}"
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1.0f)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
    }

    /**
     * Stops any ongoing speech.
     */
    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
        }
    }

    /**
     * Constructs a comforting, clear, non-technical message for the senior.
     */
    fun buildSpokenMessage(
        result: RiskResult,
        contextType: String,
        language: AppLanguage,
    ): String {
        return when (language) {
            AppLanguage.ENGLISH -> when (result.verdict) {
                RiskVerdict.STOP -> {
                    "Warning! Please STOP. This $contextType may be fraudulent. " +
                            "Do not share your OTP, PIN, or click any links. " +
                            "Talk to your family first."
                }
                RiskVerdict.VERIFY -> {
                    "Please VERIFY carefully before proceeding. " +
                            "Check with your family or bank branch before taking action."
                }
                RiskVerdict.PROCEED -> {
                    "Safe to proceed. No scam signals detected. Still remain cautious with your personal details."
                }
            }
            AppLanguage.HINDI -> when (result.verdict) {
                RiskVerdict.STOP -> {
                    "चेतावनी! कृपया रुकें। यह $contextType धोखाधड़ी हो सकता है। " +
                            "अपना ओटीपी, पिन या कोई भी जानकारी किसी को न दें। " +
                            "पहले अपने परिवार से बात करें।"
                }
                RiskVerdict.VERIFY -> {
                    "कृपया आगे बढ़ने से पहले जांच करें। " +
                            "कोई भी कदम उठाने से पहले अपने परिवार या बैंक से संपर्क करें।"
                }
                RiskVerdict.PROCEED -> {
                    "सुरक्षित लग रहा है। कोई सीधा खतरा नहीं मिला। " +
                            "फिर भी सावधानी से आगे बढ़ें।"
                }
            }
            AppLanguage.ODIA -> when (result.verdict) {
                RiskVerdict.STOP -> {
                    "ସାବଧାନ! ଦୟାକରି ଅଟକନ୍ତୁ। ଏହି $contextType ଠକେଇ ହୋଇପାରେ। " +
                            "ଆପଣଙ୍କ ଓଟିପି, ପିନ୍ କାହାକୁ ଦିଅନ୍ତୁ ନାହିଁ। " +
                            "ପ୍ରଥମେ ନିଜ ପରିବାର ସହିତ କଥା ହୁଅନ୍ତୁ।"
                }
                RiskVerdict.VERIFY -> {
                    "ଆଗକୁ ବଢ଼ିବା ପୂର୍ବରୁ ଦୟାକରି ଯାଞ୍ଚ କରନ୍ତୁ। " +
                            "ନିଜ ପରିବାର କିମ୍ବା ବ୍ୟାଙ୍କ ସହିତ ଯୋଗାଯୋଗ କରନ୍ତୁ।"
                }
                RiskVerdict.PROCEED -> {
                    "ସୁରକ୍ଷିତ ମନେହେଉଛି। କୌଣସି ଠକେଇ ସଙ୍କେତ ମିଳିନାହିଁ। " +
                            "ତଥାପି ସାବଧାନତାର ସହ ଆଗକୁ ବଢ଼ନ୍ତୁ।"
                }
            }
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
        isTtsReady = false
    }
}
