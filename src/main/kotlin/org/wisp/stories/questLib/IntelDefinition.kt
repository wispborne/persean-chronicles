package org.wisp.stories.questLib

import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.intel.misc.BreadcrumbIntel
import com.fs.starfarer.api.ui.ButtonAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.wisp.stories.wispLib.di

/**
 * @param iconPath get via [com.fs.starfarer.api.SettingsAPI.getSpriteName]
 * @param subtitleCreator the small summary on the left Intel panel sidebar
 * @param descriptionCreator the intel description on the right Intel panel sidebar
 */
abstract class IntelDefinition(
    @Transient var iconPath: (IntelDefinition.() -> String)? = null,
    @Transient var title: (IntelDefinition.() -> String)? = null,
    @Transient var subtitleCreator: (IntelDefinition.(info: TooltipMakerAPI?) -> Unit)? = null,
    var durationInDays: Float = Float.NaN,
    @Transient var descriptionCreator: (IntelDefinition.(info: TooltipMakerAPI, width: Float, height: Float) -> Unit)? = null,
    val showDaysSinceCreated: Boolean = false,
    val intelTags: List<String>,
    var startLocation: SectorEntityToken? = null,
    var endLocation: SectorEntityToken? = null,
    var removeIntelIfAnyOfTheseEntitiesDie: List<SectorEntityToken> = emptyList(),
    var soundName: String? = null,
    important: Boolean = false
) : BaseIntelPlugin() {
    companion object {
        val padding = 3f
        val bulletPointPadding = 10f
    }

    init {
        isImportant = important

        iconPath?.run { di.settings.loadTexture(this.invoke(this@IntelDefinition)) }

        di.sector.addScript(this)
    }

    /**
     * Create an instance of the implementing class. We then copy the transient fields in that class
     * to this one in [readResolve], since they do not get created by the deserializer.
     * We cannot use `this::class.java.newInstance()` because then the implementing class is required to have
     * a no-args constructor.
     */
    abstract fun createInstanceOfSelf(): IntelDefinition

    /**
     * When this class is created by deserializing from a save game,
     * it can't deserialize the anonymous methods, so we mark them as transient,
     * then manually assign them using this method, which gets called automagically
     * by the XStream serializer.
     */
    open fun readResolve(): Any {
        val newInstance = createInstanceOfSelf()
        title = newInstance.title
        iconPath = newInstance.iconPath
        subtitleCreator = newInstance.subtitleCreator
        descriptionCreator = newInstance.descriptionCreator
        @Suppress("SENSELESS_COMPARISON")
        if (removeIntelIfAnyOfTheseEntitiesDie == null) removeIntelIfAnyOfTheseEntitiesDie = emptyList()

        iconPath?.run { di.settings.loadTexture(this.invoke(this@IntelDefinition)) }
        return this
    }

    fun flipStartAndEndLocations() {
        val oldEnd = endLocation
        endLocation = startLocation
        startLocation = oldEnd
    }

    final override fun addGenericButton(info: TooltipMakerAPI?, width: Float, text: String?, data: Any?): ButtonAPI {
        return super.addGenericButton(info, width, text, data)
    }

    override fun shouldRemoveIntel(): Boolean {
        if (removeIntelIfAnyOfTheseEntitiesDie.any { !it.isAlive }
            || endLocation?.isAlive == false) {
            return true
        }

        val intelStartedTimestamp = playerVisibleTimestamp

        // Remove intel if duration has elapsed
        if (durationInDays.isFinite()
            && intelStartedTimestamp != null
            && di.sector.clock.getElapsedDaysSince(intelStartedTimestamp) >= durationInDays
        ) {
            return true
        }

        return super.shouldRemoveIntel()
    }

    final override fun createIntelInfo(info: TooltipMakerAPI, mode: IntelInfoPlugin.ListInfoMode?) {
        title?.let {
            info.addPara(
                textColor = getTitleColor(mode),
                padding = 0f
            ) { title!!.invoke(this@IntelDefinition) }
        }
        subtitleCreator?.invoke(this, info)
    }

    final override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        descriptionCreator?.invoke(this, info, width, height)

        if (showDaysSinceCreated && daysSincePlayerVisible > 0) {
            addDays(info, "ago.", daysSincePlayerVisible, Misc.getTextColor(), bulletPointPadding)
        }
    }

    final override fun hasSmallDescription(): Boolean = descriptionCreator != null

    override fun getIcon(): String = iconPath?.invoke(this@IntelDefinition)
        ?: di.settings.getSpriteName("intel", "fleet_log")
        ?: super.getIcon()

    override fun getCommMessageSound(): String {
        return soundName
            ?: getSoundLogUpdate()
            ?: super.getCommMessageSound()
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return super.getIntelTags(map)
            .apply { this += intelTags }
    }

    override fun getSortString(): String = "Location"

    override fun getSmallDescriptionTitle(): String? = title?.invoke(this@IntelDefinition)

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? =
        endLocation?.starSystem?.center
            ?: endLocation

    override fun getArrowData(map: SectorMapAPI?): MutableList<IntelInfoPlugin.ArrowData>? {
        val startLocationInner = startLocation ?: return null

        // If start and end are same, no arrow
        if (startLocationInner.containingLocation == endLocation?.containingLocation
            && startLocationInner.containingLocation?.isHyperspace != true
        ) {
            return null
        }

        return mutableListOf(
            IntelInfoPlugin.ArrowData(startLocationInner, endLocation)
                .apply {
                    color = factionForUIColors?.baseUIColor
                })
    }

    override fun notifyEnded() {
        super.notifyEnded()
        di.sector.removeScript(this)
    }
}