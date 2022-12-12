package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.characters.FullName
import org.json.JSONArray
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt2_dart.battle.Telos2Battle
import wisp.perseanchronicles.telos.pt3_arrow.Telos3HubMission
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.map

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
    people = { listOfNotNull(DragonsHubMission.karengo) },
    pages = PagesFromJson(
        pagesJson = stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "7-noPsi" to {

            },
            "7.1-noPsi" to {
                mission.setCurrentStage(Telos2HubMission.Stage.Completed, dialog, null)

                Telos3HubMission().apply {
                    if (create(null, false))
                        accept(dialog, null)
                }
            },
            // Manually show text based upon conditions.
            "7.1-psi" to {
                val page = stageJson.query<JSONArray>("/pages")
                    .map<Any, JSONObject> { it as JSONObject }
                    .firstOrNull {
                        it.optString("id") == "7.1-psi"
                    }

                if (page != null) {
                    // (if not female)
                    // how do i turn off this ship sense/feeling  i can feel you sitting on that couch and i’d rather not
                    // (if female)
                    // how do i turn off this ship sense/feeling  i can feel you sitting on the couch not that i mind but it’s distracting
                    val (l, r) = page.optString("freeText1").split("|")

                    if (game.sector.playerPerson.gender != FullName.Gender.FEMALE) {
                        para { l }
                    } else {
                        para { r }
                    }

                    // The ship shows you how to limit the integration, but you can’t help but wonder what it would have felt like to be aboard at the height of the Telos.
                    para { page.optString("freeText2") }
                }
            },
            "8-psi" to {
                mission.setCurrentStage(Telos2HubMission.Stage.Completed, dialog, null)

                Telos3HubMission().apply {
                    if (create(null, false))
                        accept(dialog, null)
                }
            },
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