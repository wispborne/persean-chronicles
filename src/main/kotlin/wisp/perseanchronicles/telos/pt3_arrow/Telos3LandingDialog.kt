package wisp.perseanchronicles.telos.pt3_arrow

import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsHubMission
import wisp.perseanchronicles.game
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
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
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    // Let player collect supplies only once.
                    "search-storage" -> option.copy(
                        showIf = { Telos3HubMission.choices.retrievedSupplies != true },
                        onOptionSelected = {
                            val random = Misc.getRandom(game.sector.memoryWithoutUpdate.getLong(MemFlags.SALVAGE_SEED), 100)
                            val salvage =
                                SalvageEntity.generateSalvage(
                                    random,
                                    1f,
                                    1f,
                                    1f,
                                    1f,
                                    dialog.interactionTarget.dropValue,
                                    dialog.interactionTarget.dropRandom
                                )
                            dialog.textPanel.addCommodityGainText(commodityId = Commodities.SUPPLIES, quantity = salvage.supplies.roundToInt())
                            Telos3HubMission.choices.retrievedSupplies = true
                        })

                    "take-ether" -> option.copy(
                        showIf = { Telos3HubMission.choices.tookEtherVials == null && Telos3HubMission.choices.destroyedEtherVials == null },
                        onOptionSelected = { Telos3HubMission.choices.tookEtherVials = true })

                    "destroy-ether" -> option.copy(
                        showIf = { Telos3HubMission.choices.tookEtherVials == null && Telos3HubMission.choices.destroyedEtherVials == null },
                        onOptionSelected = { Telos3HubMission.choices.destroyedEtherVials = true })

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