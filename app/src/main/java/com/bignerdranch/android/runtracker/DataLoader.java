package com.bignerdranch.android.runtracker;


import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

/**
 * Created by Антон on 08.04.2017.
 * DataLoader выполняет некоторые простые операции, которые
 должны выполняться всеми субклассами AsyncTaskLoader, оставляя своим суб-
 классам только реализацию loadInBackground().
 обобщенный тип D для хранения экземпляра загружа-
 емых данных.
 */
public abstract class DataLoader<D> extends AsyncTaskLoader<D> {
    private D mData;
    public DataLoader(Context context) {
        super(context);
    }
    @Override
    protected void onStartLoading() {
        if (mData != null) {
            deliverResult(mData);
        } else {
            forceLoad();
        }
    }
    @Override
    public void deliverResult(D data) {
        mData = data;
        if (isStarted())
            super.deliverResult(data);
    }
}
