package com.codejar.notekeeper.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.codejar.notekeeper.R;
import com.codejar.notekeeper.activities.MainActivity;
import com.codejar.notekeeper.activities.NoteActivity;
import com.codejar.notekeeper.noteBackup.NoteBackup;
import com.codejar.notekeeper.noteBackup.NoteBackupService;

public class NoteReminder {
    private static final String TAG = NoteReminder.class.getSimpleName();
    public static final String CANCEL_NOTIFICATION = "cancelNotification";

    private NoteReminder() {
        Log.d(TAG, "NoteReminder: constructor");
    }

    public static final int NOTIFICATION_ID = 0;
    public static final String EXTRA_CANCEL = "com.codejar.notekeeper.notifications.EXTRA_CANCEL";
    public static final String NOTIFICATION_CHANNEL_ID = "com.codejar.notekeeper.notifications.NOTIFICATION_CHANNEL_ID";

    public static void notify(Context context, String title, String text, long notId) {
        Log.d(TAG, "notify: ");
        Resources res = context.getResources();
        Bitmap picture = BitmapFactory.decodeResource(res, R.drawable.logo);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle()
                .bigText(text)
                .setBigContentTitle(title)
                .setSummaryText("Review Note " + title);

        Intent noteIntent = new Intent(context, NoteActivity.class);
        noteIntent.putExtra(NoteActivity.NOTE_ID, notId);

        Intent viewAllNotesIntent = new Intent(context, MainActivity.class);
        viewAllNotesIntent.putExtra(CANCEL_NOTIFICATION, EXTRA_CANCEL);

        Intent backupIntent = new Intent(context, NoteBackupService.class);
        backupIntent.putExtra(NoteBackupService.EXTRA_COURSE_ID, NoteBackup.ALL_COURSES);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Review Note")
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_note_reminder)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setLargeIcon(picture)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setTicker(title)
                .setStyle(bigTextStyle)
                .setContentIntent( getPendingIntent(context, noteIntent ,"activity"))
                .setAutoCancel(true)
                .addAction(R.drawable.ic_baseline_view_list_24,"VIEW ALL NOTES",
                        getPendingIntent(context, viewAllNotesIntent, "activity"))
                .addAction(0,"BACKUP NOTES",
                        getPendingIntent(context, backupIntent, "service"));
       notify(context, builder.build());
    }

    private static PendingIntent getPendingIntent(Context context, Intent intent, String type) {
        Log.d(TAG, "getPendingIntent: to start "+type);
        if (type.compareToIgnoreCase("activity") == 0) {
            return PendingIntent.getActivity(context, NOTIFICATION_ID,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }else {
            return PendingIntent.getService(context, NOTIFICATION_ID,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void notify(final Context context, final Notification notification) {
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(nm);
        }
        nm.notify(NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID, notification);
    }

    /**
     * Cancels any notifications of this type previously shown using
     * {@link #notify(Context, Notification)}.
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void cancel(final Context context) {
        Log.d(TAG, "cancel: Notification");
        final NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_ID);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static void createNotificationChannel(NotificationManager nm) {
        Log.d(TAG, "createNotificationChannel: ");
        String noteReminder = "NoteReminder";
            NotificationChannel notificationChannel =
                    new NotificationChannel(NOTIFICATION_CHANNEL_ID, noteReminder, NotificationManager.IMPORTANCE_DEFAULT);
            String description = "A channel to remind users to review a note";
            notificationChannel.setDescription(description);
            notificationChannel.enableVibration(true);
            notificationChannel.setLightColor(Color.WHITE);
            notificationChannel.enableLights(true);
            nm.createNotificationChannel(notificationChannel);
    }

}
