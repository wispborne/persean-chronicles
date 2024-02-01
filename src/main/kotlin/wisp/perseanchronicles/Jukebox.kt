package wisp.perseanchronicles

object Jukebox {
    var isSoundPlayerBusted: Boolean = false
    var currentCustomMusicId: String? = null

    enum class Song(val id: String) {
        TELOS_THEME("wisp_perseanchronicles_telosThemeMusic"),
        DOOMED("wisp_perseanchronicles_telosDoomedMusic"),
        EVASION("wisp_perseanchronicles_telosEvasionMusic"),
        EUGEL_MEETING("wisp_perseanchronicles_telosEscapeEugelDialogMusic")
    }

    fun playSong(song: Song, fadeOutSecs: Int = 0, fadeInSecs: Int = 1, loop: Boolean = true) {
        playSong(song.id, fadeOutSecs, fadeInSecs, loop)
    }

    fun playSong(songId: String?, fadeOutSecs: Int = 1, fadeInSecs: Int = 1, loop: Boolean = true) {
        if (isSoundPlayerBusted) return

        // Note that `game.soundPlayer.currentMusicId` returns the .ogg file name, not the songId.
        if (songId != null && currentCustomMusicId == songId && game.soundPlayer.currentMusicId != null) {
            return
        }

        kotlin.runCatching {
            game.logger.d { "Starting to play ${songId}." }
            game.soundPlayer.setSuspendDefaultMusicPlayback(true)
            game.soundPlayer.playCustomMusic(fadeOutSecs, fadeInSecs, songId, loop)
            currentCustomMusicId = songId
        }
            .onFailure {
                isSoundPlayerBusted = true
                game.logger.w(it)
            }
    }

    fun stopSong() {
        game.logger.d { "Pausing song '${game.soundPlayer.currentMusicId}'." }
        kotlin.runCatching {
            game.soundPlayer.pauseCustomMusic()
            game.soundPlayer.setSuspendDefaultMusicPlayback(false)
            currentCustomMusicId = null
        }
            .onFailure {
                game.logger.w(it)
            }
    }


    fun playTelosThemeMusic(fadeOutSeconds: Int = 3, fadeInSeconds: Int = 3) =
        playSong(
            song = Song.TELOS_THEME,
            fadeOutSecs = fadeOutSeconds,
            fadeInSecs = fadeInSeconds,
            loop = true
        )

    fun stopAllCustomMusic() {
        game.logger.d { "Stopping custom music." }
        playSong(
            songId = null,
            fadeOutSecs = 3
        )
    }
}