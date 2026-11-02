package com.example.util

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.ai.AssistantAction
import java.net.URLEncoder

object IntentExecutor {

    fun execute(context: Context, action: AssistantAction) {
        try {
            when (action) {
                is AssistantAction.LaunchApp -> {
                    val launchIntent = context.packageManager.getLaunchIntentForPackage(action.packageName)
                    if (launchIntent != null) {
                        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launchIntent)
                    } else if (!action.fallbackUrl.isNullOrBlank()) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(action.fallbackUrl))
                        browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(browserIntent)
                    } else {
                        Toast.makeText(context, "${action.appName} is not installed.", Toast.LENGTH_SHORT).show()
                    }
                }

                is AssistantAction.SendWhatsApp -> {
                    try {
                        val encodedMsg = URLEncoder.encode(action.message, "UTF-8")
                        val waUri = Uri.parse("https://api.whatsapp.com/send?text=$encodedMsg")
                        val waIntent = Intent(Intent.ACTION_VIEW, waUri)
                        waIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(waIntent)
                    } catch (e: Exception) {
                        // Fallback generic send
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, action.message)
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(sendIntent)
                    }
                }

                is AssistantAction.SendSms -> {
                    val uri = Uri.parse("smsto:${action.recipientPhone.orEmpty()}")
                    val smsIntent = Intent(Intent.ACTION_SENDTO, uri).apply {
                        putExtra("sms_body", action.message)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(smsIntent)
                }

                is AssistantAction.DialPhone -> {
                    val uri = Uri.parse("tel:${action.phoneNumber.orEmpty()}")
                    val dialIntent = Intent(Intent.ACTION_DIAL, uri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                }

                is AssistantAction.WebSearch -> {
                    try {
                        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH).apply {
                            putExtra(SearchManager.QUERY, action.query)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(searchIntent)
                    } catch (e: Exception) {
                        val encoded = URLEncoder.encode(action.query, "UTF-8")
                        val url = "https://www.google.com/search?q=$encoded"
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(browserIntent)
                    }
                }

                is AssistantAction.PlayMedia -> {
                    val encoded = URLEncoder.encode(action.query, "UTF-8")
                    val ytUrl = "https://www.youtube.com/results?search_query=$encoded"
                    val ytIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ytUrl)).apply {
                        setPackage("com.google.android.youtube")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    try {
                        context.startActivity(ytIntent)
                    } catch (e: Exception) {
                        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(ytUrl)).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(browserIntent)
                    }
                }

                is AssistantAction.SetTimer -> {
                    try {
                        val timerIntent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                            putExtra(AlarmClock.EXTRA_MESSAGE, "TG Assistant Timer")
                            putExtra(AlarmClock.EXTRA_LENGTH, action.minutes * 60)
                            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(timerIntent)
                    } catch (e: Exception) {
                        val clockIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(clockIntent)
                    }
                }

                is AssistantAction.OpenCamera -> {
                    val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    val cameraAppIntent = Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }

                    if (captureIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(captureIntent)
                    } else if (cameraAppIntent.resolveActivity(context.packageManager) != null) {
                        context.startActivity(cameraAppIntent)
                    } else {
                        try {
                            context.startActivity(captureIntent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Camera application not available.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                is AssistantAction.OpenSettings -> {
                    val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(settingsIntent)
                }

                is AssistantAction.SaveNote -> {
                    // Handled within ViewModel via Room
                }

                is AssistantAction.ShowSystemInfo -> {
                    // Handled within UI state
                }

                AssistantAction.None -> Unit
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open action: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}
