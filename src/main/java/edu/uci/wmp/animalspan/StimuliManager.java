package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.uci.wmp.animalspan.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

public class StimuliManager {

    private static final StimuliManager INSTANCE = new StimuliManager();

    public static final int UPDOWN = 1;
    public static final int RIGHTUP = 0;
    public static final int NOANSWER = -1;
    public static final int CORRECT = 1;
    public static final int INCORRECT = 0;
	public static final String WMP_STIMULI_PATH = "/wmplab/ToyStore/stimuli/";
    public static final String TARGET = "list1/";
    public static final String SEMANTIC = "list2/";
    public static final String PERCEPTUAL = "list3/";
    public static final String DISTRACTOR = "distractors/";
    public static final String MISC = "miscellaneous/";
	public static final String BACKGROUND_FILENAME = "background.jpeg";
	public static final String DEFAULT_THEME_NAME = "shapes";
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

    private Context context;

    public StimuliManager() {

    }

    public void setContext(Context context) { this.context = context; }

    /**
     * @param folder using static string from StimuliManager.java
     * @param filename just the filename only (e.g. 1)
     */
    public Bitmap getStimuli(String folder, int filename) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is = assetManager.open(getImagePath(folder, filename));
        return BitmapFactory.decodeStream(is);
    }

    /**
     * @param labeledFileName folder label in first digit using static int from StimuliManager.java + filename
     */
    public Bitmap getStimuli(int labeledFileName) throws FileNotFoundException {
        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + getImagePath(labeledFileName);
        File imageFile = new File(path);
        InputStream in = new FileInputStream(imageFile);
        return BitmapFactory.decodeStream(in);
    }

	/**
	 * Scale down and return bitmap to fit screen, prevent OutOfMemoryError
	 */
	public Drawable getBackground() throws IOException {
		int part = LevelManager.getInstance().part;
		int reqWidth = LevelManager.getInstance().screen_width;
		int reqHeight = LevelManager.getInstance().screen_height;
		Bitmap bitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true; // First decode with inJustDecodeBounds=true to check dimensions
		switch (part) {
			case LevelManager.MAINSCREEN:
				BitmapFactory.decodeResource(context.getResources(), R.drawable.mainscreen_toystore, options);
				options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
				bitmap =  BitmapFactory.decodeResource(context.getResources(), R.drawable.mainscreen_toystore, options);
				break;
//				return ResourcesCompat.getDrawable(context.getResources(), R.drawable.mainscreen_lineup, null);
			case LevelManager.GETREADY: // GAME
				String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + WMP_STIMULI_PATH + LevelManager.getInstance().theme + "/" + BACKGROUND_FILENAME;
				File backgroundFile = new File(path);
				if (backgroundFile.exists()) {
					BitmapFactory.decodeFile(path, options);
					options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
					bitmap = BitmapFactory.decodeFile(path, options);
				}
				else { // some themes may not have background file; in this case, load default background
					BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
					options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
					bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
				}
				break;
//				return Drawable.createFromPath(path);
			default:
				BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
				options = getBitOptionsForDecodingSampleBitmap(options, reqWidth, reqHeight);
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.background, options);
//				return ResourcesCompat.getDrawable(context.getResources(), R.drawable.background, null);
		}
//		Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
		return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bitmap, LevelManager.getInstance().screen_width, LevelManager.getInstance().screen_height, true));
	}

	private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;
			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth)
				inSampleSize *= 2;
		}
		return inSampleSize;
	}

	private BitmapFactory.Options getBitOptionsForDecodingSampleBitmap(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		return options;
	}

    public Bitmap getFeedbackAsset(int result) throws IOException {
        AssetManager assetManager = context.getAssets();
        InputStream is;
        if (result == CORRECT)
            is = assetManager.open("stimuli/" + MISC + "correct.png");
        else
            is = assetManager.open("stimuli/" + MISC + "incorrect.png");
        return BitmapFactory.decodeStream(is);
    }

    public String getImagePath(String folder, int filename) {
        return "stimuli/" + folder + filename + ".png";
    }

    public String getImagePath(int labeledFileName) {
        int folderLabel = labeledFileName - (labeledFileName % 100);
//        Log.d("getImagePath()", "folderLabel " + folderLabel);
        switch (folderLabel) {
            case TARGET_LABEL:
                return "/wmplab/Toy Store/stimuli/" + TARGET + labeledFileName % 100 + ".png";
            case SEMANTIC_LABEL:
                return "/wmplab/Toy Store/stimuli/" + SEMANTIC + labeledFileName % 100 + ".png";
            case PERCEPTUAL_LABEL:
                return "/wmplab/Toy Store/stimuli/" + PERCEPTUAL + labeledFileName % 100 + ".png";
            case DISTRACTOR_LABEL:
                return "/wmplab/Toy Store/stimuli/" + DISTRACTOR + labeledFileName % 100 + ".png";
            default:
                return "error";
        }
    }

    public static StimuliManager getInstance() { return INSTANCE; }
}
