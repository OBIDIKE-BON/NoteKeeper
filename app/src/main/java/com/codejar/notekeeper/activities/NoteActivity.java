package com.codejar.notekeeper.activities;


//import android.content.CursorLoader;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;

import com.codejar.notekeeper.NoteBroadcastReceiver.NoteBroadcastReceiver;
import com.codejar.notekeeper.R;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.codejar.notekeeper.data.NoteKeeperContract.NoteInfoEntry;
import com.codejar.notekeeper.data.NotekeeperOpenHelper;
import com.codejar.notekeeper.myCustom.ModuleStatusView;
import com.codejar.notekeeper.noteBroadcast.NoteEventBroadCast;
import com.codejar.notekeeper.notifications.NoteReminder;
import com.codejar.notekeeper.viewModels.NoteActivityViewModel;

import java.lang.ref.SoftReference;
import java.util.Random;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;


public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String NOTE_ID = "com.codejar.notekeeper.activities.NoteActivity.NOTE_POSITION";
    public static final int ID_NOT_SET = -1;
    public static final int NONE_SELECTED = -1;
    public static final int NOTES_LOADER_ID = 0;
    public static final int COURSES_LOADER_ID = 1;
    private static final String TAG = NoteActivity.class.getSimpleName();

    Spinner mSpinner_courses;
    EditText mNoteTitle;
    EditText mNoteText;

    private long mNoteId;
    private boolean mIsNewNote;
    private boolean mIsCancel;
    private NoteActivityViewModel mViewModel;
    private int noteDirection = NONE_SELECTED;
    private NotekeeperOpenHelper mDbOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIdPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;
    private SimpleCursorAdapter mCourseAdapter;
    private Cursor mCourseCursor;
    private boolean mIsNotesQueryFinished;
    private boolean mIsCoursesQueryFinished;
    private int mNoteIdPos;
    private Uri mNoteUri;
    private ProgressBar mProgressUpdate;
    private ModuleStatusView mModuleStatusView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        boolean prefDarkTheme;
        prefDarkTheme = sharedPreferences.getBoolean("enable_dark_mode", false);

        setTheme(prefDarkTheme ? R.style.DarkTheme_NoActionBar : R.style.AppTheme_NoActionBar);

        Log.d(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        loadCourseData();

        if (savedInstanceState != null && mViewModel.mIsNewModel) {
            mViewModel.restoreState(savedInstanceState);
        }

        readDisplayValues();

        if (!mIsNewNote) {
            loadNoteInBackGround();
        }

        mModuleStatusView = findViewById(R.id.moduleStatusView);
        setupModuleStatus();
    }

    private void setupModuleStatus() {
        Log.d(TAG, "setupModuleStatus: ");
        boolean[] courseModules = new boolean[11];
        int numberOfCompletedModules = 7;
        for (int i = 0; i < numberOfCompletedModules; i++)
            courseModules[i] = true;

        mModuleStatusView.setModuleStatus(courseModules);
    }

    private void loadNoteInBackGround() {
        Log.d(TAG, "loadNoteInBackGround: ");
        LoaderManager.getInstance(this).restartLoader(NOTES_LOADER_ID, null, this);
    }

    private void init() {
        Log.d(TAG, "init: ");
        ViewModelProvider viewModelProvider  = new ViewModelProvider(getViewModelStore(),
                ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));
        mDbOpenHelper = new NotekeeperOpenHelper(this);
        mViewModel = viewModelProvider.get(NoteActivityViewModel.class);
        mSpinner_courses = findViewById(R.id.courses_spinner);
        mNoteTitle = findViewById(R.id.txt_note_title);
        mNoteText = findViewById(R.id.txt_note_text);
        mProgressUpdate = findViewById(R.id.progress_update);

        mCourseAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,
                null, new String[]{Courses.COLUMN_COURSE_TITLE}, new int[]{android.R.id.text1}, 0);
        mCourseAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner_courses.setAdapter(mCourseAdapter);
    }

    private void loadCourseData() {
        Log.d(TAG, "loadCourseData: ");
        LoaderManager.getInstance(this).restartLoader(COURSES_LOADER_ID, null, this);
    }

    private void saveOriginalNoteValues(int noteId, String noteTitle, String noteText, String courseId) {
        Log.d(TAG, "saveOriginalNoteValues: ");
        if (mIsNewNote) {
            return;
        }
        mViewModel.mOriginalNoteId = noteId;
        mViewModel.mOriginalCourseId = courseId;
        mViewModel.mOriginalNoteTitle = noteTitle;
        mViewModel.mOriginalNoteText = noteText;
    }

    private void readDisplayValues() {
        Log.d(TAG, "readDisplayValues: ");
        Intent intent = getIntent();
        mNoteId = intent.getLongExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteId == ID_NOT_SET;
        if (mIsNewNote) {
            createNewNote();
        }
//            mNote = DataManager.getInstance().getNotes().get(mId);
    }

    private void createNewNote() {

        ContentValues values = new ContentValues();

        values.put(Notes.COLUMN_NOTE_TITLE, "");
        values.put(Notes.COLUMN_NOTE_TEXT, "");
        values.put(Notes.COLUMN_COURSE_ID, "");

        new InsertTask(this).execute(values);
    }

    private void displayNote() {
        Log.d(TAG, "displayNote: ");
        String courseId = mNoteCursor.getString(mCourseIdPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);
        int noteId = mNoteCursor.getInt(mNoteIdPos);

        int courseIndex = getCourseOfSelectedNote(courseId);
        mSpinner_courses.setSelection(courseIndex);
        mNoteTitle.setText(noteTitle);
        mNoteText.setText(noteText);

        saveOriginalNoteValues(noteId, noteTitle, noteText, courseId);

        NoteEventBroadCast.sendEventBroadcast(this, courseId, "editing Note: " + noteTitle);
    }

    private int getCourseOfSelectedNote(@NonNull String courseId) {
        Log.d(TAG, "getCourseOfSelectedNote: ");
        int cursorColumnPos = mCourseCursor.getColumnIndex(Courses.COLUMN_COURSE_ID);
        int coursePosition = 0;
        do {
            String currentCourseId = mCourseCursor.getString(cursorColumnPos);
            if (courseId.equals(currentCourseId))
                break;

            coursePosition++;

        } while (mCourseCursor.moveToNext());

        return coursePosition;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu: ");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_send_mail:
                sendMail();
                return true;
            case R.id.action_cancel:
                mIsCancel = true;
                finish();
                return true;
            case R.id.action_next:
                noteDirection = R.id.action_next;
                moveTo();
                return true;
            case R.id.action_previous:
                noteDirection = R.id.action_previous;
                moveTo();
                return true;
            case R.id.action_review_note:
                setReminder();
                return true;
        }
        return true;
    }

    private void setReminder() {
        String title = mNoteTitle.getText().toString();
        String text = mNoteText.getText().toString();
//        NoteReminder.notify(this, title, text, mNoteId);
        Intent intent = new Intent(this, NoteBroadcastReceiver.class);
        intent.putExtra(NoteBroadcastReceiver.EXTRA_NOTE_TITLE, title);
        intent.putExtra(NoteBroadcastReceiver.EXTRA_NOTE_TEXT, text);
        intent.putExtra(NoteBroadcastReceiver.EXTRA_NOTE_ID, mNoteId);

        PendingIntent pendingIntent = PendingIntent.getBroadcast
                (this, NoteReminder.NOTIFICATION_ID, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long currentTime = SystemClock.elapsedRealtime();
        long ONE_HOUR = 60 * 60 * 1000;
        long TEN_SEC = 10 * 1000;
        long ALARM_TIME = currentTime + TEN_SEC;
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, ALARM_TIME, pendingIntent);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu: ");
        MenuItem nextMenuItem = menu.findItem(R.id.action_next);
        MenuItem previousMenuItem = menu.findItem(R.id.action_previous);
        int NotesSize = 8;
        if (mNoteId > NotesSize - 1) {
            if (nextMenuItem != null) {
                nextMenuItem.setIcon(R.drawable.ic_block_white_24dp);
                nextMenuItem.setEnabled(false);
            }
        }
        if (mNoteId <= 1) {
            if (previousMenuItem != null) {
                previousMenuItem.setIcon(R.drawable.ic_block_white_24dp);
                previousMenuItem.setEnabled(false);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void moveTo() {
        Log.d(TAG, "moveTo: ");
        saveNote();
        navigateNotes();
        loadCourseData();
        loadNoteInBackGround();
        invalidateOptionsMenu();
    }

    private void navigateNotes() {
        Log.d(TAG, "navigateNotes: ");
        mNoteId = noteDirection == R.id.action_next ? ++mNoteId : --mNoteId;
//        mNote = DataManager.getInstance().getNotes().get(mNoteId - 1);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause: ");
        super.onPause();
        if (mIsCancel) {
            if (mIsNewNote) {
                deleteNoteFromDatabase();
            } else {
                restorePreviousNoteValues();
            }
        } else {
            saveNote();
        }
    }

    private void deleteNoteFromDatabase() {
        Log.d(TAG, "deleteNoteFromDatabase: ");

        String criteria = NoteInfoEntry._ID + " = ?";
        String[] criteriaArgs = {Long.toString(mNoteId)};
        new DeleteTask(criteria, criteriaArgs, this).execute();
    }

    private void restorePreviousNoteValues() {
        Log.d(TAG, "restorePreviousNoteValues: ");
        String courseId = mViewModel.mOriginalCourseId;
        String noteTex = mViewModel.mOriginalNoteText;
        String noteTile = mViewModel.mOriginalNoteTitle;
        mNoteId = mViewModel.mOriginalNoteId;
        saveNoteToDatabase(noteTile, noteTex, courseId);
    }

    private void saveNote() {
        Log.d(TAG, "saveNote: ");
        String courseId = selectedCourseId();
        String noteTex = mNoteText.getText().toString();
        String noteTile = mNoteTitle.getText().toString();
        saveNoteToDatabase(noteTile, noteTex, courseId);
    }

    private String selectedCourseId() {
        return getCourseString(Courses.COLUMN_COURSE_ID);
    }

    private void saveNoteToDatabase(String noteTile, String noteTex, String courseId) {
        Log.d(TAG, "saveNoteToDatabase: ");

        String criteria = NoteInfoEntry._ID + " = ?";
        String[] criteriaArgs = {Long.toString(mNoteId)};

        ContentValues values = new ContentValues();

        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTile);
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteTex);
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId);

        new UpdateTask(criteria, criteriaArgs, this).execute(values);
    }

    private void sendMail() {
        String subject = mNoteTitle.getText().toString();
        String text = "check out the course I learnt  @ www.pluralsight.com \"" +
                getCourseString(Courses.COLUMN_COURSE_TITLE) + " \" \n " + mNoteText.getText().toString();
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc2822");
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, text);
        startActivity(intent);
    }

    private String getCourseString(String columnCourseTitle) {
        int coursePosition = mSpinner_courses.getSelectedItemPosition();
        mCourseCursor.moveToPosition(coursePosition);
        int cursorColumnPos = mCourseCursor.getColumnIndex(columnCourseTitle);
        return mCourseCursor.getString(cursorColumnPos);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mViewModel.saveState(outState);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader;
        if (id == NOTES_LOADER_ID) {
            loader = loadNoteData();
        } else {
            loader = loadCourses();
        }
        return loader;
    }

    private CursorLoader loadNoteData() {
        mIsNotesQueryFinished = false;
        String[] noteColumns = {
                NoteInfoEntry._ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT,
                NoteInfoEntry.COLUMN_COURSE_ID};
        mNoteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, mNoteId);
        return new CursorLoader(this, mNoteUri, noteColumns, null, null, null);
    }

    private CursorLoader loadCourses() {
        mIsCoursesQueryFinished = false;
        // getting courseInfo table data
        Uri uri = Courses.CONTENT_URI;
        final String[] courseColumns = {
                Courses.COLUMN_COURSE_ID,
                Courses.COLUMN_COURSE_TITLE,
                Courses._ID};

        return new CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (loader.getId() == NOTES_LOADER_ID) {
            retrieveNotesDataFromCursor(data);
        } else if (loader.getId() == COURSES_LOADER_ID) {
            mCourseCursor = data;
            mCourseAdapter.changeCursor(data);
            mIsCoursesQueryFinished = true;
            confirmQueryFinished();
        }
    }

    private void retrieveNotesDataFromCursor(Cursor data) {
        mNoteCursor = data;
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);
        mCourseIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteIdPos = mNoteCursor.getColumnIndex(NoteInfoEntry._ID);
        mIsNotesQueryFinished = true;
        confirmQueryFinished();

    }

    private void confirmQueryFinished() {
        if (mIsNotesQueryFinished && mIsCoursesQueryFinished) {
            displayNote();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        if (loader.getId() == NOTES_LOADER_ID) {
            if (mNoteCursor != null) {
                mNoteCursor.close();
            }
        } else if (loader.getId() == COURSES_LOADER_ID) {
            mCourseAdapter.changeCursor(null);
        }
    }

    @Override
    protected void onDestroy() {
        mDbOpenHelper.close();
        super.onDestroy();
    }

    private static class InsertTask extends AsyncTask<ContentValues, Integer, Uri> {
        private static final String TAG = InsertTask.class.getSimpleName();
        private SoftReference<NoteActivity> mNoteActivitySoftReference;

        private InsertTask(NoteActivity noteActivitySoftReference) {
            mNoteActivitySoftReference = new SoftReference<>(noteActivitySoftReference);
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: " + Thread.currentThread().getId());
            mNoteActivitySoftReference.get().mProgressUpdate.setVisibility(View.VISIBLE);
            mNoteActivitySoftReference.get().mProgressUpdate.setProgress(1);
        }

        @Override
        protected Uri doInBackground(ContentValues... values) {
            Log.d(TAG, "doInBackground: " + Thread.currentThread().getId());
            doBackgroundWork();
            publishProgress(2);
            Uri uri = mNoteActivitySoftReference.get().getApplicationContext()
                    .getContentResolver().insert(Notes.CONTENT_URI, values[0]);
            doBackgroundWork();
            publishProgress(3);
            return uri;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "onProgressUpdate: " + Thread.currentThread().getId());
            mNoteActivitySoftReference.get().mProgressUpdate.setProgress(values[0]);
        }

        @Override
        protected void onPostExecute(Uri uri) {
            Log.d(TAG, "onPostExecute: " + Thread.currentThread().getId());
            if (uri != null) {
                mNoteActivitySoftReference.get().mNoteId =
                        ContentUris.parseId(uri);
                mNoteActivitySoftReference.get().mNoteUri = uri;
                Context context = mNoteActivitySoftReference.get().getApplicationContext();
                Toast.makeText(context, "the with Uri " + uri + " has been inserted", Toast.LENGTH_LONG).show();
            }
            mNoteActivitySoftReference.get().mProgressUpdate.setVisibility(View.GONE);
        }

        private void doBackgroundWork() {
            Log.d(TAG, "doBackgroundWork: " + Thread.currentThread().getId());
            Random r = new Random();
            int n = r.nextInt(2);
            int s = n * 3000;

            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static class UpdateTask extends AsyncTask<ContentValues, Integer, Void> {
        private static final String TAG = UpdateTask.class.getSimpleName();
        private final String mCriteria;
        private final String[] mCriteriaArgs;
        private SoftReference<NoteActivity> mNoteActivitySoftReference;

        public UpdateTask(String criteria, String[] criteriaArgs, NoteActivity noteActivitySoftReference) {
            mNoteActivitySoftReference = new SoftReference<>(noteActivitySoftReference);
            mCriteria = criteria;
            mCriteriaArgs = criteriaArgs;
        }

        @Override
        protected Void doInBackground(ContentValues... values) {
            Log.d(TAG, "doInBackground: " + Thread.currentThread().getId());

            mNoteActivitySoftReference.get().getApplicationContext()
                    .getContentResolver().update(Notes.CONTENT_URI, values[0], mCriteria, mCriteriaArgs);
            return null;
        }
    }

    private static class DeleteTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = DeleteTask.class.getSimpleName();
        private final String mCriteria;
        private final String[] mCriteriaArgs;
        private SoftReference<NoteActivity> mNoteActivitySoftReference;

        public DeleteTask(String criteria, String[] criteriaArgs, NoteActivity noteActivitySoftReference) {
            mCriteria = criteria;
            mCriteriaArgs = criteriaArgs;
            mNoteActivitySoftReference = new SoftReference<>(noteActivitySoftReference);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            Log.d(TAG, "doInBackground: ");

            mNoteActivitySoftReference.get().getApplicationContext()
                    .getContentResolver().delete(Notes.CONTENT_URI, mCriteria, mCriteriaArgs);
            return null;
        }
    }

}
