package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import edu.uci.wmp.animalspan.fragments.Settings;

/**
 * Created by ChanWoo on 11/18/2015
 */
public class LevelManager implements Serializable {

    private static final LevelManager INSTANCE = new LevelManager();

    public static final int MIN_LEVEL = 1;                  // lowest level available
    public static final int MAX_LEVEL = 30;                 // highest level available
    public static final int STARTLEVEL = 1;                 // define which level to start by default with no saves
    public static final int DEMO_MAX_ROUNDS = 3;            // demo mode plays only 3 rounds
	public static final int MAINSCREEN = 0;                 // stage 1
	public static final int GETREADY = 1;                   // get ready
	public static final int STAGE1 = 2;                   // get ready
	public static final int STAGE2 = 3;                   // get ready
	public static final int STAGE0 = -1;                    // neither
    public static final String TRAININGMODE_ROUNDS = "rounds";
    public static final String TRAININGMODE_TIME = "time";
    public static final String TRAININGMODE_DEMO = "demo";
    private static final String SAVE_FOLDER_PATH = "/wmplab/Toy Store/data/";
    public static final String SAVE_LEVEL_FILENAME = "_data.txt";
	private static final String THEME_ORDER_FILENAME = "/wmplab/Toy Store/theme_order.txt";
    public static final String SHAREDPREF_KEY = "shared_pref";
	public static final int THEME_NOCHANGE = 0;

    private Context context;
    private Random random;

    // initialize experiment wide variables (do not change this)
    public int subject = 1;
    public int session = 1;
	public String theme = StimuliManager.DEFAULT_THEME_NAME;
    public int level = 1;
	public int roundsPlayed = 0;                            // cumulative number of rounds played; if == sessionLevels: end game
    public int trial = 0;                                   // == rounds
    public int part = STAGE0;                               // 1 = Stage1, 2 = Stage2, 0 = neither
    public int currentStimuliIndex = 0;                     // index of current pic
    public boolean testStarted = false;
    public long sessionStartMills = 0;                      // timer starting at beginning of session, used when mode = "time"
    public boolean questions = true;
	public int changeTheme = THEME_NOCHANGE;
	public List<String> themeOrder;
	public int themeIndex = 0;
	boolean themeIsLoaded;
    public boolean debug = false;
    public int points = 0;                              // records total points awarded for the session
	public List<String> strings;

    public List<Integer> stimulisequence;              // defines what stimuli has to be shown
    public List<Integer> distincttargets;              // distinct targets that the stimuli sequence is chosen from, use this for displaying image grid in Stage2
    public List<Integer> distinctdistractors;          //
    public List<Integer> correctstimulisequence;       // defines animal stimuli that should be recalled in the second part
    public List<Integer> presentationstyle;            // defines whether stimuli should be presented normally or upside-down

    public List<Integer> responsesfirstpart;           // keep track of responses in first part
    public List<Integer> secondpartsequence;           // keep track of the response in the second part
    public List<Long> rtfirstpart;                  // keep track of reaction times in first part
    public List<Long> rtsecondpart;                 // keep track of reaction times in second part
    public List<Integer> accuracyfirstpart;            // keep track of correct answers in first part
    public List<Integer> accuracysecondpart;           // keep track of correct answers in second part

    // -------------------------------------------------------------------------------------------
    // define default experiment parameters
    // -------------------------------------------------------------------------------------------
    // general settings
    public int screen_width = 1280; // taskresolutionwidth = 1024;         // define resolution for which game is optimized
    public int screen_height = 736; // getTaskresolutionheight = 768;

    // training parameters (this does NOT go into level file)
    public String trainingmode = TRAININGMODE_ROUNDS;        // time: ends session after a certain amount of time; rounds: ends session after a certain amount of rounds
    public int sessionLength = 300;               // This is the length of the session in seconds (default: 300s)
    public int numberoftrials = 10;               // define how long a training session takes in number of levels

    // default level parameters
    public String leveltrainingmode = "time";     // UNUSED time: ends level after a certain amount of time; trials: ends level after a certain amount of trials
    public int levelLength = 30;                  // UNUSED This is the length of the level in seconds
    public int numberofleveltrials = 2;           // UNUSED define how long a level takes in number of trials
    public int setsize = 4;                       // number of target stimuli to be shown
    public int distractorsize = 2;                // number of distractors (non animals) to be shown (this only works if numberofdistinctdistractors > 0)

    // miscellaneous
    public int numberofdistincttargets = 4;       // number of targets
    public int numberofperceptuallures = 2;       // (list3) number of perceptual lures shown in choice stimuli (there cannot be more lures than distict targets)
    public int numberofsemanticlures = 2;         // (list2) number of semantic lures shown in choice stimuli (there cannot be more lures than distict targets)
    public int numberofdistinctdistractors = 0;   // number of unrelated distractors

    public boolean keeptargetconstant = false;        // selects always the same stimulus set as target set (caution: this interacts with randomlypickstimuli!)
    public boolean randomlypickstimuli = true;        // select random stimuli for 1 session; True: selects images randomly based on pool of images; False: selects first n stimuli
    public boolean randomstimuliineverytrial = true;  // randomly selects new stimuli in every trial

    // first part of experiment
    public String sizeoffirststimuli = "50/50";        // size of the stimuli shown in the first part (in percent of window height)
    public int timetoanswerfirstpart = 2;              // define how much time do participants have to decide how the stimulus is presented
    public boolean showbuttonpressfeedback = false;    // enable/disable feedback for accuracy of button presses

    // second part of experiment
    public double topborder = 3.5;              // top border in percent to array of choice stimuli
    public int bottomborder = 50;               // bottom border in percent of window height to array of choice stimuli
    public int sideborder = 6;                  // side border in percent of window width to array of choice stimuli
    public double gapbetweenimages = 3.5;       // gap between stimuli in percent of window width in array of choice stimuli
    public int stimulusside = 17;               // default size of choice stimuli (deprecated)

    public int feedbacktopborder = 40;          // top border in percent
    public int feedbackbottomborder = 2;        // bottom border in percent to feedback stimuli
    public double feedbacksideborder = 3.5;     // side borders in percent to feedback stimuli
    public int feedbackstimulussize = 13;       // default size of choice stimuli (deprecated)
    public int feedbackgapbetweenimages = 1;    // gap between stimuli in percent in array of choice stimuli

    public int stimuliperline = 4;              // number of stimuli per line in choice stimuli
    public boolean showchoicefeedback = true;   // enable/disable feedback images on the bottom of the screen in the second part
    public boolean randomlydistribute = true;   // randomly distribute choice stimuli in every trial
    // -------------------------------------------------------------------------------------------

    public LevelManager() {
        random = new Random();
        level = STARTLEVEL;
        trial = 0;
        part = STAGE0;
        reset();
    }

    /**
     * Set pixel values for width & height.
     * Can be used only if context is given; thus not called in default constructor
     */
    public void setScreenDimensions() {
        WindowManager windowManager = (WindowManager) this.context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();

        // display size in pixels
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;
        screen_height = size.y;
        Log.d("Width", "" + screen_width);
        Log.d("Height", "" + screen_height);
    }

    public void reset() {
        // reinitialize lists
        stimulisequence = new ArrayList<>();
        distincttargets = new ArrayList<>();
        distinctdistractors = new ArrayList<>();
        correctstimulisequence = new ArrayList<>();
        presentationstyle = new ArrayList<>();
        secondpartsequence = new ArrayList<>();
        responsesfirstpart = new ArrayList<>();
        rtfirstpart = new ArrayList<>();
        rtsecondpart = new ArrayList<>();
        accuracyfirstpart = new ArrayList<>();
        accuracysecondpart = new ArrayList<>();
	    themeOrder = new ArrayList<>();
	    strings = new ArrayList<>();
    }

    /**
     * Setup LevelManager for new session
     */
    public void startSession() {
        stimulisequence.clear();
        distincttargets.clear();
        distinctdistractors.clear();
        correctstimulisequence.clear();
        presentationstyle.clear();
        secondpartsequence.clear();
        responsesfirstpart.clear();
        rtfirstpart.clear();
        rtsecondpart.clear();
        accuracyfirstpart.clear();
        accuracysecondpart.clear();

        if (trainingmode.equals(TRAININGMODE_DEMO)) {
            loadLevel(STARTLEVEL);
            numberoftrials = DEMO_MAX_ROUNDS;
        }
        else
            loadSavedLevel(); // sets level variable if there is a saved instance

	    if (changeTheme != THEME_NOCHANGE)
		    readThemeOrder();

        sessionStartMills = SystemClock.uptimeMillis(); // record session starting time (used for trainingmode = "time")
        trial = 0;
	    roundsPlayed = 0;
        testStarted = true;
        points = 0; // reset score
	    loadStrings();
        CSVWriter.getInstance().createCsvFile();
    }

    /**
     * Called at the beginning of a trial
     */
    public void startRound() {
        stimulisequence.clear();
        distincttargets.clear();
        distinctdistractors.clear();
        correctstimulisequence.clear();
        presentationstyle.clear();
        secondpartsequence.clear();
        responsesfirstpart.clear();
        rtfirstpart.clear();
        rtsecondpart.clear();
        accuracyfirstpart.clear();
        accuracysecondpart.clear();
        loadLevel(level);
	    applyTheme();
    }

    public void loadLevel(int level) {
        try {
            setLevel(level);
            BufferedReader br = openFileAsReader();

            String line;
            while ((line = br.readLine()) != null) {
//                Log.d("loadLevel()", line);
                processLine(line);
            }
        } catch (InvalidLevelException e) {
            Log.e("loadLevel()", "Invalid level");
        } catch (IOException e) {
            Log.e("loadLevel()", "IO exception");
        } catch (NoSuchFieldException e) {
            Log.e("loadLevel()", "NoSuchField exception");
        } catch (IllegalAccessException e) {
            Log.e("loadLevel()", "IllegalAccessException");
        }

        generateStimuliSequence();
        generateOrientationSequence();
    }

    /**
     * Sets level variable if there is a saved instance. If none, sets it to startlevel
     */
    public void loadSavedLevel() {
        try {
            File root = android.os.Environment.getExternalStorageDirectory();
            BufferedReader reader = new BufferedReader(new FileReader(root.getAbsolutePath() + SAVE_FOLDER_PATH + subject + SAVE_LEVEL_FILENAME));
            String savedLevel = reader.readLine();
            level = Integer.valueOf(savedLevel);
            Log.i("loadSavedLevel()", "Loaded level " + savedLevel);
        } catch (FileNotFoundException e) {
            Log.e("loadSavedLevel()", "No save file found, setting to startlevel (level " + STARTLEVEL);
            level = STARTLEVEL;
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("loadSavedLevel()", "Error loading save");
            level = STARTLEVEL;
            e.printStackTrace();
        }

    }

    /**
     * Save final level user has reached/will continue from.
     * If sessionFin == true, user has correctly completed the session and will continue onwards from the last level + 1 unless at level 30.
     * ELse if sessionFin == false, user has aborted the game and will have to continue from level - 1 unless at level 1.
     */
    public void saveLevelToFile(boolean sessionFin) {
        try {
            File root = android.os.Environment.getExternalStorageDirectory();
            File saveFolder = new File (root.getAbsolutePath() + SAVE_FOLDER_PATH);
            if (!saveFolder.exists())
                Log.i("saveLevelToFile()", "Save folder created " + saveFolder.mkdirs());
            File saveFile = new File (saveFolder, subject + SAVE_LEVEL_FILENAME);
            FileWriter fw = new FileWriter(saveFile, false);
            BufferedWriter writer = new BufferedWriter(fw);
            if (sessionFin)
                writer.write(Integer.toString(level));
            else { // 1 level down
                if (level > 1)
                    writer.write(Integer.toString(--level));
                else
                    writer.write(Integer.toString(level));
            }
            Log.i("saveLevelToFile()", "Saved on level " + level);
            writer.newLine();
	        Log.i("saveLevelToFile()", "Saved on theme index " + themeIndex);
	        writer.write(Integer.toString(themeIndex)); // theme order index
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	/**
	 * Fill themeOrder with list of themes specified in theme_order.txt in root wmp directory
	 */
	private void readThemeOrder() {
		try {
			File root = android.os.Environment.getExternalStorageDirectory();
			BufferedReader reader = new BufferedReader(new FileReader(root.getAbsolutePath() + THEME_ORDER_FILENAME));

			String line;
			while ((line = reader.readLine()) != null) {
				Log.d("readThemeOrder()", line);
				if (StimuliManager.hasTheme(line))
					themeOrder.add(line);
			}
		} catch (FileNotFoundException e) {
			Log.e("readThemeOrder()", "No theme order file found");
			e.printStackTrace();
		} catch (IOException e) {
			Log.e("readThemeOrder()", "Error reading theme order file");
			e.printStackTrace();
		} finally {
			if (themeOrder.isEmpty()) {
				Log.i("readThemeOrder()", "themeOrder empty; adding default");
				themeOrder.add(StimuliManager.DEFAULT_THEME_NAME);
			}
		}
	}

	/**
	 * Check if theme exists, set theme to default if not
	 * If changeTheme is on, calculate theme order index
	 * Configure number of sets in theme & number of stimuli in each set
	 * Set activity background image to theme background
	 */
	public void applyTheme() {
		if (changeTheme != THEME_NOCHANGE) {
			Log.wtf("ct", changeTheme + "");
			Log.wtf("to size", themeOrder.size() + "");
			if (!themeIsLoaded) {
				Log.wtf("calc roundsPlayed", roundsPlayed + "");
				Log.wtf("calc ct", changeTheme + "");
				themeIndex = (roundsPlayed / changeTheme) % themeOrder.size(); // calculate index of theme from list
			}
			themeIsLoaded = false;
			theme = themeOrder.get(themeIndex);
			Log.wtf("theme at index " + themeIndex, "set to " + theme);
		}
		String stimPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + StimuliManager.WMP_STIMULI_PATH;
		Log.i("checking theme ", theme);
		if (!StimuliManager.hasTheme(theme)) {
			Log.i("theme does not exist", "set to default theme");
			theme = StimuliManager.DEFAULT_THEME_NAME;
		}

		// set numberOfPicturesInCategory
		File temp = new File(stimPath + theme + "/" + StimuliManager.TARGET);
		StimuliManager.getInstance().numberOfPicturesInCategory = temp.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return !file.isDirectory();
			}
		}).length;
		Log.d("numPicturesInCategory", "" + StimuliManager.getInstance().numberOfPicturesInCategory);

		Util.setActivityBackground(context);
	}

    public void setContext(Context context) { this.context = context; }

    /**
     * Store selected variables into preferences
     */
    public void saveSharedPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Settings.SUBJECT_KEY, subject);
        editor.putInt(Settings.SESSION_KEY, session);
        editor.putBoolean(Settings.QUESTIONS_KEY, questions);
	    editor.putInt(Settings.CHANGETHEME_KEY, changeTheme);
        editor.putBoolean(Settings.DEBUG_KEY, debug);
        editor.putString(Settings.TRAININGMODE_KEY, trainingmode);
        editor.putInt(Settings.ROUNDS_KEY, numberoftrials);
        editor.putInt(Settings.SESSIONLENGTH_KEY, sessionLength);
        editor.apply();
    }

    /**
     * Read variables from preferences
     */
    public void readSharedPreferences() {
        SharedPreferences sharedPref = context.getSharedPreferences(SHAREDPREF_KEY, Context.MODE_PRIVATE);
        subject = sharedPref.getInt(Settings.SUBJECT_KEY, 1);
        session = sharedPref.getInt(Settings.SESSION_KEY, 1);
        questions = sharedPref.getBoolean(Settings.QUESTIONS_KEY, true);
	    changeTheme = sharedPref.getInt(Settings.CHANGETHEME_KEY, LevelManager.THEME_NOCHANGE);
        debug = sharedPref.getBoolean(Settings.DEBUG_KEY, true);
        trainingmode = sharedPref.getString(Settings.TRAININGMODE_KEY, TRAININGMODE_ROUNDS);
        numberoftrials = sharedPref.getInt(Settings.ROUNDS_KEY, 10);
        sessionLength = sharedPref.getInt(Settings.SESSIONLENGTH_KEY, 300);
    }

    public void setLevel(int newLevel) throws InvalidLevelException {
        if (newLevel < MIN_LEVEL || newLevel > MAX_LEVEL) {
            throw new InvalidLevelException("Level must be " + MIN_LEVEL + " <= n <= " + MAX_LEVEL);
        }
        level = newLevel;
    }

    public class InvalidLevelException extends Exception {
        public InvalidLevelException(String message) {
            super(message);
        }
    }

    public String getLevelFilePath() {
        return "/wmplab/Toy Store/levels/level" + level + ".txt";
    }

    public BufferedReader openFileAsReader() throws IOException {
//        AssetManager am = context.getAssets(); // using in-app assets
//        InputStream in = am.open(getLevelFilePath());
        String path = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + getLevelFilePath();
        File levelFile = new File(path);
        InputStream in = new FileInputStream(levelFile);
        return new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Set value of variable through reflection
     * @param varName name of variable to modify
     * @throws NoSuchFieldException
     */
    public void setLevelVariable(String varName, String newValue) throws NoSuchFieldException, IllegalAccessException {

        if (CSVWriter.getInstance().canIgnore(varName)) // if (varName.equals("showinfullscreen") || varName.equals("abortallowed"))
            return;

        Field var = this.getClass().getDeclaredField(varName);

        // check field type and assign correct value
        if (var.getType().getName().equals("int")) {
            var.setInt(this, Integer.valueOf(newValue));
//            Log.i("typecheck", "Integer");
        }
        else if (var.getType().getName().equals("java.lang.String")) {
            var.set(this, newValue);
//            Log.i("typecheck", "String");
        }
        else if (var.getType().getName().equals("boolean")) {
            var.setBoolean(this, newValue.equals("1"));
//            Log.i("typecheck", "Boolean");
        }
        else if (var.getType().getName().equals("double")) {
            var.setDouble(this, Double.valueOf(newValue));
//            Log.i("typecheck", "Double");
        }
    }

    public void processLine(String s) throws NoSuchFieldException, IllegalAccessException {
        int i = s.indexOf("=");
        if (i != -1)
            setLevelVariable(s.substring(0, i), s.substring(i + 1));
    }

    public void generateStimuliSequence() {
        fillTargets();
        fillDistractors();
        Collections.shuffle(stimulisequence);
        fillCorrectStimuliSequence();
    }

    private void fillTargets() {
        // choosing distinct targets
        List<Integer> temp = new ArrayList<>(StimuliManager.TEMPLATE); // copy values from TEMPLATE

        for (int i = 0; i < numberofdistincttargets; i++) {
	        int randomLabel = 100 * (random.nextInt(3) + 1);
            int randomIndex = random.nextInt(temp.size());
            int labeledDistinctTarget = temp.get(randomIndex) + randomLabel; // add label
            distincttargets.add(labeledDistinctTarget);
            temp.remove(randomIndex); // remove to prevent duplicates
        }

        // choosing sequence from distinct targets
        for (int i = 0; i < setsize; i++) {
            int generatedStimuli;
            do {
                int randomIndex = random.nextInt(distincttargets.size());
                generatedStimuli = distincttargets.get(randomIndex);
//                Log.d("chosen stimuli", "random: " + randomIndex + " genStim: " + generatedStimuli);
            } while (i >= 2 && stimulisequence.get(i - 1) == generatedStimuli && stimulisequence.get(i - 2) == generatedStimuli); // no three same stimuli in a row
//            while (i != 0 && stimulisequence.get(i - 1) == generatedStimuli); // no two same stimuli in a row

            stimulisequence.add(generatedStimuli);
//            Log.d("added", "" + generatedStimuli);
        }
    }

    private void fillDistractors() {
        if (numberofdistinctdistractors == 0) // no distractors in this level
            return;

        List<Integer> temp = new ArrayList<>(StimuliManager.TEMPLATE);

        for (int i = 0; i < numberofdistinctdistractors; i++) {
            int randomIndex = random.nextInt(temp.size());
            int labeledDistinctDistractor = temp.get(randomIndex) + StimuliManager.DISTRACTOR_LABEL; // add label 400
            distinctdistractors.add(labeledDistinctDistractor);
            temp.remove(randomIndex);
        }
        Log.d("distinct distractors", Util.iterableToString(distinctdistractors));

        for (int i = 0; i < distractorsize; i++) {
            int randomIndex = random.nextInt(distinctdistractors.size()); // past code had + 1 here >   = random.nextInt(numberofdistinctdistractors) + 1;
            int generatedDistractor = distinctdistractors.get(randomIndex);
            Log.d("chosen distrator", "random: " + randomIndex + " genStim: " + generatedDistractor);

            // do .. above .. while (i != 0 && stimulisequence.get(setsize + i - 1) == generatedDistractor);  // (setsize + i - 1) << added setsize to get correct index for distractors; overlaps with targets

            stimulisequence.add(generatedDistractor);
            Log.d("added", "" + generatedDistractor);
        }
    }

    /**
     * Add only non-distractors to correctstimulisequence by checking for DISTRACTOR_LABEL
     */
    public void fillCorrectStimuliSequence() {
        for (Integer stimuli : stimulisequence) {
            if ((stimuli / 100) * 100 != StimuliManager.DISTRACTOR_LABEL) // (e.g. 106 >> 1 >> 100)
                correctstimulisequence.add(stimuli);
        }
    }

    public void generateOrientationSequence() {
        for (int i = 0; i < setsize + distractorsize; i++) {
            presentationstyle.add(random.nextInt(2)); // 0 or 1
        }
    }

	/**
	 * Reads in strings from external strings.txt file into stringsList
	 */
	public void loadStrings() {
		File root = android.os.Environment.getExternalStorageDirectory();
		String path = root.getAbsolutePath() + Checks.HOME_PATH + Checks.STRINGS_NAME;
		File stringsFile = new File(path);

		try {
			InputStream in = new FileInputStream(stringsFile);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = reader.readLine()) != null) {
				strings.add(line);
			}
		} catch (FileNotFoundException e) {
			Toast.makeText(context, "strings.txt missing", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		} catch (IOException e) {
			Toast.makeText(context, "Error reading strings.txt", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}

    /**
     * Displays loaded variables to Logcat for debug purposes
     */
    public void logVariables() {
        Log.i("currentlevel", "" + level);
        Log.i("setsize", "" + setsize);
        Log.i("distractorsize", "" + distractorsize);
        Log.i("numberofdistincttargets", "" + numberofdistincttargets);
        Log.i("numberofperceptuallures", "" + numberofperceptuallures);
        Log.i("numberofsemanticlures", "" + numberofsemanticlures);
        Log.i("numdistinctdistractors", "" + numberofdistinctdistractors);
        Log.i("Stimuli list", Util.iterableToString(stimulisequence));
        Log.i("Presentation list", Util.iterableToString(presentationstyle));
    }

    public static LevelManager getInstance() {
        return INSTANCE;
    }
}
