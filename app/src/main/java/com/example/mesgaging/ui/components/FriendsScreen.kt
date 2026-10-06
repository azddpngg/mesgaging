package com.example.mesgaging.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.FriendFilter
import com.example.mesgaging.model.FriendRequest
import com.example.mesgaging.model.UserContact
import com.example.mesgaging.model.UserStatus
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceChat
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceNav
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FriendsScreen(
    contacts: List<UserContact>,
    friendRequests: List<FriendRequest>,
    currentUser: UserContact,
    activeFilter: FriendFilter,
    onFilterChange: (FriendFilter) -> Unit,
    onOpenChat: (UserContact) -> Unit,
    onStartCall: (UserContact) -> Unit,
    onOpenScanner: () -> Unit,
    onAddFriendByTag: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onRejectRequest: (String) -> Unit,
    onRemoveFriend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var addTagInput by remember { mutableStateOf("") }
    val clipboardManager = LocalClipboardManager.current

    val onlineFriends = contacts.filter { it.status == UserStatus.ONLINE || it.status == UserStatus.IDLE }

    val displayedFriends = when (activeFilter) {
        FriendFilter.ONLINE -> onlineFriends
        FriendFilter.ALL -> contacts
        else -> emptyList()
    }.filter {
        if (searchQuery.isBlank()) true
        else it.username.contains(searchQuery, ignoreCase = true) || it.discriminator.contains(searchQuery)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceChat)
    ) {
        // Friends Header Bar
        Surface(
            color = SurfaceNav,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                // Header Title
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Friends",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceHover
                    ) {
                        Text(
                            text = "${onlineFriends.size} Online",
                            color = AccentSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                // Filter Tabs (Online, All, Pending, + Add Friend)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FriendFilter.values().forEach { filter ->
                        val isSelected = activeFilter == filter
                        val isAddFriend = filter == FriendFilter.ADD_FRIEND
                        val countLabel = when (filter) {
                            FriendFilter.ONLINE -> " (${onlineFriends.size})"
                            FriendFilter.ALL -> " (${contacts.size})"
                            FriendFilter.PENDING -> if (friendRequests.isNotEmpty()) " (${friendRequests.size})" else ""
                            FriendFilter.ADD_FRIEND -> ""
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isAddFriend && isSelected -> AccentSuccess
                                        isSelected -> SurfaceHover
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onFilterChange(filter) }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${filter.title}$countLabel",
                                color = when {
                                    isAddFriend && isSelected -> Color.Black
                                    isAddFriend -> AccentSuccess
                                    isSelected -> TextPrimary
                                    else -> TextMuted
                                },
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

        // Main Tab Content
        when (activeFilter) {
            FriendFilter.ONLINE, FriendFilter.ALL -> {
                // Search Input
                Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search friends...", color = TextMuted, fontSize = 13.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DividerColor,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = SurfaceHover,
                            unfocusedContainerColor = SurfaceHover,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    )
                }

                if (displayedFriends.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "👥", fontSize = 42.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (activeFilter == FriendFilter.ONLINE) "No friends online right now" else "No friends added yet",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Switch to '+ Add Friend' or scan a QR code to connect.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp)
                    ) {
                        item {
                            Text(
                                text = "${if (activeFilter == FriendFilter.ONLINE) "ONLINE" else "ALL FRIENDS"} — ${displayedFriends.size}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp)
                            )
                        }

                        items(displayedFriends, key = { it.id }) { friend ->
                            FriendRowCard(
                                friend = friend,
                                onOpenChat = { onOpenChat(friend) },
                                onStartCall = { onStartCall(friend) },
                                onRemoveFriend = { onRemoveFriend(friend.id) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            FriendFilter.PENDING -> {
                if (friendRequests.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📬", fontSize = 40.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No pending friend requests",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Incoming requests from other users will show up here.",
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp)
                    ) {
                        item {
                            Text(
                                text = "PENDING REQUESTS — ${friendRequests.size}",
                                color = TextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        items(friendRequests, key = { it.id }) { req ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SurfaceHover,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(CircleShape)
                                                .background(SurfaceNav),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = req.avatarEmoji, fontSize = 18.sp)
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = req.username,
                                                    color = TextPrimary,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 14.sp
                                                )
                                                Text(
                                                    text = req.discriminator,
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                            Text(
                                                text = "Incoming Friend Request",
                                                color = TextSecondary,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = { onAcceptRequest(req.id) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(AccentSuccess)
                                        ) {
                                            Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.Black, modifier = Modifier.size(18.dp))
                                        }

                                        IconButton(
                                            onClick = { onRejectRequest(req.id) },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF3F3F46))
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            FriendFilter.ADD_FRIEND -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "ADD FRIEND",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "You can add friends with their username and tag (e.g. jordan#5591) or by scanning their QR pass.",
                        color = TextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Tag Input Box
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceHover,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "ADD BY USERNAME & TAG",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = addTagInput,
                                    onValueChange = { addTagInput = it },
                                    placeholder = { Text("username#0000", color = TextMuted, fontSize = 13.sp) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedContainerColor = SurfaceNav,
                                        unfocusedContainerColor = SurfaceNav,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (addTagInput.isNotBlank()) {
                                            onAddFriendByTag(addTagInput)
                                            addTagInput = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Send", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // QR Code Option
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceHover,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenScanner() }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceNav),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan QR",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Scan QR Pass",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Instantly connect with someone nearby",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Current User's Tag Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = SurfaceNav,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "YOUR DISCORD TAG", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${currentUser.username}${currentUser.discriminator}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }

                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString("${currentUser.username}${currentUser.discriminator}"))
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy Tag", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendRowCard(
    friend: UserContact,
    onOpenChat: () -> Unit,
    onStartCall: () -> Unit,
    onRemoveFriend: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceHover,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenChat() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Friend Info
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SurfaceNav),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = friend.avatarEmoji, fontSize = 18.sp)
                    }

                    // Online indicator dot
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(CircleShape)
                            .background(Color(friend.status.colorHex))
                            .border(1.5.dp, SurfaceHover, CircleShape)
                            .align(Alignment.BottomEnd)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = friend.username,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = friend.discriminator,
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = friend.customStatus.ifBlank { friend.status.label },
                        color = if (friend.status == UserStatus.ONLINE) AccentSuccess else TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            // Quick Action Buttons (Chat, Screen Share, Menu)
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenChat,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceNav)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Message",
                        tint = TextPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onStartCall,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(SurfaceNav)
                ) {
                    Icon(
                        Icons.Default.Call,
                        contentDescription = "Voice Call",
                        tint = AccentSuccess,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Voice Call") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = AccentSuccess) },
                            onClick = {
                                showMenu = false
                                onStartCall()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Send Message") },
                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                onOpenChat()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Remove Friend", color = Color(0xFFEF4444)) },
                            leadingIcon = { Icon(Icons.Default.Close, contentDescription = null, tint = Color(0xFFEF4444)) },
                            onClick = {
                                showMenu = false
                                onRemoveFriend()
                            }
                        )
                    }
                }
            }
        }
    }
}
