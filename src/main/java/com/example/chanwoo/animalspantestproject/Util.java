package com.example.chanwoo.animalspantestproject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

public final class Util {

    private static final String LOG_TAG = "Util";

    private final static String[] csvFields = new String[] {
            "Subject ID",
            "Date",
            "Time",
            "Trial",
            "Sequence Length",
            "Task",
            "Upside down",
            "Stimulus Color",
            "Reaction Time",
            "Accuracy",
            "Time Error",
            "Coins",
            "Theme",
            "Practice",
            "Answer"
    };

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

    public static void writeCsvFile(Context context) {
        File csvFolder = new File(context.getFilesDir() + "/csvdata");
        csvFolder.setReadable(true, false);
        Log.d("csvFolder loc", csvFolder.getPath());
        if (!csvFolder.exists())
            Log.d("csvFolder", "folder created = " + csvFolder.mkdir()); // create folder to save all csv files


        final String DATE = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat("HHmmss").format(Calendar.getInstance().getTime());
        final String filename = LevelManager.getInstance().subject + "_" +
                                LevelManager.getInstance().session + "_" +
                                DATE + "_" + TIME + "_" + ".csv";

        File dataFile = new File(csvFolder, filename);
        dataFile.setReadable(true, false);

        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(dataFile);
            // write fields
            for (String s : csvFields) {
                fileWriter.write(s);
                fileWriter.write(", ");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
//        fileWriter.write("Subject ID,Date,Time,Trial,Sequence Length,Task,Upside down,Stimulus Color,Reaction Time,Accuracy,Time Error,Coins,Theme,Practice,Answer");









//        File path = new File(context.getFilesDir(), )
//
//        OutputStreamWriter out;
//        try {
//            File f = new File(path.getPath() + "/csvdata/myfile.txt");
//            out = new OutputStreamWriter(context.openFileOutput(f.getPath(), context.MODE_PRIVATE));
//            out.write("test");
//            out.close();
//        }
    }

    /**
     * Create final strings on this class for fragmentName parameter
     */
    static void loadFragment(String fragmentName) {

    }
}
