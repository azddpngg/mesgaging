package com.example

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class MesgagingFirestoreRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun testAuthenticatedUser_canWriteAndReadMessage() = runBlocking {
    val uid = signInTestUser("alice@test.com")
    val msgId = "msg_${UUID.randomUUID()}"
    val msgData = hashMapOf(
      "id" to msgId,
      "channelId" to "general",
      "senderId" to uid,
      "senderName" to "Alice",
      "senderAvatar" to "🦅",
      "senderColor" to 0xFF2B2D32,
      "text" to "Hello from test!",
      "timestamp" to System.currentTimeMillis(),
      "effects" to listOf("fire"),
      "reactions" to emptyMap<String, Int>(),
      "spamMultiplier" to 1,
      "isSaved" to false
    )

    firestore.collection("channels")
      .document("general")
      .collection("messages")
      .document(msgId)
      .set(msgData)
      .await()

    val snapshot = firestore.collection("channels")
      .document("general")
      .collection("messages")
      .document(msgId)
      .get()
      .await()

    assertTrue(snapshot.exists())
    assertEquals("Hello from test!", snapshot.getString("text"))
  }

  @Test
  fun testUnauthenticatedUser_cannotReadMessages() = runBlocking {
    auth.signOut()
    try {
      firestore.collection("channels")
        .document("general")
        .collection("messages")
        .get()
        .await()
      fail("Expected PERMISSION_DENIED for unauthenticated read")
    } catch (e: Exception) {
      assertTrue(e is FirebaseFirestoreException)
    }
  }

  @Test
  fun testAuthenticatedUser_cannotImpersonateAnotherSender() = runBlocking {
    signInTestUser("alice@test.com")
    val spoofMsgId = "msg_spoof_${UUID.randomUUID()}"
    val spoofData = hashMapOf(
      "id" to spoofMsgId,
      "channelId" to "general",
      "senderId" to "different_user_id", // Mismatched!
      "senderName" to "Spoofer",
      "senderAvatar" to "🦊",
      "senderColor" to 0xFF2B2D32,
      "text" to "Impersonating!",
      "timestamp" to System.currentTimeMillis(),
      "effects" to emptyList<String>(),
      "reactions" to emptyMap<String, Int>(),
      "spamMultiplier" to 1,
      "isSaved" to false
    )

    try {
      firestore.collection("channels")
        .document("general")
        .collection("messages")
        .document(spoofMsgId)
        .set(spoofData)
        .await()
      fail("Expected PERMISSION_DENIED when senderId does not match auth.uid")
    } catch (e: Exception) {
      assertTrue(e is FirebaseFirestoreException)
    }
  }

  @Test
  fun testMultiUserSync_BobCanReadAliceMessage() = runBlocking {
    val aliceUid = signInTestUser("alice_sync@test.com")
    val msgId = "msg_alice_${UUID.randomUUID()}"
    val msgData = hashMapOf(
      "id" to msgId,
      "channelId" to "general",
      "senderId" to aliceUid,
      "senderName" to "Alice",
      "senderAvatar" to "🦅",
      "senderColor" to 0xFF2B2D32,
      "text" to "Multiplayer message!",
      "timestamp" to System.currentTimeMillis(),
      "effects" to emptyList<String>(),
      "reactions" to emptyMap<String, Int>(),
      "spamMultiplier" to 1,
      "isSaved" to false
    )

    firestore.collection("channels")
      .document("general")
      .collection("messages")
      .document(msgId)
      .set(msgData)
      .await()

    // Sign in as Bob
    signInTestUser("bob_sync@test.com")

    val bobSnapshot = firestore.collection("channels")
      .document("general")
      .collection("messages")
      .document(msgId)
      .get()
      .await()

    assertTrue(bobSnapshot.exists())
    assertEquals("Multiplayer message!", bobSnapshot.getString("text"))
  }
}
