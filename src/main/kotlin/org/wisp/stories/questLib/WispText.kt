package org.wisp.stories.questLib

import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

private object WispText {
    const val startTag = "<mark>"
    const val endTag = "</mark>"
    val regex = """$startTag(.*?)$endTag""".toRegex()
}

fun TextPanelAPI.addPara(
    textColor: Color = Misc.getTextColor(),
    highlightColor: Color = Misc.getHighlightColor(),
    stringMaker: ParagraphText.() -> String
): LabelAPI? {
    val string = stringMaker(ParagraphText)
    return this.addPara(
        string.replace(WispText.regex, "%s"),
        textColor,
        highlightColor,
        *WispText.regex.findAll(string)
            .map { it.groupValues[1] }
            .toList()
            .toTypedArray()
    )
}

fun TooltipMakerAPI.addPara(
    padding: Float = 10f,
    textColor: Color = Misc.getTextColor(),
    highlightColor: Color = Misc.getHighlightColor(),
    stringMaker: ParagraphText.() -> String
): LabelAPI? {
    val string = stringMaker(ParagraphText)
    return this.addPara(
        string.replace(WispText.regex, "%s"),
        padding,
        textColor,
        highlightColor,
        *WispText.regex.findAll(string)
            .map { it.groupValues[1] }
            .toList()
            .toTypedArray()
    )
}

object ParagraphText {
    fun highlight(string: String) = "${WispText.startTag}$string${WispText.endTag}"
    fun mark(string: String) = highlight(string)
}