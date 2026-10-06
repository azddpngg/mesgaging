package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.mesgaging.ai.GeminiAiService
import com.example.mesgaging.data.MesgagingRepository
import com.example.mesgaging.data.local.MesgagingDatabase
import com.example.mesgaging.model.ActiveCall
import com.example.mesgaging.model.CallState
import com.example.mesgaging.model.MessageEffect
import com.example.mesgaging.model.PixelAudioEmoji
import com.example.mesgaging.qr.QrCodeGenerator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read app name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("mesgaging", appName)
    }

    @Test
    fun `qr payload encoding and decoding`() {
        val payload = QrCodeGenerator.buildPayload(
            id = "test_user_42",
            username = "phantom",
            discriminator = "#1337",
            emoji = "👾",
            colorHex = 0xFF2B2D32
        )

        val parsed = QrCodeGenerator.parsePayload(payload)
        assertNotNull(parsed)
        assertEquals("test_user_42", parsed?.id)
        assertEquals("phantom", parsed?.username)
        assertEquals("#1337", parsed?.discriminator)
        assertEquals("👾", parsed?.avatarEmoji)
    }

    @Test
    fun `qr matrix generator produces valid matrix dimensions`() {
        val matrix = QrCodeGenerator.generateMatrix("mesgaging://connect?id=123", 25)
        assertEquals(25, matrix.size)
        assertEquals(25, matrix[0].size)
        assertTrue(matrix[0][0])
    }

    @Test
    fun `room database initializes cleanly and has DAOs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = MesgagingDatabase.getInstance(context)
        assertNotNull(db.messageDao())
        assertNotNull(db.contactDao())
        assertNotNull(db.userBotDao())
        assertNotNull(db.savedSnippetDao())
        assertNotNull(db.userProfileDao())
    }

    @Test
    fun `cortex answers math queries accurately`() = runBlocking {
        val aiService = GeminiAiService()
        val response = aiService.getBotResponse("Cortex", "", "what is 2 + 2")
        assertTrue(response.contains("4"))

        val multResponse = aiService.getBotResponse("Cortex", "", "what is 15 * 4")
        assertTrue(multResponse.contains("60"))
    }

    @Test
    fun `repository initializes with seeded friends`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MesgagingRepository(context)
        assertNotNull(repo.contacts)
    }

    @Test
    fun `repository allows profile updates`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = MesgagingRepository(context)
        assertEquals("You", repo.currentUser.value.username)

        repo.updateProfile("ShadowWalker", "⚡")
        assertEquals("ShadowWalker", repo.currentUser.value.username)
        assertEquals("⚡", repo.currentUser.value.avatarEmoji)
    }

    @Test
    fun `pixel call audio emoji definitions`() {
        assertEquals("💩", PixelAudioEmoji.POOP.emoji)
        assertEquals("👏", PixelAudioEmoji.APPLAUSE.emoji)
        assertEquals("😂", PixelAudioEmoji.LAUGH.emoji)
        assertEquals("😢", PixelAudioEmoji.SAD.emoji)
        assertEquals("🎉", PixelAudioEmoji.PARTY.emoji)
        assertEquals("🥁", PixelAudioEmoji.DRUM.emoji)
    }

    @Test
    fun `active call model supports call states and pixel audio emoji`() {
        val call = ActiveCall(
            id = "call_123",
            participantName = "Cortex",
            participantEmoji = "🧠",
            isBot = true,
            botId = "cortex",
            channelId = "chat_cortex",
            state = CallState.CONNECTED,
            lastEffectPlayed = PixelAudioEmoji.POOP,
            subtitleText = "You played 💩 Poop"
        )

        assertEquals("Cortex", call.participantName)
        assertEquals(CallState.CONNECTED, call.state)
        assertEquals(PixelAudioEmoji.POOP, call.lastEffectPlayed)
        assertTrue(call.isBot)
    }

    @Test
    fun `human message effects include shake bass fire and poop`() {
        val effects = setOf(
            MessageEffect.SHAKE,
            MessageEffect.BASS,
            MessageEffect.FIRE,
            MessageEffect.TTS,
            MessageEffect.FREEZE,
            MessageEffect.LOUD,
            MessageEffect.SPARK,
            MessageEffect.POOP
        )

        assertTrue(effects.contains(MessageEffect.POOP))
        assertTrue(effects.contains(MessageEffect.BASS))
        assertTrue(effects.contains(MessageEffect.FIRE))
        assertTrue(effects.contains(MessageEffect.SHAKE))
    }
}
