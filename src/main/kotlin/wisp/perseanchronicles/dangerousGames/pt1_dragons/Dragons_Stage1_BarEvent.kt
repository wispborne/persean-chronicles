package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventWiring
import wisp.questgiver.v2.QGBarEventCreator
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic.*
import wisp.questgiver.v2.spriteName

class Dragons_Stage1_BarEvent : BarEventLogic<DragonsHubMission>(
    createInteractionPrompt = {
        para { game.text["dg_dr_stg1_prompt"] }
    },
    onInteractionStarted = {
    },
    textToStartInteraction = {
        Option(
            // Head over to "Mr. Karengo"
            text = game.text["dg_dr_stg1_startBarEvent"],
            textColor = Misc.getHighlightColor()
        )
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para { game.text["dg_dr_stg1_pg1_onShown"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg1_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.text["dg_dr_stg1_pg1_opt2"] },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 2,
            people = { listOf(PerseanChroniclesNPCs.karengo) },
            onPageShown = {
                dialog.visualPanel.showMapMarker(
                    /* marker = */ DragonsHubMission.state.dragonSystem?.hyperspaceAnchor,
                    /* title = */ "",
                    /* titleColor = */ Misc.getTextColor(),
                    /* withIntel = */ false,
                    /* icon = */ DragonsHubMission.icon.spriteName(game),
                    /* text = */ null,
                    /* intelTags = */ DragonsHubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
                )
                para {
                    game.text["dg_dr_stg1_pg2_para1"]
                }
                para {
                    game.text["dg_dr_stg1_pg2_para2"]
                }
                para {
                    // A cheer goes up from the men
                    game.text["dg_dr_stg1_pg2_para3"]
                }
            },
            options = listOf(
                Option(
                    text = {
                        // We leave at dawn!
                        game.text["dg_dr_stg1_pg2_opt1"]
                    },
                    onOptionSelected = {
                        it.goToPage(3)
                    }
                ),
                Option(
                    text = { game.text["dg_dr_stg1_pg2_opt2"] },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                para {
                    game.text["dg_dr_stg1_pg3_onShown"]
                }
                mission.accept(dialog, null)
            },
            options = listOf(
                Option(
                    // Leave
                    text = { game.text["dg_dr_stg1_pg3_opt1"] },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    people = { listOf(mission.karengo) }
)

class DragonsBarEventWiring :
    BarEventWiring<DragonsHubMission>(missionId = DragonsHubMission.MISSION_ID, isPriority = false) {
    override fun createBarEventLogic() = Dragons_Stage1_BarEvent()
    override fun createMission() = DragonsHubMission()
    override fun shouldBeAddedToBarEventPool() = DragonsHubMission.state.startDateMillis == null
    override fun createBarEventCreator() = DragonsBarEventCreator(this)
    class DragonsBarEventCreator(wiring: DragonsBarEventWiring) : QGBarEventCreator<DragonsHubMission>(wiring)
}