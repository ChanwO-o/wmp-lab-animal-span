package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StimuliManager {

    public static final int UPDOWN = 1;
    public static final int RIGHTUP = 0;
    public static final int NOANSWER = -1;
    public static final int CORRECT = 1;
    public static final int INCORRECT = 0;
    public static final String TARGET = "list1/";
    public static final String SEMANTIC = "list2/";
    public static final String PERCEPTUAL = "list3/";
    public static final String DISTRACTOR = "distractors/";
    public static final String MISC = "miscellaneous/";
    public static final String FACE = "faces/";
    public static final int MIN_STIMULI_CHOICES = 1;
    public static final int MAX_STIMULI_CHOICES = 12;
//    public static final int TARGET_STIMULI_CHOICES = 12;
//    public static final int SEMANTIC_STIMULI_CHOICES = 12;
//    public static final int PERCEPTUAL_STIMULI_CHOICES = 12;
//    public static final int DISTRACTOR_STIMULI_CHOICES = 12;
    public static final int TARGET_LABEL = 100;
    public static final int SEMANTIC_LABEL = 200;
    public static final int PERCEPTUAL_LABEL = 300;
    public static final int DISTRACTOR_LABEL = 0;       // changed from 400 to 0, watch out for future unexpected errors
    public static final int MIN_CHOICE_STIMULI_SIZE = 100;
    public static final int CHOICE_STIMULI_SIZE_MULTIPLIER = 25;

    public static final ArrayList<Integer> TEMPLATE = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12));

    /**
     * @param folder using static string from StimuliManager.java
     * @param filename just the filename only (e.g. 1)
     */
    public static Bitmap getStimuli(Context context, String folder, int filename) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(getImagePath(folder, filename));
        return BitmapFactory.decodeStream(is);
    }

//    public static Bitmap getStimuli(Context context, int labeledFileName) throws IOException {
//        AssetManager assetManager = context.getAssets();
//        InputStream is = assetManager.open(getImagePath(labeledFileName));
//        return BitmapFactory.decodeStream(is);
//    }

    /**
     * @param labeledFileName folder label in first digit using static int from StimuliManager.java + filename
     */
    public static Bitmap getStimuli(int labeledFileName) throws FileNotFoundException {
        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + getImagePath(labeledFileName);
        File imageFile = new File(path);
        InputStream in = new FileInputStream(imageFile);
        return BitmapFactory.decodeStream(in);
    }

    public static Bitmap getFeedbackAsset(Context context, int result) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is;
        if (result == CORRECT)
            is = assetManager.open("stimuli/" + MISC + "correct.png");
        else
            is = assetManager.open("stimuli/" + MISC + "incorrect.png");
        return BitmapFactory.decodeStream(is);
    }

    public static String getImagePath(String folder, int filename) {
        return "stimuli/" + folder + filename + ".png";
    }

    public static String getImagePath(int labeledFileName) {
        int folderLabel = labeledFileName - (labeledFileName % 100);
//        Log.d("getImagePath()", "folderLabel " + folderLabel);
        switch (folderLabel) {
            case TARGET_LABEL:
                return "/wmplab/stimuli/" + TARGET + labeledFileName % 100 + ".png";
            case SEMANTIC_LABEL:
                return "/wmplab/stimuli/" + SEMANTIC + labeledFileName % 100 + ".png";
            case PERCEPTUAL_LABEL:
                return "/wmplab/stimuli/" + PERCEPTUAL + labeledFileName % 100 + ".png";
            case DISTRACTOR_LABEL:
                return "/wmplab/stimuli/" + DISTRACTOR + labeledFileName % 100 + ".png";
            default:
                return "error";
        }
    }

    public static <T> String iterableToString(Iterable<T> iterable) {
        StringBuilder result = new StringBuilder();
        for (T t : iterable)
            result.append(t).append(" ");
        return result.toString();
    }
}
