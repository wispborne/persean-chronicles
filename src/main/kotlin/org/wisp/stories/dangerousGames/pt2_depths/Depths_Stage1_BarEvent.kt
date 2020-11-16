package org.wisp.stories.dangerousGames.pt2_depths

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.lwjgl.input.Keyboard
import org.wisp.stories.game
import wisp.questgiver.BarEventDefinition
import wisp.questgiver.wispLib.lastName

class Depths_Stage1_BarEvent : BarEventDefinition<Depths_Stage1_BarEvent>(
    shouldShowEvent = { DepthsQuest.shouldOfferQuest(it) },
    interactionPrompt = {
        DepthsQuest.init(game.sector.playerFleet.starSystem)
        addPara { game.words["dg_de_stg1_prompt"] }
    },
    textToStartInteraction = { game.words["dg_de_stg1_startBarEvent"] },
    onInteractionStarted = {
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara { game.words["dg_de_stg1_pg1_para1"] }
                addPara { game.words["dg_de_stg1_pg1_para2"] }
                addPara { game.words["dg_de_stg1_pg1_para3"] }
                addPara { game.words["dg_de_stg1_pg1_para4"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg1_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.words["dg_de_stg1_pg1_opt2"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.words["dg_de_stg1_pg1_opt3"] },
                    onOptionSelected = { it.close(doNotOfferAgain = true) }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    game.words["dg_de_stg1_pg2_para1"]
                }
                addPara {
                    game.words["dg_de_stg1_pg2_para2"]
                }
            },
            options = listOf(
                Option(
                    text = {
                        game.words["dg_de_stg1_pg2_opt1"]
                    },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    personPortrait = "graphics/portraits/portrait20.png",
    personName = FullName("Karengo", "", FullName.Gender.MALE)
) {
    override fun createInstanceOfSelf() = Depths_Stage1_BarEvent()
}

class Depths_Stage1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = Depths_Stage1_BarEvent().buildBarEvent()
}