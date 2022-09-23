package wisp.perseanchronicles.dangerousGames.pt1_dragons

import com.fs.starfarer.api.Global
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.empty
import wisp.questgiver.wispLib.findFirst

class Dragons_Stage2_Dialog(val dragons: DragonsHubMission = Global.getSector().intelManager.findFirst()!!) : InteractionDefinition<Dragons_Stage2_Dialog>(
    pages = listOf(
        Page(
            id = 1,
            image = DragonsHubMission.dragonPlanetImage,
            onPageShown = {
                // The men load into a shuttle and Karengo directs you down through the atmosphere
                para {
                    game.text.getf(
                        "dg_dr_stg2_pg1_para1",
                        "ifColonized" to
                                if (isPlanetColonized())
                                    game.text["dg_dr_stg2_pg1_para1_ifColonized"]
                                else
                                    String.empty
                    )
                }
                para { game.text["dg_dr_stg2_pg1_para2"] }
                para { game.text["dg_dr_stg2_pg1_para3"] }
            },
            options = listOf(
                Option(text = { game.text["dg_dr_stg2_pg1_opt1"] }, onOptionSelected = { it.goToPage(2) })
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                // The dragons of ${dragonPlanet} you learned about on the "Xenofauna and Flora"
                para { game.text["dg_dr_stg2_pg2_para1"] }
                para { game.text["dg_dr_stg2_pg2_para2"] }
                para { game.text["dg_dr_stg2_pg2_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg2_pg2_opt1"] },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(
                    text = { game.text["dg_dr_stg2_pg2_opt2"] },
                    onOptionSelected = { it.goToPage(Pages.StayAfterThingHitsShip) }),
                Option(
                    text = { game.text["dg_dr_stg2_pg2_opt3"] },
                    onOptionSelected = { it.goToPage(Pages.AbandonEveryone) })
            )
        ),
        Page(
            id = Pages.StayAfterThingHitsShip,
            onPageShown = {
                para { game.text["dg_dr_stg2_pg3_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg2_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(Pages.TellEveryoneToGetOnBoard) }),
                Option(
                    text = { game.text["dg_dr_stg2_pg3_opt2"] },
                    onOptionSelected = { it.goToPage(Pages.AbandonEveryone) })
            )
        ),
        Page(
            id = Pages.AbandonEveryone,
            onPageShown = {
                para { game.text["dg_dr_stg2_pg-abandon_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg2_pg-abandon_opt1"] },
                    onOptionSelected = {
                        dragons.setCurrentStage(DragonsHubMission.Stage.FailedByAbandoningDragonriders, dialog, null)
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        ),
        Page(
            id = Pages.TellEveryoneToGetOnBoard,
            onPageShown = {
                para { game.text["dg_dr_stg2_pg4_para1"] }
                para { game.text["dg_dr_stg2_pg4_para2"] }
                para { game.text["dg_dr_stg2_pg4_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_dr_stg2_pg4_opt1"] },
                    onOptionSelected = {
                        it.goToPage(Pages.TakeOff)
                    }),
                Option(
                    text = { game.text["dg_dr_stg2_pg4_opt2"] },
                    onOptionSelected = {
                        para { game.text["dg_dr_stg2_pg4_opt2_para1"] }
                        it.goToPage(Pages.TakeOff)
                    })
            )
        ),
        Page(
            id = Pages.TakeOff,
            onPageShown = {
                para {
                    game.text.getf(
                        "dg_dr_stg2_pg5_para1",
                        "ifColonized" to
                                if (isPlanetColonized())
                                    game.text["dg_dr_stg2_pg5_para1_ifColonized"]
                                else
                                    String.empty
                    )
                }
                dragons.setCurrentStage(DragonsHubMission.Stage.ReturnToStart, dialog, null)
            },
            options = listOf(
                Option(
                    // Take the men back home
                    text = { game.text["dg_dr_stg2_pg5_opt1"] },
                    onOptionSelected = {
                        it.close(doNotOfferAgain = true)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = Dragons_Stage2_Dialog()
    private fun planetName() = DragonsHubMission.state.dragonPlanet?.name

    enum class Pages {
        TellEveryoneToGetOnBoard,
        StayAfterThingHitsShip,
        AbandonEveryone,
        TakeOff
    }

    private fun isPlanetColonized() = DragonsHubMission.state.dragonPlanet?.market?.size ?: 0 > 0
}