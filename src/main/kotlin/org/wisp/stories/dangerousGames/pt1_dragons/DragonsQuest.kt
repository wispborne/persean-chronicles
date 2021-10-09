package org.wisp.stories.dangerousGames.pt1_dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.wisp.stories.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.wispLib.*

/**
 * Bring some passengers to see dragon-like creatures on a dangerous adventure.
 * Part 1 - Bring passengers to planet.
 * Part 2 - Return them back home
 */
object DragonsQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "dragonQuestStage", defaultValue = { Stage.NotStarted }),
    autoIntelInfo = AutoIntelInfo(DragonsQuest_Intel::class.java) {
        DragonsQuest_Intel(
            startLocation = DragonsQuest.state.startingPlanet,
            endLocation = DragonsQuest.state.dragonPlanet
        )
    },
    autoBarEventInfo = AutoBarEventInfo(
        barEventCreator = DragonsPart1_BarEventCreator(),
        shouldGenerateBarEvent = { true },
        shouldOfferFromMarket = { market ->
            market.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")
                    && market.starSystem != null // No prism freeport
                    && market.size > 3
                    && DragonsQuest.state.dragonPlanet != null
        })
) {
    const val rewardCredits: Int = 95000
    private const val minimumDistanceFromPlayerInLightYearsToPlaceDragonPlanet = 5
    private val DRAGON_PLANET_TYPES = listOf(
        "terran",
        "terran-eccentric",
        "jungle",
        "US_jungle" // Unknown Skies
    )
    val icon = InteractionDefinition.Portrait("wispStories_dragonriders", "icon")
    val intelDetailHeaderImage = InteractionDefinition.Illustration("wispStories_dragonriders", "intelPicture")
    val dragonPlanetImage = InteractionDefinition.Illustration("wispStories_dragonriders", "planetIllustration")

    val state = State(PersistentMapData<String, Any?>(key = "dragonState").withDefault { null })

    class State(val map: MutableMap<String, Any?>) {
        var startDate: Long? by map
        var dragonPlanet: SectorEntityToken? by map
        var startingPlanet: SectorEntityToken? by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["dragonPlanet"] = { state.dragonPlanet?.name }
        text.globalReplacementGetters["dragonSystem"] = { state.dragonPlanet?.starSystem?.name }
        text.globalReplacementGetters["startPlanet"] = { state.startingPlanet?.name }
        text.globalReplacementGetters["startSystem"] = { state.startingPlanet?.starSystem?.name }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        findAndTagDragonPlanet(interactionTarget.starSystem)
    }

    /**
     * Find a planet with life somewhere near the center, excluding player's current location.
     */
    private fun findAndTagDragonPlanet(playersCurrentStarSystem: StarSystemAPI?) {
        val planet = try {
            game.sector.starSystemsAllowedForQuests
                .filter { it.id != playersCurrentStarSystem?.id }
                .filter { system ->
                    system.solidPlanets
                        .any { planet -> DRAGON_PLANET_TYPES.any { it == planet.typeId } }
                }
                .prefer { it.distanceFromPlayerInHyperspace > minimumDistanceFromPlayerInLightYearsToPlaceDragonPlanet }
                .sortedBy { it.distanceFromCenterOfSector }
                .flatMap { it.solidPlanets }
                .filter { planet -> DRAGON_PLANET_TYPES.any { it == planet.typeId } }
                .toList()
                .getNonHostileOnlyIfPossible()
                .run {
                    // Take all planets from the top third of the list,
                    // which is sorted by proximity to the center.
                    this.take((this.size / 3).coerceAtLeast(1))
                }
                .random()
        } catch (e: Exception) {
            // If no planets matching the criteria are found
            game.logger.i(e) { "No planets matching the criteria found for Dragons" }
            return
        }

        state.dragonPlanet = planet
    }

    fun startStage1(startLocation: SectorEntityToken) {
        state.startingPlanet = startLocation
        stage = Stage.GoToPlanet
        state.startDate = game.sector.clock.timestamp
    }

    fun failQuestByLeavingOthersToGetEatenByDragons() {
        stage = Stage.FailedByAbandoning
    }

    fun startPart2() {
        stage = Stage.ReturnToStart
        game.intelManager.findFirst(DragonsQuest_Intel::class.java)
            ?.apply {
                flipStartAndEndLocations()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }

    fun finishStage2() {
        game.sector.playerFleet.cargo.credits.add(rewardCredits.toFloat())
        stage = Stage.Done
    }

    fun restartQuest() {
        game.logger.i { "Restarting Dragons quest." }

        state.map.clear()
        stage = Stage.NotStarted
    }

    abstract class Stage(progress: Progress) : AutoQuestFacilitator.Stage(progress) {
        object NotStarted : Stage(Progress.NotStarted)
        object GoToPlanet : Stage(Progress.InProgress)
        object ReturnToStart : Stage(Progress.InProgress)
        object FailedByAbandoning : Stage(Progress.Completed)
        object Done : Stage(Progress.Completed)
    }
}