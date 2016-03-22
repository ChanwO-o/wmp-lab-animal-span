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
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import com.uci.wmp.animalspan.R;

public final class Util {

    private static final String LOG_TAG = "Util";

    static void dimSystemBar(Activity activity) {
        final View window = activity.getWindow().getDecorView();

        window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        window.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    window.postDelayed(new Runnable() {
                        public void run() {
                            window.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                    | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        }
                    }, 2000);
                }
            }
        });
    }

    static void playSoundFile(MediaPlayer mediaPlayer, String fileName, Context context) {
        try {
            mediaPlayer.reset();
            AssetManager assetManager = context.getAssets();
            AssetFileDescriptor fd = assetManager.openFd(fileName + ".wav");
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "IOException caught", e);
        } catch (IllegalStateException e) {
            Log.e(LOG_TAG, "IllegalStateException caught", e);
        }
    }

    /**
     * Create final strings on this class for fragmentName parameter
     */
    static void loadFragment(String fragmentName) {
        //TODO: Implement loadFragment()
    }


}
