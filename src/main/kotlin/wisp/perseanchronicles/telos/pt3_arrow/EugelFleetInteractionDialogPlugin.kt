package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.json.JSONArray
import org.magiclib.kotlin.addFleetMemberLossText
import org.magiclib.kotlin.adjustReputationWithPlayer
import org.magiclib.kotlin.getMarketsInLocation
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.CustomFleetInteractionDialogPlugin
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.asList

class EugelFleetInteractionDialogPlugin : CustomFleetInteractionDialogPlugin<EugelFleetInteractionDialogPlugin.BattleCommsInteractionDialog>() {
    override fun createCustomDialogLogic() = BattleCommsInteractionDialog(this)

    class BattleCommsInteractionDialog(
        parentDialog: EugelFleetInteractionDialogPlugin,
        val json: JSONArray = TelosCommon.readJson()
            .query("/wisp_perseanchronicles/telos/part3_arrow/stages/eugelDialog/pages")
    ) : InteractionDialogLogic<BattleCommsInteractionDialog>(
        firstPageSelector = {
            if (Telos3HubMission.state.talkedWithEugel == true)
                single { it.id == "already-talked" }
            else
                single { it.id == "0" }
        },
        pages = PagesFromJson(
            pagesJson = json,
            onPageShownHandlersByPageId = mapOf(
                "0" to {
                    dialog.visualPanel.showPersonInfo(PerseanChroniclesNPCs.captainEugel)
                },
                "2-luddFriend-scuttlingConfirmed" to {
                    removeAllPlayerTelosShipsInSector(dialog.textPanel)
                    Telos3HubMission.state.scuttledTelosShips = true
                },
                "2-luddFriend-scuttlingConfirmed-2" to {
                    dialog.textPanel.adjustReputationWithPlayer(Factions.LUDDIC_CHURCH, 0.1f)
                },
            ),
            optionConfigurator = { options ->
                options.map { option ->
                    when (option.id) {
                        "eugelBranch" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                if (game.sector.getFaction(Factions.LUDDIC_CHURCH).relToPlayer.rel >= 0.2f) {
                                    navigator.goToPage("2-luddFriend")
                                } else {
                                    navigator.goToPage("2-notLuddFriend")
                                }
                                Telos3HubMission.state.talkedWithEugel = true
                            }
                        )

                        "continueToBattleOpt" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                parentDialog.optionSelected(null, OptionId.CONTINUE)
                            })

                        "closeComms" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                parentDialog.optionSelected(null, OptionId.CUT_COMM)
                            })

                        "leave" -> option.copy(
                            onOptionSelected = {
                                navigator.close(doNotOfferAgain = true)
                            }
                        )

                        else -> option
                    }
                }
            }
        )
    )
    // TODO unlock an achievement for winning the battle.
}

fun removeAllPlayerTelosShipsInSector(textPanelAPI: TextPanelAPI) {
    game.sector.allLocations
        .asSequence()
        .flatMap { it.fleets }
        .filter { it.faction == game.sector.playerFaction }
        .flatMap { it.fleetData?.membersListCopy.orEmpty() }
        .plus(
            // Ships in storage
            game.sector.allLocations
                .asSequence()
                .flatMap { it.getMarketsInLocation().orEmpty() }
                .flatMap { it.submarketsCopy.orEmpty() }
                .flatMap { it.cargo?.fleetData?.membersListCopy.orEmpty() }
        )
        .filter { it.hullId == TelosCommon.VARA_ID || it.hullId == TelosCommon.ITESH_ID || it.hullId == TelosCommon.AVALOK_ID }
        .forEach {
            game.logger.i { "Removing ${it.id} from fleet ${it.fleetData?.fleet?.nameWithFaction}." }
            textPanelAPI.addFleetMemberLossText(it)
            it.fleetData?.removeFleetMember(it)
        }
}