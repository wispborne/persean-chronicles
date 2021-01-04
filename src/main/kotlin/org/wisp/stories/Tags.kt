package org.wisp.stories

import com.fs.starfarer.api.campaign.StarSystemAPI
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX

internal object Tags {
    val TAG_BLACKLISTED_SYSTEM = "${MOD_PREFIX}_blacklisted_system"

    private val Dme = listOf(
        "theme_breakers",
        "theme_breakers_resurgent"
    )

    private val Vanilla = listOf(
        "theme_remnant_main",
        "theme_remnant_resurgent"
    )

    /**
     * These tags mark systems that we don't want to drop the player into, eg systems with remnants.
     */
    val systemTagsToAvoidRandomlyChoosing: List<String> =
        listOf(TAG_BLACKLISTED_SYSTEM)
            .plus(Vanilla)
            .plus(Dme)

    /**
     * These are systems we don't want to allow the mod to interact with at all.
     */
    val systemTagsToBlacklist = listOf(
        TAG_BLACKLISTED_SYSTEM
    )
}

internal val StarSystemAPI.isValidSystemForQuest: Boolean
    get() = this.tags.none { Tags.systemTagsToAvoidRandomlyChoosing.contains(it) }

internal val StarSystemAPI.isBlacklisted: Boolean
    get() = this.tags.any { Tags.systemTagsToBlacklist.contains(it) }