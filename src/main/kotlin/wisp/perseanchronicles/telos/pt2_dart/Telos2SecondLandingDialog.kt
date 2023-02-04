package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.perseanchronicles.telos.pt2_dart.battle.Telos2BattleCoordinator
import wisp.perseanchronicles.telos.pt3_arrow.Telos3HubMission
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos2SecondLandingDialog(
    stageJson: JSONObject =
        if (Telos2HubMission.choices.injectedSelf == true) {
            Telos2HubMission.part2Json.query("/stages/landOnPlanetSecondEther")
        } else {
            Telos2HubMission.part2Json.query("/stages/landOnPlanetSecondNoEther")
        },
    mission: Telos2HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic<Telos2SecondLandingDialog>(
    onInteractionStarted = {

    },
    people = { listOfNotNull(DragonsHubMission.karengo) },
    pages = PagesFromJson(
        pagesJson = stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "1" to {
                TelosCommon.playThemeMusic()
            },
            "7-vara" to {
                // Resume music
                TelosCommon.playThemeMusic()
            },
            "4-noPsi" to {
                // You linger for a moment, thinking. You know the Church of Ludd well,
                //              and recall an old firebrand named Eugel. Could it be the same man?\n\nYou shake your head and
                //              follow Karengo to the hanger.
                val page = navigator.currentPage()?.extraData!!
                if (game.sector.playerFaction.isAtWorst(Factions.LUDDIC_CHURCH, RepLevel.COOPERATIVE)) {
                    para { page["ludd-friendly"] as String }
                    para { page["ludd-friendly2"] as String }
                }
            },
            "6-ask" to {
                // You linger for a moment, thinking. You know the Church of Ludd well,
                //              and recall an old firebrand named Eugel. Could it be the same man?\n\nYou shake your head and
                //              follow Karengo to the hanger.
                val page = navigator.currentPage()?.extraData!!
                if (game.sector.playerFaction.isAtWorst(Factions.LUDDIC_CHURCH, RepLevel.COOPERATIVE)) {
                    para { page["ludd-friendly"] as String }
                    para { page["ludd-friendly2"] as String }
                }
            },
            "5-noPsi" to {
                // Resume music
                TelosCommon.playThemeMusic()
            },
            // Manually show text based upon conditions.
            "9-check-karengo" to {
                val page = navigator.currentPage()?.extraData!!

                if (game.sector.playerPerson.gender == FullName.Gender.FEMALE) {
                    // how do i turn off this ship sense/feeling  i can feel you sitting on the couch not that i mind but it’s distracting
                    para { page["shipSense1-female"] as String }
                } else {
                    // how do i turn off this ship sense/feeling  i can feel you sitting on that couch and i’d rather not
                    para { page["shipSense1-notFemale"] as String }
                }

                // The ship shows you how to limit the integration, but you can’t help but wonder what it would have felt like to be aboard at the height of the Telos.
                para { page["shipSense2"] as String }
            },
            // Without Ether
            "7.1-noPsi" to {
                completeMission(mission)
            },
            // With Ether
            "12-leave" to {
                completeMission(mission)
            },
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "startBattle" -> option.copy(
                        onOptionSelected = {
                            Telos2BattleCoordinator.startBattle()
                            mission.setCurrentStage(Telos2HubMission.Stage.PostBattle, this.dialog, null)
                            if (Telos2HubMission.choices.injectedSelf == true) {
                                navigator.goToPage("6-finished-battle")
                            } else {
                                navigator.goToPage("3-noPsi")
                            }
                        }
                    )

                    "leave" -> option.copy(
                        onOptionSelected = {
                            game.soundPlayer.setSuspendDefaultMusicPlayback(false)
                            TelosCommon.stopAllCustomMusic()
                            navigator.close(doNotOfferAgain = true)
                        }
                    )

                    else -> option
                }
            }
            // TODO check out FronSecWSEidolonOpen.java
        }
    )
) {
    private fun completeMission(mission: Telos2HubMission) {
        mission.setCurrentStage(Telos2HubMission.Stage.Completed, dialog, null)

        Telos3HubMission().apply {
            if (create(null, false))
                accept(dialog, null)
        }
    }
}