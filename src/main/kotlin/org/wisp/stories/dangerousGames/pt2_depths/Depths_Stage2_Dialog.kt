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
                    game.words.fmt(
                        "dg_de_stg2-goToPlanet_pg1_para1",
                        mapOf(
                            "ifPlayerOwnedWorld" to
                                    if (DepthsQuest.depthsPlanet?.market?.isPlayerOwned ?: false)
                                        game.words["dg_de_stg2-goToPlanet_pg1_para1_ifPlayerOwnedWorld"]
                                    else
                                        String.empty
                        )
                    )
                }

                para { game.words["dg_de_stg2-goToPlanet_pg1_para2"] }
            }
        )
    )
) {
    override fun createInstanceOfSelf() = Depths_Stage2_Dialog()
}