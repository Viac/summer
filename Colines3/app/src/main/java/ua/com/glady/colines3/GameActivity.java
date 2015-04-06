package ua.com.glady.colines3;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.glady.colines3.Tools.GamePreferences;

public class GameActivity extends ActionBarActivity implements View.OnTouchListener {

    TextView tvCurrentScore;

    TextView tvBestScore;

    LinearLayout surface;

    GameSurfaceView gameView;

    GamePreferences preferences;

    GameModel game;

    // Alias for anonymous methods
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvCurrentScore = (TextView) this.findViewById(R.id.tvCurrentScore);
        tvBestScore = (TextView) findViewById(R.id.tvBestScore);

        preferences = new GamePreferences(getPreferences(MODE_PRIVATE));

        game = new GameModel(preferences);
        game.setScoreUpdatedListener(onScoreUpdated);
        game.setGameOverListener(onGameOver);

        game.reset();

        surface = (LinearLayout) findViewById(R.id.surface);
        surface.setOnTouchListener(this);

        gameView = new GameSurfaceView(this, game);

        surface.addView(gameView);

        // initial score value
        onScoreUpdated.onEvent();
    }

    /**
     * Reacts on event "Game score updated"
     */
    INotifyEvent onScoreUpdated = new INotifyEvent() {
        @Override
        public void onEvent() {
            // We need to invalidate information on the screen
            tvCurrentScore.setText(String.valueOf(game.getScore()));
            tvBestScore.setText(String.format(getString(R.string.BestScore), game.getBestScore()));

            // And show a cup when user get win score
            if ((game.getScore() >= game.WIN_SCORE) && (!game.getAlreadyCongratulated())) {
                GameOver go = new GameOver();
                go.showPopup(context, preferences, true, game.getScore());
                game.setAlreadyCongratulated(true);
            }
        }
    };

    /**
     * Reacts on event "game over"
     */
    INotifyEvent onGameOver = new INotifyEvent() {
        @Override
        public void onEvent() {
            GameOver go = new GameOver();
            go.showPopup(context, preferences, false, game.getScore());
            game.saveBestScore();
            game.reset();
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_reset) {
            askToResetGame();
            return true;
        }

        if (id == R.id.action_settings) {
            GameplayPreferences.show(this, preferences);
            return true;
        }

        if (id == R.id.action_info) {
            Toast.makeText(this, "Implement me!", Toast.LENGTH_SHORT).show();

            SharedPreferences.Editor ed = preferences.getsPref().edit();
            ed.clear();
            ed.commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Shows a popup with confirmation for restart game
     */
    public void askToResetGame() {
        new AlertDialog.Builder(this)
                .setMessage(this.getString(R.string.ResetConfirmation))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        game.saveBestScore();
                        game.reset();
                    }
                })
                .setNegativeButton(getString(R.string.No), null)
                .show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // pressing
                break;
            case MotionEvent.ACTION_MOVE: // moving
                game.setX(x);
                break;
            case MotionEvent.ACTION_UP: // releasing
                game.drop(x);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        game.saveBestScore();
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

}
