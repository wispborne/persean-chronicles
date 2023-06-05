package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Nirvana_Stage2_Dialog(val nirv: NirvanaHubMission? = Global.getSector().intelManager.findFirst()) :
    InteractionDialogLogic<Nirvana_Stage2_Dialog>(
        onInteractionStarted = {
        },
        pages = listOf(
            IInteractionLogic.Page(
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
                    IInteractionLogic.Option(
                        // what are you building?
                        text = { game.text["nirv_stg2_pg1_opt1"] },
                        onOptionSelected = { it.goToPage(2) }
                    ),
                    IInteractionLogic.Option(
                        // leave
                        text = { game.text["nirv_stg2_pg1_opt2"] },
                        onOptionSelected = { it.close(doNotOfferAgain = true) }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 2,
                onPageShown = {
                    para { game.text["nirv_stg2_pg2_para1"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["nirv_stg2_pg2_para2"] }
                    }
                },
                options = listOf(
                    IInteractionLogic.Option(
                        // when getting answers?
                        text = { game.text["nirv_stg2_pg2_opt1"] },
                        onOptionSelected = {
                            it.goToPage(3)
                        }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 3,
                onPageShown = {
                    para { game.text["nirv_stg2_pg3_para1"] }
                    nirv?.setCurrentStage(NirvanaHubMission.Stage.Completed, dialog, null)
                },
                options = listOf(
                    IInteractionLogic.Option(
                        text = { game.text["nirv_stg2_pg3_opt1"] },
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    ),
                    IInteractionLogic.Option(
                        text = { game.text["nirv_stg2_pg3_opt2"] },
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    )
                )
            )
        )
    )