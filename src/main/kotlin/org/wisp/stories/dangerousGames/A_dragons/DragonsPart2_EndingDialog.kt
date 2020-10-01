package org.wisp.stories.dangerousGames.A_dragons

import wisp.questgiver.wispLib.game
import wisp.questgiver.wispLib.lastName
import wisp.questgiver.InteractionDefinition

class DragonsPart2_EndingDialog : InteractionDefinition<DragonsPart2_EndingDialog>(
    onInteractionStarted = {},
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara {
                    "As you land and fuel up, Charengo herds the seven surviving men out of the ship. " +
                            "\"Meet us at the bar, yeah?\" he says to you. \"This day deserves a celebration.\"" +
                            " You nod, but can't help but notice red eyes and wet cheeks among the rest of the Dragonriders as they shuffle off."
                }
            },
            options = listOf(
                Option(
                    text = { "Continue" },
                    onOptionSelected = { it.goToPage(2) }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    "Later that day, you spot the Dragonriders at a corner table of the bar. " +
                            "The men, with the exception of Charengo, appear subdued. "
                }
                addPara {
                    "\"How goes it, ${game.sector.playerPerson.lastName}?\" he asks. " +
                            "\"We were just raising a glass to mission success - made possible by you, of course. " +
                            "Great flying, brother.\" The bartender arrives with a round of drinks and Charengo raises one. " +
                            "\"To the Dragonriders!\" he says."
                }
            },
            options = listOf(
                Option(
                    text = { "\"To the Dragonriders!\"" },
                    onOptionSelected = { it.goToPage(3) }
                ),
                Option(
                    text = { "\"To our fallen friends!\"" },
                    onOptionSelected = {
                        addPara { "Charengo gives you an unreadable look and lowers his glass." }
                        it.goToPage(3)
                    }
                ),
                Option(
                    text = { "\"I'm good, thanks.\"" },
                    onOptionSelected = {
                        addPara { "Charengo gives you an unreadable look and lowers his glass." }
                        it.goToPage(3)
                    }
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                addPara { "\"Your payment, plus Bolek and Jarek's signup fees. ${DragonsQuest.rewardCredits}.\"" }
            },
            options = listOf(
                Option(
                    text = { "\"Thanks.\"" },
                    onOptionSelected = { it.goToPage(4) }
                ),
                Option(
                    text = { "\"I can't accept their money. It's not right.\"" },
                    onOptionSelected = {
                        addPara { "Charengo shrugs and doesn't take it back." }
                        it.goToPage(4)
                    }
                )
            )
        ),
        Page(
            id = 4,
            onPageShown = {
                addPara {
                    "\"Maybe I'll see you again sometime, yeah? I'm in need of a pilot with some guts.\" " +
                            "He nods in your direction, clearly dismissing you."
                }
            },
            options = listOf(
                Option(
                    text = { "Leave" },
                    onOptionSelected = {
                        DragonsQuest.finishStage2()
                        val interactionTarget = dialog.interactionTarget
                        it.close(doNotOfferAgain = true)
                        // Show normal planet dialog after quest finishes
                        game.sector.campaignUI.showInteractionDialog(interactionTarget)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = DragonsPart2_EndingDialog()
}