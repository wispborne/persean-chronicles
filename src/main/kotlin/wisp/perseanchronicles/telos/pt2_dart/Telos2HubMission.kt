package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.addPara
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.qgFormat
import wisp.questgiver.wispLib.trigger

class Telos2HubMission : QGHubMission() {
    companion object {
        // Hardcode because it's being used in rules.csv.
        val MISSION_ID = "wisp_perseanchronicles_telosPt2"

        val part2Json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part2_dart") as JSONObject
        }

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosState").withDefault { null })
        const val badFleetDefeatTrigger = "wisp_perseanchronicles_telosPt2_badfleetdefeated"
    }

    class State(val map: MutableMap<String, Any?>) {
        var startDateMillis: Long? by map
        var completeDateInMillis: Long? by map
    }

    init {
        missionId = MISSION_ID
    }

    override fun updateTextReplacements(text: Text) {
    }

    override fun create(createdAt: MarketAPI?, barEvent: Boolean): Boolean {
        // if already accepted by the player, abort
        if (!setGlobalReference("$$MISSION_ID")) {
            return false
        }

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.seed)

        setStartingStage(Stage.DestroyFleet)
        setSuccessStage(Stage.Completed)

        name = part2Json.query("/strings/title")

        // todo change me
        setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

        val badFleetFlag = "$${MISSION_ID}_badfleet"
        val badFleetImportantFlag = "${badFleetFlag}_important"

        trigger {
            beginStageTrigger(Stage.DestroyFleet)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.LOWER,
                Factions.PIRATES,
                FleetTypes.SCAVENGER_MEDIUM,
                Telos1HubMission.state.karengoPlanet
            )
            triggerMakeHostile()
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(Telos1HubMission.state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation(badFleetFlag, null)
            triggerOrderFleetPatrol(false, Telos1HubMission.state.karengoPlanet)
            triggerFleetMakeImportant(badFleetImportantFlag, Stage.DestroyFleet)
            triggerFleetAddDefeatTrigger(badFleetDefeatTrigger)
        }

        return true
    }

    override fun callAction(
        action: String?,
        ruleId: String?,
        dialog: InteractionDialogAPI?,
        params: MutableList<Misc.Token>?,
        memoryMap: MutableMap<String, MemoryAPI>?
    ): Boolean {
        // Trigger set in `create` (triggerFleetAddDefeatTrigger), fired off to rules.csv when fleet dies, listen for it here.
        if (action == badFleetDefeatTrigger) {
            setCurrentStage(Stage.LandOnPlanetFirst, null, null)
            return true
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap)
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        state.startDateMillis = game.sector.clock.timestamp
        setCurrentStage(Stage.DestroyFleet, null, null)
        makePrimaryObjective(Telos1HubMission.state.karengoPlanet)
        makeImportant(Telos1HubMission.state.karengoPlanet, null, Stage.DestroyFleet, Stage.LandOnPlanetFirst)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

//        setCurrentStage(Stage.Completed, dialog, memoryMap) goes in interaction dialog
        state.completeDateInMillis = game.sector.clock.timestamp
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Restarting ${this.name} quest." }

        state.map.clear()
        currentStage = null
    }

    /**
     * Bullet points on left side of intel.
     */
    override fun getNextStepText(): String? {
        return when (currentStage) {
            Stage.DestroyFleet -> "Destroy the fleet around \${telosPt1Stg1DestPlanet} in \${telosPt1Stg1DestSystem}.".qgFormat()
            Stage.LandOnPlanetFirst -> "Land on \${telosPt1Stg1DestPlanet} in \${telosPt1Stg1DestSystem}.".qgFormat()
            else -> null
        }
    }

    /**
     * Description on right side of intel.
     */
    override fun addDescriptionForCurrentStage(info: TooltipMakerAPI, width: Float, height: Float) {
        when (currentStage) {
            Stage.DestroyFleet -> {
                info.addPara { part2Json.query<String>("/stages/destroyFleet/intel/desc").qgFormat() }
            }
            Stage.LandOnPlanetFirst -> {
                info.addPara { part2Json.query<String>("/stages/landOnPlanetFirst/intel/desc").qgFormat() }
            }
        }
    }

    enum class Stage {
        DestroyFleet,
        LandOnPlanetFirst,
        LandOnPlanetSecond,
        Completed,
    }
}