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
            LevelManager.getInstance().reset();
            LevelManager.getInstance().loadLevel(LevelManager.getInstance().level);
            LevelManager.getInstance().logVariables();
            LevelManager.getInstance().testStarted = true; // starting test; no back button allowed if abortallowed == true
        }
        else
            Log.e("Stage 1 lm", "level manager is null!");
        readyStartTime = SystemClock.uptimeMillis();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_get_ready, container, false);
        TextView tvGetReady = (TextView) view.findViewById(R.id.tvGetReady);
        ivReadyNext = (ImageView) view.findViewById(R.id.ivReadyGo);

        String readyPrompt = String.format("Get ready for level %d!", LevelManager.getInstance().level);
        tvGetReady.setText(readyPrompt);

        ivReadyNext.setVisibility(View.GONE); // hide button for 1 second
        ivReadyNext.setOnClickListener(this);

        handler.postDelayed(showButton, 0);

        return view;
    }

    @Override
    public void onClick(View v) {
        FragmentManager fm = getActivity().getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Stage1 stage1 = new Stage1();
        ft.replace(R.id.fragment_container, stage1);
        ft.commit();
    }
}
