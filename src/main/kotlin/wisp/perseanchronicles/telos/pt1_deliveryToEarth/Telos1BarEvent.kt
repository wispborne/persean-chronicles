package wisp.perseanchronicles.telos.pt1_deliveryToEarth

import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.json.JSONArray
import org.json.JSONObject
import org.magiclib.kotlin.forEach
import wisp.perseanchronicles.common.PerseanChroniclesNPCs
import wisp.perseanchronicles.game
import wisp.perseanchronicles.nirvana.NirvanaHubMission
import wisp.questgiver.v2.BarEventLogic
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.json.InteractionPromptFromJson
import wisp.questgiver.v2.json.PagesFromJson
import wisp.questgiver.v2.json.TextToStartInteractionFromJson
import wisp.questgiver.v2.json.query

class Telos1BarEventLogic(
) : BarEventLogic<Telos1HubMission>() {
    val stageJson: JSONObject by lazy { Telos1HubMission.part1Json.query("/stages/deliveryToEarth") }
    override fun createInteractionPrompt() =
        InteractionPromptFromJson<Telos1BarEventLogic>(barEventJson = stageJson.getJSONObject("barEvent"))()

    override fun onInteractionStarted() {
        dialog.visualPanel.showMapMarker(
            Telos1HubMission.state.karengoSystem?.hyperspaceAnchor,
            TextToStartInteractionFromJson<BarEventLogic<Telos1HubMission>>(
                barEventJson = stageJson.getJSONObject("barEvent")
            ).invoke(
                this as Telos1BarEventLogic
            ).text,
            Misc.getTextColor(),
            true,
            mission.icon,
            null,
            Telos1HubMission.tags.minus(Tags.INTEL_ACCEPTED).toSet()
        )
    }

    override fun textToStartInteraction() = TextToStartInteractionFromJson<Telos1BarEventLogic>(barEventJson = stageJson.getJSONObject("barEvent"))()

    override fun pages() = object : PagesFromJson<Telos1BarEventLogic>() {
        override fun pagesJson(): JSONArray {
            return stageJson.getJSONArray("pages")
        }

        override fun onPageShownHandlersByPageId() = mapOf(
            "1" to {
                val page = navigator.currentPage()

                if (page != null) {
                    // If player has started Nirvana quest
                    if (NirvanaHubMission.state.startDateMillis != null) {
                        // "Ah," she says, glancing up from her TriPad. "You've done work for us before." It's not phrased as a question, but you nod all the same.
                        (page.extraData["freetext1-worked-with-nirvana"] as JSONArray).forEach<String> { para { it } }

                        // Started but not finished Nirvana yet.
                        if (NirvanaHubMission.state.completeDateInMillis == null) {
                            // If it has been less than 1 year since player took the Nirvana mission
                            if ((game.sector.clock.timestamp - NirvanaHubMission.state.startDateMillis!!) < 31556926000) {
                                // "Actually it looks like...the contract is still open. But that's not what I'm here about today. To business.
                                (page.extraData["freetext2-nirvanaquest-in-progress"] as JSONArray).forEach<String> { para { it } }
                            } else {
                                // If it's been more than a year lol
                                // "Actually it looks like...the contract is still open. And has been for," she blows out some air, "quite a while."
                                // "But that's not what I'm here about, although I know my colleagues would appreciate that delivery. Let's talk about today's business.
                                (page.extraData["freetext2-nirvanaquest-in-progress-over-1-cycle"] as JSONArray).forEach<String> { para { it } }
                            }
                        }
                    }
                }
            }
        )

        override fun optionConfigurator() = { options: List<IInteractionLogic.Option<Telos1BarEventLogic>> ->
            options.map { option ->
                when (option.id) {
                    "done" -> option.copy(
                        onOptionSelected = {
                            mission.accept(dialog, null)
                            it.close(doNotOfferAgain = true)
                        })

                    "decline" -> option.copy(
                        onOptionSelected = {
                            it.close(doNotOfferAgain = false)
                        })

                    else -> option
                }
            }
        }
    }

    override fun people() =
        { listOf(PerseanChroniclesNPCs.kellyMcDonald) }
}