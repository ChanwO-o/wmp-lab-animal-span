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
import java.util.Arrays;

public class Checks {
    private static final Checks INSTANCE = new Checks();

    public final String LEVELFOLDER_PATH = "/wmplab/levels/";
    public final String STIMULIFOLDER_PATH = "/wmplab/stimuli/";

    private Context context;

    public Checks() { }

    public class InvalidLevelFilesException extends IOException {}
    public class InvalidStimuliFilesException extends IOException {}


    public void setContext(Context context) { getInstance().context = context; }

    /**
     * Create and populate asset directories
     */
    public void runAllChecks() {
        try {
            Checks.getInstance().checkLevelsDirectory();
            Checks.getInstance().checkStimuliDirectory();
        }
        catch (Checks.InvalidLevelFilesException e) {
            Toast.makeText(getInstance().context, "Error checking level files", Toast.LENGTH_SHORT).show();
        }
        catch (Checks.InvalidStimuliFilesException e) {
            Toast.makeText(getInstance().context, "Error checking stimuli files", Toast.LENGTH_SHORT).show();
        }
    }

    public void checkLevelsDirectory() throws InvalidLevelFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outLevelFolderPath = root.getAbsolutePath() + LEVELFOLDER_PATH;
        File outLevelFolder = new File(outLevelFolderPath);
//        outLevelFolder.mkdirs();
        if (!outLevelFolder.mkdirs())
            return; // doesn't check if folder is populated yet

        try { copyDirectory("levels/", outLevelFolderPath); }
        catch (IOException e) { throw new InvalidLevelFilesException(); }
    }

    public void checkStimuliDirectory() throws InvalidStimuliFilesException {
        File root = android.os.Environment.getExternalStorageDirectory();
        String outStimuliFolderPath = root.getAbsolutePath() + STIMULIFOLDER_PATH;
        File outStimuliFolder = new File(outStimuliFolderPath);
//        outStimuliFolder.mkdirs();
        if (!outStimuliFolder.mkdirs())
            return; // doesn't check if folder is populated yet

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
