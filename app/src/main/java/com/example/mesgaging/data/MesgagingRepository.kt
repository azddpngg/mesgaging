package com.example.mesgaging.data

import android.content.Context
import android.net.Uri
import com.example.mesgaging.ai.GeminiAiService
import com.example.mesgaging.audio.LiveVoiceCallEngine
import com.example.mesgaging.cloud.CloudStorageManager
import com.example.mesgaging.cloud.CloudUploadResult
import com.example.mesgaging.screen.ScreenShareManager
import com.example.mesgaging.data.local.ContactEntity
import com.example.mesgaging.data.local.MessageEntity
import com.example.mesgaging.data.local.MesgagingDatabase
import com.example.mesgaging.data.local.SavedSnippetEntity
import com.example.mesgaging.data.local.UserBotEntity
import com.example.mesgaging.data.local.UserProfileEntity
import com.example.mesgaging.model.ChatMessage
import com.example.mesgaging.model.FriendRequest
import com.example.mesgaging.model.MessageEffect
import com.example.mesgaging.model.SavedSnippet
import com.example.mesgaging.model.UserBot
import com.example.mesgaging.model.UserContact
import com.example.mesgaging.model.UserStatus
import com.example.mesgaging.notification.MesgagingNotificationManager
import com.example.mesgaging.qr.QrCodeGenerator
import android.util.Log
import com.example.R
import com.example.mesgaging.audio.EffectSoundManager
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID
import kotlin.random.Random

class MesgagingRepository(val context: Context) {

    val soundManager = EffectSoundManager(context)
    val aiService = GeminiAiService()
    val notificationManager = MesgagingNotificationManager(context)
    val cloudStorageManager = CloudStorageManager()
    val callEngine = LiveVoiceCallEngine(context)
    val screenShareManager = ScreenShareManager(context)

    val firestore: FirebaseFirestore = try {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    } catch (e: Exception) {
        Log.e("MesgagingRepository", "Firestore init error", e)
        FirebaseFirestore.getInstance()
    }
    val auth: FirebaseAuth = Firebase.auth
    private val activeSyncRegistrations = mutableMapOf<String, ListenerRegistration>()

    private val db = MesgagingDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val defaultUser = UserContact(
        id = "user_me",
        username = "GhostEcho",
        discriminator = "#4092",
        avatarEmoji = "🦅",
        avatarColor = 0xFF2B2D32,
        status = UserStatus.ONLINE,
        customStatus = "Ready to chat & call",
        qrPayload = QrCodeGenerator.buildPayload("user_me", "GhostEcho", "#4092", "🦅", 0xFF2B2D32)
    )

    // Reactive StateFlows backed by Room Database
    val currentUser: StateFlow<UserContact> = db.userProfileDao().getUserProfileFlow()
        .map { entity ->
            if (entity != null) {
                UserContact(
                    id = entity.id,
                    username = entity.username,
                    discriminator = entity.discriminator,
                    avatarEmoji = entity.avatarEmoji,
                    avatarColor = entity.avatarColor,
                    status = UserStatus.ONLINE,
                    customStatus = entity.customStatus,
                    qrPayload = entity.qrPayload
                )
            } else {
                defaultUser
            }
        }.stateIn(scope, SharingStarted.Eagerly, defaultUser)

    val contacts: StateFlow<List<UserContact>> = db.contactDao().getAllContactsFlow()
        .map { list ->
            list.map { entity ->
                UserContact(
                    id = entity.id,
                    username = entity.username,
                    discriminator = entity.discriminator,
                    avatarEmoji = entity.avatarEmoji,
                    avatarColor = entity.avatarColor,
                    status = when (entity.statusLabel.lowercase()) {
                        "online" -> UserStatus.ONLINE
                        "idle" -> UserStatus.IDLE
                        else -> UserStatus.OFFLINE
                    },
                    customStatus = entity.customStatus,
                    qrPayload = entity.qrPayload,
                    connectedAt = entity.connectedAt
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val userBots: StateFlow<List<UserBot>> = db.userBotDao().getAllBotsFlow()
        .map { list ->
            list.filter { it.id != "cortex" }.map { entity ->
                UserBot(
                    id = entity.id,
                    name = entity.name,
                    avatarEmoji = entity.avatarEmoji,
                    rolePrompt = entity.rolePrompt,
                    triggerPrefix = entity.triggerPrefix,
                    defaultEffects = deserializeEffects(entity.defaultEffectsCsv),
                    createdAt = entity.createdAt
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val messages: StateFlow<Map<String, List<ChatMessage>>> = db.messageDao().getAllMessagesFlow()
        .map { list ->
            list.filter { it.channelId != "chat_cortex" }.groupBy({ it.channelId }, { entity ->
                ChatMessage(
                    id = entity.id,
                    channelId = entity.channelId,
                    senderId = entity.senderId,
                    senderName = entity.senderName,
                    senderAvatar = entity.senderAvatar,
                    senderColor = entity.senderColor,
                    text = entity.text,
                    photoUri = entity.photoUri,
                    fileName = entity.fileName,
                    fileSize = entity.fileSize,
                    fileUri = entity.fileUri,
                    timestamp = entity.timestamp,
                    effects = deserializeEffects(entity.effectsCsv),
                    reactions = deserializeReactions(entity.reactionsJson),
                    spamMultiplier = entity.spamMultiplier,
                    isSaved = entity.isSaved
                )
            })
        }.stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val savedSnippets: StateFlow<List<SavedSnippet>> = db.savedSnippetDao().getAllSnippetsFlow()
        .map { list ->
            list.map { entity ->
                SavedSnippet(
                    id = entity.id,
                    title = entity.title,
                    content = entity.content,
                    category = entity.category,
                    photoUri = entity.photoUri,
                    effects = deserializeEffects(entity.effectsCsv),
                    createdAt = entity.createdAt
                )
            }
        }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    // In-memory Real Friend Requests (Empty by default - no fake friends)
    private val _friendRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val friendRequests: StateFlow<List<FriendRequest>> = _friendRequests.asStateFlow()

    init {
        scope.launch {
            seedDatabaseIfEmpty()
            startFirestoreChannelSync("chat_general")
        }
    }

    fun startFirestoreChannelSync(channelId: String) {
        if (activeSyncRegistrations.containsKey(channelId)) return
        try {
            val reg = firestore.collection("channels")
                .document(channelId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("MesgagingRepository", "Firestore sync error for $channelId: ${error.message}")
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        scope.launch {
                            val currentUserId = currentUser.value.id
                            val authedUid = auth.currentUser?.uid
                            for (doc in snapshot.documents) {
                                try {
                                    val id = doc.getString("id") ?: doc.id
                                    val senderId = doc.getString("senderId") ?: ""
                                    val senderName = doc.getString("senderName") ?: "Anonymous"
                                    val senderAvatar = doc.getString("senderAvatar") ?: "🦅"
                                    val senderColor = (doc.get("senderColor") as? Number)?.toLong() ?: 0xFF2B2D32
                                    val text = doc.getString("text") ?: ""
                                    val photoUri = doc.getString("photoUri")
                                    val fileName = doc.getString("fileName")
                                    val fileSize = doc.getString("fileSize")
                                    val fileUri = doc.getString("fileUri")
                                    val timestamp = (doc.get("timestamp") as? Number)?.toLong() ?: System.currentTimeMillis()
                                    val effectsList = (doc.get("effects") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                                    val spamMultiplier = (doc.get("spamMultiplier") as? Number)?.toInt() ?: 1
                                    val isSaved = doc.getBoolean("isSaved") ?: false
                                    val reactionsMap = (doc.get("reactions") as? Map<*, *>) ?: emptyMap<String, Any>()
                                    val reactionsJson = JSONObject(reactionsMap).toString()

                                    val existing = db.messageDao().getMessageById(id)
                                    if (existing == null) {
                                        db.messageDao().insertMessage(
                                            MessageEntity(
                                                id = id,
                                                channelId = channelId,
                                                senderId = senderId,
                                                senderName = senderName,
                                                senderAvatar = senderAvatar,
                                                senderColor = senderColor,
                                                text = text,
                                                photoUri = photoUri,
                                                fileName = fileName,
                                                fileSize = fileSize,
                                                fileUri = fileUri,
                                                timestamp = timestamp,
                                                effectsCsv = effectsList.joinToString(","),
                                                reactionsJson = reactionsJson,
                                                spamMultiplier = spamMultiplier,
                                                isSaved = isSaved
                                            )
                                        )
                                        // Incoming message from another user: play pop sound and trigger effects!
                                        if (senderId != currentUserId && senderId != authedUid) {
                                            soundManager.playIncomingMessageSound()
                                            val parsedEffects = deserializeEffects(effectsList.joinToString(","))
                                            if (parsedEffects.isNotEmpty()) {
                                                soundManager.playMessageEffects(parsedEffects, text)
                                            }
                                        }
                                    } else {
                                        if (existing.reactionsJson != reactionsJson) {
                                            db.messageDao().updateReactions(id, reactionsJson)
                                        }
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            activeSyncRegistrations[channelId] = reg
        } catch (e: Exception) {
            Log.e("MesgagingRepository", "Error starting sync for $channelId", e)
        }
    }

    private fun generateRandomRealAccount(): UserProfileEntity {
        val adjectives = listOf(
            "Cyber", "Shadow", "Neon", "Phantom", "Quantum", "Echo", "Vortex", "Astral",
            "Nova", "Solar", "Glitch", "Hyper", "Apex", "Frost", "Iron", "Viper", "Specter",
            "Pulse", "Chrono", "Zero", "Storm", "Rogue", "Blaze", "Zenith", "Silent"
        )
        val nouns = listOf(
            "Falcon", "Raven", "Wolf", "Ghost", "Hawk", "Phoenix", "Fox", "Striker",
            "Drifter", "Samurai", "Cobra", "Dragon", "Cipher", "Knight", "Ranger", "Lynx",
            "Tiger", "Onyx", "Blaze", "Sparrow", "Vanguard", "Titan", "Nomad", "Hunter"
        )
        val emojis = listOf("🦅", "⚡", "🐺", "🕶️", "🚀", "🔥", "🎧", "👾", "🦊", "🌪️", "💎", "🌙", "⚔️", "🎯", "🕹️")
        val colors = listOf(0xFF2B2D32, 0xFF4F46E5, 0xFF0D9488, 0xFFE11D48, 0xFF7C3AED, 0xFF2563EB, 0xFFD97706)

        val name = "${adjectives.random()}${nouns.random()}"
        val disc = "#${Random.nextInt(1000, 9999)}"
        val emoji = emojis.random()
        val color = colors.random()
        val id = "usr_${UUID.randomUUID().toString().take(8)}"
        val qrPayload = QrCodeGenerator.buildPayload(id, name, disc, emoji, color)

        return UserProfileEntity(
            id = id,
            username = name,
            discriminator = disc,
            avatarEmoji = emoji,
            avatarColor = color,
            customStatus = "Ready to chat & call",
            qrPayload = qrPayload
        )
    }

    private suspend fun seedDatabaseIfEmpty() {
        // Clean out any stale fake bots, fake messages, or fake contacts
        db.userBotDao().deleteBot("cortex")
        db.messageDao().deleteFakeMessages()
        db.contactDao().deleteFakeContacts()

        // Real account: Check if user profile exists; if not, automatically generate a real account with a random username
        val existingProfile = db.userProfileDao().getUserProfile()
        if (existingProfile == null) {
            val realAccount = generateRandomRealAccount()
            db.userProfileDao().insertOrUpdateProfile(realAccount)
        }
    }

    suspend fun uploadToCloud(context: Context, uri: Uri, fileName: String): CloudUploadResult {
        val result = cloudStorageManager.uploadFile(context, uri, fileName)
        if (result.success && result.directDownloadUrl != null) {
            notificationManager.showCloudUploadNotification(fileName, result.directDownloadUrl)
        }
        return result
    }

    fun updateProfile(username: String, emoji: String) {
        val current = currentUser.value
        val newName = username.trim().ifBlank { current.username }
        val newEmoji = emoji.trim().ifBlank { current.avatarEmoji }
        val newPayload = QrCodeGenerator.buildPayload(current.id, newName, current.discriminator, newEmoji, current.avatarColor)

        scope.launch {
            db.userProfileDao().insertOrUpdateProfile(
                UserProfileEntity(
                    id = current.id,
                    username = newName,
                    discriminator = current.discriminator,
                    avatarEmoji = newEmoji,
                    avatarColor = current.avatarColor,
                    customStatus = current.customStatus,
                    qrPayload = newPayload
                )
            )
        }
    }

    fun createUserBot(
        name: String,
        avatarEmoji: String,
        rolePrompt: String,
        triggerPrefix: String,
        defaultEffects: Set<MessageEffect>
    ): UserBot {
        val botId = UUID.randomUUID().toString().take(8)
        val bot = UserBot(
            id = botId,
            name = name.trim(),
            avatarEmoji = avatarEmoji.ifBlank { "🤖" },
            rolePrompt = rolePrompt.trim(),
            triggerPrefix = triggerPrefix.ifBlank { name.lowercase() },
            defaultEffects = defaultEffects
        )

        scope.launch {
            db.userBotDao().insertBot(
                UserBotEntity(
                    id = bot.id,
                    name = bot.name,
                    avatarEmoji = bot.avatarEmoji,
                    rolePrompt = bot.rolePrompt,
                    triggerPrefix = bot.triggerPrefix,
                    defaultEffectsCsv = serializeEffects(bot.defaultEffects),
                    createdAt = bot.createdAt
                )
            )
        }
        return bot
    }

    fun addContactFromQr(
        id: String,
        username: String,
        discriminator: String,
        avatarEmoji: String,
        avatarColor: Long
    ): UserContact {
        val contact = UserContact(
            id = id,
            username = username,
            discriminator = discriminator,
            avatarEmoji = avatarEmoji,
            avatarColor = avatarColor,
            status = UserStatus.ONLINE,
            customStatus = "Connected via QR Pass",
            qrPayload = QrCodeGenerator.buildPayload(id, username, discriminator, avatarEmoji, avatarColor),
            connectedAt = System.currentTimeMillis()
        )

        scope.launch {
            db.contactDao().insertContact(
                ContactEntity(
                    id = contact.id,
                    username = contact.username,
                    discriminator = contact.discriminator,
                    avatarEmoji = contact.avatarEmoji,
                    avatarColor = contact.avatarColor,
                    statusLabel = "Online",
                    customStatus = contact.customStatus,
                    qrPayload = contact.qrPayload,
                    connectedAt = contact.connectedAt
                )
            )
        }
        return contact
    }

    fun addFriendByTag(tag: String): UserContact? {
        val trimmed = tag.trim().removePrefix("@")
        val parts = trimmed.split("#")
        val name = parts.firstOrNull()?.trim() ?: return null
        if (name.isBlank()) return null
        val disc = if (parts.size > 1 && parts[1].isNotBlank()) "#${parts[1].trim()}" else "#${Random.nextInt(1000, 9999)}"

        val friendId = "usr_${name.lowercase().replace(" ", "_")}_${disc.removePrefix("#")}"
        val newContact = UserContact(
            id = friendId,
            username = name,
            discriminator = disc,
            avatarEmoji = "💬",
            avatarColor = 0xFF5865F2,
            status = UserStatus.ONLINE,
            customStatus = "Added by Tag",
            qrPayload = QrCodeGenerator.buildPayload(friendId, name, disc, "💬", 0xFF5865F2),
            connectedAt = System.currentTimeMillis()
        )

        scope.launch {
            db.contactDao().insertContact(
                ContactEntity(
                    id = newContact.id,
                    username = newContact.username,
                    discriminator = newContact.discriminator,
                    avatarEmoji = newContact.avatarEmoji,
                    avatarColor = newContact.avatarColor,
                    statusLabel = "Online",
                    customStatus = newContact.customStatus,
                    qrPayload = newContact.qrPayload,
                    connectedAt = newContact.connectedAt
                )
            )
        }
        return newContact
    }

    fun acceptFriendRequest(requestId: String) {
        val req = _friendRequests.value.find { it.id == requestId } ?: return
        val contact = UserContact(
            id = "user_${req.username.lowercase().replace(" ", "_")}",
            username = req.username,
            discriminator = req.discriminator,
            avatarEmoji = req.avatarEmoji,
            avatarColor = req.avatarColor,
            status = UserStatus.ONLINE,
            customStatus = "Connected via Friend Request",
            qrPayload = QrCodeGenerator.buildPayload("user_${req.username}", req.username, req.discriminator, req.avatarEmoji, req.avatarColor),
            connectedAt = System.currentTimeMillis()
        )
        scope.launch {
            db.contactDao().insertContact(
                ContactEntity(
                    id = contact.id,
                    username = contact.username,
                    discriminator = contact.discriminator,
                    avatarEmoji = contact.avatarEmoji,
                    avatarColor = contact.avatarColor,
                    statusLabel = "Online",
                    customStatus = contact.customStatus,
                    qrPayload = contact.qrPayload,
                    connectedAt = contact.connectedAt
                )
            )
        }
        _friendRequests.value = _friendRequests.value.filter { it.id != requestId }
    }

    fun rejectFriendRequest(requestId: String) {
        _friendRequests.value = _friendRequests.value.filter { it.id != requestId }
    }

    fun removeContact(contactId: String) {
        scope.launch {
            db.contactDao().deleteContact(contactId)
        }
    }

    fun sendMessage(
        channelId: String,
        text: String,
        effects: Set<MessageEffect> = emptySet(),
        photoUri: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        fileUri: String? = null,
        spamMultiplier: Int = 1
    ): ChatMessage {
        val user = currentUser.value
        val authedUid = auth.currentUser?.uid ?: user.id
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            senderId = authedUid,
            senderName = user.username,
            senderAvatar = user.avatarEmoji,
            senderColor = user.avatarColor,
            text = text,
            photoUri = photoUri,
            fileName = fileName,
            fileSize = fileSize,
            fileUri = fileUri,
            timestamp = System.currentTimeMillis(),
            effects = effects,
            spamMultiplier = spamMultiplier
        )

        // 1. Instant local Room persistence for zero-lag UI
        scope.launch {
            db.messageDao().insertMessage(
                MessageEntity(
                    id = msg.id,
                    channelId = msg.channelId,
                    senderId = msg.senderId,
                    senderName = msg.senderName,
                    senderAvatar = msg.senderAvatar,
                    senderColor = msg.senderColor,
                    text = msg.text,
                    photoUri = msg.photoUri,
                    fileName = msg.fileName,
                    fileSize = msg.fileSize,
                    fileUri = msg.fileUri,
                    timestamp = msg.timestamp,
                    effectsCsv = serializeEffects(msg.effects),
                    reactionsJson = serializeReactions(msg.reactions),
                    spamMultiplier = msg.spamMultiplier,
                    isSaved = msg.isSaved
                )
            )
        }

        // 2. Play local sound effects
        if (effects.isNotEmpty()) {
            soundManager.playMessageEffects(effects, text)
        }

        // 3. Broadcast to Firestore so message appears for everyone in real-time
        try {
            val firestoreData = hashMapOf(
                "id" to msg.id,
                "channelId" to msg.channelId,
                "senderId" to authedUid,
                "senderName" to user.username,
                "senderAvatar" to user.avatarEmoji,
                "senderColor" to user.avatarColor,
                "text" to msg.text,
                "photoUri" to msg.photoUri,
                "fileName" to msg.fileName,
                "fileSize" to msg.fileSize,
                "fileUri" to msg.fileUri,
                "timestamp" to msg.timestamp,
                "effects" to msg.effects.map { it.id },
                "reactions" to msg.reactions,
                "spamMultiplier" to msg.spamMultiplier,
                "isSaved" to msg.isSaved
            )
            firestore.collection("channels")
                .document(channelId)
                .collection("messages")
                .document(msg.id)
                .set(firestoreData)
                .addOnFailureListener { e ->
                    Log.w("MesgagingRepository", "Firestore send message error: ${e.message}")
                }
        } catch (e: Exception) {
            Log.e("MesgagingRepository", "Error broadcasting message to Firestore", e)
        }
        return msg
    }

    fun insertBotReply(
        channelId: String,
        botId: String,
        botName: String,
        botAvatar: String,
        text: String,
        effects: Set<MessageEffect> = emptySet()
    ): ChatMessage {
        val msg = ChatMessage(
            id = UUID.randomUUID().toString(),
            channelId = channelId,
            senderId = "bot_$botId",
            senderName = botName,
            senderAvatar = botAvatar,
            senderColor = 0xFF5865F2,
            text = text,
            timestamp = System.currentTimeMillis(),
            effects = effects
        )

        scope.launch {
            db.messageDao().insertMessage(
                MessageEntity(
                    id = msg.id,
                    channelId = msg.channelId,
                    senderId = msg.senderId,
                    senderName = msg.senderName,
                    senderAvatar = msg.senderAvatar,
                    senderColor = msg.senderColor,
                    text = msg.text,
                    photoUri = null,
                    fileName = null,
                    fileSize = null,
                    fileUri = null,
                    timestamp = msg.timestamp,
                    effectsCsv = serializeEffects(msg.effects),
                    reactionsJson = serializeReactions(msg.reactions),
                    spamMultiplier = msg.spamMultiplier,
                    isSaved = msg.isSaved
                )
            )

            // Post notification for incoming bot reply
            notificationManager.showMessageNotification(
                senderName = botName,
                messageText = text,
                avatarEmoji = botAvatar,
                channelId = channelId
            )
        }
        return msg
    }

    fun purgeChannelMessages(channelId: String) {
        scope.launch {
            db.messageDao().deleteMessagesForChannel(channelId)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        scope.launch {
            val existing = db.messageDao().getMessageById(messageId) ?: return@launch
            val currentReactions = deserializeReactions(existing.reactionsJson).toMutableMap()
            currentReactions[emoji] = (currentReactions[emoji] ?: 0) + 1
            db.messageDao().updateReactions(messageId, serializeReactions(currentReactions))

            // Broadcast reaction to Firestore
            try {
                firestore.collection("channels")
                    .document(existing.channelId)
                    .collection("messages")
                    .document(messageId)
                    .update("reactions.$emoji", FieldValue.increment(1))
            } catch (_: Exception) {}
        }
    }

    fun saveSnippet(title: String, content: String, category: String, photoUri: String?, effects: Set<MessageEffect>) {
        scope.launch {
            db.savedSnippetDao().insertSnippet(
                SavedSnippetEntity(
                    id = UUID.randomUUID().toString(),
                    title = title.ifBlank { "Untitled Note" },
                    content = content,
                    category = category.ifBlank { "General" },
                    photoUri = photoUri,
                    effectsCsv = serializeEffects(effects),
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteSnippet(id: String) {
        scope.launch {
            db.savedSnippetDao().deleteSnippet(id)
        }
    }

    private fun serializeEffects(effects: Set<MessageEffect>): String {
        return effects.joinToString(",") { it.id }
    }

    private fun deserializeEffects(csv: String): Set<MessageEffect> {
        if (csv.isBlank()) return emptySet()
        return csv.split(",").mapNotNull { id ->
            MessageEffect.entries.find { it.id == id.trim() }
        }.toSet()
    }

    private fun serializeReactions(map: Map<String, Int>): String {
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k, v) }
        return json.toString()
    }

    private fun deserializeReactions(jsonStr: String): Map<String, Int> {
        if (jsonStr.isBlank()) return emptyMap()
        return try {
            val json = JSONObject(jsonStr)
            val result = mutableMapOf<String, Int>()
            json.keys().forEach { key ->
                result[key] = json.getInt(key)
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }
}
