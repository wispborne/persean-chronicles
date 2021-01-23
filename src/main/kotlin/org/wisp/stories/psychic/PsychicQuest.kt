package org.wisp.stories.psychic

import org.wisp.stories.QuestFacilitator
import wisp.questgiver.wispLib.Text

class PsychicQuest: QuestFacilitator {


    override fun updateTextReplacements(text: Text) {
        TODO("Not yet implemented")
    }

    enum class Stage {
        NotStarted,
        GoToPsychic,
        ReturnToOrigin,
        Done
    }
}