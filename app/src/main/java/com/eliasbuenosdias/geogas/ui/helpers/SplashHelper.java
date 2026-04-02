package com.eliasbuenosdias.geogas.ui.helpers;

import android.animation.Animator;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eliasbuenosdias.geogas.R;

/**
 * Gestiona la visualización y animación de la pantalla de carga.
 */
public class SplashHelper {

    private final View splashScreen;
    private final ProgressBar progressBar;
    private final TextView progressText;
    private final TextView loadingText;
    private final View postSplashControl;

    public SplashHelper(View root, View postSplashControl) {
        this.splashScreen = root.findViewById(R.id.splashScreen);
        this.progressBar = root.findViewById(R.id.splashProgressBar);
        this.progressText = root.findViewById(R.id.splashProgressText);
        this.loadingText = root.findViewById(R.id.splashLoadingText);
        this.postSplashControl = postSplashControl;
    }

    public void updateStatus(int progress, String message) {
        if (progressBar != null)
            progressBar.setProgress(progress);
        if (progressText != null)
            progressText.setText(progress + "%");
        if (loadingText != null)
            loadingText.setText(message);
    }

    public void hide() {
        if (splashScreen == null || splashScreen.getVisibility() == View.GONE)
            return;

        splashScreen.animate().alpha(0f).setDuration(600).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator a) {
                splashScreen.setVisibility(View.GONE);
                if (postSplashControl != null)
                    postSplashControl.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationStart(Animator a) {
            }

            @Override
            public void onAnimationCancel(Animator a) {
            }

            @Override
            public void onAnimationRepeat(Animator a) {
            }
        }).start();
    }

    /**
     * Instantly hides the splash screen without animation.
     * Used when the Activity is recreated due to a locale change.
     */
    public void forceHide() {
        if (splashScreen != null) {
            splashScreen.setVisibility(View.GONE);
        }
        if (postSplashControl != null) {
            postSplashControl.setVisibility(View.VISIBLE);
        }
    }
}
