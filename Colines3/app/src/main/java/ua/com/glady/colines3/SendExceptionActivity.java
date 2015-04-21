package ua.com.glady.colines3;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;

/**
 * This activity provides ability to send bugreport for developers
 *
 * Created by Slava on 19.03.2015.
 */
public class SendExceptionActivity extends ActionBarActivity implements View.OnClickListener {

    private static final String BUGREPORT_COLLECTING_EMAIL = "1001lines@viac-soft.in.ua";

    private String exceptionReport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_send_exception);

        exceptionReport = this.getIntent().getExtras().getString("exceptionReport");
    }

    /**
     * Runs e-mail intent
     */
    private void sendException() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("plain/text");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{BUGREPORT_COLLECTING_EMAIL});
        intent.putExtra(Intent.EXTRA_SUBJECT, "1001 lines exception");
        intent.putExtra(Intent.EXTRA_TEXT, exceptionReport); // do this so some email clients don't complain about empty body.
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks if user allows to send bugreport
     *
     * @return true if user allows to send bugreport
     */
    private boolean needToSendException() {
        CheckBox cb = (CheckBox) this.findViewById(R.id.cbSendException);
        return cb.isChecked();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btCloseApplication) {
            if (needToSendException())
                sendException();
            this.finish();
        }
    }
}
