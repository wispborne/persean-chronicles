package wisp.perseanchronicles.telos.pt2_dart

import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.game
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos2SecondLandingDialog(
    stageJson: JSONObject = Telos2HubMission.part2Json.query("/stages/landOnPlanetSecond"),
    mission: Telos2HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic<Telos2SecondLandingDialog>(
    onInteractionStarted = {

    },
    people = { listOf(DragonsQuest.karengo) },
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = emptyMap(),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "leave" -> option.copy(
                        onOptionSelected = {
                            mission.setCurrentStage(Telos2HubMission.Stage.Completed, this.dialog, null)
                            navigator.close(doNotOfferAgain = true)
                        }
                    )
                    else -> option
                }
            }
        }
    )
)