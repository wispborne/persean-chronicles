package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.game
import wisp.questgiver.AutoQuestFacilitator
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.starSystemsNotOnBlacklist
import wisp.questgiver.wispLib.*
import kotlin.math.roundToInt

object RileyQuest : AutoQuestFacilitator(
    stageBackingField = PersistentData(key = "rileyStage", defaultValue = { Stage.NotStarted }),
    autoIntel = AutoIntel(RileyIntel::class.java) {
        RileyIntel(
            startLocation = RileyQuest.startLocation!!,
            endLocation = RileyQuest.destinationPlanet!!
        )
    },
    autoBarEvent = AutoBarEvent(
        barEventCreator = Riley_Stage1_BarEventCreator(),
        shouldOfferFromMarket = { market ->
            market.size > 5 // Lives on a populous world
                    && market.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")
                    && market.connectedEntities.none { it?.id == RileyQuest.destinationPlanet?.id }
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

    var startDate: Long? by PersistentNullableData("rileyStartDate")
        private set

    var startLocation: SectorEntityToken? by PersistentNullableData("rileyStartPlanet")
        private set

    var destinationPlanet: SectorEntityToken? by PersistentNullableData("rileyDestinationPlanet")
        private set

    val choices: Choices = Choices(PersistentMapData<String, Any?>(key = "rileyChoices").withDefault { null })

    val isFatherWorkingWithGovt: Boolean
        get() = destinationPlanet?.faction?.id?.toLowerCase() in govtsSponsoringSafeAi

    /**
     * All choices that can be made.
     * Leave `map` public and accessible so it can be cleared if the quest is restarted.
     */
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
        text.globalReplacementGetters["rileyDestPlanet"] = { destinationPlanet?.name }
        text.globalReplacementGetters["rileyCredits"] = { Misc.getDGSCredits(REWARD_CREDITS.toFloat()) }
        text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        text.globalReplacementGetters["rileyDestSystem"] = { destinationPlanet?.starSystem?.name }
        text.globalReplacementGetters["rileyDestPlanetDistanceLY"] = {
            if (destinationPlanet == null) String.empty
            else startLocation?.starSystem?.distanceFrom(destinationPlanet!!.starSystem)
                ?.roundToInt()
                ?.coerceAtLeast(1)
                .toString()
        }
        text.globalReplacementGetters["rileyDestPlanetControllingFaction"] =
            { destinationPlanet?.faction?.displayNameWithArticle }
        text.globalReplacementGetters["rileyOriginPlanet"] = { startLocation?.name }
        text.globalReplacementGetters["rileyBountyCredits"] = { Misc.getDGSCredits(BOUNTY_CREDITS.toFloat()) }
    }

    override fun regenerateQuest(interactionTarget: SectorEntityToken, market: MarketAPI?) {
        startLocation = interactionTarget
        findAndTagNewDestinationPlanet(interactionTarget)
    }

    /**
     * On player accepting the quest.
     */
    fun start(startingEntity: SectorEntityToken) {
        game.logger.i { "Riley start planet set to ${startingEntity.fullName} in ${startingEntity.starSystem.baseName}" }
        startLocation = startingEntity
        stage = Stage.InitialTraveling
        startDate = game.sector.clock.timestamp
        game.sector.addScript(Riley_Stage2_TriggerDialogScript())
        game.sector.addListener(EnteredDestinationSystemListener())
    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagNewDestinationPlanet(startEntity: SectorEntityToken) {
        destinationPlanet =
            game.sector.starSystemsNotOnBlacklist
                .sortedByDescending { it.distanceFrom(startEntity.starSystem) }
                .filter { it.id != startEntity.starSystem?.id }
                .flatMap { it.solidPlanets }
                .prefer { it.market?.factionId?.toLowerCase() in govtsSponsoringSafeAi }
                .prefer { (it.market?.size ?: 0) > 2 }
                .getNonHostileOnlyIfPossible()
                .take(5)
                .random()
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

        startDate = null
        startLocation = null
        destinationPlanet = null
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