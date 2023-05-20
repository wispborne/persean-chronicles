package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.StarAge
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.addNebulaFromPNG
import org.magiclib.util.MagicCampaign
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.placeInSector
import java.awt.Color

object MenriSystemCreator {

    fun createMenriSystem(): PlanetAPI? {
        // gate
        // habitable with small rings
        // two? jump points
        // some hidey points
        val systemOrbitDays = 180f
        val planetName = "Menri"
        val systemName = "Lama"

        if (game.sector.getStarSystem(systemName) != null) {
            game.logger.w { "Lama system already exists!" }
            return game.sector.getStarSystem(systemName).planets.firstOrNull { it.name == planetName }
        }

        val system = game.sector.createStarSystem(systemName)

        system.backgroundTextureFilename = "graphics/backgrounds/background5.jpg"
        system.addTag(Tags.THEME_HIDDEN)

        // Star
        val star = system.initStar(
            "pc_ama",
            StarTypes.YELLOW,
            420f,
            600f
        )
            .apply {
                spec
                name = systemName
                applySpecChanges()
            }

        // System nebula
        val nebula = system.addNebulaFromPNG(
            image = "graphics/telos/terrain/lama_nebula.png",
            centerX = 0f,
            centerY = 0f,
            category = "terrain",
            key = "nebula",
            tilesWide = 4,
            tilesHigh = 4,
            terrainType = Terrain.NEBULA,
            age = StarAge.AVERAGE
        )

        // Menri
        val menri = system.addPlanet(
            "pc_menri",
            star,
            planetName,
            "rocky_metallic",
            0f,
            100f,
            3900f,
            systemOrbitDays
        )
            .apply {
                market.addCondition(Conditions.HABITABLE)
                market.addCondition(Conditions.FARMLAND_POOR)
                market.addCondition(Conditions.ORE_RICH)
                market.addCondition(Conditions.RARE_ORE_MODERATE)
                market.addCondition(Conditions.THIN_ATMOSPHERE)
                market.addCondition(Conditions.RUINS_SCATTERED)
            }

        // Menri Ring
        system.addRingBand(
            /* focus = */ menri,
            /* category = */ "wisp_perseanchronicles_telos",
            /* key = */ "menri_ring",
            /* bandWidthInTexture = */ 512f,
            /* bandIndex = */ 0,
            /* color = */ Color(255, 220, 220, 200),
            /* bandWidthInEngine = */ 156f,
            /* middleRadius = */ 200f,
            /* orbitDays = */ -140f
        )

        // Gas Giant
        system.addPlanet(
            /* id = */ "pc_Dal",
            /* focus = */ star,
            /* name = */ "Dal",
            /* type = */ "gas_giant",
            /* angle = */ 0f,
            /* radius = */ 400f,
            /* orbitRadius = */ 5300f,
            /* orbitDays = */ systemOrbitDays
        )

        // Jump point A
        MagicCampaign.addJumpPoint(
            /* id = */ "pc_jpA",
            /* name = */ "Inner System Jump-point",
            /* linkedPlanet = */ null,
            /* orbitCenter = */ star,
            /* orbitStartAngle = */ 105f,
            /* orbitRadius = */ 10000f,
            /* orbitDays = */ systemOrbitDays
        )

        // Jump point B
        MagicCampaign.addJumpPoint(
            /* id = */ "pc_jpB",
            /* name = */ "Inner System Jump-point",
            /* linkedPlanet = */ null,
            /* orbitCenter = */ star,
            /* orbitStartAngle = */ 225f,
            /* orbitRadius = */ 9000f,
            /* orbitDays = */ systemOrbitDays
        )

        // Create gas giant jump point and add matching jump point sides in hyperspace.
        // If you don't call this, the hyperspace side isn't generated and also it crashes when you hover them.
        system.autogenerateHyperspaceJumpPoints(
            true, false, false
        )

        // Place system
        if (!system.placeInSector(
                startAtHyperspaceLocation = Misc.getFactionMarkets(Factions.TRITACHYON).firstOrNull()?.locationInHyperspace
                    ?: game.sector.playerFleet.locationInHyperspace
            )
        ) {
            game.logger.e { "Catastrophic failure! Unable to find somewhere to place Lama! Quest cannot continue." }
            return null
        }

        // Pave the hyperspace clouds
        MagicCampaign.hyperspaceCleanup(system)

        system.updateAllOrbits()

        game.logger.i { "Placed Lama at ${system.location.x}, ${system.location.y} in the ${system.constellation?.name} constellation." }

        return menri
    }
}