package org.wisp.stories

import wisp.questgiver.wispLib.Text

interface QuestFacilitator {
    /**
     * Idempotent method to configure `game.text.globalReplacementGetters`.
     */
    fun updateTextReplacements(text: Text)
}