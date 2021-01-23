package org.wisp.stories.nirvana

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.*

object NirvanaQuest : QuestFacilitator {

    const val REWARD_CREDITS = 100000F
    const val CARGO_TYPE = Commodities.HEAVY_MACHINERY
    const val CARGO_WEIGHT = 5

    val icon: InteractionDefinition.Image by lazy {
        InteractionDefinition.Image(
            category = "wisp_perseanchronicles_nirvana",
            id = "davidRengel",
            width = 128f,
            height = 128f,
            displayHeight = 128f,
            displayWidth = 128f
        )
    }
    val background: InteractionDefinition.Image by lazy {
        InteractionDefinition.Image("wisp_perseanchronicles_nirvana", "background")
    }

    var startLocation: SectorEntityToken? by PersistentNullableData("nirvanaStartLocation")
        private set

    var destPlanet: SectorEntityToken? by PersistentNullableData("nirvanaDestPlanet")
        private set

    var stage: Stage by PersistentData(key = "nirvanaStage", defaultValue = { Stage.NotStarted })
        private set

    val destSystem: StarSystemAPI?
        get() = destPlanet?.starSystem

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["nirvanaCredits"] = { Misc.getDGSCredits(REWARD_CREDITS) }
        text.globalReplacementGetters["nirvanaDestPlanet"] = { destPlanet?.name }
        text.globalReplacementGetters["nirvanaDestSystem"] = { destSystem?.baseName }
        text.globalReplacementGetters["nirvanaCargoTons"] = { CARGO_WEIGHT.toString() }
        text.globalReplacementGetters["nirvanaStarName"] = { destPlanet?.starSystem?.star?.name }
    }

    /**
     * Only from Independent worlds.
     */
    fun shouldMarketOfferQuest(market: MarketAPI): Boolean =
        market.factionId.toLowerCase() in listOf(Factions.INDEPENDENT)

    fun init() {
        fun isValidPlanet(planet: PlanetAPI): Boolean =
            (planet.faction?.isHostileTo(game.sector.playerFaction) != true)
                    && planet.market?.factionId?.toLowerCase() !in listOf("luddic_church", "luddic_path")
                    && !planet.isGasGiant
                    && !planet.isStar

        val system = Utilities.getSystemsForQuestTarget()
            .filter { sys -> sys.star.spec.isPulsar && sys.planets.any { isValidPlanet(it) } }
            .prefer { it.distanceFromPlayerInHyperspace >= 18 } // 18+ LY away
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

    fun start(startLocation: SectorEntityToken) {
        this.startLocation = startLocation
        game.logger.i { "Nirvana start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        stage = Stage.GoToPlanet
        game.sector.intelManager.addIntel(NirvanaIntel(startLocation, destPlanet!!))
        game.sector.playerFleet.cargo.addCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())
    }

    fun doesPlayerHaveCargo() =
        game.sector.playerFleet.cargo.getCommodityQuantity(CARGO_TYPE) >= CARGO_WEIGHT

    fun complete() {
        stage = Stage.Completed

        game.sector.playerFleet.cargo.removeCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())
        game.sector.playerFleet.cargo.credits.add(REWARD_CREDITS)

        game.intelManager.findFirst(NirvanaIntel::class.java)
            ?.endAndNotifyPlayer()
    }

    enum class Stage {
        NotStarted,
        GoToPlanet,
        Completed
    }
}