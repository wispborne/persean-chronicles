package org.wisp.stories.dangerousGames.A_dragons

import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator
import org.lwjgl.input.Keyboard
import wisp.questgiver.wispLib.game
import wisp.questgiver.wispLib.lastName
import wisp.questgiver.BarEventDefinition

class DragonsPart1_BarEvent : BarEventDefinition<DragonsPart1_BarEvent>(
    shouldShowEvent = { DragonsQuest.shouldOfferQuest(it) },
    interactionPrompt = {
        DragonsQuest.findAndTagDragonPlanetIfNeeded(game.sector.playerFleet.starSystem)
        addPara {
            "The moment you pass through the bar door, a strong drink is pressed into your hand. " +
                    "You look at it, dumbfounded, and then look up to the waiter who gave it to you. " +
                    "\"Compliments of Mr. Charengo,\" he says, nodding his head towards a crowd of people, mostly young men, before moving on."
        }
    },
    textToStartInteraction = { "Head over to \"Mr. Charengo\"" },
    onInteractionStarted = {
    },
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                addPara {
                    "As you make your way over, you note that nearly everyone in the bar has the same drink you do. " +
                            "In the middle of the crowd, a large, heavily muscled, and tattooed man is gesticulating wildly, apparently telling a story."
                }
            },
            options = listOf(
                Option(
                    text = { "Go listen to the man" },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { "Leave" },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                addPara {
                    "\"...so then he says to me, he says, have you ever ridden one? And I look back at this crazy joithead and I say, you're a crazy joithead. " +
                            "But he's got me thinking, y'know? Nobody has ever ridden one. Least, not nobody that could talk after.\" He lets out a laugh. " +
                            "\"So I ask you all; who wants to join Charengo to make history? Who wants to ride a dragon?\""
                }
                addPara {
                    "You've heard the stories of the dragons of ${DragonsQuest.dragonPlanet?.name} as a child; great beasts that control the skies, astonishingly similar to the dragons of lore. " +
                            "The galaxy is host to many dangerous creatures, but these living myths hold a special place near the top of that list."
                }
                addPara {
                    "A cheer goes up from the men, especially the drunk ones, and \"Mr. Charengo\" starts to hand out death and accident waivers " +
                            "and collect money for the expedition. Suddenly, his face changes. \"Listen up!\" he yells, " +
                            "\"Seems my regular pilot got in the way of a pirate fleet, and my replacement pilot misplaced " +
                            "his balls this evening and can't find 'em! So we need ourselves a ship and a captain! Who's ready to pay for their drink?\""
                }
            },
            options = listOf(
                Option(
                    text = {
                        "\"Captain ${game.sector.playerPerson.lastName}, at your service. We leave at dawn!\""
                    },
                    onOptionSelected = {
                        it.goToPage(3)
                    }
                ),
                Option(
                    text = { "Stay silent" },
                    onOptionSelected = { it.close(doNotOfferAgain = false) },
                    shortcut = Shortcut(Keyboard.KEY_ESCAPE)
                )
            )
        ),
        Page(
            id = 3,
            onPageShown = {
                addPara {
                    "You wake up significantly after dawn with empty bottles and unconscious men strewn all over your ship. " +
                            "Charengo gives you a crooked smile, shielding his eyes from the light. \"Alright, ${game.sector.playerPerson.lastName},\" he says, \"let's do this.\""
                }
            },
            options = listOf(
                Option(
                    text = { "Leave" },
                    onOptionSelected = {
                        DragonsQuest.startQuest1(startLocation = this.dialog.interactionTarget)
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    ),
    personPortrait = "graphics/portraits/portrait20.png",
    personName = FullName("Charengo", "", FullName.Gender.MALE)
) {
    override fun createInstanceOfSelf() = DragonsPart1_BarEvent()
}

class DragonsPart1_BarEventCreator : BaseBarEventCreator() {
    override fun createBarEvent(): PortsideBarEvent = DragonsPart1_BarEvent().buildBarEvent()
}