package com.bignerdranch.android.runtracker;


import android.support.v4.app.Fragment;

/**
 * Created by Антон on 08.04.2017.
 */

public class RunListActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new RunListFragment();
    }
}
