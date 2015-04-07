package ua.com.glady.colines3;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import ua.com.glady.colines3.Tools.FacebookShare;
import ua.com.glady.colines3.Tools.GamePreferences;

/**
 * Shows popup when user win or loose game
 *
 * Created by Slava on 02.04.2015.
 */
public class GameOver {

    public static void showPopup(final Context mContext, final GamePreferences gamePreferences,
                           boolean winGame, int gameScore){

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        // we have special layout for it just to improve design process
        LayoutInflater li = LayoutInflater.from(mContext);

        // this case is a most known (may be single) exception from the rule and it's not error
        @SuppressLint("InflateParams") View view = li.inflate(R.layout.game_over, null);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        String s;

        // First we need to figure out what to show as a header
        if (winGame)
            s = mContext.getString(R.string.WinCaption);
        else
            s = mContext.getString(R.string.GameOver);
        ((TextView) (view.findViewById(R.id.tvGameOverCaption))).setText(s);

        // Than we need to figure out what to show as a message
        if (gameScore > gamePreferences.getBestSavedScore())
            ((TextView) (view.findViewById(R.id.tvGameOverMessage))).setText(mContext.getString(R.string.NewRecord) + ": " + gameScore);
        else
            ((TextView) (view.findViewById(R.id.tvGameOverMessage))).setText(mContext.getString(R.string.Score) + ": " + gameScore);

        // Making Continue button
        (view.findViewById(R.id.tvContinue)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Making Share button
        (view.findViewById(R.id.imShare)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FacebookShare.shareOnFacebook(mContext, "https://play.google.com/store/apps/details?id=ua.com.glady.colines3");
                gamePreferences.setAlreadyShared(true);
            }
        });

        // Making Rate button
        (view.findViewById(R.id.imRate)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=ua.com.glady.colines3")));
                gamePreferences.setAlreadyRated(true);
            }
        });

        // Updating "buttons" to show it only if need. Alternative is sign. expensive.
        boolean isVisible;

        isVisible = winGame || (gameScore > gamePreferences.getBestSavedScore());
        if (!isVisible)
            view.findViewById(R.id.imCup).setVisibility(View.INVISIBLE);

        isVisible = ! gamePreferences.getAlreadyRated();
        if (!isVisible)
            (view.findViewById(R.id.imRate)).setVisibility(View.INVISIBLE);

        isVisible = ! gamePreferences.getAlreadyShared();
        if (!isVisible)
            (view.findViewById(R.id.imShare)).setVisibility(View.INVISIBLE);

        dialog.show();
    }

}