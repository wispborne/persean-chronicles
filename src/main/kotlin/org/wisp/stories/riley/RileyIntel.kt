package org.wisp.stories.riley

import com.fs.starfarer.api.impl.campaign.ids.Tags
import wisp.questgiver.IntelDefinition

class RileyIntel : IntelDefinition(
    iconPath = null,
    title = { "" },
    subtitleCreator = {},
    descriptionCreator = { info, width, height ->

    },
    showDaysSinceCreated = true,
    intelTags = listOf(Tags.INTEL_STORY)
) {
    override fun createInstanceOfSelf() = RileyIntel()
}