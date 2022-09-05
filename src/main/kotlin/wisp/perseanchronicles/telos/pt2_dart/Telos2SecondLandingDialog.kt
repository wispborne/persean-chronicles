package wisp.perseanchronicles.telos.pt2_dart

import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt2_dart.battle.Telos2Battle
import wisp.perseanchronicles.telos.pt3_arrow.Telos3HubMission
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
        onPageShownHandlersByPageId = mapOf(
            "7.1-noPsi" to {
                mission.setCurrentStage(Telos2HubMission.Stage.Completed, dialog, null)

                Telos3HubMission().apply {
                    if (create(null, false))
                        accept(dialog, null)
                }
            }
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "startBattle" -> option.copy(
                        onOptionSelected = {
                            Telos2Battle.startBattle()
                            mission.setCurrentStage(Telos2HubMission.Stage.PostBattle, this.dialog, null)
                            if (Telos2HubMission.choices.injectedSelf == true) {
                                navigator.goToPage("3-psi")
                            } else {
                                navigator.goToPage("3-noPsi")
                            }
                        }
                    )

                    "leave" -> option.copy(
                        onOptionSelected = {
                            mission.setCurrentStage(Telos2HubMission.Stage.Completed, this.dialog, null)
                            Telos3HubMission().apply {
                                if (create(createdAt = null, barEvent = false))
                                    accept(/* dialog = */ null, /* memoryMap = */ null)
                            }
                            navigator.close(doNotOfferAgain = true)
                        }
                    )

                    else -> option
                }
            }
            // TODO check out FronSecWSEidolonOpen.java
        }
    )
)