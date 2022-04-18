package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.starSystemsAllowedForQuests
import wisp.questgiver.wispLib.*
import kotlin.math.roundToInt

object RileyQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "rileyStage", defaultValue = { Stage.NotStarted }),
    autoIntelInfo = AutoIntelInfo(RileyIntel::class.java) {
        RileyIntel(
            startLocation = RileyQuest.state.startLocation,
            endLocation = RileyQuest.state.destinationPlanet
        )
    },
    autoBarEventInfo = AutoBarEventInfo(
        barEventCreator = Riley_Stage1_BarEventCreator(),
        shouldGenerateBarEvent = { true },
        shouldOfferFromMarket = { market ->
            market.size > 5 // Lives on a populous world
                    && market.starSystem != null // No prism freeport
                    && market.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")
                    && market.connectedEntities.none { it?.id == RileyQuest.state.destinationPlanet?.id }
                    && RileyQuest.state.destinationPlanet != null
        }
    )
) {
    const val REWARD_CREDITS = 80000
    const val BOUNTY_CREDITS = 20000
    const val TIME_LIMIT_DAYS = 30
    const val DAYS_UNTIL_DIALOG = 3

    // Both Hegemony and VIC would have cause to work on subservient AI
    private val govtsSponsoringSafeAi = listOf(Factions.HEGEMONY, "vic")
    val icon = InteractionDefinition.Portrait(category = "wispStories_riley", id = "icon")

    val isFatherWorkingWithGovt: Boolean
        get() = state.destinationPlanet?.faction?.id?.toLowerCase() in govtsSponsoringSafeAi

    val state = State(PersistentMapData<String, Any?>(key = "rileyState").withDefault { null })
    val choices: Choices = Choices(PersistentMapData<String, Any?>(key = "rileyChoices").withDefault { null })

    class State(val map: MutableMap<String, Any?>) {
        var startDate: Long? by map
        var startLocation: SectorEntityToken? by map
        var destinationPlanet: SectorEntityToken? by map
    }

    class Choices(val map: MutableMap<String, Any?>) {
        var askedWhyNotBuyOwnShip by map
        var refusedPayment by map
        var askedAboutDJingPay by map
        var visitedFather by map
        var movedCloserToRiley by map
        var heldRiley by map
        var askedIfLegal by map
        var askedWhatRileyThinks by map
        var triedToConvinceToJoinYou by map
        var leftRileyWithFather by map
        var destroyedTheCore by map
        var turnedInForABounty by map
    }

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["rileyDestPlanet"] = { state.destinationPlanet?.name }
        text.globalReplacementGetters["rileyCredits"] = { Misc.getDGSCredits(REWARD_CREDITS.toFloat()) }
        text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        text.globalReplacementGetters["rileyDestSystem"] = { state.destinationPlanet?.starSystem?.name }
        text.globalReplacementGetters["rileyDestPlanetDistanceLY"] = {
            if (state.destinationPlanet == null) String.empty
            else state.destinationPlanet?.starSystem?.let { dest ->
                state.startLocation?.starSystem?.distanceFrom(dest)
                    ?.roundToInt()
                    ?.coerceAtLeast(1)
                    .toString()
            }
        }
        text.globalReplacementGetters["rileyDestPlanetControllingFaction"] =
            { state.destinationPlanet?.faction?.displayNameWithArticle }
        text.globalReplacementGetters["rileyOriginPlanet"] = { state.startLocation?.name }
        text.globalReplacementGetters["rileyBountyCredits"] = { Misc.getDGSCredits(BOUNTY_CREDITS.toFloat()) }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        state.startLocation = interactionTarget
        findAndTagNewDestinationPlanet(interactionTarget)
    }

    /**
     * On player accepting the quest.
     */
    fun start(startingEntity: SectorEntityToken) {
        game.logger.i { "Riley start planet set to ${startingEntity.fullName} in ${startingEntity.starSystem.baseName}" }
        state.startLocation = startingEntity
        stage = Stage.InitialTraveling
        state.startDate = game.sector.clock.timestamp
        game.sector.addScript(Riley_Stage2_TriggerDialogScript())
        game.sector.addListener(EnteredDestinationSystemListener())

    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagNewDestinationPlanet(startEntity: SectorEntityToken) {
        state.destinationPlanet =
            game.sector.starSystemsAllowedForQuests
                .sortedByDescending { it.center.distanceFrom(startEntity.starSystem?.center ?: startEntity) }
                .filter { it.id != startEntity.starSystem?.id }
                .flatMap { it.solidPlanets }
                .prefer { it.market?.factionId?.toLowerCase() in govtsSponsoringSafeAi }
                .prefer { (it.market?.size ?: 0) > 2 }
                .getNonHostileOnlyIfPossible()
                .take(5)
                .ifEmpty { null }
                ?.random()
                .also { planet ->
                    game.logger.i { "Riley destination planet set to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }
                }
    }

    fun showDaysPassedDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage2_Dialog().build(), game.sector.playerFleet)
    }

    fun startStage3() {
        stage = Stage.TravellingToSystem
    }

    fun showEnteredDestSystemDialog() {
        game.sector.campaignUI.showInteractionDialog(Riley_Stage3_Dialog().build(), game.sector.playerFleet)
    }

    fun startStage4() {
        stage = Stage.LandingOnPlanet
    }

    fun complete() {
        stage = Stage.Completed

        if (choices.refusedPayment != true) {
            game.sector.playerFleet.cargo.credits.add(REWARD_CREDITS.toFloat())
        }
    }

    fun restartQuest() {
        game.logger.i { "Restarting Riley quest." }

        state.map.clear()
        choices.map.clear()
        stage = Stage.NotStarted
    }

    abstract class Stage(progress: Progress) : AutoQuestFacilitator.Stage(progress) {
        object NotStarted : Stage(Progress.NotStarted)
        object InitialTraveling : Stage(Progress.InProgress)
        object TravellingToSystem : Stage(Progress.InProgress)
        object LandingOnPlanet : Stage(Progress.InProgress)
        object Completed : Stage(Progress.Completed)
    }
}