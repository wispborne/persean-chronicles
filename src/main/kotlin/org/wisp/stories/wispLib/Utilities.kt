package org.wisp.stories.wispLib

import com.fs.starfarer.api.campaign.StarSystemAPI
import org.wisp.stories.isBlacklisted
import org.wisp.stories.isValidSystemForQuest

object Utilities {
    fun getSystems(): List<StarSystemAPI> =
        game.sector.starSystems
            .filterNot { it.isBlacklisted }

    fun getSystemsForQuestTarget(): List<StarSystemAPI> =
        game.sector.starSystems
            .filter { it.isValidSystemForQuest }
}

