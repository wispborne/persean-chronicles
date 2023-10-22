package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.impl.campaign.ids.Factions
import org.json.JSONArray
import org.magiclib.kotlin.adjustReputationWithPlayer
import org.magiclib.kotlin.getMarketsInLocation
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.CustomFleetInteractionDialogPlugin
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query

class EugelFleetInteractionDialogPlugin : CustomFleetInteractionDialogPlugin<EugelFleetInteractionDialogPlugin.BattleCommsInteractionDialog>() {
    override fun createCustomDialogLogic() = BattleCommsInteractionDialog(this)

    class BattleCommsInteractionDialog(
        parentDialog: EugelFleetInteractionDialogPlugin,
        val json: JSONArray = TelosCommon.readJson()
            .query("/wisp_perseanchronicles/telos/part3_arrow/stages/eugelDialog/pages")
    ) : InteractionDialogLogic<BattleCommsInteractionDialog>(
        firstPageSelector = {
            if (Telos3HubMission.state.talkedWithEugel == true)
                single { it.id == "0-already-talked" }
            else
                first()
        },
        pages = PagesFromJson(
            pagesJson = json,
            onPageShownHandlersByPageId = mapOf(
                "2-luddFriend-scuttlingConfirmed-2" to {
                    dialog.textPanel.adjustReputationWithPlayer(Factions.LUDDIC_CHURCH, 0.1f)
                },
            ),
            optionConfigurator = { options ->
                options.map { option ->
                    when (option.id) {

                        "scuttleTelosShipsOpt" -> option.copy(
                            disableAutomaticHandling = true,
                            onOptionSelected = {
                                removeAllPlayerTelosShipsInSector()
                                Telos3HubMission.state.scuttledTelosShips = true
                            })

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

                        else -> option
                    }
                }
            }
        )
    )
    // TODO unlock an achievement for winning the battle.
}

fun removeAllPlayerTelosShipsInSector() {
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
            it.fleetData?.removeFleetMember(it)
        }
}