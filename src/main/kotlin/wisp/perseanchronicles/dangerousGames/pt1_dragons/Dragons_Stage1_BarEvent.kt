package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.lwjgl.input.Keyboard
import wisp.perseanchronicles.game
import wisp.questgiver.AutoBarEventDefinition
import wisp.questgiver.BarEventDefinition
import wisp.questgiver.wispLib.lastName

class Dragons_Stage1_BarEvent : AutoBarEventDefinition<Dragons_Stage1_BarEvent>(
    questFacilitator = DragonsQuest,
    createInteractionPrompt = {
        para { game.text["dg_dr_stg1_prompt"] }
    },
    onInteractionStarted = {},
    textToStartInteraction = { game.text["dg_dr_stg1_startBarEvent"] },
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
                        DragonsQuest.startStage1(startLocation = this.dialog.interactionTarget)
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    personPortrait = "graphics/portraits/portrait20.png",
    personName = FullName("Karengo", "", FullName.Gender.MALE)
) {
    override fun createInstanceOfSelf() = Dragons_Stage1_BarEvent()
}

class DragonsPart1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = Dragons_Stage1_BarEvent().buildBarEvent()
}