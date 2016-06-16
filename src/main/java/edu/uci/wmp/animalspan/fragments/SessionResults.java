package edu.uci.wmp.animalspan.fragments;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.questions.EffortQuestion;
import edu.uci.wmp.animalspan.fragments.questions.ReflectionQuestion;


public class SessionResults extends Fragment {

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
        ImageView ivBackToMain = (ImageView) view.findViewById(R.id.ivBackToMain);

        LevelManager.getInstance().saveLevelToFile(); // save progress
        int score = LevelManager.getInstance().recalledImages * LevelManager.getInstance().level;
        Toast.makeText(getActivity(), LevelManager.getInstance().recalledImages + " x " + LevelManager.getInstance().level, Toast.LENGTH_SHORT).show();
        String result = "You earned " + score + " points!";
        tvResults.setText(result);

        ivBackToMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Util.loadFragment(getActivity(), new MainActivityFragment());
            }
        });
        return view;
    }
}
