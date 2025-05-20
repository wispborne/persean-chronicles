package wisp.perseanchronicles.telos.pt3_arrow

import org.json.JSONObject
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst
import wisp.questgiver.wispLib.qgFormat

class Telos3EscapedDialog(
) : InteractionDialogLogic() {
    //    people = { listOfNotNull(PerseanChroniclesNPCs.karengo) },
//    firstPageSelector = {
//        val pages = this
//
//        // Resume from where player left off.
//        if (Telos3HubMission.state.visitedPrimaryPlanet == true) {
//            if (Telos2HubMission.choices.injectedSelf == true)
//                pages.single { it.id == "4-ether-go-inside" }
//            else
//                pages.single { it.id == "14-noEther" }
//        } else if (Telos2HubMission.choices.injectedSelf == true)
//            pages.single { it.id == "1-ether-start" }
//        else {
//            pages.single { it.id == "1-noEther-start" }
//        }
//    },
    fun stageJson(): JSONObject = Telos3HubMission.part3Json.query("/stages/escaped")
    val mission: Telos3HubMission by lazy { game.sector.intelManager.findFirst()!! }

    override fun pages() = object : PagesFromJson<Telos3EscapedDialog>() {
        override fun pagesJson() = stageJson().getJSONArray("pages") //stageJson.query("/pages")
        override fun onPageShownHandlersByPageId() = mapOf(
            "1-escaped" to {
                dialog.visualPanel.showImagePortion(IInteractionLogic.Illustration("illustrations", "jump_point_hyper"))
            },
            "3-explanation" to {
                val page = navigator.currentPage()!!
                if (game.memory["\$gaPZ_scannedZiggurat"] == true) {
                    para { (page.extraData["response-zigg"] as String).qgFormat() }
                } else {
                    para { (page.extraData["response-noZigg"] as String).qgFormat() }
                }
            },
            "4-cliffhanger" to {
                mission.setCurrentStage(Telos3HubMission.Stage.Completed, dialog, null)
            }
        )

        override fun optionConfigurator() = { options: List<IInteractionLogic.Option<Telos3EscapedDialog>> ->
            options.map { option ->
                when (option.id) {
                    "endOfPhase2" -> option.copy(onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    })

                    else -> option
                }
            }
        }
    }
}