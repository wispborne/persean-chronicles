package org.wisp.stories.riley

import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.QuestFacilitator
import org.wisp.stories.dangerousGames.Utilities
import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.*
import kotlin.math.roundToInt
import kotlin.random.Random

object RileyQuest : QuestFacilitator {
    const val REWARD_CREDITS = 80000
    const val BOUNTY_CREDITS = 20000
    const val TIME_LIMIT_DAYS = 30
    const val DAYS_UNTIL_DIALOG = 3
    val govtsSponsoringSafeAi = listOf(Factions.HEGEMONY, "vic")
    val icon by lazy {
        InteractionDefinition.Image(
            category = "wispStories_riley",
            id = "icon",
            width = 128f,
            height = 128f,
            displayHeight = 128f,
            displayWidth = 128f
        )
    }

    var startDate: Long? by PersistentNullableData("rileyStartDate")
        private set

    var startLocation: SectorEntityToken? by PersistentNullableData("rileyStartPlanet")
        private set

    var destinationPlanet: SectorEntityToken? by PersistentNullableData("rileyDestinationPlanet")
        private set

    var stage: Stage by PersistentData(key = "rileyStage", defaultValue = { Stage.NotStarted })
        private set

    val isFatherWorkingWithGovt: Boolean
        get() = destinationPlanet?.faction?.id?.toLowerCase() in govtsSponsoringSafeAi

    val choices: Choices = Choices(PersistentMapData<String, Any?>(key = "rileyChoices").withDefault { null })

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

    fun shouldMarketOfferQuest(marketAPI: MarketAPI): Boolean =
        stage == Stage.NotStarted
                && marketAPI.size > 5 // Lives on a populous world
                && marketAPI.factionId.toLowerCase() !in listOf("luddic_church", "luddic_path")
                && marketAPI.starSystem in Utilities.getSystemsForQuestTarget() // Valid system, not blacklisted
                && Random.nextInt(100) < 33 // 33% chance

    override fun updateTextReplacements(text: Text) {
        text.globalReplacementGetters["rileyDestPlanet"] = { destinationPlanet?.name }
        text.globalReplacementGetters["rileyCredits"] = { Misc.getDGSCredits(REWARD_CREDITS.toFloat()) }
        text.globalReplacementGetters["rileyTimeLimitDays"] = { TIME_LIMIT_DAYS }
        text.globalReplacementGetters["rileyDestSystem"] = { destinationPlanet?.starSystem?.baseName }
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

    /**
     * On player interacting with bar event prompt. Chooses the destination planet.
     */
    fun init(startingEntity: SectorEntityToken) {
        startLocation = startingEntity
        findAndTagDestinationPlanetIfNeeded(startingEntity)
        updateTextReplacements(game.text)
    }

    /**
     * On player accepting the quest.
     */
    fun start(startingEntity: SectorEntityToken) {
        game.logger.i { "Riley start planet set to ${startingEntity.fullName} in ${startingEntity.starSystem.baseName}" }
        startLocation = startingEntity
        updateTextReplacements(game.text)
        stage = Stage.InitialTraveling
        startDate = game.sector.clock.timestamp
        game.sector.addScript(Riley_Stage2_TriggerDialogScript())
        game.sector.intelManager.addIntel(RileyIntel(startingEntity, destinationPlanet!!))
        game.sector.addListener(EnteredDestinationSystemListener())
    }

    /**
     * Randomly choose a planet that is far from starting point and owned by certain factions.
     */
    private fun findAndTagDestinationPlanetIfNeeded(startEntity: SectorEntityToken) {
        if (destinationPlanet == null) {
            val planets = Utilities.getSystemsForQuestTarget()
                .sortedByDescending { it.distanceFrom(startEntity.starSystem) }
                .flatMap<StarSystemAPI, PlanetAPI> { it.planets }

            // Both Hegemony and VIC would have cause to work on subservient AI
            destinationPlanet = planets
                .prefer { it.market?.factionId?.toLowerCase() in govtsSponsoringSafeAi }
                .getNonHostileOnlyIfPossible()
                .take(5)
                .random()
                .also { planet ->
                    game.logger.i { "Riley destination planet set to ${planet?.fullName} in ${planet?.starSystem?.baseName}" }
                }
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

        game.intelManager.findFirst(RileyIntel::class.java)
            ?.endAndNotifyPlayer()
    }

    enum class Stage {
        NotStarted,
        InitialTraveling,
        TravellingToSystem,
        LandingOnPlanet,
        Completed
    }
}