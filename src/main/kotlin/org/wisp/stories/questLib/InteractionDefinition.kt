package org.wisp.stories.questLib

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.InteractionDialogPlugin
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.combat.EngagementResultAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

abstract class InteractionDefinition<S : InteractionDefinition<S>>(
    @Transient var onInteractionStarted: S.() -> Unit,
    @Transient var pages: List<Page<S>>,
    private val shouldValidateOnDialogStart: Boolean = true
) {
    class Page<S>(
        val id: Any,
        val image: Image? = null,
        val onPageShown: S.() -> Unit,
        val options: List<Option<S>>
    )

    open class Option<S>(
        val text: S.() -> String,
        val shortcut: Shortcut? = null,
        val showIf: S.() -> Boolean = { true },
        val onOptionSelected: S.(InteractionDefinition<*>.PageNavigator) -> Unit,
        val id: String = Misc.random.nextInt().toString()
    )

    /**
     * Create an instance of the implementing class. We then copy the transient fields in that class
     * to this one in [readResolve], since they do not get created by the deserializer.
     * We cannot use `this::class.java.newInstance()` because then the implementing class is required to have
     * a no-args constructor.
     */
    abstract fun createInstanceOfSelf(): InteractionDefinition<S>

    /**
     * When this class is created by deserializing from a save game,
     * it can't deserialize the anonymous methods, so we mark them as transient,
     * then manually assign them using this method, which gets called automagically
     * by the XStream serializer.
     */
    open fun readResolve(): Any {
        val newInstance = createInstanceOfSelf()
        onInteractionStarted = newInstance.onInteractionStarted
        pages = newInstance.pages
        return this
    }

//    interface PageNavigator<S> {
//        fun goToPage(pageId: Any)
//        fun gotoPage(page: Page<S>)
//        fun close(hideQuestOfferAfterClose: Boolean)
//    }

    open inner class PageNavigator() {
        open fun goToPage(pageId: Any) {
            showPage(pages.single { it.id == pageId })
        }

        open fun goToPage(page: Page<S>) {
            showPage(page)
        }

        open fun close(hideQuestOfferAfterClose: Boolean) {
            dialog.dismiss()
        }

        open fun showPage(page: Page<S>) {
            dialog.optionPanel.clearOptions()

            if (page.image != null) {
                dialog.visualPanel.showImagePortion(
                    page.image.category,
                    page.image.id,
                    page.image.width,
                    page.image.height,
                    page.image.xOffset,
                    page.image.yOffset,
                    page.image.displayWidth,
                    page.image.displayHeight
                )
            }

            page.onPageShown(this@InteractionDefinition as S)
            page.options
                .filter { it.showIf(this@InteractionDefinition) }
                .forEach { option ->
                    dialog.optionPanel.addOption(option.text(this@InteractionDefinition as S), option.id)

                    if (option.shortcut != null) {
                        dialog.optionPanel.setShortcut(
                            option.id,
                            option.shortcut.code,
                            option.shortcut.holdCtrl,
                            option.shortcut.holdAlt,
                            option.shortcut.holdShift,
                            false
                        )
                    }
                }
        }
    }

    /**
     * @param code constant from [org.lwjgl.input.Keyboard]
     */
    data class Shortcut(
        val code: Int,
        val holdCtrl: Boolean = false,
        val holdAlt: Boolean = false,
        val holdShift: Boolean = false
    )

    data class Image(
        val category: String,
        val id: String,
        val width: Float = 640f,
        val height: Float = 400f,
        val xOffset: Float = 0f,
        val yOffset: Float = 0f,
        val displayWidth: Float = 480f,
        val displayHeight: Float = 300f
    )

    lateinit var dialog: InteractionDialogAPI
    val navigator = PageNavigator()

    fun addPara(
        textColor: Color = Misc.getTextColor(),
        highlightColor: Color = Misc.getHighlightColor(),
        stringMaker: ParagraphText.() -> String
    ) = dialog.textPanel.addPara(textColor, highlightColor, stringMaker)


    /**
     * Needed so we can figure out which BarEvents are part of this mod
     * when looking at [BarEventManager.getInstance().active.items].
     */
    abstract inner class InteractionDialog : InteractionDialogPlugin

    fun build(): InteractionDialog {
        return object : InteractionDialog() {

            /**
             * Called when this class is instantiated.
             */
            init {
                if (shouldValidateOnDialogStart) {

                }
            }

            /**
             * Called when the dialog is shown.
             */
            override fun init(dialog: InteractionDialogAPI) {
                this@InteractionDefinition.dialog = dialog
                onInteractionStarted(this@InteractionDefinition as S)

                if (pages.any()) {
                    navigator.showPage(pages.first())
                }
            }

            override fun optionSelected(optionText: String?, optionData: Any?) {
                val optionSelected = pages
                    .flatMap { page ->
                        page.options
                            .filter { option -> option.id == optionData }
                    }.singleOrNull()
                    ?: return

                optionSelected.onOptionSelected(this@InteractionDefinition as S, navigator)
            }

            // Other overrides that are necessary but do nothing
            override fun optionMousedOver(optionText: String?, optionData: Any?) {
            }

            override fun getMemoryMap(): MutableMap<String, MemoryAPI> = mutableMapOf()
            override fun backFromEngagement(battleResult: EngagementResultAPI?) {
            }

            override fun advance(amount: Float) {
            }

            override fun getContext(): Any? = null
        }
    }
}