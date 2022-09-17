package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.pt1_deliveryToEarth.Telos1HubMission
import wisp.questgiver.BarEventWiring
import wisp.questgiver.QGBarEventCreator
import wisp.questgiver.spriteName
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic.*

class Dragons_Stage1_BarEvent : BarEventLogic<DragonsHubMission>(
    createInteractionPrompt = {
        para { game.text["dg_dr_stg1_prompt"] }
    },
    onInteractionStarted = {
        dialog.visualPanel.showMapMarker(
            Telos1HubMission.state.karengoSystem?.hyperspaceAnchor,
            "",
            Misc.getTextColor(),
            true,
            DragonsHubMission.icon.spriteName(game),
            null,
            Telos1HubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
        )
    },
    textToStartInteraction = { Option(game.text["dg_dr_stg1_startBarEvent"]) },
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
            onPageShown = {
                para {
                    game.text["dg_dr_stg1_pg2_para1"]
                }
                para {
                    game.text["dg_dr_stg1_pg2_para2"]
                }
                para {
                    game.text["dg_dr_stg1_pg2_para3"]
                }
            },
            options = listOf(
                Option(
                    text = {
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
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg1_pg3_opt1"] },
                    onOptionSelected = {
                        mission.setCurrentStage(DragonsHubMission.Stage.GoToPlanet, dialog, null)
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