package ua.com.glady.colines3;

import android.content.Context;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Implements object to draw game state
 *
 * Created by Slava on 27.03.2015.
 */
public class GameSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    // Drawing performs in separate thread
    private DrawThread drawThread;

    // Source of data to draw
    private GameModel game;

    public GameSurfaceView(Context context, GameModel game) {
        super(context);
        this.game = game;
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawThread = new DrawThread(getHolder(), game);
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;

        // Finishing thread
        drawThread.setRunning(false);
        while (retry) {
            try {
                drawThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // if not success - will try again
            }
        }
    }
}