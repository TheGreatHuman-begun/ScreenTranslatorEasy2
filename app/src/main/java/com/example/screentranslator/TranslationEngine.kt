package com.example.screentranslator

interface TranslationEngine { suspend fun translate(text: String, source: String, target: String): String }
