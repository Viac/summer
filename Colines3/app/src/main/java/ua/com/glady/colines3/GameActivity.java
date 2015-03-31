package ua.com.glady.colines3;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class GameActivity extends ActionBarActivity implements View.OnTouchListener, View.OnClickListener {

    TextView tvCurrentScore;
    TextView tvBestScore;

    LinearLayout surface;

    GameSurfaceView gameView;

    GameModel game;

    INotifyEvent onScoreUpdated = new INotifyEvent() {
        @Override
        public void onEvent() {
        tvCurrentScore.setText(String.format(getString(R.string.CurrentScore), game.getScore()));
        tvBestScore.setText(String.format(getString(R.string.BestScore), game.getBestScore()));
        }
    };

    INotifyEvent onGameOver = new INotifyEvent() {
        @Override
        public void onEvent() {
            Toast.makeText(getApplicationContext(), "Game over!!!!!", Toast.LENGTH_LONG).show();
            game.saveBestScore();
            game.reset();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        tvCurrentScore = (TextView) this.findViewById(R.id.tvCurrentScore);
        tvBestScore = (TextView) findViewById(R.id.tvBestScore);

        game = new GameModel(getPreferences(MODE_PRIVATE));
        game.setOnScoreUpdated(onScoreUpdated);
        game.setOnGameOver(onGameOver);

        // todo: switch orientation!
        game.reset();

        surface = (LinearLayout)findViewById(R.id.surface);
        surface.setOnTouchListener(this);

        gameView = new GameSurfaceView(this, game);

        surface.addView(gameView);

        // initial scores value
        onScoreUpdated.onEvent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = (int) event.getX();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: // нажатие
                break;
            case MotionEvent.ACTION_MOVE: // движение
                game.setX(x);
                break;
            case MotionEvent.ACTION_UP: // отпускание
                game.drop(x);
                break;
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btRestart){
            game.reset();
        }
    }
}
