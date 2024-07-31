package com.example.rohananumoludodgegame;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    GameSurface gameSurface;
    double xVal, yVal, zVal;

    int f = 50;
    boolean hitPlayer = false;

    int enemyPositionX, enemyPositionY, spaceshipPositionX, spaceshipPositionY;

    int score = 0;

    boolean ffy = true;

    private long startTime;
    private long currentTime;
    private long elapsedTime;
    private long remainingTime;
    private static final long GAME_DURATION = 20000;
    int timeInSecs;

    MediaPlayer player;
    SoundPool soundpool;
    int soundID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gameSurface = new GameSurface(this);
        setContentView(gameSurface);


        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometerSensor, sensorManager.SENSOR_DELAY_NORMAL);

        startTime = System.currentTimeMillis();

        soundpool = new SoundPool(2, AudioManager.STREAM_MUSIC, 0);
        soundID = soundpool.load(this, R.raw.collision, 1);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //x.setText("X AXIS: "+ event.values[0]);
        //y.setText("Y AXIS: " + event.values[1]);
        //z.setText("Z AXIS: " + event.values[2]);
        xVal = event.values[0];
        yVal = event.values[1];
        zVal = event.values[2];
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onPause() {
        super.onPause();
        gameSurface.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(ffy){
            f+=10000;
            ffy = false;
        }
        else{
            f-=10000;
            ffy = true;
        }
        return super.onTouchEvent(event);
    }

    public class GameSurface extends SurfaceView implements Runnable, SensorEventListener{

        Thread gameThread;

        SurfaceHolder holder;

        volatile boolean running = false;

        Bitmap spaceship2, spaceship, spaceship3;

        Bitmap enemy2, enemy;

        Bitmap background;

        int ballx = 0;
        int enemyx = 0;
        int enemyy = 0;

        int flip = 0;
        int flippy = 0;

        Paint paintProperty;

        int screenWidth;
        int screenHeight;

        public GameSurface(Context context) {
            super(context);
            holder = getHolder();

            spaceship2 = BitmapFactory.decodeResource(getResources(), R.drawable.spaceship);
            spaceship = Bitmap.createScaledBitmap(spaceship2, 200, 200, false);

            enemy2 = BitmapFactory.decodeResource(getResources(), R.drawable.enemy);
            enemy = Bitmap.createScaledBitmap(enemy2, 200, 200, false);

            background = BitmapFactory.decodeResource(getResources(), R.drawable.space);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);

            screenWidth = sizeOfScreen.x;
            screenHeight = sizeOfScreen.y;

            paintProperty = new Paint();
        }


        @Override
        public void run() {
            Canvas canvas = null;
            Drawable d = getResources().getDrawable(R.drawable.space, null);

            player = MediaPlayer.create(MainActivity.this, R.raw.jazz);
            player.setLooping(true);
            player.start();
            if(!player.isPlaying()){
                player.start();
                player.setLooping(true);
            }

            while(running){
                if(holder.getSurface().isValid() == false)
                    continue;
                canvas = holder.lockCanvas(null);
                d.setBounds(getLeft(), getTop(), getRight(), getBottom());
                d.draw(canvas);


                Log.d("Ball&ScreenWidthFlip", ballx+" "+screenWidth+" "+flip);
                if(ballx == screenWidth/2-spaceship.getWidth()/2 && xVal < 0.00){
                    ballx += 0;
                }else if(ballx == -1*screenWidth/2+spaceship.getWidth()/2 && xVal > 0.00){
                    ballx += 0;
                } else if(xVal < 0.00) {
                    flip = 5;
                    ballx += flip;
                }else if(xVal > 0.00){
                    flip = -5;
                    ballx += flip;
                }else if(xVal == 0.00){
                    ballx +=0;
                }

                if(xVal == 0 || xVal != 0){
                    flippy = (int)(Math.random()*50) - 30;
                    enemyx += flippy;
                    enemyy += f;
                    if((enemyy+20) > 2000){
                        enemyy = 0;
                        enemyx = 0;
                    }
                }

                enemyPositionX = 440+enemyx;
                enemyPositionY = 20+enemyy;
                spaceshipPositionX = (screenWidth/2)-spaceship.getWidth()/2+ballx;
                spaceshipPositionY = 1150;

                Log.d("IntersectCheck!!", "x: "+ enemyPositionX);
                Log.d("IntersectCheck!!", "x: "+ enemyPositionY);

                if((enemyPositionX >= (((screenWidth/2)-spaceship.getWidth()/2+ballx)-100) && enemyPositionX <= (((screenWidth/2)-spaceship.getWidth()/2+ballx)+100)) && (enemyPositionY >= 1100 && enemyPositionY <= 1150)){
                    score --;
                    spaceship3 = BitmapFactory.decodeResource(getResources(), R.drawable.spacebroke);
                    spaceship = Bitmap.createScaledBitmap(spaceship3, 200, 200, false);
                    hitPlayer = true;
                    soundpool.play(soundID, 1, 1, 0, 0, 1);
                }else{
                    hitPlayer = false;
                }

                if((enemyPositionY >= 1100 && enemyPositionY <= 1150) && !hitPlayer){
                    score ++;
                }

               canvas.drawBitmap(spaceship, (screenWidth/2)-spaceship.getWidth()/2+ballx,
                        1150, null);

                canvas.drawBitmap(enemy, 440+enemyx, 20+enemyy, null);

                Paint paint = new Paint();
                paint.setColor(Color.CYAN);
                paint.setTextSize(70);
                canvas.drawText("SCORE: " + score, 70, 100, paint);

                currentTime = System.currentTimeMillis();
                elapsedTime = currentTime - startTime;
                remainingTime = GAME_DURATION - elapsedTime;
                if (remainingTime <= 0)
                    remainingTime = 0;
                timeInSecs = (int) (remainingTime/1000);

                canvas.drawText("Time Left: " + timeInSecs, 600, 100, paint);

                if(remainingTime == 0){
                    score = 0;
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    canvas.drawText("GAME OVER", 350, 700, paint);
                    ballx = 10000;
                    enemyx = 10000;
                    enemyy = 10000;
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        public void pause(){
            running = false;
            while(true){
                try {
                    gameThread.join();
                }
                catch(InterruptedException e){

                }
            }

        }

        @Override
        public void onSensorChanged(SensorEvent event) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }


}
