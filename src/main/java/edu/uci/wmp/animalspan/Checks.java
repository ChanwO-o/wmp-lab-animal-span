package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;
import android.widget.Toast;

import com.uci.wmp.animalspan.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Checks {
    private static final Checks INSTANCE = new Checks();

	public static final String HOME_PATH = "/wmplab/Toy Store/";
    public static final String LEVELFOLDER_PATH = "/wmplab/Toy Store/levels/";
    public static final String STIMULIFOLDER_PATH = "/wmplab/Toy Store/stimuli/";
	public static final String FEEDBACKFOLDER_PATH = "/wmplab/Toy Store/feedback/";
    public static final String STIMULI_LIST1_PATH = STIMULIFOLDER_PATH + "list1/";
    public static final String STIMULI_LIST2_PATH = STIMULIFOLDER_PATH + "list2/";
    public static final String STIMULI_LIST3_PATH = STIMULIFOLDER_PATH + "list3/";
    public static final String STIMULI_DIST_PATH = STIMULIFOLDER_PATH + "distractors/";
	public static final String STRINGS_NAME = "strings.txt";
	public static final String THEMEORDER_NAME = "theme_order.txt";

    private StringBuilder errorMessages = new StringBuilder();
    private Context context;

    public Checks() { }

    public class InvalidLevelFilesException extends IOException {}
	public class InvalidStimuliFilesException extends IOException {}
	public class InvalidFeedbackFilesException extends IOException {}
	public class InvalidStringsFileException extends IOException {}
	public class InvalidThemeOrderFileException extends IOException {}

    public void setContext(Context context) { getInstance().context = context; }

    /**
     * Check Levels directory and stimuli directory, fire toast message of all missing directories and files
     */
    public boolean runAllChecks() {
        boolean checkPass = true;
        try { Checks.getInstance().checkLevelsDirectory(); }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error checking level files", Toast.LENGTH_SHORT).show();
            Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            checkPass = false;
        }
        errorMessages.setLength(0); // clear error message queue

        try { Checks.getInstance().checkStimuliDirectory(); }
        catch (Checks.InvalidStimuliFilesException e) {
            Toast.makeText(getInstance().context, "Error checking stimuli files", Toast.LENGTH_SHORT).show();
            Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            checkPass = false;
        }
        errorMessages.setLength(0);

	    try { Checks.getInstance().checkFeedbackDirectory(); }
	    catch (Checks.InvalidFeedbackFilesException e) {
		    Toast.makeText(getInstance().context, "Error checking feedback files", Toast.LENGTH_SHORT).show();
		    Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
		    checkPass = false;
	    }
	    errorMessages.setLength(0);

	    try { Checks.getInstance().checkStringsFile(); }
	    catch (Checks.InvalidStringsFileException e) {
		    Toast.makeText(getInstance().context, "Error checking string file", Toast.LENGTH_SHORT).show();
		    Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
		    checkPass = false;
	    }
	    errorMessages.setLength(0);


	    return checkPass;
    }

    /**
     * Populate all level & stimuli files, return true if successful
     */
    public boolean populateAssets() {
        try {
            populateLevelDirectory();
            populateStimuliDirectory();
	        populateFeedbackDirectory();
	        populateStrings();
	        populateThemeOrder();
            return true;
        }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error populating level files", Toast.LENGTH_SHORT).show();
            return false;
        }
        catch (Checks.InvalidStimuliFilesException e) {
	        Toast.makeText(getInstance().context, "Error populating stimuli files", Toast.LENGTH_SHORT).show();
	        return false;
        }
        catch (Checks.InvalidFeedbackFilesException e) {
	        Toast.makeText(getInstance().context, "Error populating feedback files", Toast.LENGTH_SHORT).show();
	        return false;
        }
        catch (Checks.InvalidStringsFileException e) {
	        Toast.makeText(getInstance().context, "Error populating string file", Toast.LENGTH_SHORT).show();
	        return false;
        }
        catch (Checks.InvalidThemeOrderFileException e) {
	        Toast.makeText(getInstance().context, "Error populating theme order file", Toast.LENGTH_SHORT).show();
	        return false;
        }
    }

    /**
     * Checks Levels directory for missing files
     * Append missing file names or directories to error message queue
     */
    public void checkLevelsDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);

        if (!outLevelFolder.exists()) { // no dir; don't bother looking through level files
            Log.e("checkLevelsDirectory()", "Level Folder does not exist");
            errorMessages.append("Level folder does not exist\n");
            throw new InvalidLevelFilesException();
        }

        String[] levelFiles = outLevelFolder.list();
        Set<Integer> checkedLevels = new HashSet<>();

        for (String level : levelFiles) {
            if (level.length() != 10 && level.length() != 11)               // level file names are 10 or 11 characters long
                continue;
            if (!(level.startsWith("level") && level.endsWith(".txt")))     // level file names start and end with "level", ".txt"
                continue;
            int numStartIndex = 5;
            int numEndIndex = level.indexOf(".txt");
            int levelNumber = Integer.valueOf(level.substring(numStartIndex, numEndIndex)); // get level number
            if (levelNumber < LevelManager.MIN_LEVEL || LevelManager.MAX_LEVEL < levelNumber)   // level file's number must be in range
                continue;
            checkedLevels.add(levelNumber);
        }

        for (int i = LevelManager.MIN_LEVEL; i <= LevelManager.MAX_LEVEL; ++i) {
            if (!checkedLevels.contains(i)) {
                String missingMsg = "level" + i + ".txt is missing\n";
                errorMessages.append(missingMsg);
            }
        }
        if (errorMessages.length() != 0)
            throw new InvalidLevelFilesException();
    }

    /**
     * Checks Stimuli directory for missing files
     * Append missing file names or directories to error message queue
     */
    public void checkStimuliDirectory() throws InvalidStimuliFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        File outStimuliFolder = new File(root.getAbsolutePath() + STIMULIFOLDER_PATH);
	    final String DEFAULT_STIM_PATH = root.getAbsolutePath() + STIMULIFOLDER_PATH + StimuliManager.DEFAULT_THEME_NAME + "/";
	    File defaultThemeFolder = new File(DEFAULT_STIM_PATH);
        File list1Folder = new File(DEFAULT_STIM_PATH + StimuliManager.TARGET);
        File list2Folder = new File(DEFAULT_STIM_PATH + StimuliManager.SEMANTIC);
        File list3Folder = new File(DEFAULT_STIM_PATH + StimuliManager.PERCEPTUAL);
        File distFolder = new File(DEFAULT_STIM_PATH + StimuliManager.DISTRACTOR);
        File[] stimFolders = new File[] {list1Folder, list2Folder, list3Folder, distFolder};

	    if (!outStimuliFolder.exists()) { // no dir; don't bother looking through stimuli files
		    Log.e("checkStimuliDirectory()", "Stimuli Folder does not exist");
		    errorMessages.append("Stimuli folder does not exist\n");
		    throw new InvalidStimuliFilesException();
	    }

	    if (!defaultThemeFolder.exists()) {
		    Log.e("checkStimuliDirectory()", "Default theme Folder does not exist");
		    errorMessages.append("Default theme folder does not exist\n");
		    throw new InvalidStimuliFilesException();
	    }

        for (File stimFolder : stimFolders) // check each stimuli folder one by one
        {
            if (!stimFolder.exists()) {
                String missingFolderMsg = stimFolder.getName() + " folder missing\n";
                errorMessages.append(missingFolderMsg);
                continue;
            }

            String[] stimuliFiles = stimFolder.list();
            Set<Integer> checkedStimuli = new HashSet<>();

            for (String stimuli : stimuliFiles) {
                if (stimuli.length() != 5 && stimuli.length() != 6)         // stimuli file names are 5 or 6 characters long
                    continue;
                if (!stimuli.endsWith(".png"))                              // stimuli file names end with ".png"
                    continue;
                int numEndIndex = stimuli.indexOf(".png");
                int stimuliNumber = Integer.valueOf(stimuli.substring(0, numEndIndex)); // get stimuli number
                if (stimuliNumber < StimuliManager.MIN_STIMULI_CHOICES || StimuliManager.MAX_STIMULI_CHOICES < stimuliNumber)   // stimuli file's number must be in range
                    continue;
                checkedStimuli.add(stimuliNumber);
            }

            for (int i = StimuliManager.MIN_STIMULI_CHOICES; i <= StimuliManager.MAX_STIMULI_CHOICES; ++i) {
                if (!checkedStimuli.contains(i)) {
                    String missingMsg = stimFolder.getName() + "/" + i + ".png is missing\n";
                    errorMessages.append(missingMsg);
                }
            }
        }
        if (errorMessages.length() != 0)
            throw new InvalidStimuliFilesException();
    }

	/**
	 * Checks Feedback directory for missing files
	 * Append missing file names or directories to error message queue
	 */
	public void checkFeedbackDirectory() throws InvalidFeedbackFilesException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outFeedbackFolderPath = root.getAbsolutePath() + FEEDBACKFOLDER_PATH;
		File outFeedbackFolder = new File(outFeedbackFolderPath);

		if (!outFeedbackFolder.exists()) {
			Log.e("checkFeedbackDir()", "Feedback Folder does not exist");
			errorMessages.append("Feedback folder does not exist\n");
			throw new InvalidFeedbackFilesException();
		}

		String[] feedbackFiles = outFeedbackFolder.list();
		if (!Arrays.asList(feedbackFiles).contains("roundfeedback_down.txt"))
			errorMessages.append("round_down.txt does not exist\n");
		else if (!Arrays.asList(feedbackFiles).contains("roundfeedback_same.txt"))
			errorMessages.append("round_same.txt does not exist\n");
		else if (!Arrays.asList(feedbackFiles).contains("roundfeedback_up.txt"))
			errorMessages.append("round_up.txt does not exist\n");

		if (errorMessages.length() != 0)
			throw new InvalidFeedbackFilesException();
	}

	/**
	 * Checks Strings file
	 */
	public void checkStringsFile() throws InvalidStringsFileException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outStringsPath = root.getAbsolutePath() + HOME_PATH + STRINGS_NAME;
		File stringsFile = new File(outStringsPath);

		if (!stringsFile.exists()) {
			Log.e("checkStringsFile()", "Strings file does not exist");
			errorMessages.append("strings.txt does not exist\n");
			throw new InvalidStringsFileException();
		}

		if (errorMessages.length() != 0)
			throw new InvalidStringsFileException();
	}

    public void populateLevelDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);
        Log.i("populateLevelDir()", "folder " + outLevelFolder.mkdirs());

        try { copyDirectory("levels/", outLevelFolderPath); }
        catch (IOException e) { throw new InvalidLevelFilesException(); }
    }

    public void populateStimuliDirectory() throws InvalidStimuliFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String stimuliFolderPath = root.getAbsolutePath() + STIMULIFOLDER_PATH;
	    String defaultThemeFolderPath = stimuliFolderPath + StimuliManager.DEFAULT_THEME_NAME + "/";
//	    File stimuliFolder = new File(stimuliFolderPath);
        File defaultThemeFolder = new File(defaultThemeFolderPath);
	    Log.i("populateStimuliDir()", "Folders created " + defaultThemeFolder.mkdirs());

        try {
            String[] destinations = new String[] { StimuliManager.TARGET, StimuliManager.SEMANTIC, StimuliManager.PERCEPTUAL, StimuliManager.DISTRACTOR };
            for (String dest : destinations) {
                String categoryFolderPath = defaultThemeFolderPath + dest;
                File categoryFolder = new File(categoryFolderPath);
                Log.i("populateStimuliDir()", "category " + dest + " created " + categoryFolder.mkdirs());
                copyDirectory("stimuli/" + StimuliManager.DEFAULT_THEME_NAME + "/" + dest, categoryFolderPath);
            }
        }
        catch (IOException e) { e.printStackTrace(); throw new InvalidStimuliFilesException(); }
    }

	public void populateFeedbackDirectory() throws InvalidFeedbackFilesException {
		final int[] files = new int[] { R.raw.roundfeedback_down, R.raw.roundfeedback_same, R.raw.roundfeedback_up };
		File root = android.os.Environment.getExternalStorageDirectory();
		String outFeedbackFolderPath = root.getAbsolutePath() + FEEDBACKFOLDER_PATH;
		File outFeedbackFolder = new File(outFeedbackFolderPath);
		Log.i("populateFeedbackDir()", "folder " + outFeedbackFolder.mkdirs());

		try {
			for (int i : files) {
				String fileName;
				if (i == R.raw.roundfeedback_down)
					fileName = "roundfeedback_down.txt";
				else if (i == R.raw.roundfeedback_same)
					fileName = "roundfeedback_same.txt";
				else
					fileName = "roundfeedback_up.txt";
				copyResource(i, outFeedbackFolderPath + fileName);
			}
		}
		catch (IOException e) { e.printStackTrace(); throw new InvalidFeedbackFilesException(); }
	}

	/**
	 * Setup default english strings
	 */
	public void populateStrings() throws InvalidStringsFileException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outStringsPath = root.getAbsolutePath() + HOME_PATH + STRINGS_NAME;

		try {
			copyResource(R.raw.strings, outStringsPath);
		}
		catch (IOException e) { e.printStackTrace(); throw new InvalidStringsFileException(); }
	}

	/**
	 * Setup theme order with single default theme
	 */
	public void populateThemeOrder() throws InvalidThemeOrderFileException {
		File root = android.os.Environment.getExternalStorageDirectory();
		String outTOPath = root.getAbsolutePath() + HOME_PATH + THEMEORDER_NAME;
		BufferedWriter writer;

		try {
			File themeOrderFile = new File(outTOPath);
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(themeOrderFile)));
			writer.write(StimuliManager.DEFAULT_THEME_NAME);
			writer.close(); // not sure if this is handled; if exception is thrown above this line isn't reached
		}
		catch (IOException e) { e.printStackTrace(); throw new InvalidThemeOrderFileException(); }
	}

    private void copyDirectory(String assetFolderPath, String outFolderPath) throws IOException {
        Log.i("copyDirectory()", assetFolderPath + " -> " + outFolderPath);
        String trimmedAssetFolderPath = assetFolderPath.substring(0, assetFolderPath.length() - 1);
        AssetManager am = context.getAssets();
        InputStream in;
        OutputStream out;

        for (String filename : am.list(trimmedAssetFolderPath)) {
            Log.d("reading asset", filename);
            in = am.open(assetFolderPath + filename);
            File outFile = new File(outFolderPath, filename);
            out = new FileOutputStream(outFile);

            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        }
    }

	private void copyResource(int id, String filePath) throws IOException {
		Log.d("copyResource()", "start");
		InputStream in = context.getResources().openRawResource(id);
		FileOutputStream out = new FileOutputStream(filePath);
		byte[] buffer = new byte[1024];
		int read;
		try {
			while ((read = in.read(buffer)) > 0)
				out.write(buffer, 0, read);
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			in.close();
			out.close();
		}
		Log.d("copyResource()", "end");
	}

    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }

    }

    public static Checks getInstance() {
        return INSTANCE;
    }
}
