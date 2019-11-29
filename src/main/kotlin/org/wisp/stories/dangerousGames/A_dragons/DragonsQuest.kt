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

    var isDragonQuestPart1Started: Boolean? by PersistentData("isDragonQuestPart1Started")
        private set
    var isDragonQuestPart1Complete: Boolean? by PersistentData("isDragonQuestPart1Complete")
        private set

    var isDragonQuestPart2Started: Boolean? by PersistentData("isDragonQuestPart2Started")
        private set
    var isDragonQuestPart2Complete: Boolean? by PersistentData("isDragonQuestPart2Complete")
        private set

    val isDragonQuestComplete: Boolean
        get() = isDragonQuestPart1Complete == true
                && isDragonQuestPart2Complete == true

    var didFailByLeavingOthersToGetEaten: Boolean? by PersistentData("didFailByLeavingToGetEaten", false)
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
        isDragonQuestPart1Started = true
        di.intelManager.addIntel(DragonsQuest_Intel(startLocation))
    }

    fun failedByLeavingToGetEatenByDragons() {
        didFailByLeavingOthersToGetEaten = true
        di.intelManager.findFirst(DragonsQuest_Intel::class.java)
            ?.endAfterDelay()
    }

    fun completePart1() {
        isDragonQuestPart1Complete = true
    }

    fun startPart2() {
        isDragonQuestPart2Started = true
    }
}