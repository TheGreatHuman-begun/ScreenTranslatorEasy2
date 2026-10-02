package com.example.screentranslator

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.mlkit.nl.translate.Translation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MlKitTranslationEngine : TranslationEngine {
    override suspend fun translate(text: String, source: String, target: String): String {
        if (text.isBlank() || source == target) return text
        val options = TranslatorOptions.Builder().setSourceLanguage(source).setTargetLanguage(target).build()
        val translator = Translation.getClient(options)
        return try {
            suspendCancellableCoroutine { cont ->
                translator.downloadModelIfNeeded(DownloadConditions.Builder().build()).addOnSuccessListener {
                    translator.translate(text).addOnSuccessListener { cont.resume(it) }.addOnFailureListener { cont.resumeWithException(it) }
                }.addOnFailureListener { cont.resumeWithException(it) }
            }
        } finally { translator.close() }
    }
}
