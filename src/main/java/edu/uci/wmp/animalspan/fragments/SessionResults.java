package edu.uci.wmp.animalspan.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;
import edu.uci.wmp.animalspan.fragments.questions.Questions;


public class SessionResults extends Fragment {

    private ImageView ivBackToMain;

    public SessionResults() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LevelManager.getInstance().testStarted = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_session_results, container, false);

        TextView tvResults = (TextView) view.findViewById(R.id.tvResuts);
        ivBackToMain = (ImageView) view.findViewById(R.id.ivBackToMain);

//        TextView tvLastLevelReached = (TextView) view.findViewById(R.id.tvLastLevelReached);
//        TextView tvFirstPartResponses = (TextView) view.findViewById(R.id.tvFirstPartResponses);
//        TextView tvSecondPartResponses = (TextView) view.findViewById(R.id.tvSecondPartResponses);
//        TextView tvFirstPartReactionTime = (TextView) view.findViewById(R.id.tvFirstPartReactionTime);
//        TextView tvFirstPartAccuracy = (TextView) view.findViewById(R.id.tvFirstPartAccuracy);
//
//        tvLastLevelReached.setText("" + LevelManager.getInstance().level);
//        tvFirstPartResponses.setText(StimuliManager.listToString(LevelManager.getInstance().responsesfirstpart));
//        tvSecondPartResponses.setText(StimuliManager.listToString(LevelManager.getInstance().secondpartsequence));
//        tvFirstPartReactionTime.setText(StimuliManager.listToString(LevelManager.getInstance().rtfirstpart));
//        tvFirstPartAccuracy.setText(StimuliManager.listToString(LevelManager.getInstance().accuracyfirstpart));

        String result = "Great job! You earned " + 5 + "coins!";
        tvResults.setText(result);

        ivBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fm = getActivity().getFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();
                if (LevelManager.getInstance().questions) {
                    Questions questions = new Questions();
                    ft.replace(R.id.fragment_container, questions);
                } else {
                    MainActivityFragment mainActivityFragment = new MainActivityFragment();
                    ft.replace(R.id.fragment_container, mainActivityFragment);
                }
                ft.commit();
            }
        });

        writeCSVFile();
        return view;
    }

    public void writeCSVFile() {
//        String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
//        String fileName = "AnalysisData.csv";
//        String filePath = baseDir + File.separator + fileName;
//        File f = new File(filePath );
//        CSVWriter writer;
//// File exist
//        if(f.exists() && !f.isDirectory()){
//            mFileWriter = new FileWriter(filePath , true);
//            writer = new CSVWriter(mFileWriter);
//        }
//        else {
//            writer = new CSVWriter(new FileWriter(filePath));
//        }
//        String[] data = {"Ship Name","Scientist Name", "...",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").formatter.format(date)});
//
//        writer.writeNext(data);
//
//        writer.close();
    }
}
