package org.wisp.stories

interface QuestFacilitator {
    /**
     * Idempotent method to configure `game.text.globalReplacementGetters`.
     */
    fun updateTextReplacements()
}