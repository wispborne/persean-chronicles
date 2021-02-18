package org.wisp.stories.laborer

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.campaign.CampaignClockAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.wisp.stories.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.starSystemsNotOnBlacklist
import wisp.questgiver.wispLib.*
import kotlin.random.Random

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

    var payout: Int by PersistentData("laborerPayout") { calculatePayment() }

    val choices: Choices =
        Choices(PersistentMapData<String, Any?>(key = "laborerChoices").withDefault { null })

    class Choices(val map: MutableMap<String, Any?>) {
        var askedAllWorkDriedUp by map
        var askedHowDoIKnowYoullPay by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["laborerDestPlanet"] = { destPlanet?.name }
        text.globalReplacementGetters["laborerDestSystem"] = { destSystem?.name }
        text.globalReplacementGetters["laborerCredits"] = { Misc.getDGSCredits(payout.toFloat()) }
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

        if (!game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.addScript(PayoutScript(game.sector.clock))
        }
    }

    fun payPlayer() {
        stage = Stage.Paid
        game.sector.playerFleet.cargo.credits.add(payout.toFloat())
        getShownIntel()?.sendUpdateIfPlayerHasIntel(null, false)

        if (game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.removeScript(PayoutScript(game.sector.clock))
        }
    }

    private fun calculatePayment(): Int =
        WeightedRandomPicker<Int>()
            .apply {
                this.add(100000, 1f) // 4.88%
                this.add(80000, 1.5f) // 7.32%
                this.add(50000, 2f) // 9.76%
                this.add(30000, 3f) // 14.63%
                this.add(10000, 4f) // 19.51%
                this.add(5000, 4f) // 19.51%
                this.add(1000, 4f) // 19.51%
                this.add(0, 2f) // 9.76%
            }
            .pick()

    fun restartQuest() {
        game.logger.i { "Restarting Laborer quest." }

        startDate = null
        startLocation = null
        destPlanet = null
        payout = calculatePayment()
        stage = Stage.NotStarted
    }

    abstract class Stage(progress: Progress) : AutoQuestFacilitator.Stage(progress) {
        object NotStarted : Stage(Progress.NotStarted)
        object GoToPlanet : Stage(Progress.InProgress)
        object Completed : Stage(Progress.Completed)
        object Paid : Stage(Progress.Completed)
    }

    class PayoutScript(clock: CampaignClockAPI) : EveryFrameScript {
        private var isDone = false
        val intervalUtil = IntervalUtil(
//            clock.convertToSeconds(14f), clock.convertToSeconds(365f)
            clock.convertToSeconds(1f), clock.convertToSeconds(1f)
        )

        override fun isDone(): Boolean = isDone

        override fun runWhilePaused(): Boolean = false

        override fun advance(amount: Float) {
            intervalUtil.advance(amount)

            if (intervalUtil.intervalElapsed()) {
                payPlayer()
                isDone = true
            }
        }
    }
}