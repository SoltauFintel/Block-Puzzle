package de.mwvb.blockpuzzle.playingfield;

import de.mwvb.blockpuzzle.sound.SoundService;

public interface IPlayingFieldView {

    SoundService getSoundService();

    void setPlayingField(PlayingField playingField);

    void draw();

}
