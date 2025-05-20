package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import org.json.JSONObject
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt2_dart.Telos2HubMission
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telo1CompleteDialog(
    private val mission: Telos1HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic() {
    private fun stageJson(): JSONObject = Telos1HubMission.part1Json.query("/stages/deliveryDropoff")

    override fun onInteractionStarted() {
        this.dialog.visualPanel.showImagePortion(IInteractionLogic.Illustration("wisp_perseanchronicles_telos", "shipInSpace"))
    }

    override fun pages() = object : PagesFromJson<Telo1CompleteDialog>() {
        override fun pagesJson() = stageJson().getJSONArray("pages") //stageJson.query("/pages")

        override fun onPageShownHandlersByPageId() = mapOf(
            "1.1" to {
                mission.setCurrentStage(Telos1HubMission.Stage.Completed, dialog, null)
            },
            "2" to {
                // "Hello"
                dialog.visualPanel.showPersonInfo(PerseanChroniclesNPCs.karengo)
            },
            "3" to {
                // Start Part 2 on finishing dialog.
                Telos2HubMission().apply {
                    if (create(null, false))
                        accept(dialog, null)
                }
            }
        )

        override fun optionConfigurator() = { options: List<IInteractionLogic.Option<Telo1CompleteDialog>> ->
            options.map { option ->
                when (option.id) {
                    "close" -> option.copy(
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        })

                    else -> option
                }
            }
        }
    }
}