package org.wisp.stories.riley

import org.wisp.stories.QuestFacilitator

class RileyQuest: QuestFacilitator {


    override fun updateTextReplacements() {
        TODO("Not yet implemented")
    }

    enum class Stage {
        NotStarted,
        GoToFather,
        Completed
    }
}