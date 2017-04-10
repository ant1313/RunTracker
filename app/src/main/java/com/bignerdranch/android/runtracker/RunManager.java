package com.bignerdranch.android.runtracker;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.bignerdranch.android.runtracker.RunDatabaseHelper.LocationCursor;
import com.bignerdranch.android.runtracker.RunDatabaseHelper.RunCursor;
/**
 * Created by Антон on 06.04.2017.
 */

public class RunManager {
    private static final String TAG = "RunManager";

    private static final String PREFS_FILE = "runs";
    private static final String PREF_CURRENT_RUN_ID = "RunManager.currentRunId";

    public static final String ACTION_LOCATION =
            "com.bignerdranch.android.runtracker.ACTION_LOCATION";

    private static final String TEST_PROVIDER = "TEST_PROVIDER";

    private static RunManager sRunManager;
    private Context mAppContext;
    private LocationManager mLocationManager;

    private RunDatabaseHelper mHelper;
    private SharedPreferences mPrefs;
    private long mCurrentRunId;

    // Закрытый конструктор заставляет использовать
// RunManager.get(Context)
    private RunManager(Context appContext) {
        mAppContext = appContext;
        mLocationManager = (LocationManager) mAppContext
                .getSystemService(Context.LOCATION_SERVICE);

        mHelper = new RunDatabaseHelper(mAppContext);
        mPrefs = mAppContext.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE);
    }

    public static RunManager get(Context c) {
        if (sRunManager == null) {
// Использование контекста приложения для предотвращения
// утечки активностей
            sRunManager = new RunManager(c.getApplicationContext());
        }
        return sRunManager;
    }
//создает объект Intent, который
//будет рассылаться при обновлении местоположения
    private PendingIntent getLocationPendingIntent(boolean shouldCreate) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        int flags = shouldCreate ? 0 : PendingIntent.FLAG_NO_CREATE;
        return PendingIntent.getBroadcast(mAppContext, 0, broadcast, flags);
    }
//приказываем LocationManager передавать обновленую
//информацию местоположения через поставщика данных GPS как можно чаще.
    public void startLocationUpdates() {
        String provider = LocationManager.GPS_PROVIDER;

        // Если имеется поставщик тестовых данных и он активен,
// использовать его.
        if (mLocationManager.getProvider(TEST_PROVIDER) != null &&
                mLocationManager.isProviderEnabled(TEST_PROVIDER)) {
            provider = TEST_PROVIDER;
        }
        Log.d(TAG, "Using provider " + provider);

        // Получение последнего известного местоположения
// и его рассылка (если данные доступны).
                Location lastKnown = mLocationManager.getLastKnownLocation(provider);
        if (lastKnown != null) {
// Время инициализируется текущим значением
            lastKnown.setTime(System.currentTimeMillis());
            broadcastLocation(lastKnown);
        }

// Запуск обновлений из LocationManager
        PendingIntent pi = getLocationPendingIntent(true);

        if (ActivityCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(mAppContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mLocationManager.requestLocationUpdates(provider, 0, 0, pi);
    }

    public void stopLocationUpdates() {
        PendingIntent pi = getLocationPendingIntent(false);
        if (pi != null) {
            mLocationManager.removeUpdates(pi);
            pi.cancel();
        }
    }
    //определяет, зарегистрирован ли объект PendingIntent в ОС.
    public boolean isTrackingRun()
    {
        return getLocationPendingIntent(false) != null;
    }
    public boolean isTrackingRun(Run run) {
        return run != null && run.getId() == mCurrentRunId;
    }

    private void broadcastLocation(Location location) {
        Intent broadcast = new Intent(ACTION_LOCATION);
        broadcast.putExtra(LocationManager.KEY_LOCATION_CHANGED, location);
        mAppContext.sendBroadcast(broadcast);
    }

    public Run startNewRun() {
// Вставка объекта Run в базу данных
        Run run = insertRun();
// Запуск отслеживания серии
        startTrackingRun(run);
        return run;
    }
    public void startTrackingRun(Run run) {
// Получение идентификатора
        mCurrentRunId = run.getId();
// Сохранение его в общих настройках
        mPrefs.edit().putLong(PREF_CURRENT_RUN_ID, mCurrentRunId).commit();
// Запуск обновления данных местоположения
        startLocationUpdates();
    }
    public void stopRun() {
        stopLocationUpdates();
        mCurrentRunId = -1;
        mPrefs.edit().remove(PREF_CURRENT_RUN_ID).commit();
    }
    private Run insertRun() {
        Run run = new Run();
        run.setId(mHelper.insertRun(run));
        return run;
    }

    public RunDatabaseHelper.RunCursor queryRuns() {
        return mHelper.queryRuns();
    }

    public void insertLocation(Location loc) {
        if (mCurrentRunId != -1) {
            mHelper.insertLocation(mCurrentRunId, loc);
        } else {
            Log.e(TAG, "Location received with no tracking run; ignoring.");
        }
    }
    public Run getRun(long id) {
        Run run = null;
        RunDatabaseHelper.RunCursor cursor = mHelper.queryRun(id);
        cursor.moveToFirst();
// Если строка присутствует, получить объект серии
        if (!cursor.isAfterLast())
            //Если набор содержит хотя бы одну строку, isAfterLast() вернет false,
            run = cursor.getRun();
        cursor.close();
        return run;
    }
    //Получение последнего местоположения для серии
    public Location getLastLocationForRun(long runId) {
        Location location = null;
        RunDatabaseHelper.LocationCursor cursor = mHelper.queryLastLocationForRun(runId);
        cursor.moveToFirst();
// Если набор не пуст, получить местоположение
        if (!cursor.isAfterLast())
            location = cursor.getLocation();
        cursor.close();
        return location;
    }
    public LocationCursor queryLocationsForRun(long runId) {
        return mHelper.queryLocationsForRun(runId);
    }
}
