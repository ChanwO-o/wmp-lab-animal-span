package edu.uci.wmp.animalspan.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.uci.wmp.animalspan.R;

import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.Util;


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

        if (!LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
            LevelManager.getInstance().saveLevelToFile(true); // save progress
        String result = "You earned " + LevelManager.getInstance().points + " points!";
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
