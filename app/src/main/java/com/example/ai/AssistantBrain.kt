package com.example.ai

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object AssistantBrain {

    private val HINDI_PATTERNS = listOf(
        "namaste", "pranam", "kya", "kaise", "karo", "bhejo", "chalao", "kholo",
        "batao", "aap", "tum", "kaun", "sunao", "mera", "meri", "mere", "hum",
        "nahi", "haan", "theek", "shukriya", "dhanyawad", "kripya", "bhai", "yaar",
        "kahan", "kitna", "kab", "gaana", "chutkula", "samachar", "yaad", "likho",
        "baje", "subah", "shaam", "raat", "haal", "kaam", "chahiye"
    )

    fun isHindi(input: String): Boolean {
        // Check for Devanagari script unicode range: \u0900-\u097F
        for (char in input) {
            if (char in '\u0900'..'\u097F') return true
        }
        val lower = input.lowercase(Locale.ROOT)
        val words = lower.split(Regex("[\\s,?.!]+"))
        val hindiCount = words.count { HINDI_PATTERNS.contains(it) }
        return hindiCount >= 1
    }

    fun processQuery(input: String, userName: String = "Mitul"): AssistantResponse {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            return AssistantResponse(
                replyText = "Hello $userName, I'm listening! You can speak or type a command.",
                spokenText = "Hello $userName, I'm listening!",
                action = AssistantAction.None,
                language = "en"
            )
        }

        // Clean wake words like "TGA", "TG AI ASSISTANT", "Hey TGA", "TG", "Suno TG"
        val cleanInput = trimmed.replace(
            Regex("(?i)^(hey\\s+tg\\s+ai\\s+assistant|tg\\s+ai\\s+assistant|tg\\s+assistant|hey\\s+tga|tga|hey\\s+tg|tg|assistant|ok\\s+tg|suno\\s+tg|suno\\s+tga|listen\\s+tga|listen\\s+tg)\\s*[,:]?\\s*"),
            ""
        ).trim()
        val queryToProcess = if (cleanInput.isNotBlank()) cleanInput else trimmed

        val hindiMode = isHindi(trimmed) || isHindi(queryToProcess)
        val lower = queryToProcess.lowercase(Locale.ROOT)

        // 1. YouTube commands
        if (lower.contains("youtube")) {
            val query = extractSearchQuery(lower, "youtube")
            return if (query.isNotBlank()) {
                if (hindiMode) {
                    AssistantResponse(
                        replyText = "Ji $userName ji, YouTube par \"$query\" chala rahi hoon!",
                        spokenText = "Haan $userName ji, YouTube par $query chala rahi hoon!",
                        action = AssistantAction.PlayMedia(query),
                        language = "hi"
                    )
                } else {
                    AssistantResponse(
                        replyText = "Opening YouTube and playing \"$query\" for you, $userName.",
                        spokenText = "Playing $query on YouTube for you, $userName.",
                        action = AssistantAction.PlayMedia(query),
                        language = "en"
                    )
                }
            } else {
                if (hindiMode) {
                    AssistantResponse(
                        replyText = "Haan $userName ji, YouTube open kar diya!",
                        spokenText = "Haan $userName ji, YouTube khol diya!",
                        action = AssistantAction.LaunchApp("YouTube", "com.google.android.youtube", "https://youtube.com"),
                        language = "hi"
                    )
                } else {
                    AssistantResponse(
                        replyText = "Opening YouTube right now for you, $userName.",
                        spokenText = "Opening YouTube for you now, $userName.",
                        action = AssistantAction.LaunchApp("YouTube", "com.google.android.youtube", "https://youtube.com"),
                        language = "en"
                    )
                }
            }
        }

        // 2. WhatsApp commands
        if (lower.contains("whatsapp")) {
            val msg = extractMessagePayload(trimmed, "whatsapp")
            return if (msg.isNotBlank()) {
                if (hindiMode) {
                    AssistantResponse(
                        replyText = "Ji $userName ji, WhatsApp par message bhejne ke liye tayyar hai:\n\"$msg\"",
                        spokenText = "Haan $userName ji, WhatsApp par message tayyar hai.",
                        action = AssistantAction.SendWhatsApp(msg),
                        language = "hi"
                    )
                } else {
                    AssistantResponse(
                        replyText = "Ready to send WhatsApp message for you, $userName:\n\"$msg\"",
                        spokenText = "Ready to send WhatsApp message, $userName.",
                        action = AssistantAction.SendWhatsApp(msg),
                        language = "en"
                    )
                }
            } else {
                if (hindiMode) {
                    AssistantResponse(
                        replyText = "Haan $userName ji, WhatsApp khol diya!",
                        spokenText = "Haan $userName ji, WhatsApp open kar diya!",
                        action = AssistantAction.LaunchApp("WhatsApp", "com.whatsapp", "https://api.whatsapp.com"),
                        language = "hi"
                    )
                } else {
                    AssistantResponse(
                        replyText = "Opening WhatsApp for you, $userName.",
                        spokenText = "Opening WhatsApp now, $userName.",
                        action = AssistantAction.LaunchApp("WhatsApp", "com.whatsapp", "https://api.whatsapp.com"),
                        language = "en"
                    )
                }
            }
        }

        // 3. Instagram commands
        if (lower.contains("instagram") || lower.contains("insta")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Haan $userName ji, Instagram khol diya!",
                    spokenText = "Haan $userName ji, Instagram open kar diya!",
                    action = AssistantAction.LaunchApp("Instagram", "com.instagram.android", "https://instagram.com"),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Opening Instagram for you, $userName.",
                    spokenText = "Opening Instagram now, $userName.",
                    action = AssistantAction.LaunchApp("Instagram", "com.instagram.android", "https://instagram.com"),
                    language = "en"
                )
            }
        }

        // 4. Camera commands
        if (lower.contains("camera") || lower.contains("photo") || lower.contains("kheecho") || lower.contains("selfie")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Camera open kar diya $userName ji, muskuraiye!",
                    spokenText = "Camera open kar diya $userName ji, muskuraiye!",
                    action = AssistantAction.OpenCamera,
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Opening Camera for you, $userName. Say cheese!",
                    spokenText = "Opening Camera now, $userName.",
                    action = AssistantAction.OpenCamera,
                    language = "en"
                )
            }
        }

        // 5. Calling / Phone dialer
        if (lower.contains("call") || lower.contains("dial") || lower.contains("phone lagao")) {
            val phone = extractPhoneNumber(trimmed)
            return if (hindiMode) {
                AssistantResponse(
                    replyText = if (phone != null) "Ji $userName ji, $phone dial karne ke liye dialer khol rahi hoon." else "Phone dialer khol rahi hoon, $userName ji.",
                    spokenText = "Phone dialer khol rahi hoon, $userName ji.",
                    action = AssistantAction.DialPhone(phone),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = if (phone != null) "Opening dialer for $phone, $userName." else "Opening phone dialer for you, $userName.",
                    spokenText = "Opening phone dialer for you, $userName.",
                    action = AssistantAction.DialPhone(phone),
                    language = "en"
                )
            }
        }

        // 6. SMS messaging
        if (lower.contains("sms") || lower.contains("text message") || (lower.contains("message") && !lower.contains("whatsapp"))) {
            val msg = extractMessagePayload(trimmed, "message")
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "SMS compose khol raha hoon $userName ji: \"${if (msg.isNotBlank()) msg else "Hello"}\"",
                    spokenText = "SMS app khol raha hoon.",
                    action = AssistantAction.SendSms(message = if (msg.isNotBlank()) msg else "Hello"),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Opening SMS messenger for you, $userName: \"${if (msg.isNotBlank()) msg else "Hello"}\"",
                    spokenText = "Opening SMS messenger.",
                    action = AssistantAction.SendSms(message = if (msg.isNotBlank()) msg else "Hello"),
                    language = "en"
                )
            }
        }

        // 7. Settings command
        if (lower.contains("setting") || lower.contains("settings")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Device settings khol raha hoon, $userName ji.",
                    spokenText = "Device settings khol raha hoon.",
                    action = AssistantAction.OpenSettings,
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Opening Android System Settings for you, $userName.",
                    spokenText = "Opening System Settings.",
                    action = AssistantAction.OpenSettings,
                    language = "en"
                )
            }
        }

        // 8. Maps & Navigation
        if (lower.contains("map") || lower.contains("maps") || lower.contains("direction") || lower.contains("rasta") || lower.contains("navigate")) {
            val destination = extractSearchQuery(lower, "to")
            val target = if (destination.isNotBlank()) destination else "nearby"
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Google Maps par rasta dhoondh raha hoon $userName ji: $target",
                    spokenText = "Maps khol raha hoon.",
                    action = AssistantAction.LaunchApp("Google Maps", "com.google.android.apps.maps", "https://maps.google.com/?q=$target"),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Opening Google Maps navigation for $target, $userName.",
                    spokenText = "Opening Maps navigation.",
                    action = AssistantAction.LaunchApp("Google Maps", "com.google.android.apps.maps", "https://maps.google.com/?q=$target"),
                    language = "en"
                )
            }
        }

        // 9. Timer / Alarm
        if (lower.contains("timer") || lower.contains("alarm")) {
            val minutes = extractNumber(lower) ?: 5
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Ji $userName ji, $minutes minute ka timer / alarm set karne ke liye Clock open kar raha hoon.",
                    spokenText = "$minutes minute ka timer set kar raha hoon.",
                    action = AssistantAction.SetTimer(minutes),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Setting a $minutes minute timer for you, $userName. Opening Clock app.",
                    spokenText = "Setting a $minutes minute timer.",
                    action = AssistantAction.SetTimer(minutes),
                    language = "en"
                )
            }
        }

        // 10. Notes / Reminders
        if (lower.contains("note") || lower.contains("yaad rakhna") || lower.contains("reminder") || lower.contains("likh lo")) {
            val noteContent = extractNoteText(trimmed)
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Ji $userName ji, maine note save kar liya hai:\n\"$noteContent\"",
                    spokenText = "Note save ho gaya hai, $userName ji.",
                    action = AssistantAction.SaveNote("Note by $userName", noteContent, isReminder = lower.contains("reminder")),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Saved to your notes, $userName:\n\"$noteContent\"",
                    spokenText = "Saved note for you, $userName.",
                    action = AssistantAction.SaveNote("Note by $userName", noteContent, isReminder = lower.contains("reminder")),
                    language = "en"
                )
            }
        }

        // 11. Music / Play media
        if (lower.contains("play") || lower.contains("music") || lower.contains("gaana") || lower.contains("song")) {
            val song = extractSongName(trimmed)
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Ji $userName ji, \"$song\" chala raha hoon.",
                    spokenText = "$song chala raha hoon.",
                    action = AssistantAction.PlayMedia(song),
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Playing \"$song\" for you, $userName. Enjoy the beats!",
                    spokenText = "Playing $song now.",
                    action = AssistantAction.PlayMedia(song),
                    language = "en"
                )
            }
        }

        // 12. Identity & Personal Companion questions
        if (lower.contains("who are you") || lower.contains("tum kaun ho") || lower.contains("aap kaun") || lower.contains("tera naam") || lower.contains("your name")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Mera naam TG AI Assistant hai! Main vishesh roop se aapke liye—yaani $userName bhai ke liye—ek personal, smart aur friendly companion hoon. Main aapke phone operations, media, WhatsApp, reminders aur har sawal ka turant jawab dene ke liye hazir hoon!",
                    spokenText = "Mera naam TG AI Assistant hai! Main aapke liye vishesh roop se banaya gaya companion hoon, $userName bhai.",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "I am TG AI Assistant, your personal, intelligent, and friendly companion created specifically for you, $userName! I can open apps, control media, compose messages, save notes, and answer any of your questions.",
                    spokenText = "I am TG AI Assistant, your personal companion created specifically for you, $userName!",
                    language = "en"
                )
            }
        }

        if (lower.contains("who is mitul") || lower.contains("mitul kaun") || lower.contains("who is nitesh") || lower.contains("mere baare mein")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Aap $userName bhai hain—mere brilliant creator aur boss! Mere liye sabse zaroori vyakti aap hain, aur main hamesha aapki seva mein tatpar hoon.",
                    spokenText = "Aap $userName bhai hain, mere brilliant creator aur boss!",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "You are $userName—my brilliant creator, boss, and the person I was specially engineered to assist! Everything I do is optimized for you.",
                    spokenText = "You are $userName, my brilliant creator and boss!",
                    language = "en"
                )
            }
        }

        // 13. Capabilities query
        if (lower.contains("what can you do") || lower.contains("tum kya kar sakte") || lower.contains("help") || lower.contains("madad")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "$userName bhai, main aapke liye yeh sab kar sakta hoon:\n\n" +
                            "• App Launch: YouTube, WhatsApp, Instagram, Camera, Maps\n" +
                            "• Messaging: Direct WhatsApp aur SMS prefilled message\n" +
                            "• Phone: Contacts dialer aur calls\n" +
                            "• Media: Songs aur YouTube video playback\n" +
                            "• Productivity: Notes likhna aur Timers set karna\n" +
                            "• Knowledge & Chit-chat: Chutkule, shayari, math, aur gyanbhari baatein!",
                    spokenText = "$userName bhai, main apps khol sakta hoon, WhatsApp bhej sakta hoon, notes likh sakta hoon aur sawalon ke jawab de sakta hoon.",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Here are my core capabilities for you, $userName:\n\n" +
                            "• Open Apps: YouTube, WhatsApp, Instagram, Camera, Maps\n" +
                            "• Messaging: Compose direct WhatsApp & SMS messages\n" +
                            "• Phone: Instant dialer and call support\n" +
                            "• Media: Play songs and videos on YouTube\n" +
                            "• Productivity: Save notes, set timers, and alarms\n" +
                            "• Knowledge & Companion: Math calculations, jokes, tech tips, and chat in Hindi & English!",
                    spokenText = "$userName, I can launch apps, send WhatsApp messages, save notes, play music, and answer questions.",
                    language = "en"
                )
            }
        }

        // 14. Jokes & Fun
        if (lower.contains("joke") || lower.contains("chutkula") || lower.contains("hasao")) {
            val hindiJokes = listOf(
                "Teacher: $userName, agar tumhare paas 10 aam hain aur 2 maine le liye toh kya bacha?\n$userName: Sir, aam toh 8 bache par aapke prati meri izzat khatam ho gayi! 😄",
                "Pappu: Yaar Wi-Fi ka password kya hai?\nShopkeeper: Pehle chai piyo!\nPappu: Theek hai, 1 chai do.\nChai peene ke baad: Ab password batao?\nShopkeeper: 'PehleChaiPiyo' bina space ke! 😂",
                "Doctor: Aapko kaun si bimari hai?\nPatient: Wi-Fi na milne par bechaini aur battery 10% par aane par chakkar aana! 📱⚡"
            )
            val englishJokes = listOf(
                "Why do programmers prefer dark mode, $userName? Because light attracts bugs! 💻",
                "There are 10 types of people in the world: those who understand binary, and those who don't! 😄",
                "Why was the cell phone wearing glasses? Because it lost all its contacts! 👓📱"
            )
            val joke = if (hindiMode) hindiJokes.random() else englishJokes.random()
            return AssistantResponse(
                replyText = joke,
                spokenText = joke.replace(Regex("[^a-zA-Z0-9\\s,?.!'\"-]"), ""),
                language = if (hindiMode) "hi" else "en"
            )
        }

        // 15. Quotes & Shayari
        if (lower.contains("quote") || lower.contains("shayari") || lower.contains("motivat")) {
            val hindiQuotes = listOf(
                "\"Manzil unhi ko milti hai jinke sapno mein jaan hoti hai,\nPankh se kuch nahi hota, hauslon se udaan hoti hai!\" 🌟\n— $userName bhai, hamesha aage badhte rahiye!",
                "\"Mushkilon se bhaagna aasan hota hai, har pehlu zindagi ka imtehaan hota hai,\nDarrne walon ko milta nahi kuch zindagi mein, ladne walon ke kadmon mein jahaan hota hai!\" 🚀",
                "\"Koshish aakhiri saans tak karni chahiye, ya toh lakshya milega ya anubhav!\" ✨"
            )
            val englishQuotes = listOf(
                "\"The best way to predict the future is to invent it.\" — Alan Kay. Keep building brilliance, $userName! 🚀",
                "\"Success is not final, failure is not fatal: it is the courage to continue that counts.\" — Winston Churchill ⚡",
                "\"Do something today that your future self will thank you for.\" Let's make today count, $userName! 🌟"
            )
            val quote = if (hindiMode) hindiQuotes.random() else englishQuotes.random()
            return AssistantResponse(
                replyText = quote,
                spokenText = quote.replace(Regex("[^a-zA-Z0-9\\s,?.!'\"-]"), ""),
                language = if (hindiMode) "hi" else "en"
            )
        }

        // 16. Time & Date
        if (lower.contains("time") || lower.contains("samay") || lower.contains("date") || lower.contains("tareekh")) {
            val sdfTime = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val sdfDate = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
            val now = Date()
            val timeStr = sdfTime.format(now)
            val dateStr = sdfDate.format(now)
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "$userName bhai, abhi samay hai: $timeStr\nAur aaj ki tareekh hai: $dateStr.",
                    spokenText = "$userName bhai, abhi samay hai $timeStr aur aaj $dateStr hai.",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Current time is $timeStr\nToday is $dateStr, $userName.",
                    spokenText = "The time is $timeStr on $dateStr.",
                    language = "en"
                )
            }
        }

        // 17. Math evaluation
        val mathResult = tryEvaluateMath(lower)
        if (mathResult != null) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "$userName bhai, hisaab ka jawab hai: $mathResult",
                    spokenText = "Jawab hai $mathResult",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "The answer is: $mathResult, $userName.",
                    spokenText = "The answer is $mathResult.",
                    language = "en"
                )
            }
        }

        // 18. Greetings
        if (lower.contains("hello") || lower.contains("hi ") || lower == "hi" || lower.contains("namaste") || lower.contains("hey")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Namaste $userName ji! Main aapki TG Assistant hoon. Kahiye, aaj main aapke liye kya kaam karoon?",
                    spokenText = "Namaste $userName ji! Kahiye, main aapke liye kya kaam karoon?",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "Hello $userName! TG Assistant is ready. What can I do for you today?",
                    spokenText = "Hello $userName! What can I do for you today?",
                    language = "en"
                )
            }
        }

        if (lower.contains("how are you") || lower.contains("kaise ho") || lower.contains("kya haal")) {
            return if (hindiMode) {
                AssistantResponse(
                    replyText = "Main bilkul acchi hoon $userName ji! Aapka din kaisa ja raha hai?",
                    spokenText = "Main bilkul acchi hoon $userName ji!",
                    language = "hi"
                )
            } else {
                AssistantResponse(
                    replyText = "I am doing great, $userName! How can I assist you right now?",
                    spokenText = "I am doing great, $userName! How can I assist you?",
                    language = "en"
                )
            }
        }

        // 19. Default fallback: Helpful conversational companion response with Google search action
        return if (hindiMode) {
            AssistantResponse(
                replyText = "Ji $userName ji, maine aapki baat suni: \"$trimmed\".\nIske baare mein vistaar se jaanne ke liye main Google par search bhi kar sakti hoon:",
                spokenText = "Ji $userName ji, iske liye Google search uplabdh hai.",
                action = AssistantAction.WebSearch(trimmed),
                language = "hi"
            )
        } else {
            AssistantResponse(
                replyText = "Understood, $userName. For \"$trimmed\", I can search the web for the latest detailed results:",
                spokenText = "I found this web search option for you, $userName.",
                action = AssistantAction.WebSearch(trimmed),
                language = "en"
            )
        }
    }

    private fun extractSearchQuery(input: String, keyword: String): String {
        val idx = input.indexOf(keyword)
        if (idx == -1) return ""
        val sub = input.substring(idx + keyword.length).trim()
        val cleaned = sub.replace(Regex("(?i)^(pe|par|me|mein|search for|search|video of|song|chalao|play|kholo|open|khol do|open ker do|open kar do|chala do)\\s*"), "").trim()
        val pureLaunchPhrases = setOf(
            "open", "kholo", "khol do", "chalao", "chala do",
            "open ker do", "open kar do", "start", "launch", "karo", "do", ""
        )
        if (pureLaunchPhrases.contains(cleaned.lowercase(Locale.ROOT))) {
            return ""
        }
        return cleaned
    }

    private fun extractMessagePayload(input: String, trigger: String): String {
        val lower = input.lowercase(Locale.ROOT)
        val idx = lower.indexOf(trigger)
        if (idx == -1) return ""
        val sub = input.substring(idx + trigger.length).trim()
        return sub.replace(Regex("^(message|to|ko|pe|saying|likho|bhejo)\\s*"), "").trim()
    }

    private fun extractPhoneNumber(input: String): String? {
        val matcher = Pattern.compile("(\\+?[0-9]{10,13})").matcher(input)
        return if (matcher.find()) matcher.group(1) else null
    }

    private fun extractNumber(input: String): Int? {
        val matcher = Pattern.compile("(\\d+)").matcher(input)
        return if (matcher.find()) matcher.group(1)?.toIntOrNull() else null
    }

    private fun extractNoteText(input: String): String {
        return input.replace(Regex("(?i)^(take a note|note down|save note|note|yaad rakhna|likh lo|reminder)\\s*"), "").trim()
    }

    private fun extractSongName(input: String): String {
        val cleaned = input.replace(Regex("(?i)^(play song|play music|play|music|gaana chalao|song)\\s*"), "").trim()
        return if (cleaned.isNotBlank()) cleaned else "Top Hits"
    }

    private fun tryEvaluateMath(input: String): String? {
        val cleaned = input.replace(Regex("(?i)(what is|calculate|solve|hisaab|batao|\\?)"), "").trim()
        val matcher = Pattern.compile("([0-9.]+)\\s*([+\\-*/xX])\\s*([0-9.]+)").matcher(cleaned)
        if (matcher.find()) {
            val a = matcher.group(1)?.toDoubleOrNull() ?: return null
            val op = matcher.group(2) ?: return null
            val b = matcher.group(3)?.toDoubleOrNull() ?: return null
            val res = when (op) {
                "+" -> a + b
                "-" -> a - b
                "*", "x", "X" -> a * b
                "/" -> if (b != 0.0) a / b else return "Undefined (division by zero)"
                else -> return null
            }
            return if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.2f", res)
        }
        return null
    }
}
