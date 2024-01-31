package com.example.portfolio

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.example.portfolio.ml.KerasfC
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class MyAccessibilityService : AccessibilityService() {
    private val textViewsText = mutableListOf<String>()
    override fun onAccessibilityEvent(event: AccessibilityEvent) {

        val source: AccessibilityNodeInfo = event.source ?: return
        readTextViews(source, textViewsText)

//         Calling Model Class
        val intent = Intent(this, ModelInferenceManager::class.java)
        intent.putExtra("text_data", ArrayList(textViewsText))
        startService(intent)

        for (text in textViewsText) {
            Log.d("out", text)
        }
    }

    override fun onInterrupt() {

    }
    private fun readTextViews(nodeInfo: AccessibilityNodeInfo, textViewsText: MutableList<String>) {
        if (nodeInfo.text != null) {
            textViewsText.add(nodeInfo.text.toString())
        }

        for (i in 0 until nodeInfo.childCount) {
            val childNodeInfo = nodeInfo.getChild(i)
            if (childNodeInfo != null) {
                readTextViews(childNodeInfo, textViewsText)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()

        val serviceInfo = serviceInfo
        serviceInfo.flags =
//            AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS or
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
               AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
        serviceInfo.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                AccessibilityEvent.TYPE_VIEW_CLICKED


        serviceInfo.packageNames = arrayOf<String>("com.example.portfolio",
            "com.amazon.android","com.tul.tatacliq", "com.meesho.supply","com.flipkart.android",
            "com.myntra.android", "com.snapdeal.main")
//        serviceInfo.notificationTimeout = 0;
        setServiceInfo(serviceInfo)
    }

    private fun preProcess(textData: List<String>): FloatArray {
        return textData.map { cleanText(it) }
            .map { tokenizeAndVectorize(it) }
            .flatten()
            .toFloatArray()
    }

    private fun cleanText(text: String): String {
        var cleanedText = text.lowercase()

        val badSymbolsRegex = Regex("[\\p{Punct}&&[^-]]")
        cleanedText = badSymbolsRegex.replace(cleanedText, "")

        cleanedText = cleanedText.replace("x", "")

        val stopWords = setOf("the", "a", "an", "in", "on", "of")
        cleanedText = cleanedText.split(" ").filterNot { it in stopWords }.joinToString(" ")

        return cleanedText
    }

    private fun tokenizeAndVectorize(text: String): List<Float> {

        val words = text.split("\\s+".toRegex())

        val wordIndexMap = mutableMapOf<String, Int>()
        var index = 0

        for (word in words) {
            if (!wordIndexMap.containsKey(word)) {
                wordIndexMap[word] = index
                index++
            }
        }


        val wordIndices = words.map { wordIndexMap[it] ?: -1 }

        val vectorizedText = wordIndices.map { wordIndex ->
            val oneHotVector = FloatArray(index) { 0f }
            if (wordIndex != -1) {
                oneHotVector[wordIndex] = 1f
            }
            oneHotVector.toList()
        }.flatten()

        return vectorizedText
    }
}

