const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read messages", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("channels").doc("general").collection("messages").get());
});

test("Authenticated user: can post a valid message to channel", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const validMsg = {
    id: "msg_1",
    channelId: "general",
    senderId: ALICE_UID,
    senderName: "Alice",
    senderAvatar: "🦅",
    senderColor: 0xFF2B2D32,
    text: "Hello world!",
    timestamp: Date.now(),
    effects: ["fire"],
    reactions: {},
    spamMultiplier: 1,
    isSaved: false
  };
  await assertSucceeds(
    aliceDb.collection("channels").doc("general").collection("messages").doc("msg_1").set(validMsg)
  );
});

test("Authenticated user: cannot impersonate another sender", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const spoofMsg = {
    id: "msg_spoof",
    channelId: "general",
    senderId: BOB_UID, // Spoofing Bob!
    senderName: "Bob",
    senderAvatar: "🐺",
    senderColor: 0xFF2B2D32,
    text: "I am Bob!",
    timestamp: Date.now(),
    effects: [],
    reactions: {},
    spamMultiplier: 1,
    isSaved: false
  };
  await assertFails(
    aliceDb.collection("channels").doc("general").collection("messages").doc("msg_spoof").set(spoofMsg)
  );
});

test("Authenticated user: Bob can read Alice's message in public channel", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("channels").doc("general").collection("messages").doc("msg_alice").set({
      id: "msg_alice",
      channelId: "general",
      senderId: ALICE_UID,
      senderName: "Alice",
      senderAvatar: "🦅",
      senderColor: 0xFF2B2D32,
      text: "Broadcast for everyone!",
      timestamp: Date.now(),
      effects: [],
      reactions: {},
      spamMultiplier: 1,
      isSaved: false
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertSucceeds(
    bobDb.collection("channels").doc("general").collection("messages").doc("msg_alice").get()
  );
});
