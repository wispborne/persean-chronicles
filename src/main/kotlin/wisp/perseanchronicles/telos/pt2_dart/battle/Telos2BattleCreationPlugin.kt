package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.impl.campaign.ids.Terrain
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game
import java.awt.Color
import java.util.*

class Telos2BattleCreationPlugin() : BattleCreationPluginImpl() {
    override fun createMap(random: Random?) {
        // Vanilla sets map to half of width and height, but let's make this a small map so it doesn't last forever.
        val sizeDivider = 4
        loader.initMap(-width / sizeDivider, width / sizeDivider, -height / sizeDivider, height / sizeDivider)

        val playerFleet = context.playerFleet
        var nebulaTex: String? = null
        var nebulaMapTex: String? = null
        var inNebula = false

        var numRings = 0f

        var coronaColor: Color? = null
        val terrains = game.sector.starSystems.random().terrainCopy

        // this assumes that all nebula in a system are of the same color
        for (terrain in terrains) {
            //if (terrain.getType().equals(Terrain.NEBULA)) {
            if (terrain.plugin is NebulaTextureProvider) {
                if (terrain.plugin.containsEntity(playerFleet)) {
                    inNebula = true
                    if (terrain.plugin is NebulaTextureProvider) {
                        val provider = terrain.plugin as NebulaTextureProvider
                        nebulaTex = provider.nebulaTex
                        nebulaMapTex = provider.nebulaMapTex
                    }
                } else {
                    if (nebulaTex == null) {
                        if (terrain.plugin is NebulaTextureProvider) {
                            val provider = terrain.plugin as NebulaTextureProvider
                            nebulaTex = provider.nebulaTex
                            nebulaMapTex = provider.nebulaMapTex
                        }
                    }
                }
            } else if (terrain.plugin is StarCoronaTerrainPlugin && pulsar == null) {
                val plugin = terrain.plugin as StarCoronaTerrainPlugin
                if (plugin.containsEntity(playerFleet)) {
                    val angle = Misc.getAngleInDegrees(terrain.location, playerFleet.location)
                    var color = plugin.getAuroraColorForAngle(angle)
                    var intensity = plugin.getIntensityAtPoint(playerFleet.location)
                    intensity = 0.4f + 0.6f * intensity
                    val alpha = (80f * intensity).toInt()
                    color = Misc.setAlpha(color, alpha)
                    if (coronaColor == null || coronaColor.alpha < alpha) {
                        coronaColor = color
                        coronaIntensity = intensity
                        corona = plugin
                    }
                }
            } else if (terrain.plugin is PulsarBeamTerrainPlugin) {
                val plugin = terrain.plugin as PulsarBeamTerrainPlugin
                if (plugin.containsEntity(playerFleet)) {
                    val angle = Misc.getAngleInDegreesStrict(terrain.location, playerFleet.location)
                    var color = plugin.getPulsarColorForAngle(angle)
                    var intensity = plugin.getIntensityAtPoint(playerFleet.location)
                    intensity = 0.4f + 0.6f * intensity
                    val alpha = (80f * intensity).toInt()
                    color = Misc.setAlpha(color, alpha)
                    if (coronaColor == null || coronaColor.alpha < alpha) {
                        coronaColor = color
                        coronaIntensity = intensity
                        pulsar = plugin
                        corona = null
                    }
                }
            } else if (terrain.type == Terrain.RING) {
                if (terrain.plugin.containsEntity(playerFleet)) {
                    numRings++
                }
            }
        }
        if (nebulaTex != null) {
            loader.setNebulaTex(nebulaTex)
            loader.setNebulaMapTex(nebulaMapTex)
        }

        if (coronaColor != null) {
            loader.setBackgroundGlowColor(coronaColor)
        }

        var numNebula = 15
        if (inNebula) {
            numNebula = 100
        }
        if (!inNebula && playerFleet.isInHyperspace) {
            numNebula = 0
        }

        for (i in 0 until numNebula) {
            val x = random!!.nextFloat() * width - width / 2
            val y = random.nextFloat() * height - height / 2
            var radius = 100f + random.nextFloat() * 400f
            if (inNebula) {
                radius += 100f + 500f * random.nextFloat()
            }
            loader.addNebula(x, y, radius)
        }

        val numAsteroidsWithinRange = countNearbyAsteroids(playerFleet)

        val numAsteroids = Math.min(400, ((numAsteroidsWithinRange + 1f) * 20f).toInt())

        loader.addAsteroidField(
            0f, 0f, random!!.nextFloat() * 360f, width,
            20f, 70f, numAsteroids
        )

        if (numRings > 0) {
            var numRingAsteroids = (numRings * 300 + numRings * 600f * random.nextFloat()).toInt()
            //int numRingAsteroids = (int) (numRings * 1600 + (numRings * 600f) * (float) Math.random());
            if (numRingAsteroids > 1500) {
                numRingAsteroids = 1500
            }
            loader.addRingAsteroids(
                0f, 0f, random.nextFloat() * 360f, width,
                100f, 200f, numRingAsteroids
            )
        }

        //setRandomBackground(loader);

        //setRandomBackground(loader);
//        loader.setBackgroundSpriteName(playerFleet.containingLocation.backgroundTextureFilename)
//		loader.setBackgroundSpriteName("graphics/backgrounds/hyperspace_bg_cool.jpg");
//		loader.setBackgroundSpriteName("graphics/ships/onslaught/onslaught_base.png");

        //		loader.setBackgroundSpriteName("graphics/backgrounds/hyperspace_bg_cool.jpg");
//		loader.setBackgroundSpriteName("graphics/ships/onslaught/onslaught_base.png");
//        if (playerFleet.containingLocation === Global.getSector().hyperspace) {
//            loader.setHyperspaceMode(true)
//        } else {
        loader.setHyperspaceMode(false)
//        }

        //addMultiplePlanets();

        //addMultiplePlanets();
        addClosestPlanet()
    }
}