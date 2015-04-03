package ua.com.glady.colines3;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import ua.com.glady.colines3.Tools.GamePreferences;

/**
 * Created by Slava on 03.04.2015.
 */
public class GameplayPreferences {

    public static void show(final Context mContext, final GamePreferences preferences){

        final Dialog d = new Dialog(mContext);

        LinearLayout l = new LinearLayout(mContext);
        l.setOrientation(LinearLayout.VERTICAL);

        TextView tv1 = new TextView(mContext);
        tv1.setText("Basic width");
        l.addView(tv1);

        final EditText eWidth = new EditText(mContext);
        eWidth.setText(String.valueOf(preferences.getsPref().getInt("BasicWidth", 30)));
        l.addView(eWidth);

        TextView tv2 = new TextView(mContext);
        tv2.setText("colors count");
        l.addView(tv2);

        final EditText eColorsCount = new EditText(mContext);
        eColorsCount.setText(String.valueOf(preferences.getsPref().getInt("ColorsCount", 5)));
        l.addView(eColorsCount);

        Button btOk = new Button(mContext);
        btOk.setText("OK");
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor e = preferences.getsPref().edit();
                e.putInt("BasicWidth", Integer.valueOf(eWidth.getText().toString()));
                e.putInt("ColorsCount", Integer.valueOf(eColorsCount.getText().toString()));
                e.commit();
                d.dismiss();
                Toast.makeText(mContext, "Restart game", Toast.LENGTH_SHORT).show();
            }
        });
        l.addView(btOk);


        d.setContentView(l);
        d.show();
    }

}
