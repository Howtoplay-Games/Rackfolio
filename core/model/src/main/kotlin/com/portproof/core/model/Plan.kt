package com.portproof.core.model

/** What the user's plan allows. Every plan works fully offline. */
sealed interface AppPlan {
    /** Free plan: at most [activeProjectLimit] active (not completed, not archived) projects. */
    data class Free(val activeProjectLimit: Int = PlanRules.FREE_ACTIVE_PROJECT_LIMIT) : AppPlan

    /** PortProof Pro (Google Play subscription): no project limit. */
    data object Pro : AppPlan
}

/** Active-project limit of this plan, or null when there is none. */
val AppPlan.activeProjectLimit: Int?
    get() = (this as? AppPlan.Free)?.activeProjectLimit

object PlanRules {
    /** Active projects (not completed, not archived) allowed on the free plan. */
    const val FREE_ACTIVE_PROJECT_LIMIT = 3

    /** True when one more active project fits within [plan], given [activeCount] active projects now. */
    fun canAddActiveProject(plan: AppPlan, activeCount: Int): Boolean {
        val limit = plan.activeProjectLimit ?: return true
        return activeCount < limit
    }
}

/**
 * Thrown by project writes that would take the free plan above its active-project limit. Nothing is
 * written when it is thrown.
 */
class ActiveProjectLimitException(val limit: Int) :
    IllegalStateException("The free plan allows $limit active projects")
