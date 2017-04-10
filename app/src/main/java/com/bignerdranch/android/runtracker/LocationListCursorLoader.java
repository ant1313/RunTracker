package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.database.Cursor;

/**
 * Created by Антон on 09.04.2017.
 */

public class LocationListCursorLoader extends SQLiteCursorLoader {
    private long mRunId;
    public LocationListCursorLoader(Context c, long runId) {
        super(c);
        mRunId = runId;
    }
    @Override
    protected Cursor loadCursor() {
        return RunManager.get(getContext()).queryLocationsForRun(mRunId);
    }
}
