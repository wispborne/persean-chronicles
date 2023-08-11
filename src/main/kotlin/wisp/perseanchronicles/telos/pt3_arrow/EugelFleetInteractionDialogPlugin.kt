package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Factions
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

class EugelFleetInteractionDialogPlugin(val mission: Telos3HubMission) : CustomFleetInteractionDialogPlugin<EugelFleetInteractionDialogPlugin.BattleCommsInteractionDialog>() {
    override fun createCustomDialogLogic() = BattleCommsInteractionDialog(this, mission)

    class BattleCommsInteractionDialog(
        parentDialog: EugelFleetInteractionDialogPlugin,
        val mission: Telos3HubMission,
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
                    dialog.textPanel.adjustReputationWithPlayer(PerseanChroniclesNPCs.karengo, -0.2f)
                    mission.setNoRepChanges()
                    mission.setCurrentStage(Telos3HubMission.Stage.CompletedSacrificeShips, dialog, emptyMap())
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

//                        "continueToBattleOpt" -> option.copy(
//                            disableAutomaticHandling = true,
//                            onOptionSelected = {
//                                parentDialog.optionSelected(null, OptionId.CONTINUE)
//                            })

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

// function to check if a ship is a Telos ship
fun isTelosShip(ship: FleetMemberAPI): Boolean {
    return ship.hullId == TelosCommon.VARA_ID || ship.hullId == TelosCommon.ITESH_ID || ship.hullId == TelosCommon.AVALOK_ID
}

/**
 * Removes all Telos ships from the player's fleet and storage.
 * If the player's flagship is a Telos ship, moves the player to a different ship.
 * If the player has no non-Telos ships, gives them a Kite.
 *
 * @param textPanelAPI The text panel to add the loss text to.
 */
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
        .filter { isTelosShip(it) }
        .forEach { ship ->
            // If the Telos ship to destroy is the player's flagship, move the player to a different ship.
            if (ship.captain.isPlayer) {
                val nonTelosShips = game.sector.playerFleet.fleetData.membersListCopy.filter { !isTelosShip(it) }

                if (nonTelosShips.isEmpty()) {
                    // If the player only has Telos ships, give them a Kite. Don't spend it all in one place.
                    game.sector.playerFleet.fleetData.addFleetMember(game.factory.createFleetMember(FleetMemberType.SHIP, "kite_original_Stock"))
                    game.sector.playerFleet.forceSync()
                }

                val yourNewShip = game.sector.playerFleet.fleetData.membersListCopy.first { !isTelosShip(it) }
                yourNewShip.captain = game.sector.playerPerson
                game.sector.playerFleet.fleetData.setFlagship(yourNewShip)
                game.sector.playerFleet.forceSync()
            }

            // Then, remove the Telos ship.
            game.logger.i { "Removing ${ship.id} from fleet ${ship.fleetData?.fleet?.nameWithFaction}." }
            textPanelAPI.addFleetMemberLossText(ship)
            val fleet = ship.fleetData.fleet

            if (fleet.isPlayerFleet) {
                game.sector.playerFleet.fleetData.removeFleetMember(ship)
            } else {
                // Does this actually work? It doesn't when used on the player fleet.
                // TODO check that it works on stored ships
                ship.fleetData?.removeFleetMember(ship)
            }
q
            fleet.forceSync()
            fleet.fleetData?.syncMemberLists()
        }
}