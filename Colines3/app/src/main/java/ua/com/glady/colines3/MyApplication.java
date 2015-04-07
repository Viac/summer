package ua.com.glady.colines3;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This class replaced native application since we have to handle uncaught exception in same
 * way for all application
 *
 * Created by Slava on 27.03.2015.
 */
public class MyApplication extends Application {

    public void onCreate ()
    {
        super.onCreate();

        // Setup handler for uncaught exceptions.
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                handleUncaughtException (e);
            }
        });
    }

    /**
     * @param e exception that we need to analyze
     * @return json string with exception stacktrace and some device information
     *  if there is an error while creating json string, just stacktrace returned
     */
    private String getExceptionReport(Throwable e){
        JSONObject json = new JSONObject();

        String stackTrace = Log.getStackTraceString(e);
        try {
            json.put("stacktrace", stackTrace);
            json.put("brand", Build.BRAND);
            json.put("device", Build.DEVICE);
            json.put("model", Build.MODEL);
            json.put("id", Build.ID);
            json.put("version_release", Build.VERSION.RELEASE);
            json.put("version_incremental", Build.VERSION.INCREMENTAL);
            return json.toString();
        } catch (JSONException e1) {
            // something wrong, but "at least I have chicken.."
            return stackTrace;
        }
    }

    /**
     * This method defines how all uncaught exceptions will be handled in application
     * @param e uncaught exception
     */
    void handleUncaughtException(final Throwable e) {
        // not all Android versions will print the stack trace automatically
        e.printStackTrace();

        Log.d("glady", "Crash:" + Log.getStackTraceString(e));

        Intent intent = new Intent();
        intent.setAction("ua.com.glady.colines3.SendExceptionActivity"); // see step 5.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // required when starting from Application
        intent.putExtra("exceptionReport", getExceptionReport(e));
        startActivity(intent);

        // We can't do anything new with it
        System.exit(1);
    }

}
