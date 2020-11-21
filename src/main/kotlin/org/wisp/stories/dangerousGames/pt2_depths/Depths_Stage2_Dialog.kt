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
                    game.text.getf(
                        "dg_de_stg2_pg1_para1",
                        "ifPlayerOwnedWorld" to
                                if (DepthsQuest.depthsPlanet?.market?.isPlayerOwned == true)
                                    game.text["dg_de_stg2_pg1_para1_ifPlayerOwnedWorld"]
                                else
                                    String.empty
                    )
                }

                para { game.text["dg_de_stg2_pg1_para2"] }
                para { game.text["dg_de_stg2_pg1_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_pg1_opt1"] },
                    onOptionSelected = { it.goToPage(2) }
                ),
                Option(
                    text = { game.text["dg_de_stg2_pg1_opt2"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_pg1_opt2_onSelected"] }
                        it.goToPage(2)
                    }
                )
            )
        ),
        Page(
            id = 2,
            onPageShown = {
                para {
                    game.text["dg_de_stg2_pg2_para1"]
                    game.text["dg_de_stg2_pg2_para2"]
                }
            },
            options = listOf(Option(
                text = { game.text["dg_de_stg2_pg2_opt1"] },
                onOptionSelected = { it.goToPage(3) }
            ))
        ),
        Page(
            id = 3,
            onPageShown = {
                para { game.text["dg_de_stg2_pg3_para1"] }
                para { game.text["dg_de_stg2_pg3_para2"] }
                para { game.text["dg_de_stg2_pg3_para3"] }
                para { game.text["dg_de_stg2_pg3_para4"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(PageId.ViewRiddle1) }
                )
            )
        ),
        Page(
            id = PageId.ViewRiddle1,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optEast"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_East_Morg) }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optNorth"] },
                    onOptionSelected = {
                        didSucceedAtFirstRiddle = true
                        it.goToPage(PageId.Riddle1_North_Success)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optSouth"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle1_South_Smoke) }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optWest"] },
                    onOptionSelected = {
                        didCrashIntoRiddle1Wall = true
                        it.goToPage(PageId.Riddle1_West_Wall)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_East_Morg,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_east_para1"] }
                para { game.text["dg_de_stg2_riddle1_east_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_east_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle1_east_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle1_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_South_Smoke,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_south_para1"] }
                para { game.text["dg_de_stg2_riddle1_south_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_south_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle1_south_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle1_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_West_Wall,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_west_para1"] }
                para { game.text["dg_de_stg2_riddle1_west_para2"] }
                para { game.text["dg_de_stg2_riddle1_west_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_west_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle1_west_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle1_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_North_Success,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_north_para1"] }
                para { game.text["dg_de_stg2_riddle1_north_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_north_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewRiddle2)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle1_Failed,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle1_failure_para1"] }
                para { game.text["dg_de_stg2_riddle1_failure_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle1_failure_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewRiddle2)
                    }
                )
            )
        ),
        Page(
            id = PageId.ViewRiddle2,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_optEast"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle2_East_Success_SecondSuccess) }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle2_optNorth"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle2_North_Vines) }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle2_optWest"] },
                    onOptionSelected = {
                        if (!didCrashIntoRiddle1Wall)
                            it.goToPage(PageId.Riddle2_West_FirstWallCrash)
                        else
                            it.goToPage(PageId.Riddle2_West_SecondWallCrash)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_North_Vines,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_north_para1"] }
                para { game.text["dg_de_stg2_riddle2_north_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_north_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle2_north_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle2_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_West_FirstWallCrash,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_west_1stWallCrash_para1"] }
                para { game.text["dg_de_stg2_riddle2_west_1stWallCrash_para2"] }
                para { game.text["dg_de_stg2_riddle2_west_1stWallCrash_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_west_1stWallCrash_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle2_west_1stWallCrash_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle2_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_West_SecondWallCrash,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_west_2ndWallCrash_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_west_2ndWallCrash_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle2_west_2ndWallCrash_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle2_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_Failed,
            onPageShown = {
                if(didSucceedAtFirstRiddle) {
                    para { game.text["dg_de_stg2_riddle2_failure_firstFail_para1"] }
                    para { game.text["dg_de_stg2_riddle2_failure_firstFail_para2"] }
                } else {
                    para { game.text["dg_de_stg2_riddle2_failure_secondFail_para1"] }
                    para { game.text["dg_de_stg2_riddle2_failure_secondFail_para2"] }
                }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_failure_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewRiddle3)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_East_Success_FirstSuccess,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_east_firstSuccess_para1"] }
                para { game.text["dg_de_stg2_riddle2_east_firstSuccess_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_east_firstSuccess_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewRiddle3)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle2_East_Success_SecondSuccess,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle2_east_secondSuccess_para1"] }
                para { game.text["dg_de_stg2_riddle2_east_secondSuccess_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle2_east_secondSuccess_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewRiddle3)
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = Depths_Stage2_Dialog()

    var didSucceedAtFirstRiddle = false
    var didCrashIntoRiddle1Wall = false

    enum class PageId {
        ViewRiddle1,
        Riddle1_East_Morg,
        Riddle1_North_Success,
        Riddle1_South_Smoke,
        Riddle1_West_Wall,
        Riddle1_Failed,
        ViewRiddle2,
        Riddle2_East_Success_FirstSuccess,
        Riddle2_East_Success_SecondSuccess,
        Riddle2_North_Vines,
        Riddle2_West_FirstWallCrash,
        Riddle2_West_SecondWallCrash,
        Riddle2_Failed,
        ViewRiddle3,
        Riddle3_East,
        Riddle3_North,
        Riddle3_West_FirstWallCrash,
        Riddle3_West_SecondWallCrash,
        Riddle3_Failed,
    }
}