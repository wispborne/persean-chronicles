package wisp.perseanchronicles.laborer

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignClockAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import wisp.perseanchronicles.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.spriteName
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.wispLib.*

object LaborerQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "laborerStage", defaultValue = { Stage.NotStarted }),
    autoBarEventInfo = AutoBarEventInfo(
        barEventCreator = Laborer_Stage1_BarEventCreator(),
        shouldGenerateBarEvent = { true },
        shouldOfferFromMarket = { market ->
            market.factionId.toLowerCase() in listOf(Factions.INDEPENDENT.toLowerCase())
                    && market.starSystem != null // No prism freeport
                    && market.size > 2
                    && market.hasIndustry(Industries.MINING)
                    && LaborerQuest.state.destPlanet != null
        }
    ),
    autoIntelInfo = AutoIntelInfo(LaborerIntel::class.java) {
        LaborerIntel(LaborerQuest.state.startLocation, LaborerQuest.state.destPlanet)
    }
) {
    val portrait = InteractionDefinition.Portrait("wisp_perseanchronicles_laborer", "portrait")

    val state = State(PersistentMapData<String, Any?>(key = "laborerState").withDefault { null })

    class State(val map: MutableMap<String, Any?>) {
        var startDate: Long? by map
        var startLocation: SectorEntityToken? by map
        var destPlanet: SectorEntityToken? by map
        var payout: Int by map.setDefault { calculatePayout() }

        val destSystem: StarSystemAPI?
            get() = destPlanet?.starSystem
    }

    val choices: Choices =
        Choices(PersistentMapData<String, Any?>(key = "laborerChoices").withDefault { null })

    class Choices(val map: MutableMap<String, Any?>) {
        var askedAllWorkDriedUp by map
        var askedHowDoIKnowYoullPay by map
    }

    val dale: PersonAPI by lazy {
        Global.getSettings().createPerson().apply {
            this.name = FullName("Dale", String.empty, FullName.Gender.MALE)
            this.setFaction(Factions.INDEPENDENT)
            this.postId = Ranks.CITIZEN
            this.rankId = Ranks.CITIZEN
            this.portraitSprite = portrait.spriteName(game)
        }
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["laborerDestPlanet"] = { state.destPlanet?.name }
        text.globalReplacementGetters["laborerDestSystem"] = { state.destSystem?.name }
        text.globalReplacementGetters["laborerCredits"] = { Misc.getDGSCredits(state.payout.toFloat()) }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        state.startLocation = interactionTarget

        state.destPlanet = game.sector.starSystemsAllowedForQuests
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
        state.startDate = game.sector.clock.timestamp
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
        game.sector.playerFleet.cargo.credits.add(state.payout.toFloat())
        getShownIntel()?.sendUpdateIfPlayerHasIntel(null, false)

        if (game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.removeScript(PayoutScript(game.sector.clock))
        }
    }

    private fun calculatePayout(): Int =
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

        state.map.clear()
        stage = Stage.NotStarted

        if (game.sector.hasScript(PayoutScript::class.java)) {
            game.sector.removeScript(PayoutScript(game.sector.clock))
        }
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
            clock.convertToSeconds(14f), clock.convertToSeconds(180f)
        )

        override fun isDone(): Boolean = isDone

        override fun runWhilePaused(): Boolean = false

        override fun advance(amount: Float) {
            intervalUtil.advance(amount)

            if (intervalUtil.intervalElapsed() && !isDone) {
                payPlayer()
                isDone = true
            }
        }
    }
}