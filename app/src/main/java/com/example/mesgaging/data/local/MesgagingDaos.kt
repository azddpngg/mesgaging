package com.example.mesgaging.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<MessageEntity>)

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): MessageEntity?

    @Query("UPDATE messages SET reactionsJson = :reactionsJson WHERE id = :id")
    suspend fun updateReactions(id: String, reactionsJson: String)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessage(id: String)

    @Query("DELETE FROM messages WHERE id LIKE 'msg_alex_%' OR id LIKE 'cortex%' OR id = 'cortex_hello' OR senderId = 'alex_rivers'")
    suspend fun deleteFakeMessages()

    @Query("DELETE FROM messages WHERE channelId = :channelId")
    suspend fun deleteMessagesForChannel(channelId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY connectedAt DESC")
    fun getAllContactsFlow(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts ORDER BY connectedAt DESC")
    suspend fun getAllContacts(): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Query("SELECT * FROM contacts WHERE id = :id LIMIT 1")
    suspend fun getContactById(id: String): ContactEntity?

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    @Query("DELETE FROM contacts WHERE id IN ('alex_rivers', 'maya_lin', 'sam_chen', 'jordan_reed')")
    suspend fun deleteFakeContacts()

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()
}

@Dao
interface UserBotDao {
    @Query("SELECT * FROM user_bots ORDER BY createdAt ASC")
    fun getAllBotsFlow(): Flow<List<UserBotEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBot(bot: UserBotEntity)

    @Query("DELETE FROM user_bots WHERE id = :id")
    suspend fun deleteBot(id: String)
}

@Dao
interface SavedSnippetDao {
    @Query("SELECT * FROM saved_snippets ORDER BY createdAt DESC")
    fun getAllSnippetsFlow(): Flow<List<SavedSnippetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSnippet(snippet: SavedSnippetEntity)

    @Query("DELETE FROM saved_snippets WHERE id = :id")
    suspend fun deleteSnippet(id: String)
}

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profile LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile LIMIT 1")
    suspend fun getUserProfile(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)
}
