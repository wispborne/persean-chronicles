package wisp.perseanchronicles.telos.pt2_dart.battle

import org.json.JSONArray
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.v2.CustomFleetInteractionDialogPlugin
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query

class Telos2PirateFleetInteractionDialogPluginImpl :
    CustomFleetInteractionDialogPlugin<Telos2PirateFleetInteractionDialogPluginImpl.BattleCommsInteractionDialog>() {
    override fun createCustomDialogLogic() = BattleCommsInteractionDialog(this)

    fun crippleEnemyFleet() {
        for (member in otherFleet.fleetData.membersListCopy) {
            val deployCost: Float = member.deployCost
            val harryCost = deployCost * 2f
            member.repairTracker.applyCREvent(-harryCost, "engaged while u-turning")
        }
    }

    class BattleCommsInteractionDialog(
        private val parentDialog: Telos2PirateFleetInteractionDialogPluginImpl,
        val json: JSONArray = TelosCommon.readJson()
            .query("/wisp_perseanchronicles/telos/part1_deliveryToEarth/stages/pirateComms/pages")
    ) : InteractionDialogLogic() {

        override fun pages() = object : PagesFromJson<BattleCommsInteractionDialog>() {
            override fun pagesJson() = json
            override fun onPageShownHandlersByPageId() = emptyMap<String, () -> Unit>()
            override fun optionConfigurator() = { options: List<IInteractionLogic.Option<BattleCommsInteractionDialog>> ->
                options.map { option ->
                    when (option.id) {
                        "closeComms" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                parentDialog.optionSelected(null, OptionId.CUT_COMM)
                            })

                        "startPirateBattleWithAdvantage" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                Telos2HubMission.state.talkedToPirateFleet = true
                                parentDialog.crippleEnemyFleet()
                                parentDialog.optionSelected(null, OptionId.ENGAGE)
                            })

                        else -> option
                    }
                }
            }
        }

        override fun firstPageSelector() =
            if (Telos2HubMission.state.talkedToPirateFleet == true) {
                pages.single { it.id == "0-already-talked" }
            } else {
                Telos2HubMission.state.talkedToPirateFleet = true
                pages.first()
            }
    }
}
