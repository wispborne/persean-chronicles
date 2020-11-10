package org.wisp.stories.dangerousGames.pt1_dragons

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.empty

class DragonsPart1_EndingDialog : InteractionDefinition<DragonsPart1_EndingDialog>(
    onInteractionStarted = {},
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara {
                    game.words.fmt(
                        "dd_dr_stg2_pg1_para1",
                        mapOf(
                            "planetName" to planetName(),
                            "ifColonized" to
                                    if (isPlanetColonized())
                                        game.words["dd_dr_stg2_pg1_para1_ifColonized"]
                                    else
                                        String.empty
                        )
                    )
                }
                addPara { game.words["dd_dr_stg2_pg1_para2"] }
                addPara { game.words["dd_dr_stg2_pg1_para3"] }

            },
            options = listOf(
                Option(text = { game.words["dd_dr_stg2_pg1_opt1"] }, onOptionSelected = { it.goToPage(2) })
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    game.words.fmt(
                        "dd_dr_stg2_pg2_para1",
                        mapOf("planetName" to planetName())
                    )
                }
                addPara { game.words["dd_dr_stg2_pg2_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg2_pg2_opt1"] },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(
                    text = { game.words["dd_dr_stg2_pg2_opt2"] },
                    onOptionSelected = { it.goToPage(Pages.StayAfterThingHitsShip) }),
                Option(
                    text = { game.words["dd_dr_stg2_pg2_opt3"] },
                    onOptionSelected = { it.goToPage(Pages.AbandonEveryone) })
            )
        ),
        Page(
            id = Pages.StayAfterThingHitsShip,
            onPageShown = {
                addPara { game.words["dd_dr_stg2_pg3_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg2_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(
                    text = { game.words["dd_dr_stg2_pg3_opt2"] },
                    onOptionSelected = { it.goToPage(Pages.AbandonEveryone) })
            )
        ),
        Page(
            id = Pages.AbandonEveryone,
            onPageShown = {
                addPara { game.words["dd_dr_stg2_pg-abandon_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg2_pg-abandon_para1"] },
                    onOptionSelected = {
                        DragonsQuest.failQuestByLeavingToGetEatenByDragons()
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        ),
        Page(
            id = Pages.TellEveryoneToGetOnBoard,
            onPageShown = {
                addPara { game.words["dd_dr_stg2_pg4_para1"] }
                addPara { game.words["dd_dr_stg2_pg4_para2"] }
                addPara { game.words["dd_dr_stg2_pg4_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg2_pg4_opt1"] },
                    onOptionSelected = {
                        it.goToPage(Pages.TakeOff)
                    }),
                Option(
                    text = { game.words["dd_dr_stg2_pg4_opt2"] },
                    onOptionSelected = {
                        addPara { game.words["dd_dr_stg2_pg4_opt2_para1"] }
                        it.goToPage(Pages.TakeOff)
                    })
            )
        ),
        Page(
            id = Pages.TakeOff,
            onPageShown = {
                addPara {
                    game.words.fmt(
                        "dd_dr_stg2_pg5_para1",
                        mapOf(
                            "ifColonized" to
                                    if (isPlanetColonized())
                                        game.words["dd_dr_stg2_pg5_para1_ifColonized"]
                                    else
                                        String.empty
                        )
                    )
                }
            },
            options = listOf(
                Option(
                    text = { game.words["dd_dr_stg2_pg5_opt1"] },
                    onOptionSelected = {
                        DragonsQuest.startPart2()
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = DragonsPart1_EndingDialog()
    private fun planetName() = DragonsQuest.dragonPlanet?.name

    enum class Pages {
        TellEveryoneToGetOnBoard,
        StayAfterThingHitsShip,
        AbandonEveryone,
        TakeOff
    }

    private fun isPlanetColonized() = DragonsQuest.dragonPlanet?.activePerson != null
}