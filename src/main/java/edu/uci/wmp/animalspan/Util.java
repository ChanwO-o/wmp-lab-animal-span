package edu.uci.wmp.animalspan;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.view.View;

import com.uci.wmp.animalspan.R;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public final class Util {

    /**
     * Clear status & navigation bars from screen, keep them off with STICKY flag
     */
    public static void dimSystemBar(Activity activity) {
        final View window = activity.getWindow().getDecorView();
        setFlags(window);

        window.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() { // don't need to run thread; STICKY flag automatically keeps them cleared
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == View.SYSTEM_UI_FLAG_VISIBLE) {
                    window.postDelayed(new Runnable() {
                        public void run() {
                            setFlags(window);
                        }
                    }, 2000);
                }
            }
        });
    }

    private static void setFlags(View window) {
        window.setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    /**
     * Replace main fragment container with fragment given in parameter
     */
    public static void loadFragment(Activity activity, Fragment fragment) {
        FragmentManager fm = activity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_container, fragment);
        ft.commit();
    }

    public static <T> String iterableToString(Iterable<T> iterable) {
        StringBuilder result = new StringBuilder();
        for (T t : iterable)
            result.append(t).append(" ");
        return result.toString();
    }

    /**
     * Get current timestamp
     */
    public static String getTimestamp(final String TIMESTAMP_DATE, final String TIMESTAMP_TIME) {
        final String DATE = new SimpleDateFormat(TIMESTAMP_DATE, Locale.US).format(Calendar.getInstance().getTime());
        final String TIME = new SimpleDateFormat(TIMESTAMP_TIME, Locale.US).format(Calendar.getInstance().getTime());
        return DATE + " " + TIME;
    }

	/**
	 * Set activity background to match current theme, or reset to default background
	 */
	public static void setActivityBackground(Context context) {
		try {
			((Activity) context).findViewById(R.id.fragment_container).setBackground(StimuliManager.getInstance().getBackground());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
