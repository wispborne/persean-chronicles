package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos2FirstLandingDialog(
    stageJson: JSONObject = Telos2HubMission.part2Json.query("/stages/landOnPlanetFirst"),
    mission: Telos2HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic<Telos2FirstLandingDialog>(
    onInteractionStarted = null,
    people = { listOfNotNull(DragonsHubMission.karengo) },
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "1" to {
                TelosCommon.playThemeMusic()
            },
            "1.6" to {
                dialog.visualPanel.showImagePortion(
                    IInteractionLogic.Illustration(
                        category = "wisp_perseanchronicles_telos",
                        id = "chapel"
                    )
                )
            },
            "11.1" to {
                mission.setCurrentStage(Telos2HubMission.Stage.LandOnPlanetSecondEther, this.dialog, null)
            },
            "12.2" to {
                mission.setCurrentStage(Telos2HubMission.Stage.LandOnPlanetSecondNoEther, this.dialog, null)
            }
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "requestMoreInfo" -> option.copy(
                        showIf = { Telos2HubMission.choices.askedForMoreEtherInfo == null },
                        onOptionSelected = {
                            Telos2HubMission.choices.askedForMoreEtherInfo = true
                        })

                    "afterYou" -> option.copy(
                        showIf = { Telos2HubMission.choices.toldKarengoToTakeEtherFirst == null },
                        onOptionSelected = {
                            Telos2HubMission.choices.toldKarengoToTakeEtherFirst = true
                        })

                    "injectSelf" -> option.copy(
                        onOptionSelected = {
                            Telos2HubMission.choices.injectedSelf = true
                        })

                    "noInject" -> option.copy(
                        text = if (Misc.random.nextFloat() > 0.98f) {
                            { """"Holy shit, no."""" } // 2% chance lol
                        } else {
                            option.text
                        },
                        showIf = { Telos2HubMission.choices.toldKarengoToTakeEtherFirst == true },
                        onOptionSelected = {
                            Telos2HubMission.choices.injectedSelf = false
                        }
                    )

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