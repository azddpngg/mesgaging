package com.example.mesgaging.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
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
    val timestamp: Long,
    val effectsCsv: String = "", // Comma-separated MessageEffect IDs
    val reactionsJson: String = "", // JSON map of emoji to count
    val spamMultiplier: Int = 1,
    val isSaved: Boolean = false
)

@Entity(tableName = "contacts")
data class ContactEntity(
    @PrimaryKey val id: String,
    val username: String,
    val discriminator: String,
    val avatarEmoji: String,
    val avatarColor: Long,
    val statusLabel: String,
    val customStatus: String,
    val qrPayload: String,
    val isBot: Boolean = false,
    val botId: String? = null,
    val connectedAt: Long
)

@Entity(tableName = "user_bots")
data class UserBotEntity(
    @PrimaryKey val id: String,
    val name: String,
    val avatarEmoji: String,
    val rolePrompt: String,
    val triggerPrefix: String,
    val defaultEffectsCsv: String = "",
    val createdAt: Long
)

@Entity(tableName = "saved_snippets")
data class SavedSnippetEntity(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val category: String,
    val photoUri: String? = null,
    val effectsCsv: String = "",
    val createdAt: Long
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: String = "user_me",
    val username: String,
    val discriminator: String,
    val avatarEmoji: String,
    val avatarColor: Long,
    val customStatus: String,
    val qrPayload: String
)
