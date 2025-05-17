package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import org.magiclib.kotlin.adjustReputationWithPlayer
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos2FirstLandingDialog(
    val stageJson: JSONObject = Telos2HubMission.part2Json.query("/stages/landOnPlanetFirst"),
    val mission: Telos2HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic() {
    override fun people() = { listOfNotNull(PerseanChroniclesNPCs.karengo) }

    override fun pages() = object : PagesFromJson<Telos2FirstLandingDialog>() {
        override fun pagesJson() = stageJson.getJSONArray("pages") //stageJson.query("/pages")

        override fun onPageShownHandlersByPageId() = mapOf(
            "1" to {
                game.jukebox.playTelosThemeMusic()
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
                mission.setCurrentStage(Telos2HubMission.Stage.LandOnPlanetSecondEther, dialog, null)
            },
            "12.2" to {
                mission.setCurrentStage(Telos2HubMission.Stage.LandOnPlanetSecondNoEther, dialog, null)
            }
        )

        override fun optionConfigurator() = { options: List<IInteractionLogic.Option<Telos2FirstLandingDialog>> ->
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
                            // Injected with Ether
                            game.sector.playerPerson.addTag(TelosCommon.ETHER_OFFICER_TAG)
                            PerseanChroniclesNPCs.karengo.addTag(TelosCommon.ETHER_OFFICER_TAG)
                            PerseanChroniclesNPCs.karengo.adjustReputationWithPlayer(.05f, dialog.textPanel)
                        })

                    "noInject" -> option.copy(
                        text = if (Misc.random.nextFloat() > 0.95f) {
                            { """"Holy shit, no."""" } // 5% chance lol
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
    }
}