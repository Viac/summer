package ua.com.glady.colines3;

import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class GameActivity extends ActionBarActivity implements View.OnTouchListener {

    TextView score;
    LinearLayout surface;

    GameModel game;

    INotifyEvent onScoreUpdated = new INotifyEvent() {
        @Override
        public void onEvent() {
            score.setText(game.getScore());
            score.invalidate();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        game = new GameModel();
        game.setOnScoreUpdated(onScoreUpdated);

        score = (TextView) this.findViewById(R.id.tvScore);

        surface = (LinearLayout)findViewById(R.id.surface);
        surface.setOnTouchListener(this);

        surface.addView(new GameSurfaceView(this, game));
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_run) {
            run();
            return true;
        }

        if (id == R.id.action_reset) {
            reset();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void reset() {
        game.reset();
    }

    private void run() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int x = Math.round(event.getX());

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
}
