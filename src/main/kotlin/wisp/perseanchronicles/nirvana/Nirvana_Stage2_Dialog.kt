package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.findFirst

class Nirvana_Stage2_Dialog(val nirv: NirvanaHubMission? = Global.getSector().intelManager.findFirst()) :
    InteractionDefinition<Nirvana_Stage2_Dialog>(
        onInteractionStarted = {
        },
        pages = listOf(
            Page(
                id = 1,
                image = NirvanaHubMission.icon,
                onPageShown = {
                    para { game.text["nirv_stg2_pg1_para1"] }
                    para { game.text["nirv_stg2_pg1_para2"] }
                    AddRemoveCommodity.addCommodityLossText(
                        NirvanaHubMission.CARGO_TYPE,
                        NirvanaHubMission.CARGO_WEIGHT,
                        dialog.textPanel
                    )
                },
                options = listOf(
                    Option(
                        // what are you building?
                        text = { game.text["nirv_stg2_pg1_opt1"] },
                        onOptionSelected = { it.goToPage(2) }
                    ),
                    Option(
                        // leave
                        text = { game.text["nirv_stg2_pg1_opt2"] },
                        onOptionSelected = { it.close(doNotOfferAgain = true) }
                    )
                )
            ),
            Page(
                id = 2,
                onPageShown = {
                    para { game.text["nirv_stg2_pg2_para1"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["nirv_stg2_pg2_para2"] }
                    }
                },
                options = listOf(
                    Option(
                        // when getting answers?
                        text = { game.text["nirv_stg2_pg2_opt1"] },
                        onOptionSelected = {
                            it.goToPage(3)
                        }
                    )
                )
            ),
            Page(
                id = 3,
                onPageShown = {
                    para { game.text["nirv_stg2_pg3_para1"] }
                    nirv?.setCurrentStage(NirvanaHubMission.Stage.Completed, dialog, null)
                },
                options = listOf(
                    Option(
                        text = { game.text["nirv_stg2_pg3_opt1"] },
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    ),
                    Option(
                        text = { game.text["nirv_stg2_pg3_opt2"] },
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    )
                )
            )
        )
    ) {
    override fun createInstanceOfSelf(): InteractionDefinition<Nirvana_Stage2_Dialog> = Nirvana_Stage2_Dialog()
}