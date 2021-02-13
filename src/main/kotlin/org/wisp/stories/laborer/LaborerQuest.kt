package org.wisp.stories.laborer

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import org.wisp.stories.game
import wisp.questgiver.*
import wisp.questgiver.wispLib.*

object LaborerQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "laborerStage", defaultValue = { Stage.NotStarted }),
    autoBarEvent = AutoBarEvent(Laborer_Stage1_BarEventCreator()) { market ->
        market.factionId.toLowerCase() in listOf(Factions.INDEPENDENT.toLowerCase())
                && market.size > 2
                && market.hasIndustry(Industries.MINING)
    },
    autoIntel = AutoIntel(LaborerIntel::class.java) {
        LaborerIntel(LaborerQuest.startLocation!!, LaborerQuest.destPlanet!!)
    }
) {

//    val REWARD_CREDITS: Float
//        get() = Questgiver.calculateCreditReward(startLocation, destPlanet, scaling = 1.3f)

//    val background = InteractionDefinition.Illustration(category = "wisp_perseanchronicles_laborer", id = "background")

    var startDate: Long? by PersistentNullableData("laborerStartDate")
        private set

    var startLocation: SectorEntityToken? by PersistentNullableData("laborerStartLocation")
        private set

    var destPlanet: SectorEntityToken? by PersistentNullableData("laborerDestPlanet")
        private set

    val destSystem: StarSystemAPI?
        get() = destPlanet?.starSystem

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["laborerDestPlanet"] = { destPlanet?.name }
        text.globalReplacementGetters["laborerDestSystem"] = { destSystem?.name }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        fun isValidPlanet(planet: PlanetAPI): Boolean =
            (planet.faction?.isHostileTo(game.sector.playerFaction) != true)
                    && planet.market?.factionId?.toLowerCase() !in listOf("luddic_church", "luddic_path")
                    && planet.market?.hasIndustry(Industries.MINING) == true
                    && planet.market.size > 2

        this.startLocation = interactionTarget

        destPlanet = game.sector.starSystemsNotOnBlacklist
            .filter { it.distanceFromPlayerInHyperspace > 3f }
            .flatMap { it.solidPlanets }
            .filter { isValidPlanet(it) }
            .ifEmpty { null }
            ?.random()
            ?: kotlin.run {
                game.errorReporter.reportCrash(NullPointerException("No planet found for Laborer quest."))
                return
            }
    }

    fun start(startLocation: SectorEntityToken) {
        game.logger.i { "Laborer start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        stage = Stage.GoToPlanet
    }

    fun complete() {
        stage = Stage.Completed
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

    fun restartQuest() {
        game.logger.i { "Restarting Laborer quest." }

        startDate = null
        startLocation = null
        destPlanet = null
        stage = Stage.NotStarted
    }

    abstract class Stage(progress: Progress) : AutoQuestFacilitator.Stage(progress) {
        object NotStarted : Stage(Progress.NotStarted)
        object GoToPlanet : Stage(Progress.InProgress)
        object Completed : Stage(Progress.Completed)
        object CompletedSecret : Stage(Progress.Completed)
    }
}