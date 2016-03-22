package edu.uci.wmp.animalspan;

import android.content.Context;
import android.util.Log;

import com.uci.wmp.animalspan.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class CSVWriter {
    public static final CSVWriter INSTANCE = new CSVWriter();

    private static final String FOLDERPATH = "/wmplab/csvdata";
    private static final String COMMA = ", ";
    private static final String NEW_LINE = "\n ";
    private static final String LAST_FIELD = "randomlydistribute";
    private static final String FORMAT_DATE = "yyyy_MM_dd";
    private static final String FORMAT_TIME = "HHmmss";

    private Context context;
    private File csvFile;

    public CSVWriter() {

    }

    public void setContext(Context context) {
        getInstance().context = context;
    }

    /**
     * Makes directory wmplab/csvdata/
     * Generates filename (current date & time)
     * Writes first line of fields
     */
    public void createCsvFile() {
//        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS); // use to save in other directories
        File root = android.os.Environment.getExternalStorageDirectory();
        File csvFolder = new File (root.getAbsolutePath() + FOLDERPATH);
        if (!csvFolder.exists())
            Log.d("csvFolder", "folder created: " + csvFolder.mkdirs());

        final String DATE = new SimpleDateFormat(FORMAT_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(FORMAT_TIME, Locale.US).format(Calendar.getInstance().getTime());
        final String filename = LevelManager.getInstance().subject + "_" +
                LevelManager.getInstance().session + "_" +
                DATE + "_" + TIME + "_" + ".csv";

        csvFile = new File(csvFolder, filename);
        writeLine(getFields());
    }

    /**
     * Writes single line to the csv file
     */
    private void writeLine(String line) {
        FileWriter fw;
        BufferedWriter out = null;
//        FileOutputStream os = null;
        try {
            Log.d("path", csvFile.getAbsolutePath());
            fw = new FileWriter(csvFile, true);
            out = new BufferedWriter(fw);
            out.write(line); // write line
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }

    /**
     * Reads fields from csvfields.txt, concatenate and return as one string
     */
    private String getFields() {
        String result = "";
        InputStream inputStream = context.getResources().openRawResource(R.raw.csvfields);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String field = line.trim();
                result += field;
                if (!field.equals(LAST_FIELD)) // last field must not have a comma
                    result += COMMA;
            }
            result += NEW_LINE; // new line
        } catch (IOException e) {
            Log.e("getFields()", "error reading fields");
            e.printStackTrace();
        }
        return result;
    }

    public void collectData() {
        int curInd = LevelManager.getInstance().currentStimuliIndex;
        String data = "";

        data += "CST" + COMMA; // experiment
        data += LevelManager.getInstance().subject + COMMA;
        data += LevelManager.getInstance().session + COMMA;
        data += LevelManager.getInstance().level + COMMA;
        data += LevelManager.getInstance().trial + COMMA;
        data += LevelManager.getInstance().part + COMMA;

        data += StimuliManager.getImagePath(LevelManager.getInstance().stimulisequence.get(curInd)) + COMMA; // stimulus
        data += LevelManager.getInstance().stimulisequence.get(curInd) / 100 + COMMA; // stimulus class
        data += LevelManager.getInstance().presentationstyle.get(curInd) + COMMA; // presentation style
        data += LevelManager.getInstance().responsesfirstpart.get(curInd) + COMMA; // accuracy
        data += 0 + COMMA; // reaction time (rt)
        // timestamp
        final String DATE = new SimpleDateFormat(FORMAT_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(FORMAT_TIME, Locale.US).format(Calendar.getInstance().getTime());
        final String TIMESTAMP = LevelManager.getInstance().subject + "_" +
                LevelManager.getInstance().session + "_" +
                DATE + "_" + TIME + "_" + ".csv";
        data += TIMESTAMP + COMMA;

        // taskresolution
        data += "[" + LevelManager.getInstance().screen_width + "; " + LevelManager.getInstance().screen_height + "]" + COMMA;
        data += LevelManager.getInstance().showinfullscreen + COMMA;
        data += LevelManager.getInstance().abortallowed + COMMA;
        data += LevelManager.getInstance().trainingmode + COMMA;
        data += LevelManager.getInstance().sessionLength + COMMA;
        data += LevelManager.getInstance().numberoftrials + COMMA;
        data += LevelManager.getInstance().setsize + COMMA;             // not sure
        data += LevelManager.getInstance().distractorsize + COMMA;
        data += LevelManager.getInstance().numberofdistincttargets + COMMA;
        data += LevelManager.getInstance().numberofperceptuallures + COMMA;
        data += LevelManager.getInstance().numberofsemanticlures + COMMA;
        data += LevelManager.getInstance().numberofdistinctdistractors + COMMA;
        data += LevelManager.getInstance().keeptargetconstant + COMMA;
        data += LevelManager.getInstance().randomlypickstimuli + COMMA;
        data += LevelManager.getInstance().randomstimuliineverytrial + COMMA;
        data += LevelManager.getInstance().sizeoffirststimuli + COMMA;
        data += LevelManager.getInstance().timetoanswerfirstpart + COMMA;
        data += LevelManager.getInstance().showbuttonpressfeedback + COMMA;
        data += LevelManager.getInstance().topborder + COMMA;
        data += LevelManager.getInstance().bottomborder + COMMA;
        data += LevelManager.getInstance().sideborder + COMMA;
        data += LevelManager.getInstance().gapbetweenimages + COMMA;
        data += LevelManager.getInstance().stimulusside + COMMA;
        data += LevelManager.getInstance().feedbacktopborder + COMMA;
        data += LevelManager.getInstance().feedbackbottomborder + COMMA;
        data += LevelManager.getInstance().feedbacksideborder + COMMA;
        data += LevelManager.getInstance().feedbackstimulussize + COMMA;
        data += LevelManager.getInstance().feedbackgapbetweenimages + COMMA;
        data += LevelManager.getInstance().stimuliperline + COMMA;
        data += LevelManager.getInstance().showchoicefeedback + COMMA;
        data += LevelManager.getInstance().randomlydistribute + COMMA;

        data += NEW_LINE;
        writeLine(data);
    }

//    private static void experiment(FileOutputStream os) throws IOException {
//        os.write(("CST" + COMMA).getBytes());
//    }
//
//    private static void subject(FileOutputStream os) throws IOException {
//        os.write((LevelManager.getInstance().subject + COMMA).getBytes());
//    }
//
//    private static void session(FileOutputStream os) throws IOException {
//        os.write((LevelManager.getInstance().session + COMMA).getBytes());
//    }
//
//    private static void level(FileOutputStream os) throws IOException {
//        os.write((LevelManager.getInstance().level + COMMA).getBytes());
//    }
//
//    private static void trialnumber(FileOutputStream os) throws IOException {
//        os.write((LevelManager.getInstance().trial + COMMA).getBytes());
//    }


    public static CSVWriter getInstance() {
        return INSTANCE;
    }
}
