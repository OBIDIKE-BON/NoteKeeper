package com.codejar.notekeeper.noteBackup;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.util.Log;

import com.codejar.notekeeper.notifications.NoteReminder;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class NoteBackupService extends IntentService {
    // TODO: Rename parameters
     public static final String EXTRA_COURSE_ID = "com.codejar.notekeeper.noteBackup.extra.EXTRA_COURSE_ID";
    private static final String TAG = NoteBackupService.class.getSimpleName();

    public NoteBackupService() {
        super("NoteBackupService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "onHandleIntent: thread:"+ Thread.currentThread().getId());
        NoteReminder.cancel(this);
               if (intent!=null){
                   String extraCourseId = intent.getStringExtra(EXTRA_COURSE_ID);
                   if (extraCourseId != null) {
                       NoteBackup.doBackup(this, extraCourseId);
                   }
               }
    }
}
