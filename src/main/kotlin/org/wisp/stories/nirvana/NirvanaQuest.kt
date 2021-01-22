package org.wisp.stories.nirvana

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.wispLib.PersistentNullableData

object NirvanaQuest : QuestFacilitator {
    var startLocation: SectorEntityToken? by PersistentNullableData("nirvanaStartLocation")
        private set

    var destPlanet: SectorEntityToken? by PersistentNullableData("nirvanaDestPlanet")
        private set

    val destSystem = destPlanet?.starSystem

    override fun updateTextReplacements() {
        TODO("Not yet implemented")
    }

    fun setUpPlanet() {
        fun isValidPlanet(planet: PlanetAPI): Boolean =
            (planet.faction?.isHostileTo(game.sector.playerFaction) != true)
                    && planet.market.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")

        val system = Utilities.getSystemsForQuestTarget()
            .filter { sys -> sys.star.spec.isPulsar && sys.planets.any { isValidPlanet(it) } }
            .random()


        val planet = system.planets
            .filter { isValidPlanet(it) }
            .minBy { it.market?.hazardValue ?: 500f }
            ?: kotlin.run {
                game.errorReporter.reportCrash(NullPointerException("No planet found in ${system.name} for Nirvana quest."))
                return
            }

        // Change the planet to be tidally locked so there's a realistic place to set up a base camp.
        planet.spec.rotation = 0f
        planet.applySpecChanges()

        destPlanet = planet
    }

}