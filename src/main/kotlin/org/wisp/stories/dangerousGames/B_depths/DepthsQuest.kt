package org.wisp.stories.dangerousGames.B_depths

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import org.wisp.stories.wispLib.*

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
        FailedByAbandoning,
        Done
    }

    /** @since 1.0 */
    var stage: Stage by PersistentData(key = "depthsQuestStage", defaultValue = Stage.NotStarted)

    val depthsPlanet: SectorEntityToken?
        get() = Utilities.getSystems()
            .asSequence()
            .mapNotNull {
                it.getEntitiesWithTag(TAG_DEPTHS_PLANET)
                    .firstOrNull()
            }
            .firstOrNull()

    fun shouldOfferQuest(marketAPI: MarketAPI): Boolean =
        stage == Stage.NotStarted
                && marketAPI.starSystem != null // No Prism Freeport, just normal systems

    /**
     * Find a planet with oceans somewhere near the center, excluding player's current location.
     */
    fun findAndTagDepthsPlanetIfNeeded(playersCurrentStarSystem: StarSystemAPI?) {
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
                di.errorReporter.reportCrash(e)
                return
            }

            system.addTag(TAG_DEPTHS_PLANET)
        }
    }

    fun clearDepthsPlanetTag() {
        while (depthsPlanet != null) {
            di.logger.i { "Removing tag $TAG_DEPTHS_PLANET from planet ${depthsPlanet?.fullName} in ${depthsPlanet?.starSystem?.baseName}" }
            depthsPlanet?.removeTag(TAG_DEPTHS_PLANET)
        }
    }

    fun startQuest1(startLocation: SectorEntityToken) {
        stage = Stage.GoToPlanet
        di.intelManager.addIntel(DepthssQuest_Intel(startLocation, depthsPlanet!!))
    }

    fun failQuestByLeavingToGetEatenByDepthss() {
        stage = Stage.FailedByAbandoning
        di.intelManager.findFirst(DepthssQuest_Intel::class.java)
            ?.apply {
                endAfterDelay()
                sendUpdateIfPlayerHasIntel(null, false)
            }

    }

    fun startPart2() {
        stage = Stage.ReturnToStart
        di.intelManager.findFirst(DepthssQuest_Intel::class.java)
            ?.apply {
                flipStartAndEndLocations()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }

    fun finishStage2() {
        di.sector.playerFleet.cargo.credits.add(rewardCredits.toFloat())
        stage = Stage.Done
        di.intelManager.findFirst(DepthssQuest_Intel::class.java)
            ?.apply {
                endAfterDelay()
                sendUpdateIfPlayerHasIntel(null, false)
            }
    }
}