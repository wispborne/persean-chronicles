package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Drops
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.perseanchronicles.telos.TelosCommon
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.getPageById
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.addCommodityGainText
import wisp.questgiver.wispLib.findFirst
import kotlin.math.roundToInt

class Telos3LandingDialog(
    stageJson: JSONObject = Telos3HubMission.part3Json.query("/stages/goToPlanet"),
    mission: Telos3HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic<Telos3LandingDialog>(
    onInteractionStarted = {

    },
    people = { listOfNotNull(DragonsHubMission.karengo) },
    firstPageSelector = {
        if (Telos3HubMission.state.visitedPrimaryPlanet == true)
            this.single { it.id == "4-go-inside" }
        else
            this.first()
    },
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(
            "1-start" to {
                TelosCommon.playThemeMusic()
                Telos3HubMission.state.visitedPrimaryPlanet = true
            },
            "4-labs" to {
                Telos3HubMission.state.visitedLabs = true
            },
            "4-survivors" to {
                Telos3HubMission.state.searchedForSurvivors = true
            },
            "4-common-areas" to {
                Telos3HubMission.state.sawKryptaDaydream = true
            },
            "4-labs-2" to {
                if (Telos3HubMission.state.etherVialChoice == null)
                    para { getPageById(stageJson.query("/pages"), "4-labs-2")?.optString("vials") ?: "" }
            },
            "4-storage" to {
                if (Telos3HubMission.state.retrievedSupplies != true) {
                    val random = Misc.getRandom(game.sector.memoryWithoutUpdate.getLong(MemFlags.SALVAGE_SEED), 100)
                    dialog.interactionTarget.addDropValue(Drops.SUPPLY, (game.sector.playerFleet.totalSupplyCostPerDay * 3000).roundToInt())
                    val supplies = SalvageEntity.generateSalvage(
                        random,
                        1f,
                        1f,
                        1f,
                        1f,
                        dialog.interactionTarget.dropValue,
                        dialog.interactionTarget.dropRandom
                    ).supplies.roundToInt()
                    dialog.interactionTarget.dropValue.clear()

                    // todo add any that don't fit onboard to orbit
                    dialog.textPanel.addCommodityGainText(commodityId = Commodities.SUPPLIES, quantity = supplies)
                    Telos3HubMission.state.retrievedSupplies = true
                }
            }
        ),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    // Let player collect supplies only once.
                    "search-storage" -> option.copy(
                        showIf = { Telos3HubMission.state.retrievedSupplies != true })

                    "take-ether" -> option.copy(
                        showIf = { Telos3HubMission.state.etherVialChoice == null },
                        onOptionSelected = { Telos3HubMission.state.etherVialChoice = Telos3HubMission.EtherVialsChoice.Took })

                    "destroy-ether" -> option.copy(
                        showIf = { Telos3HubMission.state.etherVialChoice == null },
                        onOptionSelected = { Telos3HubMission.state.etherVialChoice = Telos3HubMission.EtherVialsChoice.Destroyed })

                    "return-to-orbit" -> option.copy(
                        onOptionSelected = { }
                    )

                    "leave" -> {
                        option.copy(onOptionSelected = {
                            mission.setCurrentStage(Telos3HubMission.Stage.EscapeSystem, null, null)
                            this.navigator.close(doNotOfferAgain = true)
                        })
                    }

                    else -> option
                }
            }
        }
    )
)