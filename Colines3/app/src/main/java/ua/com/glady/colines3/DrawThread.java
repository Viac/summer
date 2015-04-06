package ua.com.glady.colines3;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Implements thread that performs drawing on canvas
 *
 * Created by Slava on 27.03.2015.
 */
class DrawThread extends Thread{

    private boolean runFlag = false;
    private final SurfaceHolder surfaceHolder;

    private GameModel game;

    public DrawThread(SurfaceHolder surfaceHolder, GameModel game){
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
                // Now we get canvas object and performs drawing
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