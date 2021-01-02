package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.wispLib.*

class RileyQuest : QuestFacilitator {
    companion object {
        const val REWARD_CREDITS = 100000
        const val BOUNTY_CREDITS = 20000
        const val TIME_LIMIT_DAYS = 30
    }

    var startPlanet: SectorEntityToken? by PersistentNullableData("rileyStartPlanet")
        private set

    var destinationPlanet: SectorEntityToken? by PersistentNullableData("rileyDestinationPlanet")
        private set

    var stage: Stage by PersistentData("rileyStage", Stage.NotStarted)
        private set

    fun shouldMarketOfferQuest(marketAPI: MarketAPI): Boolean =
        stage == Stage.NotStarted
                && marketAPI.size > 5 // Lives on a populous world
                && marketAPI.starSystem in Utilities.getSystemsForQuestTarget() // Valid system, not blacklisted

    override fun updateTextReplacements() {
        game.text.globalReplacementGetters["rileyDestPlanet"] = { destinationPlanet?.name }
        game.text.globalReplacementGetters["rileyCredits"] = { REWARD_CREDITS }
        game.text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        game.text.globalReplacementGetters["rileyDestSystem"] = { destinationPlanet?.starSystem?.baseName }
        game.text.globalReplacementGetters["rileyDestPlanetDistanceLY"] = {
            if (destinationPlanet == null) String.empty
            else startPlanet?.starSystem?.distanceFrom(destinationPlanet!!.starSystem).toString()
        }
        game.text.globalReplacementGetters["rileyDestPlanetControllingFaction"] =
            { destinationPlanet?.faction?.displayName }
        game.text.globalReplacementGetters["rileyOriginPlanet"] = { startPlanet?.name }
        game.text.globalReplacementGetters["rileyBountyCredits"] = { BOUNTY_CREDITS }
    }

    /**
     * On player interacting with bar event prompt. Chooses the destination planet.
     */
    fun init(startingPlanet: PlanetAPI) {
        findAndTagDestinationPlanetIfNeeded(startingPlanet)
        updateTextReplacements()
    }

    /**
     * On player accepting the quest.
     */
    fun start(startingPlanet: PlanetAPI) {
        game.logger.i { "Riley start planet set to ${startingPlanet.fullName} in ${startingPlanet.starSystem.baseName}" }
        startPlanet = startingPlanet
    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagDestinationPlanetIfNeeded(startPlanet: PlanetAPI) {
        if (destinationPlanet == null) {
            val planets = Utilities.getSystemsForQuestTarget()
                .sortedByDescending { it.distanceFrom(startPlanet.starSystem) }
                .flatMap { it.planets }

            // Both Hegemony and VIC would have cause to work on subservient AI
            destinationPlanet = planets
                .filter { it.market?.factionId?.toLowerCase() in listOf("hegemony", "vic") }
                .ifEmpty { planets }
                .take(5)
                .random()
                .also { planet ->
                    game.logger.i { "Riley destination planet set to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }
                }
        }
    }

    enum class Stage {
        NotStarted,
        GoToFather,
        Completed
    }
}