package org.wisp.stories.dangerousGames.A_dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.wispLib.*

/**
 * Bring some passengers to see dragon-like creatures on a dangerous adventure.
 * Part 1 - Bring passengers to planet.
 * Part 2 - Return them back home
 */
object DragonsQuest {
    private val TAG_DRAGON_PLANET = "${MOD_PREFIX}_dragon_planet"
    private val DRAGON_PLANET_TYPES = listOf(
        "terran",
        "terran-eccentric",
        "jungle"
    )

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

    /**
     * @since 1.0
     */
    var stage: Stage by PersistentData("dragonQuestStage", Stage.NotStarted)
        private set

    val dragonPlanet: SectorEntityToken?
        get() = Utilities.getSystems()
            .asSequence()
            .mapNotNull {
                it.getEntitiesWithTag(TAG_DRAGON_PLANET)
                    .firstOrNull()
            }
            .firstOrNull()

    /**
     * Find a planet with life somewhere near the center
     */
    fun findAndTagDragonPlanetIfNeeded() {
        if (dragonPlanet == null) {
            val system = try {
                Utilities.getSystemsForQuestTarget()
                    .sortedBy { it.distanceFromCenterOfSector }
                    .flatMap { it.planets }
                    .filter { planet -> DRAGON_PLANET_TYPES.any { it == planet.typeId } }
                    .toList()
                    .run {
                        this.take((this.size / 3).coerceAtLeast(1))
                    }
                    .random()
            } catch (e: Exception) {
                // Can crash if no planets are found
                di.errorReporter.reportCrash(e)
                return
            }

            system.addTag(TAG_DRAGON_PLANET)
        }
    }

    fun startQuest1(startLocation: SectorEntityToken) {
        stage = Stage.GoToPlanet
        di.intelManager.addIntel(DragonsQuest_Intel(startLocation))
    }

    fun failQuestByLeavingToGetEatenByDragons() {
        stage = Stage.FailedByAbandoning
        di.intelManager.findFirst(DragonsQuest_Intel::class.java)
            ?.endAfterDelay()
    }

    fun startPart2() {
        stage = Stage.ReturnToStart
    }
}