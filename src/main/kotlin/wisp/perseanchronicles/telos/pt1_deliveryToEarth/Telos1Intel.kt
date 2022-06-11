//package wisp.perseanchronicles.telos.pt1_deliveryToEarth
//
//import com.fs.starfarer.api.campaign.SectorEntityToken
//import com.fs.starfarer.api.impl.campaign.ids.Tags
//import com.fs.starfarer.api.util.Misc
//import org.json.JSONObject
//import wisp.perseanchronicles.game
//import wisp.questgiver.*
//import wisp.questgiver.json.query
//import wisp.questgiver.wispLib.asList
//import wisp.questgiver.wispLib.preferredConnectedEntity
//import wisp.questgiver.wispLib.qgFormat
//
//class Telos1Intel(
//    startLocation: SectorEntityToken?,
//    endLocation: SectorEntityToken?,
//    stringsJson: JSONObject = Telos1Quest.json.query("strings/intel") as JSONObject
//) : IntelDefinition(
//    iconPath = { game.settings.getSpriteName(Telos1Quest.icon.category, Telos1Quest.icon.id) },
//    title = {
//        if (Telos1Quest.stage.progress != AutoQuestFacilitator.Stage.Progress.Completed)
//            stringsJson.getString("title")
//        else
//            stringsJson.getString("titleComplete")
//    },
//    subtitleCreator = { info ->
//        if (Telos1Quest.stage.progress != AutoQuestFacilitator.Stage.Progress.Completed) {
//            bullet(info)
//            info.addPara(
//                padding = 0f,
//                textColor = Misc.getGrayColor()
//            ) { stringsJson.getString("subtitle").qgFormat() }
//        }
//    },
//    descriptionCreator = { info, width, _ ->
//        info.addImage(
//            Telos1Quest.icon.spriteName(game),
//            width,
//            128f,
//            Padding.DESCRIPTION_PANEL
//        )
//        info.addPara(
//            padding = Padding.DESCRIPTION_PANEL,
//            textColor = textColorOrElseGrayIf { Telos1Quest.stage.progress == AutoQuestFacilitator.Stage.Progress.Completed }) {
//            stringsJson.getString("desc").qgFormat()
//        }
//
////        if (Telos1Quest.stage.isCompleted) {
////            when {
////                Telos1Quest.choices.destroyedTheCore == true -> {
////                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
////                        game.text["riley_intel_description_done_destroyed"]
////                    }
////                }
////                Telos1Quest.choices.turnedInForABounty == true -> {
////                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
////                        game.text["riley_intel_description_done_bounty"]
////                    }
////                }
////                Telos1Quest.choices.leftRileyWithFather == true -> {
////                    info.addPara(padding = Padding.DESCRIPTION_PANEL) {
////                        game.text["riley_intel_description_done_leftAlone"]
////                    }
////                }
////            }
////        }
//    },
//    startLocation = startLocation?.market,
//    endLocation = endLocation?.market,
//    removeIntelIfAnyOfTheseEntitiesDie = endLocation.asList(),
//    important = true,
//    intelTags = Telos1Quest.tags
//) {
//    override fun createInstanceOfSelf() =
//        Telos1Intel(startLocation?.preferredConnectedEntity, endLocation?.preferredConnectedEntity)
//}