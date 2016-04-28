package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
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

/**
 * Created by ChanWoo on 11/18/2015
 */
public class LevelManager implements Serializable {

    private static final LevelManager INSTANCE = new LevelManager();

    public static final int MIN_LEVEL = 1;                  // lowest level available
    public static final int MAX_LEVEL = 30;                 // highest level available
    public static final int STAGE1 = 1;                     // stage 1
    public static final int STAGE2 = 2;                     // stage 2
    public static final int STAGE0 = 0;                     // neither
    public static final String TRAININGMODE_ROUNDS = "rounds";
    public static final String TRAININGMODE_TIME = "time";
    public static final String TRAININGMODE_DEMO = "demo";
    public static final String SAVE_LEVEL_FILENAME = "save_level.txt";

    private Context context;
    private Random random;

    // initialize experiment wide variables (do not change this)
    public int subject = 1;
    public int session = 1;
    public int level = 1;
    public int trial = 0;                                   // == rounds
    public int part = STAGE0;                               // 1 = Stage1, 2 = Stage2, 0 = neither
    public int currentStimuliIndex = 0;                     // index of current pic
    public boolean testStarted = false;
    public long sessionStartMills = 0;                      // timer starting at beginning of session, used when mode = "time"
    public boolean questions = true;

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
//    public boolean showinfullscreen = true;        // define whether you want to show this fullscreen or not
//    public boolean abortallowed = false;           // define whether pressing escape or q aborts the program

    // training parameters (this does NOT go into level file)
    public String trainingmode = LevelManager.TRAININGMODE_ROUNDS;        // time: ends session after a certain amount of time; rounds: ends session after a certain amount of rounds
    public int sessionLength = 300;               // This is the length of the session in seconds (default: 300s)
    public int numberoftrials = 10;               // define how long a training session takes in number of levels
    public int startlevel = 1;                    // define with which level to start_old  *** changed var name level -> startlevel ***

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
    public boolean showchoicefeedback = true;   // enable/disable feedback images on the bottom of the screen in the second partff
    public boolean randomlydistribute = true;   // randomly distribute choice stimuli in every trial
    // -------------------------------------------------------------------------------------------

    public LevelManager() {
        random = new Random();
        level = startlevel;
        trial = 0;
        part = STAGE0;
        reset();
    }

    public LevelManager(Context context) {
        setContext(context);
        setScreenDimensions();
        random = new Random();
        level = startlevel;
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
        Log.i("Width", "" + screen_width);
        Log.i("Height", "" + screen_height);
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
    }

    /**
     * Setup LevelManager for new session (initialize lists, variables, etc.)
     */
    public void startSession() {
        // @TODO: implement so that only this method needs to be called at the beginning of a session, and clean up other LevelManager crap from other classes
    }

    public void startTrial() {
        // @TODO: implement so that only this method needs to be called at the beginning of a session, and clean up other LevelManager crap from other classes
    }

    public void loadLevel(int level) {
        try {
            setLevel(level);
            BufferedReader br = openFileAsReader();

            String line;
            while ((line = br.readLine()) != null) {
                Log.d("loadLevel()", line);
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
            BufferedReader reader = new BufferedReader(new FileReader(SAVE_LEVEL_FILENAME));
            String savedLevel = reader.readLine();
            Log.d("saved level loaded", savedLevel);
            getInstance().level = Integer.valueOf(savedLevel);
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("no save file found", "setting to startlevel");
            getInstance().level = startlevel;
        }

    }

    public void saveLevelToFile() {
        try {
            String filePath = context.getFilesDir().getPath() + SAVE_LEVEL_FILENAME;
            File file = new File(filePath);
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(Integer.toString(getInstance().level));
            writer.newLine();
            writer.close();
            Log.d("saveLevelToFile()", "saved at level " + getInstance().level);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setContext(Context context) {
        getInstance().context = context;
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

    public String getFilePath() {
        return "levels/level" + level + ".txt";
    }

    public BufferedReader openFileAsReader() throws IOException {
        AssetManager am = context.getAssets();
        InputStream in = am.open(getFilePath());
        return new BufferedReader(new InputStreamReader(in));
    }

    /**
     * Set value of variable through reflection
     * @param varName name of variable to modify
     * @throws NoSuchFieldException
     */
    public void setLevelVariable(String varName, String newValue) throws NoSuchFieldException, IllegalAccessException {

        // TODO: Ask Martin details on whether allowfullscreen and abortallowed will be written in future level files
        if (varName.equals("showinfullscreen") || varName.equals("abortallowed")) {
            Log.wtf("setLevelVariable()", "not using variable in LM");
            return;
        }

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
            int randomIndex = random.nextInt(temp.size());
            int labeledDistinctTarget = temp.get(randomIndex) + StimuliManager.TARGET_LABEL; // add label 100
            distincttargets.add(labeledDistinctTarget);
            temp.remove(randomIndex); // remove to prevent duplicates
        }
        Log.d("distinct targets", StimuliManager.listToString(distincttargets));

        // choosing sequence from distinct targets
        for (int i = 0; i < setsize; i++) {
            int generatedStimuli;
            do {
                int randomIndex = random.nextInt(distincttargets.size());
                generatedStimuli = distincttargets.get(randomIndex);
                Log.d("chosen stimuli", "random: " + randomIndex + " genStim: " + generatedStimuli);
            } while (i != 0 && stimulisequence.get(i - 1) == generatedStimuli); // no two same stimuli in a row @TODO: remove this line & fix; useless since I'm shuffling anyways.

            stimulisequence.add(generatedStimuli);
            Log.d("added", "" + generatedStimuli);
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
        Log.d("distinct distractors", StimuliManager.listToString(distinctdistractors));

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
     * Add only targets to correctstimulisequence by checking for TARGET_LABEL
     */
    public void fillCorrectStimuliSequence() {
        for (Integer stimuli : stimulisequence) {
            if ((stimuli / 100) * 100 == StimuliManager.TARGET_LABEL) // (e.g. 106 >> 1 >> 100)
                correctstimulisequence.add(stimuli);
        }
    }

    public void generateOrientationSequence() {
        for (int i = 0; i < setsize + distractorsize; i++) {
            presentationstyle.add(random.nextInt(2)); // 0 or 1
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
        Log.i("Stimuli list", StimuliManager.listToString(stimulisequence));
        Log.i("Presentation list", StimuliManager.listToString(presentationstyle));
    }

    public static LevelManager getInstance() {
        return INSTANCE;
    }
}
