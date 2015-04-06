package ua.com.glady.colines3;

import android.app.Application;
import android.util.Log;

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
     * This method defines how all uncaught exceptions will be handled in application
     * @param e uncaught exception
     */
    void handleUncaughtException(final Throwable e) {
        // not all Android versions will print the stack trace automatically
        e.printStackTrace();

        Log.d("glady", "Crash:" + Log.getStackTraceString(e));

        // We can't do anything new with it
        System.exit(1);
    }


}
