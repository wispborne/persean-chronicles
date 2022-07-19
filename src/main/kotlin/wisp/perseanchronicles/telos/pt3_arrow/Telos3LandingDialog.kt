package wisp.perseanchronicles.telos.pt3_arrow

import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.perseanchronicles.game
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query
import wisp.questgiver.wispLib.findFirst

class Telos3LandingDialog(
    stageJson: JSONObject = Telos3HubMission.part3Json.query("/stages/landOnPlanetFirst"),
    mission: Telos3HubMission = game.sector.intelManager.findFirst()!!
) : InteractionDialogLogic<Telos3LandingDialog>(
    onInteractionStarted = {

    },
    people = { listOf(DragonsQuest.karengo) },
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = mapOf(),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    else -> option
                }
            }
        }
    )
)