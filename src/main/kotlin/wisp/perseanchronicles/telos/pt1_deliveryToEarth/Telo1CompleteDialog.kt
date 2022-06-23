package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query

class Telo1CompleteDialog(
    stageJson: JSONObject = Telos1HubMission.part1Json.query("/stages/deliveryDropoff")
) : InteractionDialogLogic<Telo1CompleteDialog>(
    onInteractionStarted = {

    },
    pages = PagesFromJson(
        pagesJson = stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "2" to {
                this.dialog.visualPanel.showPersonInfo(DragonsQuest.karengo)
            }
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "close" -> option.copy(
                        onOptionSelected = {
                            // Start Part 2 on finishing dialog.
                            Telos2HubMission().apply {
                                if (create(null, false))
                                    accept(null, null)
                            }
                            it.close(doNotOfferAgain = true)
                        })
                    else -> option
                }
            }
        }
    ),
)