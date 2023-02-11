package wisp.perseanchronicles.common

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import wisp.perseanchronicles.game
import wisp.perseanchronicles.nirvana.NirvanaHubMission
import wisp.perseanchronicles.riley.RileyHubMission
import wisp.questgiver.spriteName
import wisp.questgiver.wispLib.empty

object PerseanChroniclesNPCs {
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
                        this.portraitSprite = "graphics/portraits/portrait20.png"
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
                        this.portraitSprite = game.settings.getSpriteName(RileyHubMission.icon.category, RileyHubMission.icon.id)
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
}