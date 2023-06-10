package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.Global
import org.magiclib.kotlin.addCommodityLossText
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.wispLib.findFirst

class Nirvana_Stage2_Dialog(val mission: NirvanaHubMission = Global.getSector().intelManager.findFirst()!!) :
    InteractionDialogLogic<Nirvana_Stage2_Dialog>(
        onInteractionStarted = {
        },
        pages = listOf(
            IInteractionLogic.Page(
                id = 1,
                people = { listOf(PerseanChroniclesNPCs.davidRengal) },
                onPageShown = {
                    // Your quartermaster drops the scientists' cargo off
                    para { game.text["nirv_stg2_pg1_para1"] }
                    // David taps at a screen. "There," he announces. "==${nirvanaCredits}==, as agreed."
                    para { game.text["nirv_stg2_pg1_para2"] }
                    dialog.textPanel.addCommodityLossText(
                        NirvanaHubMission.CARGO_TYPE,
                        NirvanaHubMission.CARGO_WEIGHT,
                    )
                    mission.setCurrentStage(NirvanaHubMission.Stage.Completed, dialog, emptyMap())
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
                        onOptionSelected = {
                            it.close(doNotOfferAgain = true)
                        }
                    )
                )
            ),
            IInteractionLogic.Page(
                id = 2,
                image = NirvanaHubMission.background,
                onPageShown = {
                    // He brightens visibly. "We're studying the ==pulsar, ${nirvanaStarName}==.
                    para { game.text["nirv_stg2_pg2_para1"] }
                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["nirv_stg2_pg2_para2"] }
                    }
                },
                options = listOf(
                    IInteractionLogic.Option(
                        // "When are you expecting to have some answers?"
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
                    // "Soon!" David says, excited. "Within the next 20 cycles, we think.
                    para { game.text["nirv_stg2_pg3_para1"] }
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