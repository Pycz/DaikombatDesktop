package com.beverly.hills.money.gang.assets.managers.registry;

import com.beverly.hills.money.gang.assets.managers.sound.PseudoRandomSoundSequence;
import lombok.Getter;

public enum SoundRegistry {

    BELL("sfx/bell.mp3"),
    BATTLE("music/battle.wav"),
    BATTLE2("music/battle2.wav"),
    MAIN_MENU("music/main_menu.mp3"),
    LAUGH("sfx/losing/laugh.mp3"),
    LAUGH_2("sfx/losing/laugh_2.mp3"),
    LAUGH_3("sfx/losing/laugh_3.mp3"),

    FIGHT("sfx/fight.mp3"),
    DING_1("sfx/ui/ding_1.mp3"),
    DING_2("sfx/ui/ding_2.mp3"),
    PING("sfx/ui/ping.mp3"),
    BOOM_1("sfx/ui/boom_1.mp3"),
    BOOM_2("sfx/ui/boom_2.mp3"),
    EXCELLENT("sfx/winning/excellent.wav"),
    HOLY_SHIT("sfx/winning/holyshit.wav"),
    HUMILIATION("sfx/winning/humiliation.wav"),
    IMPRESSIVE("sfx/winning/impressive.wav"),
    YOU_WIN("sfx/winning/you_win.wav"),
    PERFECT("sfx/winning/perfect.wav"),
    VOICE_GET_SHOT("voice/pain/voice_get_shot.mp3"),
    VOICE_GET_SHOT_2("voice/pain/voice_get_shot_2.mp3"),
    VOICE_GET_SHOT_3("voice/pain/voice_get_shot_2.mp3"),

    VOICE_ENEMY_GET_SHOT("voice/enemy/pain/oof.mp3"),
    VOICE_ENEMY_GET_SHOT_2("voice/enemy/pain/oof_2.mp3"),
    VOICE_ENEMY_GET_SHOT_3("voice/enemy/pain/oof_3.mp3"),
    VOICE_ENEMY_GET_SHOT_4("voice/enemy/pain/oof_4.mp3"),

    VOICE_ENEMY_DEATH("voice/enemy/death/death.mp3"),
    VOICE_ENEMY_DEATH_2("voice/enemy/death/death_2.mp3"),
    VOICE_ENEMY_DEATH_3("voice/enemy/death/death_3.mp3"),
    SHOTGUN("sfx/shotgun.mp3");

    @Getter
    private final String fileName;

    SoundRegistry(String fileName) {
        this.fileName = "sound/" + fileName;
    }


    public static final PseudoRandomSoundSequence TYPING_SOUND_SEQ
            = new PseudoRandomSoundSequence(DING_1, DING_2);
    public static final PseudoRandomSoundSequence LOOSING_SOUND_SEQ
            = new PseudoRandomSoundSequence(LAUGH, LAUGH_2, LAUGH_3);
    public static final PseudoRandomSoundSequence WINNING_SOUND_SEQ
            = new PseudoRandomSoundSequence(EXCELLENT, HUMILIATION, HOLY_SHIT, IMPRESSIVE, PERFECT, YOU_WIN);
    public static final PseudoRandomSoundSequence GET_SHOT_SOUND_SEQ
            = new PseudoRandomSoundSequence(VOICE_GET_SHOT, VOICE_GET_SHOT_2);

    public static final PseudoRandomSoundSequence ENEMY_GET_SHOT_SOUND_SEQ
            = new PseudoRandomSoundSequence(VOICE_ENEMY_GET_SHOT, VOICE_ENEMY_GET_SHOT_2,
            VOICE_ENEMY_GET_SHOT_3, VOICE_ENEMY_GET_SHOT_4);

    public static final PseudoRandomSoundSequence BATTLE_BG_SEQ
            = new PseudoRandomSoundSequence(BATTLE, BATTLE2);

    public static final PseudoRandomSoundSequence ENEMY_DEATH_SOUND_SEQ
            = new PseudoRandomSoundSequence(VOICE_ENEMY_DEATH, VOICE_ENEMY_DEATH_2, VOICE_ENEMY_DEATH_3);

}