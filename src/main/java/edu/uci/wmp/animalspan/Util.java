package edu.uci.wmp.animalspan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.fragments.MainActivityFragment;

public final class Util {

    private static final String LOG_TAG = "Util";

    static void dimSystemBar(Activity activity) {
        final View window = activity.getWindow().getDecorView();

//        window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        setFlags(window);

        window.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    window.postDelayed(new Runnable() {
                        public void run() {
                            setFlags(window);
                        }
                    }, 2000);
                }
            }
        });
    }

    private static void setFlags(View window) {
        window.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Replace main fragment container with fragment given in parameter
     */
    public static void loadFragment(Activity activity, Fragment fragment) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    /**
     * Get current timestamp
     */
    public static String getTimestamp(final String TIMESTAMP_DATE, final String TIMESTAMP_TIME) {
        final String DATE = new SimpleDateFormat(TIMESTAMP_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(TIMESTAMP_TIME, Locale.US).format(Calendar.getInstance().getTime());
        return DATE + " " + TIME;
    }
}
