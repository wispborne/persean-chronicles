package org.wisp.stories.dangerousGames.pt1_dragons

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.lwjgl.input.Keyboard
import org.wisp.stories.game
import wisp.questgiver.BarEventDefinition
import wisp.questgiver.wispLib.lastName

class DragonsPart1_BarEvent : BarEventDefinition<DragonsPart1_BarEvent>(
    shouldShowEvent = { DragonsQuest.shouldOfferQuest(it) },
    interactionPrompt = {
        DragonsQuest.init(game.sector.playerFleet.starSystem)
        addPara { game.words["dd_dr_stg1_prompt"] }
    },
    textToStartInteraction = { game.words["dd_dr_stg1_startBarEvent"] },
    onInteractionStarted = {
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara { game.words["dd_dr_stg1_pg1_onShown"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg1_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.words["dd_dr_stg1_pg1_opt2"] },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    game.words["dd_dr_stg1_pg2_para1"]
                }
                addPara {
                    game.words["dd_dr_stg1_pg2_para2"]
                }
                addPara {
                    game.words["dd_dr_stg1_pg2_para3"]
                }
            },
            options = listOf(
                Option(
                    text = {
                        game.words.fmt(
                            "dd_dr_stg1_pg2_opt1",
                            mapOf("playerLastName" to game.sector.playerPerson.lastName)
                        )
                    },
                    onOptionSelected = {
                        it.goToPage(3)
                    }
                ),
                Option(
                    text = { game.words["dd_dr_stg1_pg2_opt2"] },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                addPara {
                    game.words.fmt(
                        "dd_dr_stg1_pg3_onShown",
                        mapOf("playerLastName" to game.sector.playerPerson.lastName)
                    )
                }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg1_pg3_opt1"] },
                    onOptionSelected = {
                        DragonsQuest.startQuest1(startLocation = this.dialog.interactionTarget)
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    personPortrait = "graphics/portraits/portrait20.png",
    personName = FullName("Karengo", "", FullName.Gender.MALE)
) {
    override fun createInstanceOfSelf() = DragonsPart1_BarEvent()
}

class DragonsPart1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = DragonsPart1_BarEvent().buildBarEvent()
}