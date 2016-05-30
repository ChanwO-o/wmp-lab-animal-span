package edu.uci.wmp.animalspan.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.questions.EffortQuestion;
import edu.uci.wmp.animalspan.fragments.questions.ReflectionQuestion;


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

        int score = LevelManager.getInstance().recalledImages * LevelManager.getInstance().level;
        String result = "Great job!\nYou earned " + score + " points!";
        tvResults.setText(result);

        ivBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (LevelManager.getInstance().questions)
//                    Util.loadFragment(getActivity(), new ReflectionQuestion());
//                else
//                    Util.loadFragment(getActivity(), new MainActivityFragment());
                Util.loadFragment(getActivity(), new MainActivityFragment());
            }
        });
        return view;
    }
}
