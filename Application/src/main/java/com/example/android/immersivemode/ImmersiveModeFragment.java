/*
* Copyright (C) 2012 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.example.android.immersivemode;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.example.android.common.logger.Log;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;

public class ImmersiveModeFragment extends Fragment {

    public static final String TAG = "ImmersiveModeFragment";
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private final Handler hideControlsHandler = new Handler();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final View decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        int height = decorView.getHeight();
                        Log.i(TAG, "Current height: " + height);
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.sample_action) {
            toggleImmersiveMode((AppCompatActivity)getActivity());
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        View decorView = getActivity().getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener
                (onSystemUiVisibilityChangeListener);

        if (getOrientation() == ORIENTATION_LANDSCAPE) {
            scheduleControlsHide();
        }
    }

    protected int getOrientation() {
        return getResources().getConfiguration().orientation;
    }

    private final Runnable hideControlsRunnable = new Runnable() {
        @Override
        public void run() {
            final AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity != null) {
                toggleImmersiveMode(activity);
            }
        }
    };

    private void scheduleControlsHide() {
            hideControlsHandler.removeCallbacks(hideControlsRunnable);
            hideControlsHandler.postDelayed(hideControlsRunnable, AUTO_HIDE_DELAY_MILLIS);
    }

    /**
     * Detects and toggles immersive mode (also known as "hidey bar" mode).
     */
    @VisibleForTesting
    public void toggleImmersiveMode(@NonNull AppCompatActivity activity) {
        // The UI options currently enabled are represented by a bitfield returned by getSystemUiVisibility().
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            newUiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            newUiOptions |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            newUiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
    }

    View.OnSystemUiVisibilityChangeListener onSystemUiVisibilityChangeListener = new View.OnSystemUiVisibilityChangeListener() {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            AppCompatActivity activity = (AppCompatActivity)getActivity();
            android.support.v7.app.ActionBar actionBar = activity.getSupportActionBar();
            // Note that system bars will only be "visible" if none of the
            // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                // The system bars are visible.
                actionBar.show();
                scheduleControlsHide();

            } else {
                // The system bars are NOT visible.
                actionBar.hide();
            }
            int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
            Log.d(TAG, "uiOptions = " + Integer.toBinaryString(uiOptions));

        }
    };

}
