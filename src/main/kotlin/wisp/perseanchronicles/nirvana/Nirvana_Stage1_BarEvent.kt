package wisp.perseanchronicles.nirvana

import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.addCommodityGainText
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic

class Nirvana_Stage1_BarEvent : BarEventLogic<NirvanaHubMission>(
    createInteractionPrompt = {
        para { game.text["nirv_stg1_prompt"] }
    },
    onInteractionStarted = { },
    textToStartInteraction = {
        Option(
            text = game.text["nirv_stg1_startBarEvent"],
            textColor = Misc.getHighlightColor()
        )
    },
    pages = listOf(
        IInteractionLogic.Page(
            id = 1,
            people = { listOf(PerseanChroniclesNPCs.davidRengal) },
            onPageShown = {
                // "Are you Captain ${playerLastName}, captain of the ${playerFlagshipName}?" asks one of the men, and when you nod in confirmation they all grin. "Allow me to introduce myself. I'm David Rengel, and these are my associates, Fiona Jeffrey and Mike Robbins.
                para { game.text["nirv_stg1_pg1_para1"] }
                para { game.text["nirv_stg1_pg1_para2"] }
                para { game.text["nirv_stg1_pg1_para3"] }
            },
            options = listOf(
                IInteractionLogic.Option(
                    // accept
                    text = { game.text["nirv_stg1_pg1_opt1"] },
                    onOptionSelected = {
                        it.goToPage(2)
                    }
                ),
                IInteractionLogic.Option(
                    // decline
                    text = { game.text["nirv_stg1_pg1_opt2"] },
                    onOptionSelected = { navigator ->
                        navigator.close(doNotOfferAgain = false)
                    }
                )
            )
        ),
        IInteractionLogic.Page(
            id = 2,
            onPageShown = {
                // "Excellent!" he exclaims. "We need to get to ==${nirvanaDestPlanet}== in the ==${nirvanaDestSystem}==.
                para { game.text["nirv_stg1_pg2_para1"] }
                dialog.visualPanel.showMapMarker(NirvanaHubMission.state.destPlanet, null, null, false, null, null, NirvanaHubMission.tags)
            },
            options = listOf(
                IInteractionLogic.Option(
                    // fully accept
                    showIf = { game.sector.playerFleet.cargo.spaceLeft >= NirvanaHubMission.CARGO_WEIGHT },
                    text = { game.text["nirv_stg1_pg2_opt1"] }, // "Done. We'll handle that and be under way shortly."
                    onOptionSelected = {
                        para { game.text["nirv_stg1_pg2_opt1_onSelected"] }
                        dialog.textPanel.addCommodityGainText(
                            NirvanaHubMission.CARGO_TYPE,
                            NirvanaHubMission.CARGO_WEIGHT)
                        mission.accept(dialog, emptyMap())

                        navigator.promptToContinue(game.text["continue"]) {
                            navigator.close(doNotOfferAgain = true)
                        }
                    }
                ),
                IInteractionLogic.Option(
                    // not enough space
                    showIf = { game.sector.playerFleet.cargo.spaceLeft < NirvanaHubMission.CARGO_WEIGHT },
                    text = { game.text["nirv_stg1_pg2_opt2"] },
                    onOptionSelected = {
                        navigator.close(doNotOfferAgain = false)
                    }
                ),
                IInteractionLogic.Option(
                    // decline
                    showIf = { game.sector.playerFleet.cargo.spaceLeft >= NirvanaHubMission.CARGO_WEIGHT },
                    text = { game.text["nirv_stg1_pg2_opt3"] },
                    onOptionSelected = {
                        navigator.close(doNotOfferAgain = false)
                    }
                )
            )
        )
    ))