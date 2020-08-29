package com.codejar.notekeeper.activities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.os.HandlerCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.CursorLoader;
import androidx.loader.content.Loader;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.codejar.notekeeper.BuildConfig;
import com.codejar.notekeeper.CourseInfo;
import com.codejar.notekeeper.DataManager;
import com.codejar.notekeeper.noteBackup.NoteBackup;
import com.codejar.notekeeper.R;
import com.codejar.notekeeper.adapters.CoursesRecyclerAdapter;
import com.codejar.notekeeper.adapters.NoteRecyclerAdapter;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.codejar.notekeeper.data.NotekeeperOpenHelper;
import com.codejar.notekeeper.noteBackup.NoteBackupService;
import com.codejar.notekeeper.noteBackup.NoteUploaderJobService;
import com.codejar.notekeeper.notifications.NoteReminder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

//import com.codejar.notekeeper.CourseInfo;
//import com.codejar.notekeeper.DataManager;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    public static final int NOTES_LOADER_ID = 0;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int NOTES_UPLOADER_JOB_ID = 1;

    private NoteRecyclerAdapter mNoteAdapter;
    private RecyclerView mMRecyclerItems;
    private LinearLayoutManager mNoteLayoutManager;
    private StaggeredGridLayoutManager mCourseLayoutManager;
    private CoursesRecyclerAdapter mMCoursesAdapter;
    private DrawerLayout mDrawer;
    private ActionBarDrawerToggle mToggle;
    private String mUserName;
    private String mUserFavouriteSocialMedia;
    private NotekeeperOpenHelper mDbOpenHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(this);
        boolean prefDarkTheme;
        prefDarkTheme = sharedPreferences.getBoolean("enable_dark_mode", false);

        setTheme(prefDarkTheme ? R.style.DarkTheme_NoActionBar : R.style.AppTheme_NoActionBar);

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mDrawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        FloatingActionButton fab = findViewById(R.id.fab);
        mDbOpenHelper = new NotekeeperOpenHelper(this);

        setSupportActionBar(toolbar);

        mToggle = new ActionBarDrawerToggle(this, mDrawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });

        PreferenceManager.setDefaultValues(this, R.xml.root_preferences, false);
        initDisplayContent();
        navigationView.setNavigationItemSelectedListener(this);

        enableStrictMode();
    }

    private void enableStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy
                    .Builder()
                    .detectAll()
                    .penaltyLog()
                    .build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    private void initDisplayContent() {
        Log.d(TAG, "initDisplayContent: ");
        mMRecyclerItems = findViewById(R.id.list_items);
        mNoteLayoutManager = new LinearLayoutManager(this);
        mCourseLayoutManager = new StaggeredGridLayoutManager(getResources().getInteger(R.integer.course_grid_span), StaggeredGridLayoutManager.VERTICAL);
        DataManager.loadFromDb(mDbOpenHelper);
        List<CourseInfo> courseInfos= DataManager.getInstance().getCourses();
        mMCoursesAdapter = new CoursesRecyclerAdapter(this, courseInfos);

        mNoteAdapter = new NoteRecyclerAdapter(this, null);
    }

//    private Cursor loadNotesFromDb() {
//        SQLiteDatabase readableDatabase = mDbOpenHelper.getReadableDatabase();
//        //               getting noteInfo table data
//        final String[] NoteColumns = {
//                NoteInfoEntry.COLUMN_NOTE_TITLE,
//                NoteInfoEntry.COLUMN_COURSE_ID,
//                NoteInfoEntry._ID};
//
//        String orderNoteBy = NoteInfoEntry.COLUMN_COURSE_ID + "," + NoteInfoEntry.COLUMN_NOTE_TITLE;
//        return readableDatabase.query(NoteInfoEntry.TABLE_NAME, NoteColumns,
//                null, null, null, null, orderNoteBy);
//    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate: ");
        mToggle.syncState();
        Intent intent = getIntent();
        String cancel = intent.getStringExtra(NoteReminder.CANCEL_NOTIFICATION);
        if (cancel != null && cancel.equals(NoteReminder.EXTRA_CANCEL)) {
            NoteReminder.cancel(this);
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume: ");
        super.onResume();
        LoaderManager.getInstance(this).restartLoader(NOTES_LOADER_ID, null, this);
        getUserPreferences();
    }

    private void getUserPreferences() {
        Log.d(TAG, "getUserPreferences: ");
        NavigationView navigationView = findViewById(R.id.nav_view);
        View view = navigationView.getHeaderView(0);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSettings();
            }
        });
        TextView userText = view.findViewById(R.id.user_name);
        TextView emailText = view.findViewById(R.id.email_address);
        SharedPreferences preferences = getDefaultSharedPreferences(this);
        mUserName = preferences.getString("display_name", "");
        String userEmail = preferences.getString("user_email", "");
        mUserFavouriteSocialMedia = preferences.getString("user_social", "");
        userText.setText(mUserName);
        emailText.setText(userEmail);
        openDrawer(preferences);
    }

    private void openDrawer(final SharedPreferences preferences) {
        final String initialized = getString(R.string.app_is_initialized);
        boolean isInitialized = preferences.getBoolean(initialized, false);
        if (!isInitialized) {
            Handler handler = HandlerCompat.createAsync(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean(initialized, true).apply();
                    mDrawer.openDrawer(GravityCompat.START);
                }
            }, 2000);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Log.d(TAG, "onNavigationItemSelected: ");
        int id = menuItem.getItemId();
        switch (id) {
            case R.id.nav_notes:
                displayNotes();
                break;
            case R.id.nav_courses:
                displayCourses();
                break;
            case R.id.nav_send:
                handleSend();
                break;
            case R.id.nav_share:
                handleShare();
                break;
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void handleShare() {
        Log.d(TAG, "handleShare: ");
        displayToast("Sharing with: \n" + mUserName);
    }

    private void handleSend() {
        Log.d(TAG, "handleSend: ");
        displayToast("Sending to: \n" + mUserFavouriteSocialMedia);
    }

    private void displayToast(String text) {
        Log.d(TAG, "displayToast: ");
        Snackbar.make(mDrawer, text,
                Snackbar.LENGTH_LONG).show();
    }

    private void displayNotes() {
        Log.d(TAG, "displayNotes: started");
        mMRecyclerItems.setLayoutManager(mNoteLayoutManager);
        mMRecyclerItems.setAdapter(mNoteAdapter);
//        SQLiteDatabase database = mDbOpenHelper.getReadableDatabase();

        selectNavItem(R.id.nav_notes);
    }

    private void displayCourses() {
        Log.d(TAG, "displayCourses: ");
        mMRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mMRecyclerItems.setAdapter(mMCoursesAdapter);

        selectNavItem(R.id.nav_courses);
    }

    private void selectNavItem(int id) {
        Log.d(TAG, "selectNavItem: ");
        NavigationView navigationView = findViewById(R.id.nav_view);
        Menu menu = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected: ");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startSettings();
            return true;
        } else if (id == R.id.action_backup_notes) {
            backupNotes();
        } else if (id == R.id.action_upload_notes) {
            scheduleNotesUpload();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private void scheduleNotesUpload() {
        Log.d(TAG, "scheduleNotesUpload: ");
        PersistableBundle extras =new PersistableBundle();
        extras.putString(NoteUploaderJobService.EXTRA_DATA_URI, Notes.CONTENT_URI.toString());
        ComponentName componentName= new ComponentName(this, NoteUploaderJobService.class);
        JobInfo jobInfo = new JobInfo.Builder(NOTES_UPLOADER_JOB_ID, componentName)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setOverrideDeadline(30000)
                .setExtras(extras)
                .build();
        JobScheduler scheduler= (JobScheduler) getSystemService(JOB_SCHEDULER_SERVICE);
        scheduler.schedule(jobInfo);
    }

    private void backupNotes() {
        Log.d(TAG, "backupNotes: ");
        Intent intent = new Intent(this, NoteBackupService.class);
        intent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);
        startService(intent);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed: ");
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: started");
        mDbOpenHelper.close();
        super.onDestroy();
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        Log.d(TAG, "onCreateLoader: started");
        CursorLoader loader = null;
        final Uri uri = Notes.CONTENT_EXPANDED_URI;
        final String[] NoteColumns = {
                Notes._ID,
                Notes.COLUMN_NOTE_TITLE,
                Courses.COLUMN_COURSE_TITLE};

        String orderNoteBy = Courses.COLUMN_COURSE_TITLE + ","
                + Notes.COLUMN_NOTE_TITLE;
        if (id == NOTES_LOADER_ID) {
            loader = new CursorLoader(this, uri, NoteColumns, null, null, orderNoteBy);

        }
        assert loader != null;
        return loader;
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: started");
        if (loader.getId() == NOTES_LOADER_ID) {
            if (data != null) {
                mNoteAdapter.changeCursor(data);
                displayNotes();
            }
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset: started ");
        if (loader.getId() == NOTES_LOADER_ID) {
            mNoteAdapter.changeCursor(null);
        }
    }
}
