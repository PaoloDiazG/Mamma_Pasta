package com.mammapasta.guardian;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.mammapasta.closed.ClosedHoursActivity;
import com.mammapasta.closed.TimeUtils;

public class ClosedHoursWatcher implements Application.ActivityLifecycleCallbacks {

    private final Application app;
    private final Handler handler = new Handler();
    private boolean alreadyWarned = false;

    public ClosedHoursWatcher(Application app) {
        this.app = app;
        app.registerActivityLifecycleCallbacks(this);
    }

    private void checkAndRedirect(Activity activity) {
        if (activity instanceof ClosedHoursActivity) return;

        // Verifica si está cerca de la hora de cierre (por ejemplo, 21:59 o 9:59 PM)
        int currentHour = TimeUtils.getCurrentHour();
        int currentMinute = TimeUtils.getCurrentMinute();

        if (!alreadyWarned && currentHour == 21 && currentMinute >= 55) {
            alreadyWarned = true;
            Toast.makeText(activity, "¡Atención! En pocos segundos la app se dormirá 🍕💤", Toast.LENGTH_LONG).show();
        }

        handler.postDelayed(() -> {
            if (TimeUtils.isWithinClosedHours()) {
                Intent intent = new Intent(activity, ClosedHoursActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                activity.startActivity(intent);
            }
        }, 1000); // chequeo luego de 1 segundo
    }

    @Override public void onActivityResumed(Activity activity) {
        checkAndRedirect(activity);
    }

    // Métodos obligatorios de la interfaz, aunque no se usen:
    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
    @Override public void onActivityStarted(Activity activity) {}
    @Override public void onActivityPaused(Activity activity) {}
    @Override public void onActivityStopped(Activity activity) {}
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
    @Override public void onActivityDestroyed(Activity activity) {}
}
