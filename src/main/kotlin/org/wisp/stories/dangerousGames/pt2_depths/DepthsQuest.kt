package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Drops
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.game
import wisp.questgiver.wispLib.*

/**
 * Bring some passengers to find treasure on an ocean floor. Solve riddles to keep them alive.
 */
object DepthsQuest : QuestFacilitator {
    private val DEPTHS_PLANET_TYPES = listOf(
        "terran",
        "terran-eccentric",
        "water",
        "US_water", // Unknown Skies
        "US_waterB", // Unknown Skies
        "US_continent" // Unknown Skies
    )

    const val iconPath = "graphics/icons/wispStories_depths.png" // TODO
    const val rewardCredits: Int = 100000 // TODO
    const val minimumDistanceFromPlayerInLightYearsToPlaceDepthsPlanet = 5

    var stage: Stage by PersistentData(key = "depthsQuestStage", defaultValue = Stage.NotStarted)
        private set

    var depthsPlanet: SectorEntityToken? by PersistentNullableData("depthsDestinationPlanet")
        private set

    var startingPlanet: SectorEntityToken? by PersistentNullableData("depthsStartingPlanet")
        private set

    fun shouldOfferQuest(marketAPI: MarketAPI): Boolean =
        (DragonsQuest.stage == DragonsQuest.Stage.Done || DragonsQuest.stage == DragonsQuest.Stage.FailedByAbandoning)
                && stage == Stage.NotStarted
                && marketAPI.starSystem != null // No Prism Freeport, just normal systems

    object Stage2 {
        var riddle1Choice: Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice?
                by PersistentNullableData("depthsRiddle1Choice")
        var riddle2Choice: Depths_Stage2_RiddleDialog.RiddleChoice.Riddle2Choice?
                by PersistentNullableData("depthsRiddle2Choice")
        var riddle3Choice: Depths_Stage2_RiddleDialog.RiddleChoice.Riddle3Choice?
                by PersistentNullableData("depthsRiddle3Choice")

        val riddleSuccessesCount: Int
            get() = (if (riddle1Choice?.wasSuccessful() == true) 1 else 0) +
                    (if (riddle2Choice?.wasSuccessful() == true) 1 else 0) +
                    (if (riddle3Choice?.wasSuccessful() == true) 1 else 0)

        val wallCrashesCount: Int
            get() = (if (riddle1Choice is Depths_Stage2_RiddleDialog.RiddleChoice.Riddle1Choice.WestWall) 1 else 0) +
                    (if (riddle2Choice is Depths_Stage2_RiddleDialog.RiddleChoice.Riddle2Choice.WestWall) 1 else 0) +
                    (if (riddle3Choice is Depths_Stage2_RiddleDialog.RiddleChoice.Riddle3Choice.EastWall) 1 else 0)

        val didAllCrewDie: Boolean
            get() = riddle1Choice?.wasSuccessful() == false
                    && riddle2Choice?.wasSuccessful() == false
                    && riddle3Choice?.wasSuccessful() == false
    }

    fun init(playersCurrentStarSystem: StarSystemAPI?) {
        findAndTagDepthsPlanetIfNeeded(playersCurrentStarSystem)
        updateTextReplacements()
    }

    override fun updateTextReplacements() {
        game.text.globalReplacementGetters["depthsSourcePlanet"] = { startingPlanet?.name }
        game.text.globalReplacementGetters["depthsSourceSystem"] = { startingPlanet?.starSystem?.baseName }
        game.text.globalReplacementGetters["depthsPlanet"] = { depthsPlanet?.name }
        game.text.globalReplacementGetters["depthsSystem"] = { depthsPlanet?.starSystem?.baseName }
        game.text.globalReplacementGetters["depthsCreditReward"] = { rewardCredits }
    }

    /**
     * Find a planet with oceans somewhere near the center, excluding player's current location.
     * Prefer decivilized world, then uninhabited, then all others
     */
    private fun findAndTagDepthsPlanetIfNeeded(playersCurrentStarSystem: StarSystemAPI?) {
        if (depthsPlanet == null) {
            val planet = try {
                Utilities.getSystemsForQuestTarget()
                    .filter { it.id != playersCurrentStarSystem?.id }
                    .filter { it.distanceFromPlayerInHyperspace > minimumDistanceFromPlayerInLightYearsToPlaceDepthsPlanet }
                    .sortedBy { it.distanceFromCenterOfSector }
                    .flatMap { it.planets }
                    .filter { planet -> DEPTHS_PLANET_TYPES.any { it == planet.typeId } }
                    .toList()
                    .run {
                        // Take all planets from the top half of the list,
                        // which is sorted by proximity to the center.
                        val possibles = this.take((this.size / 2).coerceAtLeast(1))

                        WeightedRandomPicker<PlanetAPI>().apply {
                            possibles.forEach { planet ->
                                when {
                                    planet.market?.hasCondition(Conditions.DECIVILIZED) == true -> {
                                        game.logger.i { "Adding decivved planet ${planet.fullName} in ${planet.starSystem.baseName} to Depths candidate list" }
                                        add(planet, 3f)
                                    }
                                    planet.market?.size == 0 -> {
                                        game.logger.i { "Adding uninhabited planet ${planet.fullName} in ${planet.starSystem.baseName} to Depths candidate list" }
                                        add(planet, 2f)
                                    }
                                    else -> {
                                        game.logger.i { "Adding planet ${planet.fullName} in ${planet.starSystem.baseName} to Depths candidate list" }
                                        add(planet, 1f)
                                    }
                                }
                            }
                        }
                            .pick()!!
                    }
            } catch (e: Exception) {
                // If no planets matching the criteria are found
                game.errorReporter.reportCrash(e)
                return
            }

            game.logger.i { "Set Depths quest destination to ${planet.fullName} in ${planet.starSystem.baseName}" }
            depthsPlanet = planet
        }
    }

    fun restartQuest() {
        game.logger.i { "Restarting Depths quest." }

        depthsPlanet = null
        startingPlanet = null
        Stage2.riddle1Choice = null
        Stage2.riddle2Choice = null
        Stage2.riddle3Choice = null
        stage = Stage.NotStarted

        game.intelManager.findFirst(DepthsQuest_Intel::class.java)?.endImmediately()
        BarEventManager.getInstance().removeBarEventCreator(Depths_Stage1_BarEventCreator::class.java)
        init(game.sector.playerFleet.starSystem)
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

    fun finishQuest() {
        game.sector.playerFleet.cargo.credits.add(rewardCredits.toFloat())
        stage = Stage.Done
        game.intelManager.findFirst(DepthsQuest_Intel::class.java)
            ?.apply {
                endAfterDelay()
                sendUpdateIfPlayerHasIntel(null, false)
            }

        depthsPlanet?.let { planet ->
            if (planet.market.conditions.none { it.plugin is CrystalMarketMod }) {
                planet.market.addCondition("wispQuests_crystallineCatalyst")
            }
        }
    }

    fun generateRewardLoot(entity: SectorEntityToken): CargoAPI? {
        when (Stage2.riddleSuccessesCount) {
            3 -> {
                entity.addDropValue(Drops.BASIC, 50000)
                entity.addDropRandom("blueprints", 5)
                entity.addDropRandom("rare_tech", 2)
            }
            2 -> {
                entity.addDropValue(Drops.BASIC, 30000)
                entity.addDropRandom("blueprints", 4)
                entity.addDropRandom("rare_tech", 1)
            }
            1 -> {
                entity.addDropValue(Drops.BASIC, 20000)
                entity.addDropRandom("blueprints", 3)
                entity.addDropRandom("rare_tech", 1)
            }
            else -> {
                entity.addDropValue(Drops.BASIC, 10000)
                entity.addDropRandom("blueprints", 2)
                entity.addDropRandom("rare_tech", 1)
            }
        }

        return SalvageEntity.generateSalvage(
            Misc.getRandom(game.sector.memoryWithoutUpdate.getLong(MemFlags.SALVAGE_SEED), 100),
            1f,
            1f,
            1f,
            1f,
            entity.dropValue,
            entity.dropRandom
        )
            .apply { sort() }
    }

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
}