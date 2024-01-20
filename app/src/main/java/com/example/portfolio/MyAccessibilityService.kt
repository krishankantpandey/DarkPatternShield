package com.example.portfolio

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MyAccessibilityService : AccessibilityService() {

    private val info: AccessibilityServiceInfo = AccessibilityServiceInfo()

    fun retrieveTextFromScreen() {
        val rootNode = rootInActiveWindow ?: return

        // Call the traverse function starting from the root node
        traverseAndRetrieveText(rootNode)
    }

    private fun traverseAndRetrieveText(node: AccessibilityNodeInfo) {
        // Check if the node is a TextView or Button and has non-empty text
        if ((node.className == "android.widget.TextView" || node.className == "android.widget.Button")
            && node.text != null && !node.text.toString().isEmpty()
        ) {
            Log.i("Text on Screen", node.text.toString())
        }

        // Recursively traverse child nodes
        for (i in 0 until node.childCount) {
            traverseAndRetrieveText(node.getChild(i))
        }
    }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val source = event?.source ?: return

        traverseAndPrintText(source, 0)
    }

        private fun traverseAndPrintText(node: AccessibilityNodeInfo, level: Int) {
            if (node.className == "android.widget.TextView" && node.text != null && !node.text.toString().isEmpty()) {
                val indent = "  ".repeat(level)
                Log.i("Text on Screen", "$indent${node.text}")

                // If you want to map text with resource id
                val resourceId = getResourceId(node)
                if (resourceId != null) {
                    Log.i("Resource ID", "$indent$resourceId")
                    // Here, you can store the text and its corresponding resource id for future use
                }
            }

            for (i in 0 until node.childCount) {
                traverseAndPrintText(node.getChild(i), level + 1)
            }
        }

        private fun getResourceId(node: AccessibilityNodeInfo): String? {
            val resourceIdEntryName = node.viewIdResourceName
            return if (resourceIdEntryName != null) {
                // Extracting the resource id from the entry name
                val parts = resourceIdEntryName.split("/")
                if (parts.size == 2) {
                    parts[1]
                } else {
                    null
                }
            } else {
                null
            }
        }



    override fun onInterrupt() {
        // Handle interruptions, if needed
    }

    override fun onServiceConnected() {
        info.apply {
            // Set the type of events that this service wants to listen to. Others
            // aren't passed to this service.
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED or AccessibilityEvent.TYPE_VIEW_FOCUSED or AccessibilityEvent.TYPE_WINDOWS_CHANGED or AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED

            // If you only want this service to work with specific apps, set their
            // package names here. Otherwise, when the service is activated, it
            // listens to events from all apps.
            packageNames = arrayOf("com.example.android.myFirstApp", "com.example.android.mySecondApp")

            // Set the type of feedback your service provides.
            feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN

            // Default services are invoked only if no package-specific services are
            // present for the type of AccessibilityEvent generated. This service is
            // app-specific, so the flag isn't necessary. For a general-purpose
            // service, consider setting the DEFAULT flag.

            // flags = AccessibilityServiceInfo.DEFAULT;

            notificationTimeout = 100
        }

        this.serviceInfo = info

    }
}
