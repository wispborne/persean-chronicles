package wisp.perseanchronicles.telos.pt2_dart

import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.game
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos2SecondLandingDialog(
    stageJson: JSONObject =
        if (Telos2HubMission.choices.injectedSelf == true) {
            Telos2HubMission.part2Json.query("/stages/landOnPlanetSecondPsicon")
        } else {
            Telos2HubMission.part2Json.query("/stages/landOnPlanetSecondNoPsicon")
        },
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
                    "startBattle" -> option.copy(
                        onOptionSelected = {
                            Telos2Battle.startBattle()
                        }
                    )
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