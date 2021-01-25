package org.wisp.stories.nirvana

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsPart1_BarEventCreator
import org.wisp.stories.dangerousGames.pt1_dragons.DragonsQuest
import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.QuestFacilitator
import wisp.questgiver.wispLib.*
import kotlin.math.roundToInt

object NirvanaQuest : QuestFacilitator() {

    val REWARD_CREDITS: Float
        get() = (3500 * (destPlanet?.let { startLocation?.distanceFrom(it) } ?: 0F).roundToInt()).toFloat()
    const val CARGO_TYPE = Commodities.HEAVY_MACHINERY
    const val CARGO_WEIGHT = 5

    val icon = InteractionDefinition.Portrait(category = "wisp_perseanchronicles_nirvana", id = "davidRengel")
    val background = InteractionDefinition.Illustration(category = "wisp_perseanchronicles_nirvana", id = "background")

    var startDate: Long? by PersistentNullableData("nirvanaStartDate")
        private set

    var startLocation: SectorEntityToken? by PersistentNullableData("nirvanaStartLocation")
        private set

    var destPlanet: SectorEntityToken? by PersistentNullableData("nirvanaDestPlanet")
        private set

    var stage: Stage by PersistentData(key = "nirvanaStage", defaultValue = { Stage.NotStarted })
        private set

    val destSystem: StarSystemAPI?
        get() = destPlanet?.starSystem

    override fun getBarEventCreator() = Nirvana_Stage1_BarEventCreator()
    override fun hasBeenStarted() = stage == Stage.NotStarted

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["nirvanaCredits"] = { Misc.getDGSCredits(REWARD_CREDITS) }
        text.globalReplacementGetters["nirvanaDestPlanet"] = { destPlanet?.name }
        text.globalReplacementGetters["nirvanaDestSystem"] = { destSystem?.name }
        text.globalReplacementGetters["nirvanaCargoTons"] = { CARGO_WEIGHT.toString() }
        text.globalReplacementGetters["nirvanaStarName"] = { destPlanet?.starSystem?.star?.name }
    }

    /**
     * Only from Independent worlds.
     */
    fun shouldMarketOfferQuest(market: MarketAPI): Boolean =
        market.factionId.toLowerCase() in listOf(Factions.INDEPENDENT)
                && market.starSystem in Utilities.getSystemsForQuestTarget() // Valid system, not blacklisted
                && market.size > 3

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

    fun shouldShowStage2Dialog() =
        stage == Stage.GoToPlanet
                && game.sector.playerFleet.cargo.getCommodityQuantity(CARGO_TYPE) >= CARGO_WEIGHT

    fun complete() {
        stage = Stage.Completed

        game.sector.playerFleet.cargo.removeCommodity(CARGO_TYPE, CARGO_WEIGHT.toFloat())
        game.sector.playerFleet.cargo.credits.add(REWARD_CREDITS)

        game.intelManager.findFirst(NirvanaIntel::class.java)
            ?.endAndNotifyPlayer()
    }

    /**
     * 55 years after quest was completed.
     */
    fun shouldShowStage3Dialog() =
        stage == Stage.Completed
                && game.sector.clock.convertToMonths(startDate?.toFloat() ?: 0f) > (12 * 55)

    fun completeSecret() {
        stage = Stage.CompletedSecret
    }

    enum class Stage {
        NotStarted,
        GoToPlanet,
        Completed,
        CompletedSecret
    }
}