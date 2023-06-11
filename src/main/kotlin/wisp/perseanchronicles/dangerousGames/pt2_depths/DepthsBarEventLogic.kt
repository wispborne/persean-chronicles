package wisp.perseanchronicles.dangerousGames.pt2_depths

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import wisp.perseanchronicles.game
import wisp.questgiver.spriteName
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.spriteName

class DepthsBarEventLogic : BarEventLogic<DepthsHubMission>(
    createInteractionPrompt = {
        para { game.text["dg_de_stg1_prompt"] }
    },
    textToStartInteraction = {
        Option(
            text = game.text["dg_de_stg1_startBarEvent"],
            textColor = Misc.getHighlightColor()
        )
    },
    onInteractionStarted = {},
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            onPageShown = {
                para { game.text["dg_de_stg1_pg1_para1"] }
                para { game.text["dg_de_stg1_pg1_para2"] }
                para { game.text["dg_de_stg1_pg1_para3"] }
                para { game.text["dg_de_stg1_pg1_para4"] }

                dialog.visualPanel.showMapMarker(
                    /* marker = */ DepthsHubMission.state.depthsPlanet?.starSystem?.hyperspaceAnchor,
                    /* title = */ "",
                    /* titleColor = */ Misc.getTextColor(),
                    /* withIntel = */ false,
                    /* icon = */ DepthsHubMission.icon.spriteName(game),
                    /* text = */ null,
                    /* intelTags = */ DepthsHubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
                )
            },
            options = listOf(
                IInteractionLogic.Option(
                    text = { game.text["dg_de_stg1_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                IInteractionLogic.Option(
                    text = { game.text["dg_de_stg1_pg1_opt2"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                IInteractionLogic.Option(
                    text = { game.text["dg_de_stg1_pg1_opt3"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 2,
            onPageShown = {
                para {
                    game.text["dg_de_stg1_pg2_para1"]
                }
                para {
                    game.text["dg_de_stg1_pg2_para2"]
                }
                mission.accept(dialog, null)
                mission.setCurrentStage(DepthsHubMission.Stage.GoToPlanet, dialog, null)
            },
            options = listOf(
                IInteractionLogic.Option(
                    text = {
                        // "We leave at noon!"
                        game.text["dg_de_stg1_pg2_opt1"]
                    },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    people = { listOfNotNull(DepthsHubMission.karengo) }
)