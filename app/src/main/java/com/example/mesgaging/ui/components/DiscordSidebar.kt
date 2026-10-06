package com.example.mesgaging.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import com.example.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mesgaging.model.Channel
import com.example.mesgaging.model.NavTab
import com.example.mesgaging.model.UserBot
import com.example.mesgaging.model.UserContact
import com.example.mesgaging.model.UserStatus
import com.example.ui.theme.AccentSuccess
import com.example.ui.theme.DividerColor
import com.example.ui.theme.SurfaceHover
import com.example.ui.theme.SurfaceNav
import com.example.ui.theme.SurfaceSidebar
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DiscordSidebar(
    contacts: List<UserContact>,
    userBots: List<UserBot>,
    activeChannelId: String,
    currentNavTab: NavTab,
    currentUser: UserContact,
    onSelectChannel: (Channel) -> Unit,
    onOpenFriends: () -> Unit,
    onOpenMyQr: () -> Unit,
    onOpenScanner: () -> Unit,
    onOpenCreateBot: () -> Unit,
    onOpenNameDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val onlineCount = contacts.count { it.status == UserStatus.ONLINE || it.status == UserStatus.IDLE }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(SurfaceSidebar)
    ) {
        // App Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(SurfaceNav)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_bird_logo),
                        contentDescription = "App Logo",
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "mesgaging",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp
                    )
                }

                IconButton(
                    onClick = onOpenScanner,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan QR",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)

        // Scrollable Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Dedicated Friends Tab Button (Discord Style)
            val isFriendsSelected = currentNavTab == NavTab.FRIENDS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isFriendsSelected) SurfaceHover else Color.Transparent)
                    .clickable { onOpenFriends() }
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.People,
                        contentDescription = "Friends",
                        tint = if (isFriendsSelected) TextPrimary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Friends",
                        color = if (isFriendsSelected) TextPrimary else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = if (isFriendsSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }

                if (onlineCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SurfaceNav
                    ) {
                        Text(
                            text = "$onlineCount",
                            color = AccentSuccess,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Direct Messages Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "DIRECT MESSAGES (${contacts.size})",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onOpenScanner() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "+ SCAN QR",
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (contacts.isEmpty()) {
                Text(
                    text = "No direct chats yet. Scan a friend's QR or add them from the Friends tab.",
                    color = TextMuted,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
            } else {
                contacts.forEach { contact ->
                    val dmId = "dm_${contact.id}"
                    val isSelected = currentNavTab == NavTab.MESSAGES && activeChannelId == dmId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceHover else Color.Transparent)
                            .clickable {
                                onSelectChannel(
                                    Channel(
                                        id = dmId,
                                        name = contact.username,
                                        topic = "Direct messaging with ${contact.username}",
                                        isDm = true,
                                        recipientContactId = contact.id
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(SurfaceNav),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = contact.avatarEmoji, fontSize = 15.sp)
                            }

                            // Online dot indicator
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(Color(contact.status.colorHex))
                                    .align(Alignment.BottomEnd)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = contact.username,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = contact.customStatus.ifBlank { contact.status.label },
                                color = if (contact.status == UserStatus.ONLINE) AccentSuccess else TextMuted,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // User Bots Section (if any user bots were created)
            if (userBots.isNotEmpty()) {
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CUSTOM BOTS (${userBots.size})",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onOpenCreateBot() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+ NEW BOT",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                userBots.forEach { bot ->
                    val botChannelId = "chat_${bot.id}"
                    val isSelected = currentNavTab == NavTab.MESSAGES && activeChannelId == botChannelId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) SurfaceHover else Color.Transparent)
                            .clickable {
                                onSelectChannel(
                                    Channel(
                                        id = botChannelId,
                                        name = bot.name,
                                        topic = "Custom Bot: ${bot.rolePrompt}",
                                        isDm = true,
                                        isBotChat = true,
                                        botId = bot.id
                                    )
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(SurfaceNav),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = bot.avatarEmoji, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = bot.name,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(3.dp),
                                    color = SurfaceNav
                                ) {
                                    Text(
                                        text = "BOT",
                                        color = TextMuted,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Trigger: ${bot.triggerPrefix}",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "CUSTOM BOTS",
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onOpenCreateBot() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+ CREATE",
                            color = TextPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Bottom User Footer
        HorizontalDivider(color = DividerColor, thickness = 0.5.dp)
        Surface(
            color = SurfaceNav,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenNameDialog() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(SurfaceHover),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = currentUser.avatarEmoji, fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = currentUser.username,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentUser.discriminator} • Online",
                            color = AccentSuccess,
                            fontSize = 10.sp
                        )
                    }
                }

                IconButton(
                    onClick = onOpenMyQr,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "My QR Pass",
                        tint = TextSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}
