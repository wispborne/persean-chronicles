package wisp.perseanchronicles.telos.pt2_dart.battle

import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import org.json.JSONArray
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query

class Telos2PirateFleetInteractionDialogPluginImpl : FleetInteractionDialogPluginImpl() {
    val dialogLogic = BattleCommsInteractionDialog(this)
    val dialogPlugin = dialogLogic.build()

    override fun optionSelected(text: String?, optionData: Any?) {
        dialogLogic.dialog = this.dialog

        when (optionData) {
            OptionId.OPEN_COMM -> {
                // From vanilla `optionSelected`
                if (text != null) {
                    //textPanel.addParagraph(text, Global.getSettings().getColor("buttonText"));
                    dialog.addOptionSelectedText(optionData)
                }

                // Wisp custom
                dialogPlugin.init(dialog)
            }

            else -> {
                // If the custom pirate comms dialog logic contains the option that was just selected, use that dialog.
                // Otherwise, forward it to the vanilla dialog.
                val doesCommsDialogHandleOption = dialogLogic.pages.flatMap { it.options }
                    .any { it.id == optionData }

                if (doesCommsDialogHandleOption) {
                    dialogPlugin.optionSelected(text, optionData)
                } else {
                    super.optionSelected(text, optionData)
                }
            }
        }

    }

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