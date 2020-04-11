package org.microincorporated.duncecap;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.ToneGenerator;

public class Beeper {
    ToneGenerator tg;
    final static float MAX_DELAY = 1000f;
    final static float MIN_DELAY = MAX_DELAY/30.0f;
    final static float MULTIPLIER = 3f;
    private float delayer;
    public Beeper()
    {
        tg = new ToneGenerator(AudioManager.STREAM_ALARM, 100);
        delayer = 0.0f;
    }

    public void progressDelay(float amnt)
    {
        delayer+=Math.max((amnt * MAX_DELAY) * MULTIPLIER, MIN_DELAY);

        if(delayer >= MAX_DELAY)
        {
            this.Beep();
            delayer = 0f;
        }
    }
    public void setDelay(float f)
    {
        this.delayer = f;
    }
    public float getDelay()
    {
        return this.delayer;
    }
    public void Beep()
    {
        tg.startTone(ToneGenerator.TONE_PROP_BEEP, 50);
    }

}
