package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.NebulaEditor
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain.RingParams
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceTerrainPlugin
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game
import wisp.questgiver.wispLib.placeInSector

object MenriSystemCreator {

    fun createMenri(): Boolean {
        // gate
        // habitable with small rings
        // two? jump points
        // some hidey points
        val systemOrbitDays = 180f

        if (game.sector.getStarSystem("Lama") != null) {
            game.logger.w { "Lama system already exists!" }
            return true
        }

        val system = game.sector.createStarSystem("Lama")

        if (!system.placeInSector(
                startAtHyperspaceLocation = Misc.getFactionMarkets(Factions.TRITACHYON).firstOrNull()?.locationInHyperspace
                    ?: game.sector.playerFleet.locationInHyperspace
            )
        ) {
            game.logger.e { "Catastrophic failure! Unable to find somewhere to place Lama! Quest cannot continue." }
            return false
        }

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
                name = "Lama"
                applySpecChanges()
            }

        // Menri
        val menri = system.addPlanet(
            "pc_menri",
            star,
            "Menri",
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
        system.addTerrain(
            Terrain.RING,
            RingParams(
                156f,
                250f,
                menri,
                "Menri Dust Ring"
            )
        ).apply {
            setCircularOrbit(menri, 0f, 0f, systemOrbitDays)
            id = "pc_menri_ring"
        }

        // Gas Giant
        val gasGiantDal = system.addPlanet(
            "pc_Dal",
            star,
            "Dal",
            "gas_giant",
            0f,
            400f,
            5300f,
            systemOrbitDays
        )

        // Jump point A
        val jumpPointA = game.factory.createJumpPoint(
            "pc_jpA",
            "Jump Point"
        ).apply {
            setCircularOrbit(star, 90f, 10000f, systemOrbitDays)
            setStandardWormholeToHyperspaceVisual();
        }
        system.addEntity(jumpPointA)

        // Jump point B
        val jumpPointB = game.factory.createJumpPoint(
            "pc_jpB",
            "Jump Point"
        ).apply {
            setCircularOrbit(star, 160f, 9000f, systemOrbitDays)
            setStandardWormholeToHyperspaceVisual()
        }
        system.addEntity(jumpPointB)

        // Nebulae
        val hyperPlugin = Misc.getHyperspaceTerrain().plugin as HyperspaceTerrainPlugin
        val nebulaEditor = NebulaEditor(hyperPlugin)
        val minRadius: Float = hyperPlugin.tileSize * 2f
        val radius = system.maxRadiusInHyperspace
        nebulaEditor.clearArc(
            /* x = */ system.location.x,
            /* y = */ system.location.y,
            /* innerRadius = */ 0f,
            /* outerRadius = */ radius + minRadius,
            /* startAngle = */ 0f,
            /* endAngle = */ 360f
        )
        nebulaEditor.clearArc(
            /* x = */ system.location.x,
            /* y = */ system.location.y,
            /* innerRadius = */ 0f,
            /* outerRadius = */ radius + minRadius,
            /* startAngle = */ 0f,
            /* endAngle = */ 360f,
            /* noiseThresholdToClear = */ 0.25f
        )

        system.autogenerateHyperspaceJumpPoints(true, false)

        game.logger.i { "Placed Lama at ${system.location.x}, ${system.location.y} in the ${system.constellation?.name} constellation." }

        return true
    }
}