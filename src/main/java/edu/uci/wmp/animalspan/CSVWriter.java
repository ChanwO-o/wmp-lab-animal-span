package edu.uci.wmp.animalspan;

import android.content.Context;
import android.util.Log;

import com.uci.wmp.animalspan.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class CSVWriter {
    public static final CSVWriter INSTANCE = new CSVWriter();

    private static final String FOLDERPATH = "/wmplab/csvdata";
    private static final String COMMA = ", ";
    private static final String NEW_LINE = "\n ";
    private static final String NULL = "na ";
    private static final String LAST_FIELD = "randomlydistribute";
    private static final String FORMAT_DATE = "yyyy_MM_dd";
    private static final String FORMAT_TIME = "HHmmss";
    private static final String TIMESTAMP_DATE = "MM/dd/yyyy";
    private static final String TIMESTAMP_TIME = "HH:mm:ss";

    private StringBuilder questionResponses = new StringBuilder();

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
        StringBuilder result = new StringBuilder();
        InputStream inputStream = context.getResources().openRawResource(R.raw.csvfields);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                String field = line.trim();
                result.append(field);
                if (!field.equals(LAST_FIELD)) // last field must not have a comma
                    result.append(COMMA);
            }
            result.append(NEW_LINE); // new line
        } catch (IOException e) {
            Log.e("getFields()", "error reading fields");
            e.printStackTrace();
        }
        return result.toString();
    }

    public void collectData() {
        int curInd = LevelManager.getInstance().currentStimuliIndex;
        StringBuilder data = new StringBuilder();

        data.append("CST").append(COMMA);                                                                               // experiment
        data.append(LevelManager.getInstance().subject).append(COMMA);
        data.append(LevelManager.getInstance().session).append(COMMA);
        data.append(LevelManager.getInstance().level).append(COMMA);
        data.append(LevelManager.getInstance().trial).append(COMMA);
        data.append(LevelManager.getInstance().part).append(COMMA);
        data.append(StimuliManager.getImagePath(LevelManager.getInstance().stimulisequence.get(curInd))).append(COMMA); // stimulus
        data.append(LevelManager.getInstance().stimulisequence.get(curInd) / 100).append(COMMA);                        // stimulus class
        data.append(LevelManager.getInstance().presentationstyle.get(curInd)).append(COMMA);                            // presentation style

        if (LevelManager.getInstance().part == LevelManager.STAGE1)                                                     // 1. overall acc
            data.append(LevelManager.getInstance().accuracyfirstpart.get(curInd)).append(COMMA); // accuracy stage1
        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
            data.append(LevelManager.getInstance().accuracysecondpart.get(curInd)).append(COMMA); // accuracy stage2

        if (LevelManager.getInstance().part == LevelManager.STAGE1)                                                     // 2. stage1 & stage2 individual acc (fills two columns)
            data.append(LevelManager.getInstance().accuracyfirstpart.get(curInd)).append(COMMA).append(NULL).append(COMMA);
        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
            data.append(NULL).append(COMMA).append(LevelManager.getInstance().accuracysecondpart.get(curInd)).append(COMMA);

        double seconds = 0;                                                                                             // reaction time (rt)
        if (LevelManager.getInstance().part == LevelManager.STAGE1)
            seconds = (double) (LevelManager.getInstance().rtfirstpart.get(curInd)) / 1000;
        else if (LevelManager.getInstance().part == LevelManager.STAGE2)
            seconds = (double) (LevelManager.getInstance().rtsecondpart.get(curInd)) / 1000;
        data.append(seconds).append("s ").append(COMMA);

        data.append(Util.getTimestamp(TIMESTAMP_DATE, TIMESTAMP_TIME)).append(COMMA);                                   // timestamp

        data.append("[").append(LevelManager.getInstance().screen_width).append("; ")
                .append(LevelManager.getInstance().screen_height).append("]").append(COMMA);                            // taskresolution

        data.append(LevelManager.getInstance().trainingmode).append(COMMA);

        if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_ROUNDS))                           // sessionlength & numberofrounds
            data.append(NULL).append(COMMA).append(LevelManager.getInstance().numberoftrials).append(COMMA);
        else if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_TIME))
            data.append(LevelManager.getInstance().sessionLength).append(COMMA).append(NULL).append(COMMA);
        

        data.append(LevelManager.getInstance().setsize).append(COMMA);             // not sure
        data.append(LevelManager.getInstance().distractorsize).append(COMMA);
        data.append(LevelManager.getInstance().numberofdistincttargets).append(COMMA);
        data.append(LevelManager.getInstance().numberofperceptuallures).append(COMMA);
        data.append(LevelManager.getInstance().numberofsemanticlures).append(COMMA);
        data.append(LevelManager.getInstance().numberofdistinctdistractors).append(COMMA);
        data.append(LevelManager.getInstance().keeptargetconstant).append(COMMA);
        data.append(LevelManager.getInstance().randomlypickstimuli).append(COMMA);
        data.append(LevelManager.getInstance().randomstimuliineverytrial).append(COMMA);
        data.append(LevelManager.getInstance().sizeoffirststimuli).append(COMMA);
        data.append(LevelManager.getInstance().timetoanswerfirstpart).append(COMMA);
        data.append(LevelManager.getInstance().showbuttonpressfeedback).append(COMMA);
        data.append(LevelManager.getInstance().topborder).append(COMMA);
        data.append(LevelManager.getInstance().bottomborder).append(COMMA);
        data.append(LevelManager.getInstance().sideborder).append(COMMA);
        data.append(LevelManager.getInstance().gapbetweenimages).append(COMMA);
        data.append(LevelManager.getInstance().stimulusside).append(COMMA);
        data.append(LevelManager.getInstance().feedbacktopborder).append(COMMA);
        data.append(LevelManager.getInstance().feedbackbottomborder).append(COMMA);
        data.append(LevelManager.getInstance().feedbacksideborder).append(COMMA);
        data.append(LevelManager.getInstance().feedbackstimulussize).append(COMMA);
        data.append(LevelManager.getInstance().feedbackgapbetweenimages).append(COMMA);
        data.append(LevelManager.getInstance().stimuliperline).append(COMMA);
        data.append(LevelManager.getInstance().showchoicefeedback).append(COMMA);
        data.append(LevelManager.getInstance().randomlydistribute).append(COMMA);

        data.append(NEW_LINE);
        writeLine(data.toString());
    }

    /**
     * Collect data for each question: exp subject session nameofquestion response timestamp
     */
    public void collectQuestionResponse(String theQuestion, int resp) {
        questionResponses
                .append("CST").append(COMMA)
                .append(LevelManager.getInstance().subject).append(COMMA)
                .append(LevelManager.getInstance().session).append(COMMA)
                .append(theQuestion).append(COMMA)
                .append(resp).append(COMMA)
                .append(Util.getTimestamp(TIMESTAMP_DATE, TIMESTAMP_TIME)).append(COMMA).append(NEW_LINE);
    }

    /**
     * Writes data to data file
     */
    public void writeQuestionResponse() {
        writeLine(questionResponses.toString());
        questionResponses.setLength(0); // clear StringBuilder
    }

    public static CSVWriter getInstance() {
        return INSTANCE;
    }
}
