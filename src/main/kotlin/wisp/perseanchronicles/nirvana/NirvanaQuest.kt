package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.StarTypes
import com.fs.starfarer.api.impl.campaign.ids.Terrain
import com.fs.starfarer.api.impl.campaign.procgen.PlanetGenDataSpec
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams
import com.fs.starfarer.api.util.Misc
import org.lwjgl.util.vector.Vector2f
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.*
import kotlin.random.Random

object NirvanaQuest {

    fun isValidPlanetForDestination(planet: PlanetAPI): Boolean =
        planet.market?.factionId?.toLowerCase() !in listOf("luddic_church", "luddic_path")
                && !planet.isGasGiant
                && !planet.isStar

    fun completeSecret() {
        NirvanaHubMission.state.secretCompleteDateInMillis = game.sector.clock.timestamp
    }

    fun createPulsarSystem(): Boolean {
        if (game.sector.getStarSystem(game.text["nirv_starSystem_name"]) != null) {
            return false
        }

        // Create the system
        val newSystem = game.sector.createStarSystem(game.text["nirv_starSystem_name"])

        // Create the neutron star
        // Adapted from StarSystemGenerator.addStars
        val spec =
            game.settings.getSpec(StarGenDataSpec::class.java, StarTypes.NEUTRON_STAR, false) as StarGenDataSpec
        val radius = (spec.minRadius..spec.maxRadius).random()
        var corona: Float =
            radius * (spec.coronaMult + spec.coronaVar * (Random.nextFloat() - 0.5f))
        if (corona < spec.coronaMin) corona = spec.coronaMin

        val star = newSystem.initStar(
            "${MOD_ID}_nirvana_star",
            StarTypes.NEUTRON_STAR,
            radius,
            corona,
            spec.solarWind,
            (spec.minFlare..spec.maxFlare).random(),
            spec.crLossMult
        )

        newSystem.lightColor = Misc.interpolateColor(spec.lightColorMin, spec.lightColorMax, Random.nextFloat())
        newSystem.star = star

        // Adapted from StarSystemGenerator.setPulsarIfNeutron
        val coronaPlugin = Misc.getCoronaFor(star)

        if (coronaPlugin != null) {
            newSystem.removeEntity(coronaPlugin.entity)
        }

        newSystem.addCorona(star, 300f, 3f, 0f, 3f) // cr loss

        val eventHorizon: SectorEntityToken = newSystem.addTerrain(
            Terrain.PULSAR_BEAM,
            CoronaParams(
                star.radius + corona, (star.radius + corona) / 2f,
                star,
                spec.solarWind,
                (spec.minFlare..spec.maxFlare).random(),
                spec.crLossMult
            )
        )
        eventHorizon.setCircularOrbit(star, 0f, 0f, 100f)

        // Add planet
        addPlanetToSystem(newSystem)

        // Find a constellation to add it to
        val constellations =
            game.sector.getConstellations()
                // Prefer an un-visited constellation
                .prefer { it.systems.all { system -> system.lastPlayerVisitTimestamp == 0L } }
                .sortedByDescending { it.location.distanceFromPlayerInHyperspace }

        // Need to put in hyperspace and generate jump points so we have a maxRadiusInHyperspace
        newSystem.autogenerateHyperspaceJumpPoints(true, true)

        for (constellation in constellations) {
            val xCoords = constellation.systems.mapNotNull { it.location.x }
            val yCoords = constellation.systems.mapNotNull { it.location.y }

            // Try 5000 points
            for (i in 0..5000) {
                val point =
                    Vector2f(
                        (xCoords.minOrNull()!!..xCoords.maxOrNull()!!).random(),
                        (yCoords.minOrNull()!!..yCoords.maxOrNull()!!).random()
                    )


                val doesPointIntersectWithAnySystems = constellation.systems.any { system ->
                    doCirclesIntersect(
                        centerA = point,
                        radiusA = newSystem.maxRadiusInHyperspace,
                        centerB = system.location,
                        radiusB = system.maxRadiusInHyperspace
                    )
                }

                if (!doesPointIntersectWithAnySystems) {
                    newSystem.constellation = constellation
                    constellation.systems.add(newSystem)
                    newSystem.location.set(point)
                    newSystem.autogenerateHyperspaceJumpPoints(true, true)
                    game.logger.i { "System ${newSystem.baseName} added to the ${constellation.name} constellation." }
                    return true
                }
            }
        }

        game.logger.i { "Failed to find anywhere to add a new system!" }
        return false
    }

    fun addPlanetToSystem(system: StarSystemAPI) {
        val planetType = "barren"
        val spec = game.settings.getSpec(PlanetGenDataSpec::class.java, planetType, false) as PlanetGenDataSpec
        val planet = system.addPlanet(
            "${MOD_ID}_nirvana_planet",
            system.star,
            game.text["nirv_starSystem_planetName"],
            planetType,
            0f,
            (spec.minRadius..spec.maxRadius).random(),
            3000f,
            50f
        )
        Misc.initConditionMarket(planet)
        planet.market.addCondition(Conditions.IRRADIATED)
    }
}