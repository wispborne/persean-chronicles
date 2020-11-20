package org.wisp.stories.dangerousGames.pt2_depths

import org.wisp.stories.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.empty

class Depths_Stage2_Dialog : InteractionDefinition<Depths_Stage2_Dialog>(
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para {
                    game.words.getf(
                        "dg_de_stg2-goToPlanet_pg1_para1",
                        "ifPlayerOwnedWorld" to
                                if (DepthsQuest.depthsPlanet?.market?.isPlayerOwned == true)
                                    game.words["dg_de_stg2-goToPlanet_pg1_para1_ifPlayerOwnedWorld"]
                                else
                                    String.empty
                    )
                }

                para { game.words["dg_de_stg2-goToPlanet_pg1_para2"] }
                para { game.words["dg_de_stg2-goToPlanet_pg1_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_pg1_opt2"] },
                    onOptionSelected = {
                        para { game.words["dg_de_stg2-goToPlanet_pg1_opt2_onSelected"] }
                        it.goToPage(2)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para {
                    game.words["dg_de_stg2-goToPlanet_pg2_para1"]
                    game.words["dg_de_stg2-goToPlanet_pg2_para2"]
                }
            },
            options = listOf(Option(
                text = { game.words["dg_de_stg2-goToPlanet_pg2_opt1"] },
                onOptionSelected = { it.goToPage(3) }
            ))
        ),
        Page(
            id = 3,
            onPageShown = {
                para { game.words["dg_de_stg2-goToPlanet_pg3_para1"] }
                para { game.words["dg_de_stg2-goToPlanet_pg3_para2"] }
                para { game.words["dg_de_stg2-goToPlanet_pg3_para3"] }
                para { game.words["dg_de_stg2-goToPlanet_pg3_para4"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(PageId.ViewRiddle1) }
                )
            )
        ),
        Page(
            id = PageId.ViewRiddle1,
            onPageShown = {
                para { game.words["dg_de_stg2-goToPlanet_riddle1_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_optEast"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_East) }
                ),
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_optNorth"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_North) }
                ),
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_optSouth"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_South) }
                ),
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_optWest"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_West) }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_East,
            onPageShown = {
                para { game.words["dg_de_stg2-goToPlanet_riddle1_east_para1"] }
                para { game.words["dg_de_stg2-goToPlanet_riddle1_east_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_east_opt1"] },
                    onOptionSelected = {
                        para { game.words["dg_de_stg2-goToPlanet_riddle1_east_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle1_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_South,
            onPageShown = {
                para { game.words["dg_de_stg2-goToPlanet_riddle1_south_para1"] }
                para { game.words["dg_de_stg2-goToPlanet_riddle1_south_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.words["dg_de_stg2-goToPlanet_riddle1_south_opt1"] },
                    onOptionSelected = {
                        para { game.words["dg_de_stg2-goToPlanet_riddle1_south_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle1_Failed)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = Depths_Stage2_Dialog()

    enum class PageId {
        ViewRiddle1,
        Riddle1_East,
        Riddle1_North,
        Riddle1_South,
        Riddle1_West,
        Riddle1_Failed
    }
}