package org.wisp.stories.dangerousGames

import com.fs.starfarer.api.campaign.StarSystemAPI
import org.wisp.stories.game
import org.wisp.stories.isBlacklisted
import org.wisp.stories.isValidSystemForQuest

object Utilities {
    fun getSystems(): List<StarSystemAPI> =
        game.sector.starSystems
            .filterNot { it.isBlacklisted }

    /**
     * Returns a list of systems that may be used for quest targets,
     * taking into account blacklisted systems.
     */
    fun getSystemsForQuestTarget(): List<StarSystemAPI> =
        game.sector.starSystems
            .filter { it.isValidSystemForQuest }
}

