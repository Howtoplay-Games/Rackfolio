package com.portproof.core.ui

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.portproof.core.designsystem.icon.PortProofIcons
import com.portproof.core.model.ProjectStatus

@get:StringRes
val ProjectStatus.labelRes: Int
    get() = when (this) {
        ProjectStatus.PLANNED -> R.string.ui_status_planned
        ProjectStatus.IN_PROGRESS -> R.string.ui_status_in_progress
        ProjectStatus.ON_HOLD -> R.string.ui_status_on_hold
        ProjectStatus.COMPLETED -> R.string.ui_status_completed
    }

val ProjectStatus.icon: ImageVector
    get() = when (this) {
        ProjectStatus.PLANNED -> PortProofIcons.CircleDashed
        ProjectStatus.IN_PROGRESS -> PortProofIcons.CirclePlay
        ProjectStatus.ON_HOLD -> PortProofIcons.CirclePause
        ProjectStatus.COMPLETED -> PortProofIcons.CircleCheck
    }
