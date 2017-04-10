package com.bignerdranch.android.runtracker;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.bignerdranch.android.runtracker.RunDatabaseHelper.RunCursor;

/**
 * Created by Антон on 08.04.2017.
 */

public class RunListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
    private static final int REQUEST_NEW_RUN = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
// Инициализация загрузчика для загрузки списка серий
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.run_list_options, menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_new_run:
                Intent i = new Intent(getActivity(), RunActivity.class);
                startActivityForResult(i, REQUEST_NEW_RUN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_NEW_RUN == requestCode) {
            // Перезапуск загрузчика для получения новых серий
            getLoaderManager().restartLoader(0, null, this);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
// Аргумент id содержит идентификатор серии;
// CursorAdapter автоматически предоставляет эту информацию.
        Intent i = new Intent(getActivity(), RunActivity.class);
        i.putExtra(RunActivity.EXTRA_RUN_ID, id);
        startActivity(i);
    }

    private static class RunCursorAdapter extends CursorAdapter {
        private RunDatabaseHelper.RunCursor mRunCursor;
        public RunCursorAdapter(Context context, RunDatabaseHelper.RunCursor cursor) {
            super(context, cursor, 0);
            mRunCursor = cursor;
        }
        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
// Использование заполнителя макета для получения
// представления строки
            LayoutInflater inflater = (LayoutInflater)context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return inflater
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
        }
        @Override
        /*
        связывание представления
        вызываться кодом CursorAdapter, когда потребуется настроить представление
         для хранения данных строки в курсоре
         */
        public void bindView(View view, Context context, Cursor cursor) {
// Получение серии для текущей строки
            Run run = mRunCursor.getRun();
// Создание текстового представления начальной даты
            TextView startDateTextView = (TextView)view;
            String cellText =
                    context.getString(R.string.cell_text, run.getStartDate());
            startDateTextView.setText(cellText);
        }
    }
    private static class RunListCursorLoader extends SQLiteCursorLoader {
        public RunListCursorLoader(Context context) {
            super(context);
        }
        @Override
        protected Cursor loadCursor() {
// Запрос на получение списка серий
            return RunManager.get(getContext()).queryRuns();
        }
    }
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new RunListCursorLoader(getActivity());
    }
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
// Создание адаптера, ссылающегося на этот курсор
        RunCursorAdapter adapter =
        new RunCursorAdapter(getActivity(), (RunCursor)cursor);
        setListAdapter(adapter);
    }
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
// Прекращение использования курсора (через адаптер)
        setListAdapter(null);
    }
}
