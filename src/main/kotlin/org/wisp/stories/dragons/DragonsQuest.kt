package org.wisp.stories.dragons

import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.wispLib.*

object DragonsQuest {
    private val TAG_DRAGON_PLANET = "${MOD_PREFIX}_dragon_planet"
    private val DRAGON_PLANET_TYPES = listOf(
        "terran",
        "terran-eccentric",
        "jungle"
    )

    var isQuest1Started: Boolean? by PersistentData("isDragonQuest1Started")
        private set
    var isQuest1Complete: Boolean? by PersistentData("isDragonQuest1Complete", false)
        private set

    val dragonPlanet: SectorEntityToken?
        get() = Utilities.getSystems()
            .asSequence()
            .mapNotNull {
                it.getEntitiesWithTag(TAG_DRAGON_PLANET)
                    .firstOrNull()
            }
            .firstOrNull()

    fun findAndTagDragonPlanet() {
        if (dragonPlanet == null) {
            val system = try {
                Utilities.getSystems()
                    .sortedBy { it.distanceFromCenterOfSector }
                    .flatMap { it.planets }
                    .filter { planet -> DRAGON_PLANET_TYPES.any { it == planet.typeId } }
                    .toList()
                    .run {
                        this.take((this.size / 3).coerceAtLeast(1))
                    }
                    .random()
            } catch (e: Exception) {
                di.errorReporter.reportCrash(e)
                return
            }

            system.addTag(TAG_DRAGON_PLANET)
        }
    }

    fun startQuest1() {
        isQuest1Started = true
    }
}