package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.campaign.BaseCampaignEventListener
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Factions
import org.json.JSONArray
import org.json.JSONObject
import org.magiclib.kotlin.addFleetMemberGainText
import org.magiclib.kotlin.prepareShipForRecovery
import org.magiclib.kotlin.toStringList
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
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
    people = { listOfNotNull(PerseanChroniclesNPCs.karengo) },
    pages = PagesFromJson(
        pagesJson = stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "1" to {
                TelosCommon.playThemeMusic()
            },
            "3-noEther" to {
                // The simulated explosions echo briefly around the room before dying.
                val page = navigator.currentPage()?.extraData!!
                if (Telos2HubMission.state.wonRecordedBattle != true)
                    (page["non-cheater"] as JSONArray).toStringList().forEach { para { it } }
                else {
                    taunt()
                }
            },
            "4-noEther" to {
                // You linger for a moment, thinking. You know the Church of Ludd well,
                //              and recall an old firebrand named Eugel. Could it be the same man?\n\nYou shake your head and
                //              follow Karengo to the hanger.
                val page = navigator.currentPage()?.extraData!!
                if (game.sector.playerFaction.isAtWorst(Factions.LUDDIC_CHURCH, RepLevel.COOPERATIVE)) {
                    para { page["ludd-friendly"] as String }
                    para { page["ludd-friendly2"] as String }
                }
            },
            "6-finished-battle" to {
                // With a start, you come back to yourself. The emotional imprints of tearing
                // metal and dying Telos begin to recede.
                val page = navigator.currentPage()?.extraData!!
                if (Telos2HubMission.state.wonRecordedBattle != true)
                    (page["non-cheater"] as JSONArray).toStringList().forEach { para { it } }
                else {
                    taunt()
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
            "5-noEther" to {
                // Resume music
                TelosCommon.playThemeMusic()
                giveVara()
            },
            "7-vara" to {
                // Resume music
                TelosCommon.playThemeMusic()
                giveVara()
            },
            // Manually show text based upon conditions.
            "9-check-karengo" to {
                val page = navigator.currentPage()?.extraData!!
                Telos2HubMission.choices.checkedKarengo = true

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
            "7.1-noEther" to {
                completeMission(mission)
            },
            "10-query-history" to {
                Telos2HubMission.choices.queriedSystem = true
            },
            // With Ether
            "12-leave" to {
                completeMission(mission)
                if (TelosCommon.isPhase1) {
                    para { "==This concludes Phase 1 of the Telos storyline.==" }
                }
            },
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "startBattle" -> option.copy(
                        onOptionSelected = {
                            Telos2BattleCoordinator.startBattle({
                                runCatching {
                                    mission.setCurrentStage(Telos2HubMission.Stage.PostBattle, null, null)
                                    if (Telos2HubMission.choices.injectedSelf == true) {
                                        navigator.goToPage("6-finished-battle")
                                    } else {
                                        navigator.goToPage("3-noEther")
                                    }
                                }.onFailure { game.logger.w(it) }
                            })

                        }
                    )

                    "check-karengo" -> option.copy(showIf = { Telos2HubMission.choices.checkedKarengo != true })
                    "query-system" -> option.copy(showIf = { Telos2HubMission.choices.queriedSystem != true })
                    "return-fleet" -> option.copy(showIf = { Telos2HubMission.choices.queriedSystem == true || Telos2HubMission.choices.checkedKarengo == true })
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

        if (!TelosCommon.isPhase1) {
            Telos3HubMission().apply {
                if (create(null, false))
                    accept(dialog, null)
            }
        }
    }
}

private fun Telos2SecondLandingDialog.giveVara() {
    val vara = game.settings.getVariant("wisp_perseanchronicles_vara_Standard")
        .let { game.factory.createFleetMember(FleetMemberType.SHIP, it) }
        .apply {
            prepareShipForRecovery(
                retainAllHullmods = true,
                retainKnownHullmods = true,
                clearSMods = false,
                weaponRetainProb = 1f,
                wingRetainProb = 1f
            )
            repairTracker.cr = .7f
            shipName = Telos2HubMission.part2Json.query("/strings/varaName")
        }
    game.sector.playerFleet.fleetData.addFleetMember(vara)
    dialog.textPanel.addFleetMemberGainText(vara)
    dialog.visualPanel.showFleetMemberInfo(vara)
}

private fun Telos2SecondLandingDialog.taunt() {
    val taunts = Telos2HubMission.part2Json.query<JSONObject>("/stages/battle/cheaterTaunts")
    val pick = (1..5).random()
    (taunts["cheater$pick"] as JSONArray).toStringList().forEach { para { it } }
}