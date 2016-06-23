package edu.uci.wmp.animalspan.fragments;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.animalspan.LevelManager;
import edu.uci.wmp.animalspan.Util;

import com.uci.wmp.animalspan.R;

public class GetReady extends Fragment implements View.OnClickListener {

    ImageView ivReadyNext;
    final int READY_SHOW_BUTTON_TIME = 1;     // when to show next button in seconds
    long readyStartTime;

    private Handler handler = new Handler();

    private Runnable showButton = new Runnable() {
        @Override
        public void run() {
            long timeInMills = SystemClock.uptimeMillis() - readyStartTime;
            int seconds = (int) timeInMills / 1000;

            if (seconds >= READY_SHOW_BUTTON_TIME) {
                ivReadyNext.setVisibility(View.VISIBLE);
            } else {
                handler.postDelayed(this, 0); // loop until button is visible
            }
        }
    };

    public GetReady() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (LevelManager.getInstance() != null) {
            LevelManager.getInstance().startRound();
            LevelManager.getInstance().logVariables();
        }
        else
            Log.e("Stage 1 lm", "level manager is null!");
        readyStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_ready, container, false);
        TextView tvGetReady = (TextView) view.findViewById(R.id.tvGetReady);
        TextView tvTrialsLeft = (TextView) view.findViewById(R.id.tvTrialsLeft);
        ivReadyNext = (ImageView) view.findViewById(R.id.ivReadyGo);

        String readyPrompt = String.format("Get ready for level %d!", LevelManager.getInstance().level);
        tvGetReady.setText(readyPrompt);

        ivReadyNext.setVisibility(View.GONE); // hide button for 1 second
        ivReadyNext.setOnClickListener(this);

        LevelManager.getInstance().trial++; // one trial completed

        Log.wtf("numberoftrials", "" + LevelManager.getInstance().numberoftrials);
        Log.wtf("current trial", "" + LevelManager.getInstance().trial);

        if (LevelManager.getInstance().debug) {
            String trials = "numberoftrials: " + LevelManager.getInstance().numberoftrials +
                    "\ncurrent trial: " + LevelManager.getInstance().trial +
                    "\nTrials left: " + (LevelManager.getInstance().numberoftrials - LevelManager.getInstance().trial);
            tvTrialsLeft.setText(trials);
        }

        handler.postDelayed(showButton, 0);

        return view;
    }

    @Override
    public void onClick(View v) {
        Util.loadFragment(getActivity(), new Stage1());
    }
}
