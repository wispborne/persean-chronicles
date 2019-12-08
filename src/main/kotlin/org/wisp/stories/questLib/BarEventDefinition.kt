package org.wisp.stories.questLib

import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BarEventManager
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventWithPerson

abstract class BarEventDefinition<S : InteractionDefinition<S>>(
    @Transient private var shouldShowEvent: (MarketAPI) -> Boolean,
    @Transient var interactionPrompt: S.() -> Unit,
    @Transient var textToStartInteraction: S.() -> String,
    onInteractionStarted: S.() -> Unit,
    pages: List<Page<S>>,
    val personRank: String = Ranks.CITIZEN,
    val personFaction: String = Factions.INDEPENDENT,
    val personGender: FullName.Gender = FullName.Gender.ANY,
    val personPost: String = Ranks.CITIZEN,
    val personPortrait: String? = null
) : InteractionDefinition<S>(
    onInteractionStarted = onInteractionStarted,
    pages = pages
) {

    lateinit var manOrWoman: String
    lateinit var hisOrHer: String
    lateinit var heOrShe: String
    lateinit var event: BaseBarEventWithPerson

    /**
     * Needed so we can figure out which BarEvents are part of this mod
     * when looking at [BarEventManager.getInstance().active.items].
     */
    abstract inner class BarEvent : BaseBarEventWithPerson()

    /**
     * When this class is created by deserializing from a save game,
     * it can't deserialize the anonymous methods, so we mark them as transient,
     * then manually assign them using this method, which gets called automagically
     * by the XStream serializer.
     */
    override fun readResolve(): Any {
        val newInstance = this::class.java.newInstance()
        shouldShowEvent = newInstance.shouldShowEvent
        interactionPrompt = newInstance.interactionPrompt
        textToStartInteraction = newInstance.textToStartInteraction
        return super.readResolve()
    }

    fun buildBarEvent(): BarEvent {
        return object : BarEvent() {
            private val navigator = object :
                InteractionDefinition<*>.PageNavigator() {

                override fun close(hideQuestOfferAfterClose: Boolean) {
                    if (hideQuestOfferAfterClose) {
                        BarEventManager.getInstance().notifyWasInteractedWith(event)
                    }

                    noContinue = true
                    done = true
                }
            }

            override fun shouldShowAtMarket(market: MarketAPI?): Boolean =
                super.shouldShowAtMarket(market) && market?.let(shouldShowEvent) ?: true

            /**
             * Set up the text that appears when the player goes to the bar
             * and the option for them to init the conversation.
             */
            override fun addPromptAndOption(dialog: InteractionDialogAPI) {
                super.addPromptAndOption(dialog)
                regen(dialog.interactionTarget.market)
                this@BarEventDefinition.manOrWoman = manOrWoman
                this@BarEventDefinition.hisOrHer = hisOrHer
                this@BarEventDefinition.heOrShe = heOrShe
                this@BarEventDefinition.dialog = dialog
                this@BarEventDefinition.event = this
                interactionPrompt(this@BarEventDefinition as S)

                dialog.optionPanel.addOption(
                    textToStartInteraction(),
                    this as BaseBarEventWithPerson
                )
            }

            /**
             * Called when the player chooses to start the conversation.
             */
            override fun init(dialog: InteractionDialogAPI) {
                super.init(dialog)
                this.done = false
                dialog.visualPanel.showPersonInfo(this.person, true)
                onInteractionStarted(this@BarEventDefinition as S)

                if (pages.any()) {
                    showPage(pages.first())
                }
            }

            override fun optionSelected(optionText: String?, optionData: Any?) {
                val optionSelected = pages
                    .flatMap { page ->
                        page.options.filter { option ->
                            option.id == optionData
                        }
                    }.single()

                optionSelected.onOptionSelected(this@BarEventDefinition as S, navigator)
            }

            fun showPage(page: Page<S>) {
                if (noContinue || done) return

                dialog.optionPanel.clearOptions()

                page.onPageShown(this@BarEventDefinition as S)
                page.options.forEach { option ->
                    dialog.optionPanel.addOption(option.text(this@BarEventDefinition as S), option.id)

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
    }
}