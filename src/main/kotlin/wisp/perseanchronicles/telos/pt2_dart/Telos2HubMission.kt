package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.json.query
import wisp.questgiver.spriteName
import wisp.questgiver.v2.QGHubMission
import wisp.questgiver.wispLib.PersistentMapData
import wisp.questgiver.wispLib.Text
import wisp.questgiver.wispLib.qgFormat
import wisp.questgiver.wispLib.trigger

class Telos2HubMission : QGHubMission() {
    companion object {
        val MISSION_ID = "telosPt2"

        val json: JSONObject by lazy {
            Global.getSettings().getMergedJSONForMod("data/strings/telos.hjson", MOD_ID)
                .query("/$MOD_ID/telos/part2_dart") as JSONObject
        }

        val tags = listOf(Tags.INTEL_STORY, Tags.INTEL_ACCEPTED)

        val state = State(PersistentMapData<String, Any?>(key = "telosState").withDefault { null })
        val badFleetDefeatTrigger = "\$wisp_perseanchronicles_telosPt2_badfleetdefeated"
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
//        if (!isEnabled) return false

        // Ignore warning, there are two overrides and it's complaining about just one of them.
        @Suppress("ABSTRACT_SUPER_CALL_WARNING")
        super.create(createdAt, barEvent)
        setGenRandom(Telos1HubMission.seed)

        setStartingStage(Stage.DestroyFleet)
        setSuccessStage(Stage.Completed)

        name = json.query("/strings/intel/title") as String

        // todo change me
        setIconName(InteractionDefinition.Portrait(category = "intel", id = "red_planet").spriteName(game))

        val badFleetFlag = "$${MOD_ID}_${MISSION_ID}_badfleet"

        trigger {
            beginStageTrigger(Stage.DestroyFleet)
            triggerCreateFleet(
                FleetSize.MEDIUM,
                FleetQuality.LOWER,
                Factions.PIRATES,
                FleetTypes.SCAVENGER_MEDIUM,
                Telos1HubMission.state.karengoPlanet
            )
            triggerMakeHostileAndAggressive()
            triggerAutoAdjustFleetStrengthModerate()
            triggerPickLocationAroundEntity(Telos1HubMission.state.karengoPlanet, 1f)
            triggerSpawnFleetAtPickedLocation()
            triggerFleetMakeImportant(badFleetFlag, Stage.DestroyFleet)
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
            thisExt.setCurrentStage(Stage.LandOnPlanetFirst, null, null)
            return true
        }

        return super.callAction(action, ruleId, dialog, params, memoryMap)
    }

    override fun acceptImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.acceptImpl(dialog, memoryMap)

        state.startDateMillis = game.sector.clock.timestamp
        thisExt.setCurrentStage(Stage.DestroyFleet, null, null)
        makePrimaryObjective(Telos1HubMission.state.karengoPlanet)
        thisExt.makeImportant(Telos1HubMission.state.karengoPlanet, null, Stage.DestroyFleet)
    }

    override fun endSuccessImpl(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.endSuccessImpl(dialog, memoryMap)

//        thisExt.setCurrentStage(Stage.Completed, dialog, memoryMap) goes in interaction dialog
        state.completeDateInMillis = game.sector.clock.timestamp
    }

    override fun endAbandonImpl() {
        super.endAbandonImpl()
        game.logger.i { "Restarting ${this.name} quest." }

        state.map.clear()
        currentStage = null
    }

    override fun getNextStepText(): String? {
        return when (currentStage) {
            Stage.DestroyFleet -> "Destroy the fleet around \${telosPt1Stg1DestPlanet} in \${telosPt1Stg1DestSystem}.".qgFormat()
            Stage.LandOnPlanetFirst -> "Land on \${telosPt1Stg1DestPlanet} in \${telosPt1Stg1DestSystem}.".qgFormat()
            else -> null
        }
    }

    enum class Stage {
        DestroyFleet,
        LandOnPlanetFirst,
        LandOnPlanetSecond,
        Completed,
    }
}