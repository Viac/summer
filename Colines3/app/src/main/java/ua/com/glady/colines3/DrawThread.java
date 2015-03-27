package ua.com.glady.colines3;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.view.SurfaceHolder;

/**
 * Created by Slava on 27.03.2015.
 */
class DrawThread extends Thread{

    private boolean runFlag = false;
    private SurfaceHolder surfaceHolder;

    private GameModel game;

    public DrawThread(SurfaceHolder surfaceHolder, Resources resources, GameModel game){
        this.surfaceHolder = surfaceHolder;
        this.game = game;
    }

    public void setRunning(boolean run) {
        runFlag = run;
    }

    @Override
    public void run() {
        Canvas canvas;
        while (runFlag) {
            canvas = null;
            try {
                // получаем объект Canvas и выполняем отрисовку
                canvas = surfaceHolder.lockCanvas(null);
                synchronized (surfaceHolder) {
                    game.paint(canvas);
                }
            }
            finally {
                if (canvas != null) {
                    // Drawing complete, now show it on the screen
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}