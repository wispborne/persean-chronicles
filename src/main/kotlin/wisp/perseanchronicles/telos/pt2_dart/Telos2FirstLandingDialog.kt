package wisp.perseanchronicles.telos.pt2_dart

import com.fs.starfarer.api.util.Misc
import org.json.JSONObject
import wisp.perseanchronicles.dangerousGames.pt1_dragons.DragonsQuest
import wisp.questgiver.v2.InteractionDialogLogic
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.query

class Telos2FirstLandingDialog(
    stageJson: JSONObject = Telos2HubMission.part2Json.query("/stages/landOnPlanetFirst")
) : InteractionDialogLogic<Telos2FirstLandingDialog>(
    onInteractionStarted = {

    },
    people = { listOf(DragonsQuest.karengo) },
    pages = PagesFromJson(
        stageJson.query("/pages"),
        onPageShownHandlersByPageId = emptyMap(),
        optionConfigurator = { options ->
            options.map { option ->
                when (option.id) {
                    "requestMoreInfo" -> option.copy(
                        showIf = { Telos2HubMission.choices.askedForMorePsiconInfo == null },
                        onOptionSelected = {
                            Telos2HubMission.choices.askedForMorePsiconInfo = true
                        })
                    "afterYou" -> option.copy(
                        showIf = { Telos2HubMission.choices.toldKarengoToTakePsiconFirst == null },
                        onOptionSelected = {
                            Telos2HubMission.choices.toldKarengoToTakePsiconFirst = true
                        })
                    "injectSelf" -> option.copy(
                        onOptionSelected = {
                            Telos2HubMission.choices.injectedSelf = true
                        })
                    "noInject" -> option.copy(
                        text = if (Misc.random.nextFloat() > 0.98f) {
                            { """"Holy shit, no."""" } // 2% chance lol
                        } else {
                            option.text
                        },
                        showIf = { Telos2HubMission.choices.toldKarengoToTakePsiconFirst == true },
                        onOptionSelected = {
                            Telos2HubMission.choices.injectedSelf = false
                            navigator.close(doNotOfferAgain = false) // todo
                        }
                    )
                    else -> option
                }
            }
        }
    )
)