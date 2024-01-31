package com.example.portfolio

import android.app.IntentService
import android.content.Intent
import android.util.Log
import com.example.portfolio.ml.KerasfC
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

class ModelInferenceManager : IntentService("ModelInferenceManager") {

    @Deprecated("Deprecated in Java")
    override fun onHandleIntent(intent: Intent?) {
        val textData = intent?.getStringArrayListExtra("text_data") ?: emptyList()

        val model = KerasfC.newInstance(this)

        val preprocessedData = preProcess(textData)

        Log.d("Input working ", preprocessedData.toString())

        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, preprocessedData.size), DataType.FLOAT32)
        inputFeature0.loadArray(preprocessedData)

        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        Log.d("MODEL_OUTPUT", outputFeature0.toString())

        model.close()
    }

    private fun preProcess(textData: List<String>): FloatArray {
        return textData.map { cleanText(it) }
            .map { tokenizeAndVectorize(it) }
            .flatten()
            .toFloatArray()
    }

    private fun cleanText(text: String): String {
        var cleanedText = text.lowercase()

        // Replace symbols using a regular expression (adjust pattern as needed)
        val badSymbolsRegex = Regex("[\\p{Punct}&&[^-]]")
        cleanedText = badSymbolsRegex.replace(cleanedText, "")

        // Remove 'x' characters
        cleanedText = cleanedText.replace("x", "")

        // Remove stop words (replace with your actual STOPWORDS set)
        val stopWords = setOf("the", "a", "an", "in", "on", "of") // Add your stop words here
        cleanedText = cleanedText.split(" ").filterNot { it in stopWords }.joinToString(" ")

        return cleanedText
    }

    private fun tokenizeAndVectorize(text: String): List<Float> {

        val words = text.split("\\s+".toRegex())

        // You can apply any filtering or additional preprocessing here based on your requirements

        // Create a map to store unique words and their corresponding indices
        val wordIndexMap = mutableMapOf<String, Int>()
        var index = 0

        // Map each unique word to an index
        for (word in words) {
            if (!wordIndexMap.containsKey(word)) {
                wordIndexMap[word] = index
                index++
            }
        }

        // Convert the text to a list of word indices
        val wordIndices = words.map { wordIndexMap[it] ?: -1 }

        // Replace with your vectorization logic (e.g., using word embeddings or integer encoding)
        // Example using one-hot encoding:
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

