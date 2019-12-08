package org.wisp.stories.dangerousGames.A_dragons

import org.wisp.stories.questLib.InteractionDefinition
import org.wisp.stories.wispLib.empty

class DragonsPart1_EndingDialog : InteractionDefinition<DragonsPart1_EndingDialog>(
    onInteractionStarted = {},
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara {
                    "Karengo guides you down through the atmosphere of ${planetName()}" +
                            (if (isPlanetColonized())
                                ", steering clear of the usual tourist areas and, you realize, of the local law enforcement"
                            else String.empty) +
                            ". As you're putting down in a clearing halfway up a mountain, " +
                            "there's an otherworldly screech and your ship lurches violently to one side. If you hadn't been strapped in, " +
                            "you would have been thrown to the floor."
                }
                addPara {
                    "\"Look lively, boys!\" Karengo is the only person still standing. \"Your chariot awaits!\" " +
                            "In short order, he double-checks the jetpacks on the backs of each of the men, or \"Dragonriders\", " +
                            "as they creatively branded themselves during their first night of the voyage. Karengo opens the side " +
                            "hatch and hurls the Dragonriders out the door before following himself."
                }
                addPara { "You land and pull up every external camera you have to watch the action." }

            },
            options = listOf(
                Option(text = { "Continue" }, onOptionSelected = { it.goToPage(2) })
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    "The dragons of ${planetName()} you saw as a kid were different from the dragons of mythology, " +
                            "but the beasts you see today aren't quite the same ones you remember learning about on \"The Xenofauna Channel\", " +
                            "either. For one, the creatures outside your window are largely invisible - a trick of the way the " +
                            "light reflects around the scales on their hides. They are faster, too, or perhaps the ones on \"Xenofauna\" " +
                            "had been drugged to make them easier to film. A quick survey of the scene shows that Karengo has tagged three " +
                            "dragons with some sort of paintball gun to make them easier to track. Eight of the Dragonriders are chasing the " +
                            "largest one, which is missing part of a tooth and has some blackened scales from where it hit your shield. " +
                            "The last Dragonrider is nowhere in sight."
                }
                addPara {
                    "Suddenly, proximity sensors go off on the starboard side and a camera shows something " +
                            "- or someone - bounce off your shield and tumble down the mountainside."
                }
            },
            options = listOf(
                Option(
                    text = { "Announce that you're leaving" },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(text = { "Stay" }, onOptionSelected = { it.goToPage(Pages.StayAfterThingHitsShip) })
            )
        ),
        Page(
            id = Pages.StayAfterThingHitsShip,
            onPageShown = {
                addPara {
                    "Upon closer inspection, the object rolling down the hill is indeed a body. " +
                            "You turn away and back to the monitors, where one of the Dragonriders is keeping just above a dragon, " +
                            "attempting to lower himself down. The dragon rolls in midair, its wing catching the man and sending him flying. " +
                            "Fortunately for him, a leafy tree branch prevents any further physical harm. " +
                            "Meanwhile, a second dragon is pursuing another Dragonrider."
                }
            },
            options = listOf(
                Option(
                    text = { "Get everyone out of there" },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(
                    text = { "Take off, leaving everybody else behind" },
                    onOptionSelected = { it.goToPage(Pages.TakeOffAlone) })
            )
        ),
        Page(
            id = Pages.TakeOffAlone,
            onPageShown = {
                addPara {
                    "The ship takes off with a roar at your touch. Between the cries of the dragons, " +
                            "the sound of the engines, and the air rushing past, you can barely hear the " +
                            "fear and betrayal in the voices of the men you've left behind."
                }
            },
            options = listOf(
                Option(
                    text = { "Exit" },
                    onOptionSelected = {
                        DragonsQuest.failQuestByLeavingToGetEatenByDragons()
                        it.close(hideQuestOfferAfterClose = true)
                    }
                )
            )
        ),
        Page(
            id = Pages.TellEveryoneToGetOnBoard,
            onPageShown = {
                addPara { "You turn on the PA system. \"WE ARE LEAVING,\" you blast to the survivors, \"GET ON BOARD.\"" }
                addPara {
                    "The defeated Dragonriders need no further encouragement. They beeline for the hatch and you drop " +
                            "a section of the shield to let them through, where they collapse, wide-eyed and panting. " +
                            "Karengo isn't with them. You turn to ask about him, when a flicker of motion on a monitor catches your eye."
                }
                addPara {
                    "Karengo is there, and he's riding a dragon, gripping the ridge of scales around its neck. " +
                            "The dragon is frantically trying to get him off, but creative application of his jetpack is, " +
                            "for now, keeping him on top of the beast. He lets out wild whoops of triumph as they soar through the sky " +
                            "and at last you can understand the appeal of this whole mission. Your admiration is cut short by two " +
                            "catastrophic crashes on either side of the ship and accompanying sirens. The dragons are trying to get through."
                }
            },
            options = listOf(
                Option(text = { "Take off" },
                    onOptionSelected = {
                        it.goToPage(Pages.TakeOff)
                    }),
                Option(text = { "Swear and take off" },
                    onOptionSelected = {
                        addPara { "\"Pignuts,\" you mutter to yourself." }
                        it.goToPage(Pages.TakeOff)
                    })
            )
        ),
        Page(
            id = Pages.TakeOff,
            onPageShown = {
                addPara {
                    "The ship takes off quickly enough to make you temporarily light-headed. " +
                            "\"GET ON!\" shouts one of the men over the PA as you position the ship just below the raging dragon with Karengo attached." +
                            " Scales bounce off the roof like diamonds. He lets go of his death grip on the dragon and free falls toward you," +
                            " only slowing at the last possible moment before dropping through the top hatch. You hit the engines hard and" +
                            " they respond enthusiastically, rocketing you away from the mountain${if (isPlanetColonized()) " and into the safety of patrolled airspace" else String.empty}." +
                            " From the cabin comes the sound of back-slapping and, surprisingly, a sob."
                }
            },
            options = listOf(
                Option(
                    text = { "Leave to take the men back home" },
                    onOptionSelected = {
                        DragonsQuest.startPart2()
                        it.close(true)
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
        TakeOffAlone,
        TakeOff
    }

    private fun isPlanetColonized() = DragonsQuest.dragonPlanet?.activePerson != null
}