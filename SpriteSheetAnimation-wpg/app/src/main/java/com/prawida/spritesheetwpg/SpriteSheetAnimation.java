package com.lira.projectanimasispritesheet;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.Point;
import android.os.Bundle;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class SpriteSheetAnimation extends Activity {

    GameView gameView;

    int screenX, screenY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        screenX = size.x;
        screenY = size.y;

        gameView = new GameView(this);
        setContentView(gameView);
    }

    class GameView extends SurfaceView implements Runnable {

        Thread gameThread = null;

        SurfaceHolder ourHolder;

        volatile boolean playing;

        long fps;
        private long timeThisFrame;

        Canvas canvas;
        Paint paint;

        Bitmap bitmapCharacter;

        boolean isMoving = false;
        float walkSpeedPerSeconds = 250;
        float characterXPosition = 10;
        float characterYPosition = screenY / 1.2f;

        private int frameWidth = 50;
        private int frameHeight = 65;
        private int frameCount = 6;
        private int currentFrame = 0;
        private long lastFrameChangeTime = 0;
        private int frameLengthInMilliseconds = 100;

        // Besar dari sprite character
        private Rect frameToDraw = new Rect(0, 0, frameWidth, frameHeight);

        // Menampung letak spritesheet
        RectF whereToDraw = new RectF(characterXPosition, 100, characterXPosition + frameWidth, frameHeight);

        public GameView(Context context) {
            super(context);

            ourHolder = getHolder();
            paint = new Paint();

            bitmapCharacter = BitmapFactory.decodeResource(this.getResources(), R.drawable.character);
            bitmapCharacter = Bitmap.createScaledBitmap(bitmapCharacter, frameWidth * frameCount, frameHeight, false);

            playing = true;
        }


        @Override
        public void run() {
            while(playing) {
                long startFrameTime = System.currentTimeMillis();

                update();

                draw();

                timeThisFrame = System.currentTimeMillis() - startFrameTime;
                if (timeThisFrame >= 1) {
                    fps = 1000 / timeThisFrame;
                }
            }
        }

        public void update() {
            if (isMoving) {
                if (characterXPosition >= screenX - frameWidth || characterXPosition <= 0) {
                    walkSpeedPerSeconds = -walkSpeedPerSeconds;
                    flip();
                }

                characterXPosition = characterXPosition + (walkSpeedPerSeconds / fps);
            }
        }

        public void draw() {
            if (ourHolder.getSurface().isValid()) {
                canvas = ourHolder.lockCanvas();

                // background
                canvas.drawColor(Color.argb(255,26,128,182));
                paint.setColor(Color.argb(255,115,66,17));
                canvas.drawRect(0, characterYPosition + frameHeight, screenX, screenY, paint);
                paint.setColor(Color.argb(255,55,156,44));
                canvas.drawRect(0, characterYPosition + frameHeight, screenX, characterYPosition + frameHeight + 30, paint);
                paint.setColor(Color.argb(255,255,255,255));
                paint.setTextSize(120);
                canvas.drawText("LUFFY JALAN-JALAN", screenX / 5, screenY / 2, paint);

                // cek fps
                paint.setColor(Color.argb(255,249,129,0));
                paint.setTextSize(45);
                canvas.drawText("FPS : " + fps, 20, 40, paint);

                // character
                whereToDraw.set((int)characterXPosition, characterYPosition, (int)characterXPosition + frameWidth, characterYPosition + frameHeight);
                getCurrentFrame();
                canvas.drawBitmap(bitmapCharacter, frameToDraw, whereToDraw, paint);

                ourHolder.unlockCanvasAndPost(canvas);
            }
        }

        public void getCurrentFrame() {
            long time = System.currentTimeMillis();
            if (isMoving) {
                if (time > lastFrameChangeTime + frameLengthInMilliseconds) {
                    lastFrameChangeTime = time;
                    currentFrame++;
                    if (currentFrame >= frameCount) {
                        currentFrame = 0;
                    }
                }
            }

            frameToDraw.left = currentFrame * frameWidth;
            frameToDraw.right = frameToDraw.left + frameWidth;
        }

        public void flip() {
            Matrix matrix = new Matrix();
            matrix.preScale(-1, 1);
            bitmapCharacter = Bitmap.createBitmap(bitmapCharacter, 0, 0, bitmapCharacter.getWidth(), bitmapCharacter.getHeight(), matrix, false);
        }

        public void pause() {
            playing = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Log.e("Error", "joining thread");
            }
        }

        public void resume() {
            playing = true;
            gameThread = new Thread(this);
            gameThread.start();
        }

        @Override
        public boolean onTouchEvent(MotionEvent motionEvent) {
            switch (motionEvent.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    isMoving = true;
                    break;
                case MotionEvent.ACTION_UP:
                    isMoving = false;
                    break;
            }
            return true;
        }

    };

    @Override
    public void onResume() {
        super.onResume();

        gameView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();

        gameView.pause();
    }
}