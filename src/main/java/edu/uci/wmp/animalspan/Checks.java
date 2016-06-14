package edu.uci.wmp.animalspan;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.uci.wmp.animalspan.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Checks {
    private static final Checks INSTANCE = new Checks();

    public final String LEVELFOLDER_PATH = "/wmplab/levels/";
    public final String STIMULIFOLDER_PATH = "/wmplab/stimuli/";

    private StringBuilder errorMessages = new StringBuilder();
    private Context context;

    public Checks() { }

    public class InvalidLevelFilesException extends IOException {}
    public class InvalidStimuliFilesException extends IOException {}


    public void setContext(Context context) { getInstance().context = context; }

    /**
     * Create and populate asset directories
     */
    public boolean runAllChecks() {
        try {
            Checks.getInstance().checkLevelsDirectory();
//            Checks.getInstance().checkStimuliDirectory();
            return true;
        }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error checking level files", Toast.LENGTH_SHORT).show();
            Toast.makeText(getInstance().context, errorMessages.toString(), Toast.LENGTH_SHORT).show();
            return false;
        }
//        catch (Checks.InvalidStimuliFilesException e) {
//            Toast.makeText(getInstance().context, "Error checking stimuli files", Toast.LENGTH_SHORT).show();
//            return false;
//        }
    }

    /**
     * Populate missing files, return true if successful
     */
    public boolean populateAssets() {
        try {
            populateLevelDirectory();
            populateStimuliDirectory();
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
            Log.e("checkLevelsDirectory()", "Level Folder doesn't exist");
            errorMessages.append("Level folder does not exist\nLevels check end\n");
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
                throw new InvalidLevelFilesException();
            }
        }
        errorMessages.append("END");
    }

    public void populateLevelDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);
        outLevelFolder.mkdirs();

        try { copyDirectory("levels/", outLevelFolderPath); }
        catch (IOException e) { throw new InvalidLevelFilesException(); }
    }

    public void populateStimuliDirectory() throws InvalidStimuliFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outStimuliFolderPath = root.getAbsolutePath() + STIMULIFOLDER_PATH;
        File outStimuliFolder = new File(outStimuliFolderPath);
        outStimuliFolder.mkdirs();

        try {
            String[] destinations = new String[] { StimuliManager.TARGET, StimuliManager.SEMANTIC, StimuliManager.PERCEPTUAL, StimuliManager.DISTRACTOR };
            for (String dest : destinations) {
                String newOutStimuliFolderPath = outStimuliFolderPath + dest;
                outStimuliFolder = new File(newOutStimuliFolderPath);
                outStimuliFolder.mkdir();
                copyDirectory("stimuli/" + dest, newOutStimuliFolderPath);
            }
        }
        catch (IOException e) { e.printStackTrace(); throw new InvalidStimuliFilesException(); }
    }

    private void copyDirectory(String assetFolderPath, String outFolderPath) throws IOException {
        Log.i("copying directory", assetFolderPath + " -> " + outFolderPath);
        String trimmedAssetFolderPath = assetFolderPath.substring(0, assetFolderPath.length() - 1);
        AssetManager am = context.getAssets();
        InputStream in;
        OutputStream out;

        for (String filename : am.list(trimmedAssetFolderPath)) {
            Log.i("reading asset", filename);
            in = am.open(assetFolderPath + filename);
            File outFile = new File(outFolderPath, filename);
            out = new FileOutputStream(outFile);

            copyFile(in, out);
            in.close();
            out.flush();
            out.close();
        }
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
