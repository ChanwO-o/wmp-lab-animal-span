package edu.uci.wmp.animalspan.fragments;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.SystemClock;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import edu.uci.wmp.animalspan.CSVWriter;
import edu.uci.wmp.animalspan.LevelManager;
import com.uci.wmp.animalspan.R;
import edu.uci.wmp.animalspan.Util;
import edu.uci.wmp.animalspan.fragments.questions.EffortQuestion;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements View.OnTouchListener {

    TextView tvAnimalSpan;
    ImageView ivStart;
    ImageView ivDemo;
    MyRect topLeft, topRight, bottomLeft, bottomRight;
    MyRect[] rects;
    final static int NON_ASSIGNED_ID = 99;
    final int RECT_WIDTH = 100;
    final int RECT_HEIGHT = 100;
    final int POINTER_DOWN = 0;
    final int POINTER_UP = 1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // invisible settings buttons
        topLeft = new MyRect(0, 0, RECT_WIDTH, RECT_HEIGHT);
        topRight = new MyRect(LevelManager.getInstance().screen_width - RECT_WIDTH, 0, LevelManager.getInstance().screen_width, RECT_HEIGHT);
        bottomLeft = new MyRect(0, LevelManager.getInstance().screen_height - RECT_HEIGHT, RECT_WIDTH, LevelManager.getInstance().screen_height + RECT_HEIGHT);
        bottomRight = new MyRect(LevelManager.getInstance().screen_width - RECT_WIDTH, LevelManager.getInstance().screen_height - RECT_HEIGHT, LevelManager.getInstance().screen_width, LevelManager.getInstance().screen_height + RECT_HEIGHT);
        rects = new MyRect[] {topLeft, topRight, bottomLeft, bottomRight};

//        for (MyRect r : rects)
//            r.logDimensions();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        view.setOnTouchListener(this);

        tvAnimalSpan = (TextView) view.findViewById(R.id.tvAnimalSpan);
        ivStart = (ImageView) view.findViewById(R.id.ivStart);
        ivDemo = (ImageView) view.findViewById(R.id.ivDemo);

        // set text font
        Typeface tf = Typeface.createFromAsset(getActivity().getAssets(), "fonts/azoft-sans.ttf");
        tvAnimalSpan.setTypeface(tf);

        ivStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if user played demo mode, trainingmode stays as demo. Change it to level mode
                if (LevelManager.getInstance().trainingmode.equals(LevelManager.TRAININGMODE_DEMO))
                    LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_LEVELS;
                LevelManager.getInstance().sessionStartMills = SystemClock.uptimeMillis(); // record session starting time (used for trainingmode = "time")
                LevelManager.getInstance().trial = 0; // @TODO: move this to LevelManager.startSession()
                CSVWriter.getInstance().createCsvFile();
                Util.loadFragment(getActivity(), new GetReady());
            }
        });

        ivDemo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LevelManager.getInstance().trainingmode = LevelManager.TRAININGMODE_DEMO; // start demo mode with no limits to time or levels
                LevelManager.getInstance().sessionStartMills = SystemClock.uptimeMillis();
                LevelManager.getInstance().trial = 0; // @TODO: move this to LevelManager.startSession()
                CSVWriter.getInstance().createCsvFile();
                Util.loadFragment(getActivity(), new GetReady());
            }
        });
        return view;
    }

    /**
     * Touch events
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int actionIndex = event.getActionIndex(); // get action index from the event object
        int pointerId = event.getPointerId(actionIndex); // get pointer ID
        int pointerIndex = event.findPointerIndex(pointerId); // get pointer index
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

//        Log.d("pointerId", pointerId + "");
//        Log.i("pointer coords", x + " " + y);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                checkTouchRect(x, y, POINTER_DOWN);
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                checkTouchRect(x, y, POINTER_DOWN);
                break;

            case MotionEvent.ACTION_UP:
                checkTouchRect(x, y, POINTER_UP);
                break;

            case MotionEvent.ACTION_POINTER_UP:
                checkTouchRect(x, y, POINTER_UP);
                break;
        }
        return true;
    }

    /**
     * Returns the rectangle that contains the coords, else return null
     */
    public MyRect inRect(float x, float y) {
        for (MyRect r : rects)
            if (r.contains(x, y))
                return r;
        return null;
    }

    public void checkTouchRect(float x, float y, int pointerDirection) {
        MyRect r = inRect(x, y);
        if (r != null) {
            if (pointerDirection == POINTER_DOWN) {
                r.selected = true;
//                Log.w("touched", "down");
            }
            else if (pointerDirection == POINTER_UP) {
                r.selected = false;
//                Log.w("touched", "up");
            }
            checkAllRects();
        }
    }

    /**
     * If all rectangles are selected, call openSettings()
     */
    public void checkAllRects() {
        for (MyRect r : rects)
            if (!r.selected)
                return;
        openSettings();
    }
    public void openSettings() {
        Util.loadFragment(getActivity(), new Settings());
    }

    @SuppressLint("ParcelCreator")
    private class MyRect extends RectF {

        int pointerId;
        boolean selected;

        public MyRect(float left, float top, float right, float bottom) {
            super(left, top, right, bottom);
            pointerId = NON_ASSIGNED_ID; // default non-assigned id
            selected = false;
        }

        public void logDimensions() {
            Log.i("Dimensions", "l: " + left + " t: " + top + " r: " + right + " b: " + bottom);
        }
    }
}
