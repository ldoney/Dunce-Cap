package org.microincorporated.duncecap;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.content.Context;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Bundle;


import java.text.DecimalFormat;
import java.util.Locale;

import static android.graphics.Color.rgb;

public class MainActivity extends AppCompatActivity {

    TextView timer;
    SensorManager sensorManager;
    Sensor sensor;
    boolean gameStart;
    final static float MAX_ANGLE = 30.0f;
    boolean running;
    ViewGroup background;
    final static String HIGH = "HIGH_SCORE";
    CountDownTimer countDown;
    Beeper bpr;
    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        timer = findViewById(R.id.timer);
        background = (ViewGroup) ((ViewGroup) (findViewById(android.R.id.content))).getChildAt(0);
        gameStart = false;
        running = false;
//        MediaPlayer.create(this, R.raw.beep);
        bpr = new Beeper();
        countDown = new CountDownTimer(6000, 1000) {
            public void onTick(long millisUntilFinished) {
                timer.setText("" + (int)((millisUntilFinished / 1000)));
                if((int)(millisUntilFinished / 1000) < 1)
                {
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100);
                    timer.setText("Go!");
                    timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 75);
                }
            }

            public void onFinish() {
                startGame();
            }
        };
        countDown.start();
    }
    public void onResume() {
        super.onResume();
        sensorManager.registerListener(gyroListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
    public void onStop() {
        super.onStop();
        sensorManager.unregisterListener(gyroListener);
    }
    public void startGame()
    {
        gameStart = true;
        StartTime = SystemClock.uptimeMillis();
        running = true;
        runTimer();
    }
    float[] initial = null;
    public SensorEventListener gyroListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int acc) {
        }
        public void onSensorChanged(SensorEvent event) {
            if (gameStart) {

                float x = event.values[0] * 10;
                float y = event.values[1] * 10;
                float z = event.values[2] * 10;
                if (initial == null) {
                    initial = new float[]{x, y, z};
                }
                float[] delta = new float[]{initial[0] - x,
                        initial[1] - y};
                float highestDelta = Math.max(delta[0], delta[1]);
                float delay = (float)(Math.sin(Math.toRadians(((90f/(MAX_ANGLE))) * highestDelta)));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mixColors(delay);
                }
                bpr.progressDelay(delay);
                if (isOver(x, y) && gameStart) {
                    gameOver();
                }
            }
        }
        };


    @RequiresApi(api = Build.VERSION_CODES.O)
    public void  mixColors(double percent){
        double inverse_percent = 1.0 - percent;
        int redPart = (int) (255*percent + 0*inverse_percent);
        int greenPart = (int) (0*percent + 255*inverse_percent);
        int bluePart = (int) (0*percent + 0*inverse_percent);

        findViewById(R.id.Game).setBackgroundColor(Color.rgb(redPart,greenPart,bluePart));
    }
    public boolean isOver(float x, float y)
    {
        if(Math.abs(x) > MAX_ANGLE || Math.abs(y) > MAX_ANGLE)
        {
            return true;
        }
        return false;
    }
    private void gameOver()
    {
        running = false;
        gameStart = false;
        background.setBackgroundColor(Color.RED);
        SharedPreferences score = getApplicationContext().getSharedPreferences(HIGH, 0);
        long highScore;
        if(!score.contains("highScore"))
        {
            highScore = MillisecondTime;
        }else
        {
            highScore = score.getLong("highScore", 0);
        }
        if(MillisecondTime >= highScore)
        {
            SharedPreferences.Editor editor = score.edit();
            editor.putLong("highScore", MillisecondTime);
            editor.commit();
        }
        ((TextView)findViewById(R.id.best)).setText("Best: " + formatTime(score.getLong("highScore",0)));
        ((TextView)findViewById(R.id.score)).setText("Score: " + formatTime(MillisecondTime));
        findViewById(R.id.Game).setVisibility(View.GONE);
        findViewById(R.id.GameOver).setVisibility(View.VISIBLE);

    }
    public String formatTime(long ms)
    {
        UpdateTime = TimeBuff + ms;

        int Seconds = (int) (UpdateTime / 1000);

        int Minutes = Seconds / 60;

        Seconds = Seconds % 60;

        int MilliSeconds = (int) ((UpdateTime % 1000) / 10);
        return  String
                .format(Locale.getDefault(),
                        "%02d:%02d.%02d",
                        Minutes, Seconds, MilliSeconds);
    }

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    private void runTimer()
    {

        // Get the text view.
        final TextView timeView
                = (TextView)findViewById(
                R.id.timer);

        // Creates a new Handler
        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run()
            {
                if(running) {
                    MillisecondTime = SystemClock.uptimeMillis() - StartTime;
                    timeView.setText(formatTime(MillisecondTime));
                }
                handler.postDelayed(this, 0);
            }
        });
    }

    public void reset()
    {
        background.setBackgroundColor(Color.GREEN);
        findViewById(R.id.Game).setVisibility(View.VISIBLE);
        findViewById(R.id.GameOver).setVisibility(View.GONE);
        timer.setTextSize(TypedValue.COMPLEX_UNIT_SP, 400);
        timer.setText("5");
        crossfade(findViewById(R.id.GameOver), findViewById(R.id.Game));
    }

    public void reset(View view) {
        reset();
    }


    private void crossfade(final View oldView, final View newView) {
        newView.setAlpha(0f);
        newView.setVisibility(View.VISIBLE);

        newView.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        newView.setVisibility(View.VISIBLE);
                        countDown.start();
                    }
                });;

        oldView.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        oldView.setVisibility(View.GONE);
                        oldView.setAlpha(1.0f);
                    }
                });
    }
}
