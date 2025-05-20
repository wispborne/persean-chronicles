package wisp.questgiver.v2.json

import com.fs.starfarer.api.util.Misc
import org.json.JSONArray
import org.json.JSONObject
import org.lwjgl.input.Keyboard
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.wispLib.*
import java.awt.Color
import java.util.function.IntFunction
import kotlin.random.Random

/**
 * @param pagesJson eg
 *  ```kt
 *  Global.getSettings()
 *  .getMergedJSONForMod(jsonPath, modId)
 *  .getJSONObject(questName)
 *  .getJSONArray("stages")
 *  .getJSONObject(stageIndex)
 *  .getJSONArray("pages")
 *  ```
 * @param onPageShownHandlersByPageId Called _after_ displaying text in `paras` but _before_ running
 *   anything in the json `onPageShown`.
 *   @param optionConfigurator Common usage:
 *   ```kt
 *   "leave" -> option.copy(
 *     onOptionSelected = {
 *         navigator.close(doNotOfferAgain = true)
 *     }
 * )
 * ```
 */
abstract class PagesFromJson<S : IInteractionLogic>(
    private val pages: MutableList<IInteractionLogic.Page<IInteractionLogic>> = mutableListOf()
) : List<IInteractionLogic.Page<IInteractionLogic>> by pages {
    abstract fun pagesJson(): JSONArray
    abstract fun onPageShownHandlersByPageId(): Map<String, () -> Any?>
    abstract fun optionConfigurator(): (options: List<IInteractionLogic.Option<S>>) -> List<IInteractionLogic.Option<S>>

    init {
        pagesJson().forEach<JSONObject> { page ->
            val pageId = page.optional<String>("id")
            pages.add(
                IInteractionLogic.Page<S>(
                    id = pageId ?: Random.nextInt().toString(),
                    image = page.optJSONObject("image")?.let {
                        IInteractionLogic.Image(
                            category = it.getObj("category"),
                            id = it.getObj("id"),
                            width = it.tryGet("width") { 128f },
                            height = it.tryGet("height") { 128f },
                            xOffset = it.tryGet("xOffset") { 0f },
                            yOffset = it.tryGet("yOffset") { 0f },
                            displayWidth = it.tryGet("displayWidth") { 128f },
                            displayHeight = it.tryGet("displayHeight") { 128f },
                        )
                    },
                    onPageShown = {
                        page.optional<JSONArray>("paras")?.forEach<String> { text ->
                            para { text.qgFormat() }
                        }

                        onPageShownHandlersByPageId()[pageId]?.invoke()
                        page.optional<JSONObject>("onPageShown")?.run { goToPageIfPresent(this, navigator as IInteractionLogic.IPageNavigator<S>) }
                    },
                    options = page.optional<JSONArray>("options")
                        ?.map<JSONObject, IInteractionLogic.Option<S>> { optionJson ->
                            val optionId = optionJson.tryGet("id") { Misc.random.nextInt().toString() }
                            val text = optionJson.getObj<String>("text").qgFormat()
                            val highlightData = TextExtensions.getTextHighlightData(text)

                            IInteractionLogic.Option(
                                id = optionId,
                                text = { highlightData.newString },
                                textColor = optionJson.optional<String>("textColor")
                                    .let { runCatching { Color.getColor(it) }.getOrNull() }
                                    ?: highlightData.replacements.firstOrNull()?.highlightColor,
                                tooltip = optionJson.optional<String>("tooltip")?.qgFormat()?.let { { it } },
                                shortcut = optionJson.optional<String>("shortcut")?.let { shortcut ->
                                    runCatching {
                                        IInteractionLogic.Shortcut(
                                            code = Keyboard.getKeyIndex(shortcut.uppercase()).takeIf { it > 0 }!!,
                                            holdCtrl = false,
                                            holdAlt = false,
                                            holdShift = false,
                                        )
                                    }
                                        .onFailure {
                                            throw RuntimeException(
                                                "Invalid key '$shortcut'. Options are [${keyboardKeys().joinToString()}].",
                                                it
                                            )
                                        }
                                        .getOrNull()
                                },
                                showIf = { optionJson.optBoolean("showIf", true) },
                                onOptionSelected = { navigator ->
                                    goToPageIfPresent(optionJson, navigator as IInteractionLogic.IPageNavigator<S>)
                                    navigator.refreshOptions()
                                },
                                flagToSet = optionJson.optional<String>("setFlag"),
                                hideOptionIfFlagTrue = optionJson.optional<String>("hideIfFlagTrue")
                            )
                        }
                        ?.let { originalOptions ->
                            // Oh boy.
                            // We take in the List<Option> that were created from the .json.
                            // Then we pass that to `optionConfigurator`, which returns them but modified as the user sees fit.
                            // However, modified options may have replaced the `onOptionSelected` logic from the json,
                            // which we want to keep.
                            // So, check to see if that was changed and, if so, call the original `onOptionSelected` after
                            // calling the modified one.
                            optionConfigurator().invoke(originalOptions)
                                .map { modifiedOption ->
                                    val originalOption =
                                        originalOptions.single { origOpt -> origOpt.id == modifiedOption.id }

                                    modifiedOption.copy(
                                        onOptionSelected = {
                                            modifiedOption.onOptionSelected.invoke(it)

                                            if (modifiedOption.onOptionSelected !== originalOption.onOptionSelected) {
                                                if (!modifiedOption.disableAutomaticHandling) {
                                                    originalOption.onOptionSelected.invoke(it)
                                                }
                                            }
                                        }
                                    )
                                }
                        }
                        ?: emptyList(),
                    extraData = page.names()
                        .map<String, Pair<String, Any>> { it to page[it] }
                        .toMap()
                ) as IInteractionLogic.Page<IInteractionLogic>
            )
        }
    }

    private fun goToPageIfPresent(
        optionJson: JSONObject,
        navigator: IInteractionLogic.IPageNavigator<S>
    ) {
        optionJson.optional<String>("goToPage")?.let { pageId -> navigator.goToPage(pageId = pageId) }
    }
}

fun keyboardKeys(): List<String> = (0 until Keyboard.getKeyCount())
    .mapNotNull { runCatching { Keyboard.getKeyName(it) }.getOrNull() }


fun getPageById(pagesJson: JSONArray, pageId: String) =
    pagesJson
        .map<Any, JSONObject> { it as JSONObject }
        .firstOrNull {
            it.optString("id") == pageId
        }