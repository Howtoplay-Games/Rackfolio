package com.portproof.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.portproof.core.designsystem.component.PpStatusPill
import com.portproof.core.designsystem.component.PpTone
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.model.Project
import com.portproof.core.model.ProjectStatus

@get:StringRes
val ProjectStatus.labelRes: Int
    get() = when (this) {
        ProjectStatus.PLANNED -> R.string.ui_status_planned
        ProjectStatus.IN_PROGRESS -> R.string.ui_status_in_progress
        ProjectStatus.ON_HOLD -> R.string.ui_status_on_hold
        ProjectStatus.COMPLETED -> R.string.ui_status_completed
    }

val ProjectStatus.tone: PpTone
    get() = when (this) {
        ProjectStatus.PLANNED -> PpTone.Neutral
        ProjectStatus.IN_PROGRESS -> PpTone.Accent
        ProjectStatus.ON_HOLD -> PpTone.Warning
        ProjectStatus.COMPLETED -> PpTone.Success
    }

val ProjectStatus.icon: ImageVector
    get() = when (this) {
        ProjectStatus.PLANNED -> PortProofIcons.CircleDashed
        ProjectStatus.IN_PROGRESS -> PortProofIcons.CirclePlay
        ProjectStatus.ON_HOLD -> PortProofIcons.CirclePause
        ProjectStatus.COMPLETED -> PortProofIcons.CircleCheck
    }

/** Status pill; archived projects show "Archived" because that is what matters most. */
@Composable
fun ProjectStatusPill(project: Project, modifier: Modifier = Modifier) {
    if (project.isArchived) {
        PpStatusPill(stringResource(R.string.ui_archived), PpTone.Neutral, modifier)
    } else {
        PpStatusPill(stringResource(project.status.labelRes), project.status.tone, modifier)
    }
}
