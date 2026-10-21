package com.revlens.app.ui.components.display

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.revlens.app.domain.model.Member
import com.revlens.app.domain.model.Role
import com.revlens.app.ui.theme.RevLensTheme
import com.revlens.app.ui.theme.RevLensTypography

@Composable
fun MemberRow(
    member: Member,
    canManage: Boolean,
    onRoleChange: (Role) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    isCurrentUser: Boolean = false,
    canChangeRole: Boolean = canManage && !isCurrentUser && member.role != Role.Owner,
    canRemove: Boolean = canManage && !isCurrentUser && member.role != Role.Owner
) {
    var roleMenuExpanded by remember { mutableStateOf(false) }

    val initials = member.name.split(" ")
        .filter { it.isNotBlank() }
        .mapNotNull {
            it.firstOrNull()
                ?.uppercase()
        }
        .take(2)
        .joinToString("")
        .ifEmpty { "U" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RevLensTheme.colors.surface)
            .border(1.dp, RevLensTheme.colors.borderDefault, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(RevLensTheme.colors.brandPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = RevLensTypography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = RevLensTheme.colors.brandPrimary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isCurrentUser) "${member.name} (You)" else member.name,
                    style = RevLensTypography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = RevLensTheme.colors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${member.email} · ${member.joinedAt}",
                    style = RevLensTypography.bodySmall,
                    color = RevLensTheme.colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                Row(
                    modifier = if (canChangeRole) {
                        Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { roleMenuExpanded = true }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    } else {
                        Modifier
                    },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RoleChip(role = member.role)
                    if (canChangeRole) {
                        Icon(
                            imageVector = Icons.Filled.ArrowDropDown,
                            contentDescription = "Change Role",
                            tint = RevLensTheme.colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (canChangeRole) {
                    DropdownMenu(
                        expanded = roleMenuExpanded,
                        onDismissRequest = { roleMenuExpanded = false },
                        modifier = Modifier.background(RevLensTheme.colors.surfaceElevated)
                    ) {
                        listOf(Role.Admin, Role.Editor, Role.Viewer).forEach { targetRole ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = targetRole.name,
                                        color = if (targetRole == member.role) RevLensTheme.colors.brandPrimary else RevLensTheme.colors.textPrimary,
                                        fontWeight = if (targetRole == member.role) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    roleMenuExpanded = false
                                    if (targetRole != member.role) {
                                        onRoleChange(targetRole)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            if (canRemove) {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteOutline,
                        contentDescription = "Remove Member",
                        tint = RevLensTheme.colors.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
