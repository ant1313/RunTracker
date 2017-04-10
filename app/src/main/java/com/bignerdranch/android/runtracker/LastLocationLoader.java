package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.location.Location;

/**
 * Created by Антон on 08.04.2017.
 * Загрузка последней позиции в серии
 */

public class LastLocationLoader extends DataLoader {
    private long mRunId;
    public LastLocationLoader(Context context, long runId) {
        super(context);
        mRunId = runId;
    }
    @Override
    public Location loadInBackground() {
        return RunManager.get(getContext()).getLastLocationForRun(mRunId);
    }
}
