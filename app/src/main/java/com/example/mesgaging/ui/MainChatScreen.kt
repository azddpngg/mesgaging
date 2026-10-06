package com.example.mesgaging.ui

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.ScreenShare
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import com.example.mesgaging.ui.components.PrivacySecurityDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.mesgaging.model.NavTab
import com.example.mesgaging.ui.components.AnimatedEmojiPicker
import com.example.mesgaging.ui.components.AnimatedMessageCard
import com.example.mesgaging.ui.components.CreateBotDialog
import com.example.mesgaging.ui.components.DiscordSidebar
import com.example.mesgaging.ui.components.EffectsArmoryBar
import com.example.mesgaging.ui.components.EmojiBurstLayer
import com.example.mesgaging.ui.components.FriendsScreen
import com.example.mesgaging.ui.components.MyQrPassDialog
import com.example.mesgaging.ui.components.ProfileNameDialog
import com.example.mesgaging.ui.components.QrScannerDialog
import com.example.mesgaging.ui.components.ScreenShareOverlay
import com.example.mesgaging.ui.components.VoiceCallOverlay
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceChat
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceInput
import com.example.ui.theme.SurfaceNav
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun MainChatScreen(
    viewModel: MesgagingViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val currentNavTab by viewModel.currentNavTab.collectAsStateWithLifecycle()
    val friendFilter by viewModel.friendFilter.collectAsStateWithLifecycle()
    val friendRequests by viewModel.friendRequests.collectAsStateWithLifecycle()

    val activeChannel by viewModel.activeChannel.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val userBots by viewModel.userBots.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val selectedEffects by viewModel.selectedEffects.collectAsStateWithLifecycle()
    val spamMultiplier by viewModel.spamMultiplier.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()

    val showScannerDialog by viewModel.showScannerDialog.collectAsStateWithLifecycle()
    val showMyQrDialog by viewModel.showMyQrDialog.collectAsStateWithLifecycle()
    val showNameDialog by viewModel.showNameDialog.collectAsStateWithLifecycle()
    val showCreateBotDialog by viewModel.showCreateBotDialog.collectAsStateWithLifecycle()
    val showEmojiPicker by viewModel.showEmojiPicker.collectAsStateWithLifecycle()
    val botTypingNotice by viewModel.botTypingNotice.collectAsStateWithLifecycle()
    val activeCall by viewModel.activeCall.collectAsStateWithLifecycle()
    val micAmplitude by viewModel.micAmplitude.collectAsStateWithLifecycle()
    val liveScreenBitmap by viewModel.liveScreenFrame.collectAsStateWithLifecycle()
    val isScreenSharePaused by viewModel.isScreenSharePaused.collectAsStateWithLifecycle()
    val screenShareResolution by viewModel.screenShareResolution.collectAsStateWithLifecycle()
    val isUploadingCloud by viewModel.isUploadingCloud.collectAsStateWithLifecycle()
    val uploadProgressNotice by viewModel.uploadProgressNotice.collectAsStateWithLifecycle()
    val bannerNotice by viewModel.bannerNotice.collectAsStateWithLifecycle()
    val activeParticles by viewModel.activeParticles.collectAsStateWithLifecycle()
    val isPrivacyShieldActive by viewModel.isPrivacyShieldActive.collectAsStateWithLifecycle()
    val showPrivacySecurityDialog by viewModel.showPrivacySecurityDialog.collectAsStateWithLifecycle()
    val disappearingMinutes by viewModel.disappearingTimerMinutes.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var inputText by remember { mutableStateOf("") }
    var isArmoryExpanded by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    // Attached Media Staging
    var attachedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var attachedFileName by remember { mutableStateOf<String?>(null) }
    var attachedFileSize by remember { mutableStateOf<String?>(null) }
    var attachedFileUri by remember { mutableStateOf<Uri?>(null) }

    // Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedPhotoUri = uri
        }
    }

    // Document/File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            attachedFileUri = uri
            val displayName = uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf"
            attachedFileName = displayName
            attachedFileSize = "Attachment"
        }
    }

    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    BackHandler(enabled = activeCall != null || currentNavTab == NavTab.FRIENDS || drawerState.isOpen || showEmojiPicker) {
        if (activeCall != null) {
            viewModel.endVoiceCall()
        } else if (currentNavTab == NavTab.FRIENDS) {
            viewModel.switchNavTab(NavTab.MESSAGES)
        } else if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (showEmojiPicker) {
            viewModel.closeEmojiPicker()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = SurfaceNav,
                modifier = Modifier.width(280.dp)
            ) {
                DiscordSidebar(
                    contacts = contacts,
                    userBots = userBots,
                    activeChannelId = activeChannel.id,
                    currentNavTab = currentNavTab,
                    currentUser = currentUser,
                    onSelectChannel = { channel ->
                        viewModel.selectChannel(channel)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenFriends = {
                        viewModel.switchNavTab(NavTab.FRIENDS)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onOpenMyQr = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openMyQr()
                    },
                    onOpenScanner = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openScanner()
                    },
                    onOpenCreateBot = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openCreateBotDialog()
                    },
                    onOpenNameDialog = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openNameDialog()
                    }
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(SurfaceChat)
                .statusBarsPadding(),
            containerColor = SurfaceChat,
            topBar = {
                Surface(
                    color = SurfaceNav,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(width = 0.5.dp, color = DividerColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("drawer_menu_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = TextPrimary
                            )
                        }

                        if (currentNavTab == NavTab.FRIENDS) {
                            // Header for Friends Tab
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { coroutineScope.launch { drawerState.open() } }
                            ) {
                                Icon(Icons.Default.People, contentDescription = null, tint = TextPrimary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Friends Hub",
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Quick Switch back to Messages
                            IconButton(
                                onClick = { viewModel.switchNavTab(NavTab.MESSAGES) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                    contentDescription = "Chats",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        } else {
                            // Header for Active Chat
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { coroutineScope.launch { drawerState.open() } }
                            ) {
                                Text(
                                    text = if (activeChannel.isBotChat) "🤖" else "💬",
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = activeChannel.name,
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = botTypingNotice ?: activeChannel.topic,
                                        color = if (botTypingNotice != null) Color.White else TextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // E2EE Privacy Security Badge
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0C1A12),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF22C55E).copy(alpha = 0.5f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.openPrivacySecurityDialog() }
                                        .testTag("privacy_e2ee_badge")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Encrypted",
                                            tint = Color(0xFF22C55E),
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (disappearingMinutes > 0) "E2EE • ${if (disappearingMinutes == 1) "10s" else "${disappearingMinutes}m"}" else "E2EE",
                                            color = Color(0xFF22C55E),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                // Incognito Privacy Shield Quick Toggle
                                IconButton(
                                    onClick = { viewModel.togglePrivacyShield() },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("toggle_privacy_shield_button")
                                ) {
                                    Icon(
                                        imageVector = if (isPrivacyShieldActive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Privacy Shield",
                                        tint = if (isPrivacyShieldActive) Color(0xFF22C55E) else TextSecondary,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            IconButton(
                                onClick = {
                                    viewModel.startVoiceCall(
                                        targetName = activeChannel.name,
                                        targetId = activeChannel.recipientContactId ?: ""
                                    )
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("start_voice_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Voice Call",
                                    tint = com.example.ui.theme.AccentSuccess,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(2.dp))

                            // Switch to Friends Tab Button
                            IconButton(
                                onClick = { viewModel.switchNavTab(NavTab.FRIENDS) },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.People,
                                    contentDescription = "Friends",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                        Spacer(modifier = Modifier.width(2.dp))

                        // Single Display Name Button
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SurfaceHover)
                                .clickable { viewModel.openNameDialog() }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = currentUser.avatarEmoji, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = currentUser.username,
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Name",
                                    tint = TextMuted,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // QR Scanner Button
                        IconButton(
                            onClick = { viewModel.openScanner() },
                            modifier = Modifier.size(32.dp).testTag("qr_scan_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Scan QR",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // My QR Pass Button
                        IconButton(
                            onClick = { viewModel.openMyQr() },
                            modifier = Modifier.size(32.dp).testTag("my_qr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "My QR Pass",
                                tint = TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .imePadding()
            ) {
                if (currentNavTab == NavTab.FRIENDS) {
                    // Friends Tab UI
                    FriendsScreen(
                        contacts = contacts,
                        friendRequests = friendRequests,
                        currentUser = currentUser,
                        activeFilter = friendFilter,
                        onFilterChange = { viewModel.setFriendFilter(it) },
                        onOpenChat = { friend -> viewModel.openDirectMessageWithFriend(friend) },
                        onStartCall = { friend ->
                            viewModel.startVoiceCall(friend.username, friend.avatarEmoji, friend.id)
                        },
                        onOpenScanner = { viewModel.openScanner() },
                        onAddFriendByTag = { tag -> viewModel.addFriendByTag(tag) },
                        onAcceptRequest = { id -> viewModel.acceptFriendRequest(id) },
                        onRejectRequest = { id -> viewModel.rejectFriendRequest(id) },
                        onRemoveFriend = { id -> viewModel.removeFriend(id) }
                    )
                } else {
                    // Messages Chat UI
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Optional Notice Banner
                        if (bannerNotice != null) {
                            Surface(
                                color = SurfaceHover,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = bannerNotice!!,
                                        color = TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.dismissBanner() },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Dismiss",
                                            tint = TextMuted,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Message Stream
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Direct conversation with ${activeChannel.name}",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            if (messages.isEmpty() && botTypingNotice == null) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 48.dp, bottom = 24.dp, start = 24.dp, end = 24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = SurfaceNav,
                                            modifier = Modifier.size(54.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Filled.Chat,
                                                    contentDescription = null,
                                                    tint = TextMuted,
                                                    modifier = Modifier.size(26.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No messages yet",
                                            color = TextPrimary,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Send a message to start chatting with ${activeChannel.name}",
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }

                            items(messages, key = { it.id }) { message ->
                                AnimatedMessageCard(
                                    message = message,
                                    onTriggerEffects = { viewModel.triggerMessageEffects(it) },
                                    onReactionTap = { msgId, emoji -> viewModel.addReaction(msgId, emoji) },
                                    onSpamCopy = {
                                        viewModel.setSpamMultiplier(3)
                                        viewModel.sendMessage(it.text)
                                    }
                                )
                            }

                            if (botTypingNotice != null) {
                                item {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(14.dp),
                                            color = Color.White,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = botTypingNotice!!,
                                            color = TextMuted,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Staged Attachment Preview Bar
                        if (attachedPhotoUri != null || attachedFileName != null) {
                            Surface(
                                color = SurfaceCard,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (attachedPhotoUri != null) {
                                            Box(
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black)
                                            ) {
                                                AsyncImage(
                                                    model = attachedPhotoUri,
                                                    contentDescription = "Attached photo preview",
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = "Photo attached", color = TextPrimary, fontSize = 12.sp)
                                        } else if (attachedFileName != null) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                                contentDescription = "Attached file",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(text = attachedFileName!!, color = TextPrimary, fontSize = 12.sp)
                                        }
                                    }

                                    IconButton(
                                        onClick = {
                                            attachedPhotoUri = null
                                            attachedFileName = null
                                            attachedFileUri = null
                                            attachedFileSize = null
                                        },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = TextMuted)
                                    }
                                }
                            }
                        }

                        // Effects & Spam Bar
                        AnimatedVisibility(
                            visible = isArmoryExpanded,
                            enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                            exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
                        ) {
                            EffectsArmoryBar(
                                selectedEffects = selectedEffects,
                                onToggleEffect = { viewModel.toggleEffect(it) },
                                onSelectAll = { viewModel.selectAllEffects() },
                                onClearAll = { viewModel.clearEffects() },
                                spamMultiplier = spamMultiplier,
                                onMultiplierChange = { viewModel.setSpamMultiplier(it) }
                            )
                        }

                        // Bottom Chat Input Bar
                        Surface(
                            color = SurfaceInput,
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .border(width = 0.5.dp, color = DividerColor)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Attachment Menu Button (+)
                                    Box {
                                        IconButton(
                                            onClick = { showAttachmentMenu = true },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceHover)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Attach",
                                                tint = TextPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        DropdownMenu(
                                            expanded = showAttachmentMenu,
                                            onDismissRequest = { showAttachmentMenu = false }
                                        ) {
                                             DropdownMenuItem(
                                                text = { Text("Upload Photo to Cloud") },
                                                leadingIcon = { Icon(Icons.Default.CloudQueue, contentDescription = null, tint = Color(0xFF60A5FA)) },
                                                onClick = {
                                                    showAttachmentMenu = false
                                                    photoPickerLauncher.launch(
                                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                    )
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Upload File to Cloud") },
                                                leadingIcon = { Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF34D399)) },
                                                onClick = {
                                                    showAttachmentMenu = false
                                                    filePickerLauncher.launch("*/*")
                                                }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Message Perks / Effects") },
                                                leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null) },
                                                onClick = {
                                                    showAttachmentMenu = false
                                                    isArmoryExpanded = !isArmoryExpanded
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(6.dp))

                                    OutlinedTextField(
                                        value = inputText,
                                        onValueChange = { inputText = it },
                                        placeholder = {
                                            Text(
                                                text = "Message ${activeChannel.name}...",
                                                color = TextMuted,
                                                fontSize = 13.sp
                                            )
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chat_input_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color.Transparent,
                                            unfocusedBorderColor = Color.Transparent,
                                            focusedContainerColor = SurfaceHover.copy(alpha = 0.6f),
                                            unfocusedContainerColor = SurfaceHover.copy(alpha = 0.6f),
                                            focusedTextColor = TextPrimary,
                                            unfocusedTextColor = TextPrimary
                                        ),
                                        shape = RoundedCornerShape(18.dp),
                                        maxLines = 3
                                    )

                                    Spacer(modifier = Modifier.width(6.dp))

                                    IconButton(
                                        onClick = { viewModel.toggleEmojiPicker() },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                                            contentDescription = "Emojis",
                                            tint = if (showEmojiPicker) Color.White else TextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    val canSend = (inputText.isNotBlank() || attachedPhotoUri != null || attachedFileName != null) && !isUploadingCloud
                                    IconButton(
                                        onClick = {
                                            if (canSend) {
                                                if (attachedPhotoUri != null) {
                                                    viewModel.uploadAndSendCloudAttachment(
                                                        context = context,
                                                        uri = attachedPhotoUri!!,
                                                        fileName = "img_${System.currentTimeMillis()}.jpg",
                                                        isPhoto = true,
                                                        caption = inputText
                                                    )
                                                } else if (attachedFileUri != null) {
                                                    viewModel.uploadAndSendCloudAttachment(
                                                        context = context,
                                                        uri = attachedFileUri!!,
                                                        fileName = attachedFileName ?: "file_${System.currentTimeMillis()}",
                                                        isPhoto = false,
                                                        caption = inputText
                                                    )
                                                } else {
                                                    viewModel.sendMessage(text = inputText)
                                                }
                                                inputText = ""
                                                attachedPhotoUri = null
                                                attachedFileName = null
                                                attachedFileUri = null
                                                attachedFileSize = null
                                            }
                                        },
                                        enabled = canSend,
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (canSend) AccentSuccess else SurfaceHover)
                                            .testTag("send_message_button")
                                    ) {
                                        if (isUploadingCloud) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = Color.Black,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Send",
                                                tint = if (canSend) Color.Black else TextMuted,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }

                                // Cloud Upload Progress Notice
                                if (isUploadingCloud && uploadProgressNotice != null) {
                                    Surface(
                                        color = SurfaceHover,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(12.dp),
                                                color = Color(0xFF60A5FA),
                                                strokeWidth = 1.5.dp
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = uploadProgressNotice!!,
                                                color = TextPrimary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }

                                // Interactive Animated Emoji Tray
                                AnimatedVisibility(
                                    visible = showEmojiPicker,
                                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                                ) {
                                    AnimatedEmojiPicker(
                                        onSelectEmoji = { emoji ->
                                            inputText += emoji
                                        },
                                        onEmojiBurst = { emoji ->
                                            viewModel.triggerEmojiBurst(emoji)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Interactive Animated Emoji Burst Layer
                EmojiBurstLayer(
                    particles = activeParticles,
                    onParticleFinished = { /* particle completed */ }
                )
            }
        }
    }

    // Active Voice Call Overlay
    // Active Voice Call Overlay (with In-Call Live Screen Sharing & Real DSP Voice Effects)
    if (activeCall != null) {
        VoiceCallOverlay(
            call = activeCall!!,
            micAmplitude = micAmplitude,
            liveScreenBitmap = liveScreenBitmap,
            isScreenSharePaused = isScreenSharePaused,
            screenShareResolution = screenShareResolution,
            onEndCall = { viewModel.endVoiceCall() },
            onToggleMute = { viewModel.toggleMuteCall() },
            onToggleSpeaker = { viewModel.toggleSpeakerCall() },
            onToggleScreenShare = { activity -> viewModel.toggleInCallScreenShare(activity) },
            onToggleScreenSharePause = { viewModel.toggleScreenSharePause() },
            onSelectVoiceEffect = { effect -> viewModel.setCallVoiceEffect(effect) },
            onPlaySoundboardEffect = { sound -> viewModel.playCallSoundboardEffect(sound) }
        )
    }

    // Dialogs
    if (showScannerDialog) {
        QrScannerDialog(
            onDismiss = { viewModel.closeScanner() },
            onQrPayloadScanned = { payload ->
                viewModel.closeScanner()
                viewModel.connectFromQrPayload(payload)
            },
            onOpenDm = { contact ->
                viewModel.closeScanner()
                viewModel.openDirectMessageWithFriend(contact)
            }
        )
    }

    if (showMyQrDialog) {
        MyQrPassDialog(
            user = currentUser,
            onDismiss = { viewModel.closeMyQr() }
        )
    }

    if (showNameDialog) {
        ProfileNameDialog(
            currentUser = currentUser,
            onDismiss = { viewModel.closeNameDialog() },
            onSaveProfile = { name, emoji ->
                viewModel.updateProfile(name, emoji)
                viewModel.closeNameDialog()
            }
        )
    }

    if (showCreateBotDialog) {
        CreateBotDialog(
            onDismiss = { viewModel.closeCreateBotDialog() },
            onBotCreated = { bot ->
                viewModel.closeCreateBotDialog()
            }
        )
    }

    if (showPrivacySecurityDialog) {
        PrivacySecurityDialog(
            channelName = activeChannel.name,
            isPrivacyShieldActive = isPrivacyShieldActive,
            disappearingMinutes = disappearingMinutes,
            onTogglePrivacyShield = { viewModel.togglePrivacyShield() },
            onSetDisappearingTimer = { mins -> viewModel.setDisappearingTimer(mins) },
            onPurgeConversation = { viewModel.purgeCurrentChannelMessages() },
            onDismiss = { viewModel.closePrivacySecurityDialog() }
        )
    }
}
