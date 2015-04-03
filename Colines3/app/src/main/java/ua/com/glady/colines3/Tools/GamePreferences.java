package ua.com.glady.colines3.Tools;

import android.content.SharedPreferences;

/**
 * Created by Slava on 02.04.2015.
 */
public class GamePreferences {


    // Stored game data
    private static final String BEST_SCORE_PREFERENCES_KEY = "GameBestScore";

    // Support developer settings
    private static final String ALREADY_RATE_PREFERENCES_KEY = "IsAlreadyRated";
    private static final String ALREADY_SHARED_PREFERENCES_KEY = "IsAlreadyShared";

    // Gameplay settings (might be unused, it's a test of gameplay)
    private static final String BASIC_WIDTH_PREFERENCES_KEY = "BasicWidth";
    private static final String COLORS_COUNT_PREFERENCES_KEY = "ColorsCount";

    public SharedPreferences getsPref() {
        return sPref;
    }

    private SharedPreferences sPref;

    public GamePreferences(SharedPreferences sPref){
        this.sPref = sPref;
    }

    public int getBestSavedScore(){
        return sPref.getInt(BEST_SCORE_PREFERENCES_KEY, 0);
    }

    public void setBestSavedScore(int value){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(BEST_SCORE_PREFERENCES_KEY, value);
        ed.commit();
    }

    public boolean getAlreadyRated(){
        return sPref.getBoolean(ALREADY_RATE_PREFERENCES_KEY, false);
    }

    public void setAlreadyRated(boolean value){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(ALREADY_RATE_PREFERENCES_KEY, value);
        ed.commit();
    }

    public boolean getAlreadyShared(){
        return sPref.getBoolean(ALREADY_SHARED_PREFERENCES_KEY, false);
    }

    public void setAlreadyShared(boolean value){
        SharedPreferences.Editor ed = sPref.edit();
        ed.putBoolean(ALREADY_SHARED_PREFERENCES_KEY, value);
        ed.commit();
    }

    public int getColorsCount(){
        return sPref.getInt(COLORS_COUNT_PREFERENCES_KEY, 5);
    }

    public int getBasicWidth(){
        return sPref.getInt(BASIC_WIDTH_PREFERENCES_KEY, 30);
    }

}
