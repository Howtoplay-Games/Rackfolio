package com.portproof.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.ActivityAction
import com.portproof.core.model.ActivityEntry

/**
 * One activity-log line. [showProject] adds the project name (dashboard feed); inside a project
 * the name is redundant and omitted.
 */
@Composable
fun ActivityRow(
    entry: ActivityEntry,
    modifier: Modifier = Modifier,
    showProject: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val colors = PortProofTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .then(if (onClick != null) Modifier.clickable(role = Role.Button, onClick = onClick) else Modifier)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PpIconTile(entry.icon(), size = 40.dp, iconSize = 18.dp)
        Column(Modifier.weight(1f)) {
            Text(entry.description(), style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, maxLines = 1)
            if (showProject) {
                Text(
                    entry.projectName ?: stringResource(R.string.ui_activity_deleted_project),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Text(relativeTime(entry.occurredAt), style = MaterialTheme.typography.labelSmall, color = colors.textMuted)
    }
}

private fun ActivityEntry.icon(): ImageVector = when (action) {
    ActivityAction.CREATED -> PortProofIcons.FolderPlus
    ActivityAction.UPDATED -> PortProofIcons.Edit
    ActivityAction.STATUS_CHANGED -> newStatus?.icon ?: PortProofIcons.StatusChange
    ActivityAction.ARCHIVED -> PortProofIcons.Archive
    ActivityAction.RESTORED -> PortProofIcons.Restore
}

@Composable
private fun ActivityEntry.description(): String = when (action) {
    ActivityAction.CREATED -> stringResource(R.string.ui_activity_created)
    ActivityAction.UPDATED -> stringResource(R.string.ui_activity_updated)
    ActivityAction.STATUS_CHANGED -> {
        val status = newStatus
        if (status != null) {
            stringResource(R.string.ui_activity_status_changed, stringResource(status.labelRes))
        } else {
            stringResource(R.string.ui_activity_updated)
        }
    }
    ActivityAction.ARCHIVED -> stringResource(R.string.ui_activity_archived)
    ActivityAction.RESTORED -> stringResource(R.string.ui_activity_restored)
}
