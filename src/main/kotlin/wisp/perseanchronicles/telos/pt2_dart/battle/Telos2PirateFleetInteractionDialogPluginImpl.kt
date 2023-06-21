package wisp.perseanchronicles.telos.pt2_dart.battle

import org.json.JSONArray
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.CustomFleetInteractionDialogPlugin
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
        parentDialog: Telos2PirateFleetInteractionDialogPluginImpl,
        val json: JSONArray = TelosCommon.readJson()
            .query("/wisp_perseanchronicles/telos/part1_deliveryToEarth/stages/pirateComms/pages")
    ) : InteractionDialogLogic<BattleCommsInteractionDialog>(
        pages = PagesFromJson(
            pagesJson = json,
            onPageShownHandlersByPageId = emptyMap(),
            optionConfigurator = { options ->
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
                                parentDialog.crippleEnemyFleet()
                                parentDialog.optionSelected(null, OptionId.ENGAGE)
                            })

                        else -> option
                    }
                }
            }
        )
    )
}