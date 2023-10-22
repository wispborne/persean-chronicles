package wisp.perseanchronicles.common

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import org.magiclib.util.MagicCampaign
import wisp.perseanchronicles.game
import wisp.perseanchronicles.nirvana.NirvanaHubMission
import wisp.perseanchronicles.riley.RileyHubMission
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.spriteName
import wisp.questgiver.wispLib.Memory
import wisp.questgiver.wispLib.empty

object PerseanChroniclesNPCs {
    // The $ is automatically added. These are set to sector Memory.
    var isKarengoInFleet: Boolean by Memory("isKarengoInFleet") { false }
    var isLaborerInFleet: Boolean by Memory("isLaborerInFleet") { false }
    var isRileyInFleet: Boolean by Memory("isRileyInFleet") { false }

    val karengo: PersonAPI
        get() {
            val key = "karengo"
            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName("Karengo", "", FullName.Gender.MALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = game.settings.getSpriteName("wisp_perseanchronicles_dragonriders", "karengo")
                    }
            }

            return game.memory[key] as PersonAPI
        }


    val riley: PersonAPI
        get() {
            val key = "riley"
            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName(game.text["riley_name"], String.empty, FullName.Gender.FEMALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = RileyHubMission.icon.spriteName(game)
                    }
            }

            return game.memory[key] as PersonAPI
        }

    val riley_dad: PersonAPI
        get() {
            val key = "riley_dad"
            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName(game.text["rileyDad_name"], String.empty, FullName.Gender.MALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = RileyHubMission.dadPortrait.spriteName(game)
                    }
            }

            return game.memory[key] as PersonAPI
        }
    val riley_dad2: PersonAPI
        get() {
            val key = "riley_dad2"
            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName(game.text["rileyDad_name"], String.empty, FullName.Gender.MALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = RileyHubMission.dadPortrait2.spriteName(game)
                    }
            }

            return game.memory[key] as PersonAPI
        }

    val davidRengal: PersonAPI
        get() {
            val key = "davidRengal"

            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName("David", "Rengel", FullName.Gender.MALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = NirvanaHubMission.icon.spriteName(game)
                    }
            }

            return game.memory[key] as PersonAPI
        }


    val kellyMcDonald: PersonAPI
        get() {
            val key = "kellyMcDonald"

            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName("Kelly", "McDonald", FullName.Gender.FEMALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite = "graphics/portraits/portrait27.png"
                    }
            }

            return game.memory[key] as PersonAPI
        }

    val captainEugel: PersonAPI = game.memory["capEugel"] as? PersonAPI? ?: kotlin.run {
        MagicCampaign.createCaptainBuilder(Factions.LUDDIC_CHURCH)
            .setFirstName("Captain")
            .setLastName("Eugel")
            .setGender(FullName.Gender.MALE)
            .setPortraitId("graphics/portraits/portrait_hegemony05.png")
            .setRankId(Ranks.SPACE_COMMANDER)
            .setPostId(Ranks.POST_FLEET_COMMANDER)
            .setPersonality(Personalities.STEADY)
            .setLevel(10)
            .create()
            .also { game.memory["capEugel"] = it }
    }


    val dale: PersonAPI
        get() {
            val key = "dale"
            if (game.memory[key] == null) {
                game.memory[key] =
                    Global.getSettings().createPerson().apply {
                        this.name = FullName("Dale", String.empty, FullName.Gender.MALE)
                        this.setFaction(Factions.INDEPENDENT)
                        this.postId = Ranks.CITIZEN
                        this.rankId = Ranks.CITIZEN
                        this.portraitSprite =
                            IInteractionLogic.Portrait(category = "wisp_perseanchronicles_laborer", id = "portrait").spriteName(game)
                    }
            }

            return game.memory[key] as PersonAPI
        }
}