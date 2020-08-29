package com.codejar.notekeeper.NoteBroadcastReceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.codejar.notekeeper.notifications.NoteReminder;

public class NoteBroadcastReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTE_TITLE = "com.codejar.notekeeper.NoteBroadcastReceiver.extras.EXTRA_NOTE_TITLE";
    public static final String EXTRA_NOTE_TEXT = "com.codejar.notekeeper.NoteBroadcastReceiver.extras.EXTRA_NOTE_TEXT";
    public static final String EXTRA_NOTE_ID = "com.codejar.notekeeper.NoteBroadcastReceiver.extras.EXTRA_NOTE_ID";

    @Override
    public void onReceive(Context context, Intent intent) {
        long noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1);
        String noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE);
        String noteText = intent.getStringExtra(EXTRA_NOTE_TEXT);
        NoteReminder.notify(context, noteTitle, noteText, noteId);
    }
}
