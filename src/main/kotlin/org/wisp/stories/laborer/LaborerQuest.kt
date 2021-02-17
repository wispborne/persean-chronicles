package org.wisp.stories.laborer

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import org.wisp.stories.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.starSystemsNotOnBlacklist
import wisp.questgiver.wispLib.*

object LaborerQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "laborerStage", defaultValue = { Stage.NotStarted }),
    autoBarEvent = AutoBarEvent(Laborer_Stage1_BarEventCreator()) { market ->
        market.factionId.toLowerCase() in listOf(Factions.INDEPENDENT.toLowerCase())
                && market.size > 2
                && market.hasIndustry(Industries.MINING)
                && LaborerQuest.destPlanet != null
    },
    autoIntel = AutoIntel(LaborerIntel::class.java) {
        LaborerIntel(LaborerQuest.startLocation!!, LaborerQuest.destPlanet!!)
    }
) {
    val portrait = InteractionDefinition.Portrait("wisp_perseanchronicles_laborer", "portrait")

    var startDate: Long? by PersistentNullableData("laborerStartDate")
        private set

    var startLocation: SectorEntityToken? by PersistentNullableData("laborerStartLocation")
        private set

    var destPlanet: SectorEntityToken? by PersistentNullableData("laborerDestPlanet")
        private set

    val destSystem: StarSystemAPI?
        get() = destPlanet?.starSystem

    val choices: Choices =
        Choices(PersistentMapData<String, Any?>(key = "laborerChoices").withDefault { null })

    class Choices(val map: MutableMap<String, Any?>) {
        var askedAllWorkDriedUp by map
        var askedHowDoIKnowYoullPay by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["laborerDestPlanet"] = { destPlanet?.name }
        text.globalReplacementGetters["laborerDestSystem"] = { destSystem?.name }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        this.startLocation = interactionTarget

        destPlanet = game.sector.starSystemsNotOnBlacklist
            .filter { it.distanceFromPlayerInHyperspace > 3f }
            .flatMap { it.solidPlanets }
            .filter { planet ->
                (planet.faction?.isHostileTo(game.sector.playerFaction) != true)
                        && planet.market?.factionId?.toLowerCase() !in listOf("luddic_church", "luddic_path")
                        && planet.market?.hasIndustry(Industries.MINING) == true
                        && planet.market.size > 2
            }
            .ifEmpty { null }
            ?.random()
            ?: kotlin.run {
                game.logger.i { "No planet found for Laborer quest." }
                return
            }
    }

    fun start(startLocation: SectorEntityToken) {
        game.logger.i { "Laborer start location set to ${startLocation.fullName} in ${startLocation.starSystem.baseName}" }
        stage = Stage.GoToPlanet
    }

    fun shouldShowStage2Dialog() = stage == Stage.GoToPlanet

    fun complete() {
        stage = Stage.Completed
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
    }
}