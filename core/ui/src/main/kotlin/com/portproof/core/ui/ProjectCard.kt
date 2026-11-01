package com.portproof.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.portproof.core.designsystem.component.PpCard
import com.portproof.core.designsystem.component.PpDivider
import com.portproof.core.designsystem.component.PpIconTile
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.designsystem.theme.PortProofTheme
import com.portproof.core.model.Project

/** Project summary card used by the dashboard, the project list and search results. */
@Composable
fun ProjectCard(project: Project, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colors = PortProofTheme.colors
    PpCard(modifier = modifier, onClick = onClick, contentPadding = PaddingValues(14.dp)) {
        Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            PpIconTile(project.status.icon, size = 36.dp, iconSize = 18.dp, containerColor = colors.surfaceMuted)
            Column(Modifier.weight(1f)) {
                Text(
                    project.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = listOfNotNull(project.customerName, project.siteName).joinToString(" · ")
                Text(
                    subtitle.ifEmpty { stringResource(R.string.ui_unnamed_customer) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            ProjectStatusPill(project)
        }
        Spacer(Modifier.height(12.dp))
        PpDivider()
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            project.reference?.let { MetaItem(PortProofIcons.Hash, it, Modifier.weight(1f, fill = false)) }
            MetaItem(
                icon = PortProofIcons.Calendar,
                text = formatDateRange(project.startDate, project.endDate) ?: stringResource(R.string.ui_no_dates),
                modifier = Modifier.weight(1f),
            )
            Text(
                stringResource(R.string.ui_updated, relativeTime(project.updatedAt)),
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun MetaItem(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, null, tint = PortProofTheme.colors.textMuted, modifier = Modifier.size(13.dp))
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            color = PortProofTheme.colors.textSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
