package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.wispLib.*
import wisp.questgiver.wispLib.QuestGiver.MOD_PREFIX

/**
 * Bring some passengers to find treasure on an ocean floor. Solve riddles to keep them alive.
 */
object DepthsQuest {
    /** @since 1.0 */
    private val TAG_DEPTHS_PLANET = "${MOD_PREFIX}_depths_planet"
    private val DEPTHS_PLANET_TYPES = listOf(
        "terran",
        "terran-eccentric",
        "water",
        "US_water", // Unknown Skies
        "US_waterB", // Unknown Skies
        "US_continent" // Unknown Skies
    )

    // todo prefer decivved world, then uninhabited, then inhabited

    const val iconPath = "graphics/icons/wispStories_depths.png" // TODO
    const val rewardCredits: Int = 95000 // TODO
    const val minimumDistanceFromPlayerInLightYearsToPlaceDepthsPlanet = 5

    /**
     * Where the player is in the quest.
     * Note: Should be in order of completion.
     */
    enum class Stage {
        NotStarted,
        GoToPlanet,
        ReturnToStart,
        Done
    }

    /** @since 1.0 */
    var stage: Stage by PersistentData(key = "depthsQuestStage", defaultValue = Stage.NotStarted)

    var didFailRiddle1: Boolean by PersistentBoolean(key = "depthsQuest_didFailRiddle1", defaultValue = false)
    var didFailRiddle2: Boolean by PersistentBoolean(key = "depthsQuest_didFailRiddle2", defaultValue = false)
    var didFailRiddle3: Boolean by PersistentBoolean(key = "depthsQuest_didFailRiddle3", defaultValue = false)

    val didAllCrewDie: Boolean
        get() = didFailRiddle1 && didFailRiddle2 && didFailRiddle3

    val depthsPlanet: SectorEntityToken?
        get() = Utilities.getSystems()
            .asSequence()
            .mapNotNull {
                it.getEntitiesWithTag(TAG_DEPTHS_PLANET)
                    .firstOrNull()
            }
            .firstOrNull()

    var startingPlanet: SectorEntityToken? by PersistentNullableData("depthsStartingPlanet")
        private set

    fun shouldOfferQuest(marketAPI: MarketAPI): Boolean =
        stage == Stage.NotStarted
                && marketAPI.starSystem != null // No Prism Freeport, just normal systems

    object Stage2 {
        var riddle1Choice: Depths_Stage2_Dialog.RiddleChoice.Riddle1Choice?
                by PersistentNullableData("depthsRiddle1Choice")
        var riddle2Choice: Depths_Stage2_Dialog.RiddleChoice.Riddle2Choice?
                by PersistentNullableData("depthsRiddle2Choice")
        var riddle3Choice: Depths_Stage2_Dialog.RiddleChoice.Riddle3Choice?
                by PersistentNullableData("depthsRiddle3Choice")

        val riddleSuccessesCount: Int
            get() = (if (riddle1Choice?.wasSuccessful() == true) 1 else 0) +
                    (if (riddle2Choice?.wasSuccessful() == true) 1 else 0) +
                    (if (riddle3Choice?.wasSuccessful() == true) 1 else 0)

        val wallCrashesCount: Int
            get() = (if (riddle1Choice is Depths_Stage2_Dialog.RiddleChoice.Riddle1Choice.WestWall) 1 else 0) +
                    (if (riddle2Choice is Depths_Stage2_Dialog.RiddleChoice.Riddle2Choice.WestWall) 1 else 0) +
                    (if (riddle3Choice is Depths_Stage2_Dialog.RiddleChoice.Riddle3Choice.EastWall) 1 else 0)
    }

    fun init(playersCurrentStarSystem: StarSystemAPI?) {
        game.text.globalReplacementGetters["depthsPlanet"] = { depthsPlanet?.name }
        game.text.globalReplacementGetters["depthsSystem"] = { depthsPlanet?.starSystem?.baseName }
        game.text.globalReplacementGetters["depthsCreditReward"] = { rewardCredits }
        findAndTagDepthsPlanetIfNeeded(playersCurrentStarSystem)
    }

    /**
     * Find a planet with oceans somewhere near the center, excluding player's current location.
     */
    private fun findAndTagDepthsPlanetIfNeeded(playersCurrentStarSystem: StarSystemAPI?) {
        if (depthsPlanet == null) {
            val system = try {
                Utilities.getSystemsForQuestTarget()
                    .filter { it.id != playersCurrentStarSystem?.id }
                    .filter { it.distanceFromPlayerInHyperspace > minimumDistanceFromPlayerInLightYearsToPlaceDepthsPlanet }
                    .sortedBy { it.distanceFromCenterOfSector }
                    .flatMap { it.planets }
                    .filter { planet -> DEPTHS_PLANET_TYPES.any { it == planet.typeId } }
                    .toList()
                    .run {
                        // Take all planets from the top third of the list,
                        // which is sorted by proximity to the center.
                        this.take((this.size / 3).coerceAtLeast(1))
                    }
                    .random()
            } catch (e: Exception) {
                // If no planets matching the criteria are found
                game.errorReporter.reportCrash(e)
                return
            }

            system.addTag(TAG_DEPTHS_PLANET)
        }
    }

    fun clearDepthsPlanetTag() {
        while (depthsPlanet != null) {
            game.logger.i { "Removing tag $TAG_DEPTHS_PLANET from planet ${depthsPlanet?.fullName} in ${depthsPlanet?.starSystem?.baseName}" }
            depthsPlanet?.removeTag(TAG_DEPTHS_PLANET)
        }
    }

    fun startStage1(startLocation: SectorEntityToken) {
        stage = Stage.GoToPlanet
        startingPlanet = startLocation
        game.intelManager.addIntel(DepthsQuest_Intel(startLocation, depthsPlanet!!))
    }

    fun startStart2() {
        stage = Stage.ReturnToStart
        game.intelManager.findFirst(DepthsQuest_Intel::class.java)
            ?.apply {
                flipStartAndEndLocations()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }

    fun finishStage2() {
        game.sector.playerFleet.cargo.credits.add(rewardCredits.toFloat())
        stage = Stage.Done
        game.intelManager.findFirst(DepthsQuest_Intel::class.java)
            ?.apply {
                endAfterDelay()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }
}