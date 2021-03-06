package de.mwvb.blockpuzzle.sound;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;

import de.mwvb.blockpuzzle.R;
import de.mwvb.blockpuzzle.persistence.Persistence;

public class SoundService implements ISoundService {
    private boolean on;
    private SoundPool soundPool;
    private int crunch;
    private int money;
    private MediaPlayer explosion;
    private MediaPlayer laughter;
    private MediaPlayer jeqa;
    private MediaPlayer applause;
    private int moreThan40P;
    private int emptyScreenBonus;

    /** init SoundService */
    @SuppressLint("ObsoleteSdkInt") // falls ich das API Level senken sollte
    public void init(Context context) {
        on = new Persistence(context).isGameSoundOn();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(20)
                    .build();
        } else {
            soundPool = new SoundPool(6, AudioManager.STREAM_MUSIC, 0);
        }

        crunch = soundPool.load(context, R.raw.crunch, 0);
        money = soundPool.load(context, R.raw.money, 0);
        explosion = MediaPlayer.create(context, R.raw.explosion);
        laughter = MediaPlayer.create(context, R.raw.laughter);
        jeqa = MediaPlayer.create(context, R.raw.jeqa);
        applause = MediaPlayer.create(context, R.raw.applause);
        moreThan40P = soundPool.load(context, R.raw.more40, 0);
        emptyScreenBonus = soundPool.load(context, R.raw.emptysb, 0);
    }

    /** destroy SoundService */
    public void destroy() {
        soundPool.release();
        soundPool = null;
    }

    private void play(int soundId) {
        if (off()) return;
        soundPool.play(soundId, 1, 1, 0, 0, 1f);
    }

    @Override
    public void clear(boolean big) {
        if (big) {
            if (off()) return;
            explosion.start();
        } else {
            play(crunch);
        }
    }

    @Override
    public void gameOver() {
        if (off()) return;
        quiet();
        laughter.start();
    }

    @Override
    public void youWon() {
        if (off()) return;
        quiet();
        applause.start();
    }

    @Override
    public void oneColor() {
        play(money);
    }

    public void playSound(int number) {
        switch (number) {
            case 1:
                play(moreThan40P);
                break;
            case 2:
                play(emptyScreenBonus);
                break;
        }
    }

    @Override
    public void doesNotWork() {
        // no sound, maybe in the future
    }


    private void quiet() {
        if (off()) return;
        quiet(jeqa);
        quiet(explosion);
    }

    private void quiet(MediaPlayer mp) {
        if (off()) return;
        mp.stop();
        try {
            mp.prepare(); // direkt wieder startfähig machen
        } catch (Throwable ignore) {
        }
    }

    private boolean off() {
        return !on;
    }
}
