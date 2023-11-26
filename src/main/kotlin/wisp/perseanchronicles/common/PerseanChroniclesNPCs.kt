package wisp.perseanchronicles.common

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import org.magiclib.util.MagicCampaign
import wisp.perseanchronicles.MOD_ID
import wisp.perseanchronicles.game
import wisp.perseanchronicles.nirvana.NirvanaHubMission
import wisp.perseanchronicles.riley.RileyHubMission
import wisp.questgiver.v2.IInteractionLogic
import wisp.questgiver.v2.spriteName
import wisp.questgiver.wispLib.Memory
import wisp.questgiver.wispLib.empty

object PerseanChroniclesNPCs {

    // ==Mod integration notes==
    // =Global memory (sector memory)=
    //
    // $wisp_perseanchronicles_rileyState
    // $wisp_perseanchronicles_dragonState
    // $wisp_perseanchronicles_depthsState
    // $wisp_perseanchronicles_telosPt1State
    //
    // are Map<String, Object>
    // and they all have
    //
    // startDateMillis (Long)
    // completeDateInMillis (Long)
    //
    // if both are null, hasn't been started, if just completeDateInMillis is null, has been started but not finished

    // The $ is automatically added. These are set to sector Memory.
    // NOTE: THESE HAVE "wisp_perseanchronicles_" PREFIXES
    // Example usage: `$wisp_perseanchronicles_isKarengoInFleet`.
    var isKarengoInFleet: Boolean by Memory("isKarengoInFleet") { false }
    var isLaborerInFleet: Boolean by Memory("isLaborerInFleet") { false }
    var isRileyInFleet: Boolean by Memory("isRileyInFleet") { false }

    val karengo: PersonAPI
        get() {
            val key = "karengo"
            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName("Karengo", "", FullName.Gender.MALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = game.settings.getSpriteName("wisp_perseanchronicles_dragonriders", "karengo")
            }
        }


    val riley: PersonAPI
        get() {
            val key = "riley"
            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName(game.text["riley_name"], String.empty, FullName.Gender.FEMALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = RileyHubMission.icon.spriteName(game)
            }
        }

    val riley_dad: PersonAPI
        get() {
            val key = "riley_dad"
            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName(game.text["rileyDad_name"], String.empty, FullName.Gender.MALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = RileyHubMission.dadPortrait.spriteName(game)
            }
        }
    val riley_dad2: PersonAPI
        get() {
            val key = "riley_dad2"
            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName(game.text["rileyDad_name"], String.empty, FullName.Gender.MALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = RileyHubMission.dadPortrait2.spriteName(game)
            }
        }

    val davidRengal: PersonAPI
        get() {
            val key = "davidRengal"

            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName("David", "Rengel", FullName.Gender.MALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = NirvanaHubMission.icon.spriteName(game)
            }
        }


    val kellyMcDonald: PersonAPI
        get() {
            val key = "kellyMcDonald"

            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName("Kelly", "McDonald", FullName.Gender.FEMALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite = "graphics/portraits/portrait27.png"
            }
        }

    val captainEugel: PersonAPI
        get() {
            val key = "capEugel"
            return (game.memory[key] as? PersonAPI? ?: kotlin.run {
                MagicCampaign.createCaptainBuilder(Factions.LUDDIC_CHURCH)
                    .setFirstName("Captain")
                    .setLastName("Eugel")
                    .setGender(FullName.Gender.MALE)
                    .setRankId(Ranks.SPACE_COMMANDER)
                    .setPostId(Ranks.POST_FLEET_COMMANDER)
                    .setPersonality(Personalities.STEADY)
                    .setLevel(10)
                    .create()
                    .apply {
                        game.memory[key] = this
                    }
            }).apply {
                this.id = "${MOD_ID}_$key"
                this.portraitSprite =
                    IInteractionLogic.Portrait(category = "wisp_perseanchronicles_telos", id = "eugel_portrait").spriteName(game)
            }
        }


    val dale: PersonAPI
        get() {
            val key = "dale"
            if (game.memory[key] == null) {
                game.memory[key] = Global.getSettings().createPerson()
            }

            return (game.memory[key] as PersonAPI).apply {
                this.id = "${MOD_ID}_$key"
                this.name = FullName("Dale", String.empty, FullName.Gender.MALE)
                this.setFaction(Factions.INDEPENDENT)
                this.postId = Ranks.CITIZEN
                this.rankId = Ranks.CITIZEN
                this.portraitSprite =
                    IInteractionLogic.Portrait(category = "wisp_perseanchronicles_laborer", id = "portrait").spriteName(game)
            }
        }
}