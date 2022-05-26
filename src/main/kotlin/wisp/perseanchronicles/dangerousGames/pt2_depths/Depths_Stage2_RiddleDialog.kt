package wisp.perseanchronicles.dangerousGames.pt2_depths

import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsQuest.riddleSuccessesCount
import wisp.perseanchronicles.dangerousGames.pt2_depths.DepthsQuest.wallCrashesCount
import wisp.perseanchronicles.game
import wisp.questgiver.InteractionDefinition
import wisp.questgiver.wispLib.empty

class Depths_Stage2_RiddleDialog : InteractionDefinition<Depths_Stage2_RiddleDialog>(
    pages = listOf(
        Page(
            id = 1,
            onPageShown = {
                para {
                    game.text.getf(
                        "dg_de_stg2_pg1_para1",
                        "ifPlayerOwnedWorld" to
                                if (DepthsQuest.state.depthsPlanet?.market?.isPlayerOwned == true)
                                    game.text["dg_de_stg2_pg1_para1_ifPlayerOwnedWorld"]
                                else
                                    String.empty
                    )
                }

                para { game.text["dg_de_stg2_pg1_para2"] }
                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["dg_de_stg2_pg1_para3"] }
                }
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
            image = DepthsQuest.diveIllustration,
            onPageShown = {
                para { game.text["dg_de_stg2_pg2_para1"] }
                navigator.promptToContinue(game.text["dg_de_stg2_pg2_para1_continue"]) {
                    para { game.text["dg_de_stg2_pg2_para2"] }
                    DepthsQuest.startMusic()
                }
            },
            options = listOf(Option(
                text = { game.text["dg_de_stg2_pg2_opt1"] },
                onOptionSelected = {
                    it.goToPage(3)
                }
            ))
        ),
        Page(
            id = 3,
            onPageShown = {
                para { game.text["dg_de_stg2_pg3_para1"] }

                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["dg_de_stg2_pg3_para1_2"] }

                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["dg_de_stg2_pg3_para1_3"] }
                        para { game.text["dg_de_stg2_pg3_para2"] }
                    }
                }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_pg3_opt1"] },
                    onOptionSelected = { it.goToPage(4) }
                )
            )
        ),
        Page(
            id = 4,
            onPageShown = {
                para { game.text["dg_de_stg2_pg4_para1"] }
                para { game.text["dg_de_stg2_pg4_para2"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_pg4_opt1"] },
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
                    onOptionSelected = {
                        DepthsQuest.choices.riddle1Choice = RiddleChoice.Riddle1Choice.EastMorg
                        it.goToPage(PageId.Riddle1_East_Morg)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optNorth"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle1Choice = RiddleChoice.Riddle1Choice.NorthSuccess
                        it.goToPage(PageId.Riddle1_North_Success)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optSouth"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle1Choice = RiddleChoice.Riddle1Choice.SouthSmoke
                        it.goToPage(PageId.Riddle1_South_Smoke)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle1_optWest"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle1Choice = RiddleChoice.Riddle1Choice.WestWall
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
                    onOptionSelected = {
                        DepthsQuest.choices.riddle2Choice = RiddleChoice.Riddle2Choice.EastSuccess
                        it.goToPage(PageId.Riddle2_East_Success_SecondSuccess)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle2_optNorth"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle2Choice = RiddleChoice.Riddle2Choice.NorthVines
                        it.goToPage(PageId.Riddle2_North_Vines)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle2_optWest"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle2Choice = RiddleChoice.Riddle2Choice.WestWall
                        if (DepthsQuest.choices.riddle1Choice != RiddleChoice.Riddle1Choice.WestWall)
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
                if (DepthsQuest.choices.riddle1Choice == RiddleChoice.Riddle1Choice.NorthSuccess) {
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
        ),
        Page(
            id = PageId.ViewRiddle3,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle3_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle3_optNorth"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle3Choice = RiddleChoice.Riddle3Choice.NorthKoijuu
                        it.goToPage(PageId.Riddle3_North_Koijuu)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle3_optEast"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle3Choice = RiddleChoice.Riddle3Choice.EastWall
                        it.goToPage(PageId.Riddle3_East_Wall)
                    }
                ),
                Option(
                    text = { game.text["dg_de_stg2_riddle3_optSouth"] },
                    onOptionSelected = {
                        DepthsQuest.choices.riddle3Choice = RiddleChoice.Riddle3Choice.SouthSuccess
                        it.goToPage(PageId.Riddle3_South_Success)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle3_North_Koijuu,
            onPageShown = {
                para {
                    when (riddleSuccessesCount) {
                        2 -> game.text["dg_de_stg2_riddle3_north_quoijuu_para1_firstFail"]
                        1 -> game.text["dg_de_stg2_riddle3_north_quoijuu_para1_secondFail"]
                        0 -> game.text["dg_de_stg2_riddle3_north_quoijuu_para1_thirdFail"]
                        else -> error("Unexpected success count $riddleSuccessesCount")
                    }
                }
                para { game.text["dg_de_stg2_riddle3_north_quoijuu_para2"] }

                navigator.promptToContinue(game.text["continue"]) {
                    para { game.text["dg_de_stg2_riddle3_north_quoijuu_para3"] }
                    para { game.text["dg_de_stg2_riddle3_north_quoijuu_para4"] }

                    navigator.promptToContinue(game.text["continue"]) {
                        para { game.text["dg_de_stg2_riddle3_north_quoijuu_para5"] }
                        para { game.text["dg_de_stg2_riddle3_north_quoijuu_para6"] }
                    }
                }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle3_north_quoijuu_opt1"] },
                    onOptionSelected = { it.goToPage(PageId.Riddle3_Failed) }
                )
            )
        ),
        Page(
            id = PageId.Riddle3_East_Wall,
            onPageShown = {
                para {
                    when (wallCrashesCount) {
                        1 -> {
                            game.text["dg_de_stg2_riddle3_east_wall_firstWallFail_para1"]
                            game.text["dg_de_stg2_riddle3_east_wall_firstWallFail_para2"]
                            game.text["dg_de_stg2_riddle3_east_wall_firstWallFail_para3"]
                        }
                        2 -> game.text["dg_de_stg2_riddle3_east_wall_secondWallFail_para1"]
                        3 -> game.text["dg_de_stg2_riddle3_east_wall_thirdWallFail_para1"]
                        else -> error("Unexpected wall crash count $wallCrashesCount")
                    }
                }

            },
            options = listOf(
                Option(
                    showIf = { wallCrashesCount == 1 }, // Only failed this wall
                    text = { game.text["dg_de_stg2_riddle3_east_wall_firstWallFail_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle3_east_wall_firstWallFail_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle3_Failed)
                    }
                ),
                Option(
                    showIf = { wallCrashesCount == 2 }, // Only failed this wall and one other
                    text = { game.text["dg_de_stg2_riddle3_east_wall_secondWallFail_opt1"] },
                    onOptionSelected = {
                        para { game.text["dg_de_stg2_riddle3_east_wall_secondWallFail_opt1_onSelected"] }
                        it.goToPage(PageId.Riddle3_Failed)
                    }
                ),
                Option(
                    showIf = { wallCrashesCount == 3 }, // Failed all three.
                    text = { game.text["dg_de_stg2_riddle3_east_wall_thirdWallFail_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.Riddle3_Failed)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle3_Failed,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle3_failure_para1"] }
                para { game.text["dg_de_stg2_riddle3_failure_para2"] }
                para { game.text["dg_de_stg2_riddle3_failure_para3"] }
                para { game.text["dg_de_stg2_riddle3_failure_para4"] }

            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle3_failure_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewTreasure)
                    }
                )
            )
        ),
        Page(
            id = PageId.Riddle3_South_Success,
            onPageShown = {
                para { game.text["dg_de_stg2_riddle3_south_success_para1"] }
                para { game.text["dg_de_stg2_riddle3_south_success_para2"] }
                para { game.text["dg_de_stg2_riddle3_south_success_para3"] }
                para { game.text["dg_de_stg2_riddle3_south_success_para4"] }

            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_riddle3_south_success_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.ViewTreasure)
                    }
                )
            )
        ),
        Page(
            id = PageId.ViewTreasure,
            onPageShown = {
                para { game.text["dg_de_stg2_viewTreasure_para1"] }
                para { game.text["dg_de_stg2_viewTreasure_para2"] }

                navigator.promptToContinue(continueText = game.text["continue"]) {
                    para {
                        if (riddleSuccessesCount > 0)
                            game.text["dg_de_stg2_viewTreasure_para3_ifOthersSurvived"]
                        else
                            game.text["dg_de_stg2_viewTreasure_para3_ifOnlyPlayerSurvived"]
                    }
                    para { game.text["dg_de_stg2_viewTreasure_para4"] }
                    para {
                        if (riddleSuccessesCount > 0)
                            game.text["dg_de_stg2_viewTreasure_para5_ifOthersSurvived"]
                        else
                            game.text["dg_de_stg2_viewTreasure_para5_ifOnlyPlayerSurvived"]
                    }
                    para {
                        when {
                            riddleSuccessesCount == 3 -> game.text["dg_de_stg2_viewTreasure_para6_ifAllOthersSurvived"]
                            riddleSuccessesCount > 0 -> game.text["dg_de_stg2_viewTreasure_para6_ifSomeOthersSurvived"]
                            riddleSuccessesCount == 0 -> game.text["dg_de_stg2_viewTreasure_para6_ifOnlyPlayerSurvived"]
                            else -> error("Unexpected riddle success count $riddleSuccessesCount")
                        }
                    }
                }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_viewTreasure_opt1"] },
                    onOptionSelected = {
                        it.goToPage(PageId.BackAtTheSurface)
                    }
                )
            )
        ),
        Page(
            id = PageId.BackAtTheSurface,
            onPageShown = {
                para { game.text["dg_de_stg2_backAtSurface_para1"] }
                DepthsQuest.stopMusic()
                para {
                    if (riddleSuccessesCount > 0)
                        game.text["dg_de_stg2_backAtSurface_para2_ifOthersSurvived"]
                    else
                        game.text["dg_de_stg2_backAtSurface_para2_ifOnlyPlayerSurvived"]
                }
                para { game.text["dg_de_stg2_backAtSurface_para3"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_backAtSurface_opt1"] },
                    onOptionSelected = { it.goToPage(PageId.BackInSpace) }
                )
            )
        ),
        Page(
            id = PageId.BackInSpace,
            onPageShown = {
                para { game.text["dg_de_stg2_inSpace_para1"] }
            },
            options = listOf(
                Option(
                    text = { game.text["dg_de_stg2_inSpace_opt1"] },
                    onOptionSelected = {
                        DepthsQuest.startStart2()

                        dialog.visualPanel.showLoot(
                            game.text["dg_de_stg2_inSpace_salvaged_title"],
                            DepthsQuest.generateRewardLoot(dialog.interactionTarget),
                            false,
                            true,
                            false
                        ) {
                            it.close(doNotOfferAgain = true)
                        }

                        dialog.optionPanel.clearOptions()
                        dialog.promptText = ""
                    }
                )
            )
        )
    )
) {
    override fun createInstanceOfSelf() = Depths_Stage2_RiddleDialog()

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
        Riddle3_North_Koijuu,
        Riddle3_East_Wall,
        Riddle3_South_Success,
        Riddle3_Failed,
        ViewTreasure,
        BackAtTheSurface,
        BackInSpace
    }

    sealed class RiddleChoice {
        abstract fun wasSuccessful(): Boolean

        /**
         * If failure, Elias and Taddese die.
         */
        sealed class Riddle1Choice : RiddleChoice() {
            object EastMorg : Riddle1Choice() {
                override fun wasSuccessful() = false
            }

            object NorthSuccess : Riddle1Choice() {
                override fun wasSuccessful() = true
            }

            object SouthSmoke : Riddle1Choice() {
                override fun wasSuccessful() = false
            }

            object WestWall : Riddle1Choice() {
                override fun wasSuccessful() = false
            }
        }

        /**
         * If failure, Mussie dies.
         */
        sealed class Riddle2Choice : RiddleChoice() {
            object EastSuccess : Riddle2Choice() {
                override fun wasSuccessful() = true
            }

            object NorthVines : Riddle2Choice() {
                override fun wasSuccessful() = false
            }

            object WestWall : Riddle2Choice() {
                override fun wasSuccessful() = false
            }
        }

        /**
         * If failure, Jorma and Daciana die.
         */
        sealed class Riddle3Choice : RiddleChoice() {
            object NorthKoijuu : Riddle3Choice() {
                override fun wasSuccessful() = false
            }

            object EastWall : Riddle3Choice() {
                override fun wasSuccessful() = false
            }

            object SouthSuccess : Riddle3Choice() {
                override fun wasSuccessful() = true
            }
        }
    }
}