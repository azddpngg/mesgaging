package com.example.mesgaging.model

enum class MessageEffect(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    SHAKE("shake", "Shake", "💥", "Screen & text rattle"),
    BASS("bass", "Bass", "🔊", "Low-end haptic thud"),
    FIRE("fire", "Fire", "🔥", "Warm flame border"),
    TTS("tts", "TTS", "🗣️", "Voice read-aloud"),
    FREEZE("freeze", "Ice", "🧊", "Sub-zero frosted glass"),
    LOUD("loud", "Loud", "📢", "Heavy bold shout"),
    SPARK("spark", "Zap", "⚡", "Electric vibration spark"),
    POOP("poop", "Splat", "💩", "Funny splat sound & wobble"),

    // Compatibility aliases for previous presets
    EXPLOSION("explosion", "Shake", "💥", "Screen & text rattle"),
    INFERNO("inferno", "Fire", "🔥", "Warm flame border"),
    EARTHQUAKE("earthquake", "Shake", "💥", "Screen & text rattle"),
    GLITCH("glitch", "Zap", "⚡", "Electric vibration spark"),
    BASS_BOOST("bass_boost", "Bass", "🔊", "Low-end haptic thud"),
    MEGA_CHONK("mega_chonk", "Loud", "📢", "Heavy bold shout"),
    FROST("frost", "Ice", "🧊", "Sub-zero frosted glass"),
    RED_ALERT("red_alert", "Loud", "📢", "Heavy bold shout"),
    NUKE("nuke", "Shake", "💥", "Screen & text rattle"),
    SLAM("slam", "Shake", "💥", "Screen & text rattle"),
    EAR_RAPE("ear_rape", "Bass", "🔊", "Low-end haptic thud"),
    WOBBLE("wobble", "Shake", "💥", "Screen & text rattle"),
    SPIN("spin", "Shake", "💥", "Screen & text rattle"),
    GHOST("ghost", "Ice", "🧊", "Sub-zero frosted glass"),
    WARP("warp", "Shake", "💥", "Screen & text rattle"),
    BLOOD_MOON("blood_moon", "Fire", "🔥", "Warm flame border"),
    DISCO("disco", "Zap", "⚡", "Electric vibration spark"),
    INVERT("invert", "Loud", "📢", "Heavy bold shout")
}

// Real-Time In-Call Voice DSP Modulator Effects
enum class CallVoiceEffect(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    NORMAL("normal", "Natural", "🎙️", "Clean studio pass-through"),
    ROBOT("robot", "Robot", "🤖", "Metallic vocoder modulation"),
    RADIO("radio", "Radio", "📻", "Walkie-talkie bandpass grit"),
    DEEP("deep", "Deep", "🦍", "Bass pitch drop & resonance"),
    HELIUM("helium", "Helium", "🐿️", "High formant chipmunk shift"),
    ECHO("echo", "Echo", "🌌", "Cosmic reverb delay line")
}

// In-Call Reaction Soundboard Effects
enum class CallSoundboardEffect(
    val emoji: String,
    val label: String,
    val description: String
) {
    APPLAUSE("👏", "Applause", "Crowd cheers & clapping"),
    PARTY("🎉", "Air Horn", "Air horn fanfare blast"),
    LAUGH("😂", "Laugh", "Bouncing crowd chuckle"),
    SAD("🎺", "Sad Trombone", "Wah-wah fail sound"),
    DRUM("🥁", "Rimshot", "Ba-dum-tss punchline"),
    POOP("💨", "Whoopee", "Funny whoopee cushion")
}

typealias PixelAudioEmoji = CallSoundboardEffect

enum class CallState {
    RINGING,
    CONNECTED,
    ENDED
}

data class ActiveCall(
    val id: String,
    val participantName: String,
    val participantEmoji: String,
    val isBot: Boolean,
    val botId: String? = null,
    val channelId: String,
    val startTime: Long = System.currentTimeMillis(),
    val isMuted: Boolean = false,
    val isSpeakerOn: Boolean = true,
    val state: CallState = CallState.RINGING,
    val activeVoiceEffect: CallVoiceEffect = CallVoiceEffect.NORMAL,
    val lastEffectPlayed: CallSoundboardEffect? = null,
    val isScreenSharing: Boolean = false,
    val subtitleText: String? = null
)

enum class UserStatus(val label: String, val colorHex: Long) {
    ONLINE("Online", 0xFF3BA55D),
    IDLE("Idle", 0xFFFAA81A),
    DND("Do Not Disturb", 0xFFED4245),
    OFFLINE("Offline", 0xFF747F8D)
}

data class UserContact(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatarEmoji: String,
    val avatarColor: Long,
    val status: UserStatus,
    val customStatus: String,
    val qrPayload: String,
    val isBot: Boolean = false,
    val botId: String? = null,
    val connectedAt: Long = System.currentTimeMillis()
)

data class UserBot(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val rolePrompt: String,
    val triggerPrefix: String = "!",
    val defaultEffects: Set<MessageEffect> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
)

data class Channel(
    val id: String,
    val name: String,
    val topic: String,
    val isDm: Boolean = true,
    val recipientContactId: String? = null,
    val isBotChat: Boolean = false,
    val botId: String? = null,
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val channelId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String,
    val senderColor: Long,
    val text: String,
    val photoUri: String? = null,
    val fileName: String? = null,
    val fileSize: String? = null,
    val fileUri: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val effects: Set<MessageEffect> = emptySet(),
    val reactions: Map<String, Int> = emptyMap(),
    val spamMultiplier: Int = 1,
    val isSaved: Boolean = false
)

data class SavedSnippet(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val photoUri: String? = null,
    val effects: Set<MessageEffect> = emptySet(),
    val createdAt: Long = System.currentTimeMillis()
)

data class EmojiBurstParticle(
    val id: Long,
    val emoji: String,
    val startXRatio: Float,
    val startYRatio: Float,
    val targetXRatio: Float,
    val targetYRatio: Float,
    val scale: Float,
    val rotation: Float
)

// App Navigation Tabs
enum class NavTab(val title: String) {
    MESSAGES("Chats"),
    FRIENDS("Friends")
}

// Friends Filter Tabs
enum class FriendFilter(val title: String) {
    ONLINE("Online"),
    ALL("All"),
    PENDING("Pending"),
    ADD_FRIEND("+ Add Friend")
}

data class FriendRequest(
    val id: String,
    val username: String,
    val discriminator: String,
    val avatarEmoji: String,
    val avatarColor: Long,
    val timestamp: Long = System.currentTimeMillis()
)

// Screen Share Models
data class ScreenShareSession(
    val id: String,
    val targetUserId: String,
    val targetUserName: String,
    val targetUserEmoji: String,
    val resolution: String = "1080p 60FPS",
    val isAudioSharing: Boolean = true,
    val isPaused: Boolean = false,
    val isPrivacyShieldActive: Boolean = false,
    val startedAt: Long = System.currentTimeMillis()
)
