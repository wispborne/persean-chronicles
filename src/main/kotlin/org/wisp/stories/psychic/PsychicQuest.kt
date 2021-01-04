package org.wisp.stories.psychic

import org.wisp.stories.QuestFacilitator

class PsychicQuest: QuestFacilitator {


    override fun updateTextReplacements() {
        TODO("Not yet implemented")
    }

    enum class Stage {
        NotStarted,
        GoToPsychic,
        ReturnToOrigin,
        Done
    }
}