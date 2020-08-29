package com.codejar.notekeeper.noteBackup;

import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class NoteUploaderJobService extends JobService {

    public static final String EXTRA_DATA_URI = "com.codejar.notekeeper.noteBackup.extras.DATA_URI";
    private static final String TAG = NoteUploaderJobService.class.getSimpleName();
    private NoteUploader mNoteUploader;

    public NoteUploaderJobService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.d(TAG, "onStartJob: ");
        UploadTask task = new UploadTask();
        mNoteUploader = new NoteUploader(this);
        task.execute(params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.d(TAG, "onStopJob: ");
        mNoteUploader.cancel();
        return true;
    }

    private class UploadTask extends AsyncTask<JobParameters, Void, Void> {
        private final String TAG = UploadTask.class.getSimpleName();

        @Override
        protected Void doInBackground(JobParameters... jobParameters) {
            Log.d(TAG, "doInBackground: ");
            JobParameters jobParams = jobParameters[0];
            String stringUri = jobParams.getExtras().getString(EXTRA_DATA_URI);
            Uri dataUri = Uri.parse(stringUri);
            mNoteUploader.doUpload(dataUri);
            if (!mNoteUploader.isCanceled()) {
                jobFinished(jobParams, false);
            }
            return null;
        }
    }
}










