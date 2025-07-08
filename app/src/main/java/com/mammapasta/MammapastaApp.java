package com.mammapasta;

import android.app.Application;
import com.mammapasta.guardian.ClosedHoursWatcher;

public class MammapastaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        new ClosedHoursWatcher(this); // Activamos el vigilante
    }
}
