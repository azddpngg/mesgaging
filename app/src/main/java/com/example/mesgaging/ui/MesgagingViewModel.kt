package com.example.mesgaging.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.mesgaging.audio.EffectSoundManager
import com.example.mesgaging.data.MesgagingRepository
import com.example.mesgaging.model.ActiveCall
import com.example.mesgaging.model.CallState
import com.example.mesgaging.model.Channel
import android.app.Activity
import com.example.mesgaging.model.CallSoundboardEffect
import com.example.mesgaging.model.CallVoiceEffect
import com.example.mesgaging.model.ChatMessage
import com.example.mesgaging.model.EmojiBurstParticle
import com.example.mesgaging.model.FriendFilter
import com.example.mesgaging.model.FriendRequest
import com.example.mesgaging.model.MessageEffect
import com.example.mesgaging.model.NavTab
import com.example.mesgaging.model.PixelAudioEmoji
import com.example.mesgaging.model.SavedSnippet
import com.example.mesgaging.model.ScreenShareSession
import com.example.mesgaging.model.UserBot
import com.example.mesgaging.model.UserContact
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class MesgagingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MesgagingRepository(application)
    private val soundManager = EffectSoundManager(application)

    // Navigation Tab (Messages vs Friends)
    private val _currentNavTab = MutableStateFlow(NavTab.MESSAGES)
    val currentNavTab: StateFlow<NavTab> = _currentNavTab.asStateFlow()

    // Friends Filter Tab (Online, All, Pending, Add Friend)
    private val _friendFilter = MutableStateFlow(FriendFilter.ONLINE)
    val friendFilter: StateFlow<FriendFilter> = _friendFilter.asStateFlow()

    // Screen Share Session
    private val _screenShareSession = MutableStateFlow<ScreenShareSession?>(null)
    val screenShareSession: StateFlow<ScreenShareSession?> = _screenShareSession.asStateFlow()

    // Default Channel (General global chat room - no fake friends)
    private val _activeChannel = MutableStateFlow(
        Channel(
            id = "chat_general",
            name = "general",
            topic = "Global Chat • Cloud Storage & Calling Active",
            isDm = false
        )
    )
    val activeChannel: StateFlow<Channel> = _activeChannel.asStateFlow()

    val currentUser: StateFlow<UserContact> = repository.currentUser
    val contacts: StateFlow<List<UserContact>> = repository.contacts
    val userBots: StateFlow<List<UserBot>> = repository.userBots
    val savedSnippets: StateFlow<List<SavedSnippet>> = repository.savedSnippets
    val friendRequests: StateFlow<List<FriendRequest>> = repository.friendRequests

    val currentMessages: StateFlow<List<ChatMessage>> = combine(
        repository.messages,
        _activeChannel
    ) { msgMap, channel ->
        msgMap[channel.id] ?: emptyList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Effects arming & spam multiplier
    private val _selectedEffects = MutableStateFlow<Set<MessageEffect>>(emptySet())
    val selectedEffects: StateFlow<Set<MessageEffect>> = _selectedEffects.asStateFlow()

    private val _spamMultiplier = MutableStateFlow(1)
    val spamMultiplier: StateFlow<Int> = _spamMultiplier.asStateFlow()

    // Voice Call state & Live Audio Engine
    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    val micAmplitude: StateFlow<Float> = repository.callEngine.micAmplitude
    val callDurationSeconds: StateFlow<Long> = repository.callEngine.callSeconds

    // Real In-Call Screen Sharing stream & state
    val liveScreenFrame = repository.screenShareManager.liveScreenFrame
    val isScreenSharing = repository.screenShareManager.isSharing
    val isScreenSharePaused = repository.screenShareManager.isPaused
    val screenShareResolution = repository.screenShareManager.resolutionText

    // Cloud Uploading state
    private val _isUploadingCloud = MutableStateFlow(false)
    val isUploadingCloud: StateFlow<Boolean> = _isUploadingCloud.asStateFlow()

    private val _uploadProgressNotice = MutableStateFlow<String?>(null)
    val uploadProgressNotice: StateFlow<String?> = _uploadProgressNotice.asStateFlow()

    // Dialog & UI states
    private val _showScannerDialog = MutableStateFlow(false)
    val showScannerDialog: StateFlow<Boolean> = _showScannerDialog.asStateFlow()

    private val _showMyQrDialog = MutableStateFlow(false)
    val showMyQrDialog: StateFlow<Boolean> = _showMyQrDialog.asStateFlow()

    private val _showNameDialog = MutableStateFlow(false)
    val showNameDialog: StateFlow<Boolean> = _showNameDialog.asStateFlow()

    private val _showVaultSheet = MutableStateFlow(false)
    val showVaultSheet: StateFlow<Boolean> = _showVaultSheet.asStateFlow()

    private val _showCreateBotDialog = MutableStateFlow(false)
    val showCreateBotDialog: StateFlow<Boolean> = _showCreateBotDialog.asStateFlow()

    private val _showEmojiPicker = MutableStateFlow(false)
    val showEmojiPicker: StateFlow<Boolean> = _showEmojiPicker.asStateFlow()

    private val _botTypingNotice = MutableStateFlow<String?>(null)
    val botTypingNotice: StateFlow<String?> = _botTypingNotice.asStateFlow()

    // Privacy & Security Controls
    private val _isPrivacyShieldActive = MutableStateFlow(false)
    val isPrivacyShieldActive: StateFlow<Boolean> = _isPrivacyShieldActive.asStateFlow()

    private val _showPrivacySecurityDialog = MutableStateFlow(false)
    val showPrivacySecurityDialog: StateFlow<Boolean> = _showPrivacySecurityDialog.asStateFlow()

    private val _disappearingTimerMinutes = MutableStateFlow(0)
    val disappearingTimerMinutes: StateFlow<Int> = _disappearingTimerMinutes.asStateFlow()

    private val _activeParticles = MutableStateFlow<List<EmojiBurstParticle>>(emptyList())
    val activeParticles: StateFlow<List<EmojiBurstParticle>> = _activeParticles.asStateFlow()

    private val _bannerNotice = MutableStateFlow<String?>(null)
    val bannerNotice: StateFlow<String?> = _bannerNotice.asStateFlow()

    fun switchNavTab(tab: NavTab) {
        _currentNavTab.value = tab
    }

    fun setFriendFilter(filter: FriendFilter) {
        _friendFilter.value = filter
    }

    fun openDirectMessageWithFriend(friend: UserContact) {
        _activeChannel.value = Channel(
            id = "dm_${friend.id}",
            name = friend.username,
            topic = "Direct messaging with ${friend.username}",
            isDm = true,
            recipientContactId = friend.id
        )
        _currentNavTab.value = NavTab.MESSAGES
    }

    fun addFriendByTag(tag: String) {
        val contact = repository.addFriendByTag(tag)
        if (contact != null) {
            _bannerNotice.value = "Connected to ${contact.username}${contact.discriminator}!"
            openDirectMessageWithFriend(contact)
        } else {
            _bannerNotice.value = "Please enter a valid format: Username#1234"
        }
    }

    fun acceptFriendRequest(id: String) {
        repository.acceptFriendRequest(id)
        _bannerNotice.value = "Friend request accepted!"
    }

    fun rejectFriendRequest(id: String) {
        repository.rejectFriendRequest(id)
        _bannerNotice.value = "Friend request declined"
    }

    fun removeFriend(id: String) {
        repository.removeContact(id)
        _bannerNotice.value = "Friend removed"
        if (_activeChannel.value.recipientContactId == id) {
            _activeChannel.value = Channel(
                id = "chat_general",
                name = "general",
                topic = "Global Chat • Cloud Storage Enabled",
                isDm = false
            )
        }
    }


    fun updateProfile(name: String, emoji: String) {
        repository.updateProfile(name, emoji)
        _bannerNotice.value = "Display name updated to '$name'"
    }

    fun openNameDialog() { _showNameDialog.value = true }
    fun closeNameDialog() { _showNameDialog.value = false }

    fun openCreateBotDialog() { _showCreateBotDialog.value = true }
    fun closeCreateBotDialog() { _showCreateBotDialog.value = false }

    fun createUserBot(name: String, avatar: String, prompt: String, trigger: String, effects: Set<MessageEffect>) {
        val bot = repository.createUserBot(name, avatar, prompt, trigger, effects)
        _bannerNotice.value = "Bot '${bot.name}' created!"
        _activeChannel.value = Channel(
            id = "chat_${bot.id}",
            name = bot.name,
            topic = "Custom Bot • Trigger: '${bot.triggerPrefix}'",
            isDm = true,
            isBotChat = true,
            botId = bot.id
        )
        _currentNavTab.value = NavTab.MESSAGES
    }

    fun selectChannel(channel: Channel) {
        _activeChannel.value = channel
        _currentNavTab.value = NavTab.MESSAGES
        repository.startFirestoreChannelSync(channel.id)
    }

    fun onUserSignedIn(firebaseUser: com.google.firebase.auth.FirebaseUser?) {
        if (firebaseUser != null) {
            val name = firebaseUser.displayName?.ifBlank { null }
                ?: firebaseUser.email?.substringBefore("@")
                ?: "Pilot"
            repository.updateProfile(name, "🦅")
            repository.startFirestoreChannelSync(_activeChannel.value.id)
        }
    }

    // Actual Voice Calling
    fun startVoiceCall(targetName: String = activeChannel.value.name, targetEmoji: String = "💬", targetId: String = "") {
        soundManager.playCallRingTone()
        repository.callEngine.startCall()
        repository.notificationManager.showCallNotification(targetName, isConnected = true)
        _activeCall.value = ActiveCall(
            id = UUID.randomUUID().toString(),
            participantName = targetName,
            participantEmoji = targetEmoji,
            isBot = false,
            channelId = _activeChannel.value.id,
            state = CallState.CONNECTED
        )
    }

    fun endVoiceCall() {
        repository.screenShareManager.stopScreenShare()
        soundManager.stopCallAudio()
        soundManager.playCallEndSound()
        repository.callEngine.endCall()
        repository.notificationManager.cancelCallNotification()
        _activeCall.value = null
    }

    fun toggleMuteCall() {
        val isMuted = repository.callEngine.toggleMute()
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isMuted = isMuted)
    }

    fun toggleSpeakerCall() {
        val isSpeaker = repository.callEngine.toggleSpeaker()
        val call = _activeCall.value ?: return
        _activeCall.value = call.copy(isSpeakerOn = isSpeaker)
    }

    // In-Call Screen Sharing Toggle
    fun toggleInCallScreenShare(activity: Activity) {
        val currentCall = _activeCall.value ?: return
        if (currentCall.isScreenSharing) {
            repository.screenShareManager.stopScreenShare()
            _activeCall.value = currentCall.copy(isScreenSharing = false)
        } else {
            repository.screenShareManager.startActiveWindowCapture(activity)
            _activeCall.value = currentCall.copy(isScreenSharing = true)
        }
    }

    fun toggleScreenSharePause() {
        repository.screenShareManager.togglePause()
    }

    // Real-Time Call Voice DSP Modulation
    fun setCallVoiceEffect(effect: CallVoiceEffect) {
        repository.callEngine.setVoiceEffect(effect)
        val currentCall = _activeCall.value ?: return
        _activeCall.value = currentCall.copy(activeVoiceEffect = effect)
    }

    // In-Call Soundboard Reaction
    fun playCallSoundboardEffect(sound: CallSoundboardEffect) {
        soundManager.playCallSoundboardEffect(sound)
        val currentCall = _activeCall.value ?: return
        _activeCall.value = currentCall.copy(lastEffectPlayed = sound)
    }

    fun playCallAudioEmoji(sound: PixelAudioEmoji) = playCallSoundboardEffect(sound)

    fun toggleEffect(effect: MessageEffect) {
        val current = _selectedEffects.value
        _selectedEffects.value = if (current.contains(effect)) current - effect else current + effect
    }

    fun selectAllEffects() {
        _selectedEffects.value = setOf(
            MessageEffect.SHAKE,
            MessageEffect.BASS,
            MessageEffect.TTS,
            MessageEffect.FIRE,
            MessageEffect.FREEZE,
            MessageEffect.LOUD,
            MessageEffect.SPARK,
            MessageEffect.POOP
        )
    }

    fun clearEffects() {
        _selectedEffects.value = emptySet()
    }

    fun setSpamMultiplier(multiplier: Int) {
        _spamMultiplier.value = multiplier
    }

    fun sendMessage(
        text: String,
        photoUri: String? = null,
        fileName: String? = null,
        fileSize: String? = null,
        fileUri: String? = null
    ) {
        if (text.isBlank() && photoUri == null && fileName == null) return
        val currentChan = _activeChannel.value
        val armed = _selectedEffects.value
        val mult = _spamMultiplier.value

        val msg = repository.sendMessage(
            channelId = currentChan.id,
            text = text.trim(),
            effects = armed,
            photoUri = photoUri,
            fileName = fileName,
            fileSize = fileSize,
            fileUri = fileUri,
            spamMultiplier = mult
        )

        // Play feedback sounds & TTS if armed
        soundManager.playMessageEffects(armed, msg.text)

        // Check for custom user-created bot triggers
        checkAndTriggerBots(currentChan, text.trim())
    }

    fun uploadAndSendCloudAttachment(
        context: Context,
        uri: Uri,
        fileName: String,
        isPhoto: Boolean,
        caption: String = ""
    ) {
        viewModelScope.launch {
            _isUploadingCloud.value = true
            _uploadProgressNotice.value = "☁️ Uploading $fileName to cloud storage..."

            val result = repository.uploadToCloud(context, uri, fileName)
            _isUploadingCloud.value = false
            _uploadProgressNotice.value = null

            if (result.success && result.directDownloadUrl != null) {
                _bannerNotice.value = "☁️ $fileName successfully stored in cloud!"
                sendMessage(
                    text = caption,
                    photoUri = if (isPhoto) result.directDownloadUrl else null,
                    fileName = fileName,
                    fileSize = result.fileSizeFormatted,
                    fileUri = result.directDownloadUrl
                )
            } else {
                _bannerNotice.value = "Cloud upload failed: ${result.errorMessage ?: "Unknown error"}"
            }
        }
    }

    private fun checkAndTriggerBots(currentChan: Channel, userText: String) {
        val allBots = userBots.value
        if (allBots.isEmpty()) return

        // 1. If currently in a custom bot's direct chat
        if (currentChan.isBotChat && currentChan.botId != null) {
            val bot = allBots.find { it.id == currentChan.botId }
            if (bot != null) {
                queryBot(currentChan, bot, userText)
                return
            }
        }

        // 2. If user types custom bot's trigger prefix
        for (bot in allBots) {
            val prefix = bot.triggerPrefix.lowercase().trim()
            if (prefix.isNotBlank() && userText.lowercase().startsWith(prefix)) {
                val promptText = userText.substring(prefix.length).trim()
                queryBot(currentChan, bot, promptText.ifBlank { "Hello" })
                return
            }
        }
    }

    private fun queryBot(currentChan: Channel, bot: UserBot, query: String) {
        viewModelScope.launch {
            _botTypingNotice.value = "${bot.name} is typing..."

            val response = try {
                repository.aiService.getBotResponse(
                    botName = bot.name,
                    botPrompt = bot.rolePrompt,
                    userPrompt = query
                )
            } catch (e: Exception) {
                "Hello from ${bot.name}!"
            }

            delay(400)
            _botTypingNotice.value = null

            val botMsg = repository.insertBotReply(
                channelId = currentChan.id,
                botId = bot.id,
                botName = bot.name,
                botAvatar = bot.avatarEmoji,
                text = response,
                effects = bot.defaultEffects
            )

            soundManager.playMessageEffects(botMsg.effects, botMsg.text)
        }
    }

    fun addReaction(messageId: String, emoji: String) {
        repository.addReaction(messageId, emoji)
    }

    fun triggerMessageEffects(message: ChatMessage) {
        soundManager.playMessageEffects(message.effects, message.text)
    }

    fun dismissBanner() {
        _bannerNotice.value = null
    }

    fun connectFromQrPayload(payload: String) {
        try {
            val uri = android.net.Uri.parse(payload)
            if (uri.scheme == "mesgaging" && uri.host == "user") {
                val id = uri.getQueryParameter("id") ?: UUID.randomUUID().toString()
                val name = uri.getQueryParameter("name") ?: "Contact"
                val disc = uri.getQueryParameter("disc") ?: "#0000"
                val emoji = uri.getQueryParameter("emoji") ?: "💬"
                val color = uri.getQueryParameter("color")?.toLongOrNull() ?: 0xFF5865F2

                val newContact = repository.addContactFromQr(id, name, disc, emoji, color)
                _bannerNotice.value = "Connected to ${newContact.username} via QR!"
                _activeChannel.value = Channel(
                    id = "dm_${newContact.id}",
                    name = newContact.username,
                    topic = "Direct messaging with ${newContact.username}",
                    isDm = true,
                    recipientContactId = newContact.id
                )
                _currentNavTab.value = NavTab.MESSAGES
            }
        } catch (_: Exception) {
            _bannerNotice.value = "Invalid QR code format"
        }
    }

    fun openScanner() { _showScannerDialog.value = true }
    fun closeScanner() { _showScannerDialog.value = false }

    fun openMyQr() { _showMyQrDialog.value = true }
    fun closeMyQr() { _showMyQrDialog.value = false }

    fun openVault() { _showVaultSheet.value = true }
    fun closeVault() { _showVaultSheet.value = false }

    fun openEmojiPicker() { _showEmojiPicker.value = true }
    fun closeEmojiPicker() { _showEmojiPicker.value = false }
    fun toggleEmojiPicker() { _showEmojiPicker.value = !_showEmojiPicker.value }

    fun triggerEmojiBurst(emoji: String) {
        val particles = List(16) { i ->
            val angle = (i * (360.0 / 16.0)) * (Math.PI / 180.0)
            EmojiBurstParticle(
                id = System.nanoTime() + i,
                emoji = emoji,
                startXRatio = 0.5f,
                startYRatio = 0.85f,
                targetXRatio = (0.5f + Math.cos(angle) * 0.4f).toFloat().coerceIn(0.05f, 0.95f),
                targetYRatio = (0.85f + Math.sin(angle) * 0.35f - 0.2f).toFloat().coerceIn(0.05f, 0.95f),
                scale = 1.0f,
                rotation = (i * 22.5f)
            )
        }
        _activeParticles.value = particles
        viewModelScope.launch {
            delay(1200)
            _activeParticles.value = emptyList()
        }
    }

    fun saveMessageToVault(message: ChatMessage) {
        repository.saveSnippet(
            title = "Saved from ${message.senderName}",
            content = message.text,
            category = "Saved Texts",
            photoUri = message.photoUri,
            effects = message.effects
        )
        _bannerNotice.value = "Saved message to vault!"
    }

    fun deleteSavedSnippet(id: String) {
        repository.deleteSnippet(id)
        _bannerNotice.value = "Snippet removed from vault"
    }

    // Privacy & Ephemeral Message Actions
    fun togglePrivacyShield() {
        val newState = !_isPrivacyShieldActive.value
        _isPrivacyShieldActive.value = newState
        _bannerNotice.value = if (newState) "Privacy Shield ACTIVE: Messages blurred for privacy" else "Privacy Shield OFF"
    }

    fun openPrivacySecurityDialog() { _showPrivacySecurityDialog.value = true }
    fun closePrivacySecurityDialog() { _showPrivacySecurityDialog.value = false }

    fun setDisappearingTimer(minutes: Int) {
        _disappearingTimerMinutes.value = minutes
        _bannerNotice.value = when (minutes) {
            0 -> "Disappearing messages: OFF"
            1 -> "Self-destruct timer: 10 SECONDS"
            60 -> "Disappearing timer: 1 HOUR"
            else -> "Disappearing timer: 24 HOURS"
        }
    }

    fun purgeCurrentChannelMessages() {
        val chanId = _activeChannel.value.id
        repository.purgeChannelMessages(chanId)
        _bannerNotice.value = "Conversation history purged permanently"
    }
}
