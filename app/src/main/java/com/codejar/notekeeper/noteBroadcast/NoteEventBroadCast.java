package com.codejar.notekeeper.noteBroadcast;

import android.content.Context;
import android.content.Intent;

public class NoteEventBroadCast {
    public static final String ACTION_COURSE_EVENT = "com.codejar.notekeeper.noteBroadcast.extras.COURSE_EVENT";
    public static final String EXTRA_COURSE_ID= "com.codejar.notekeeper.noteBroadcast.extras.COURSE_ID";
    public static final String EXTRA_EVENT_MESSAGE= "com.codejar.notekeeper.noteBroadcast.extras.EVENT_MESSAGE";

    public static  void  sendEventBroadcast(Context context, String courseId, String message){
        Intent intent = new Intent(ACTION_COURSE_EVENT);
        intent.putExtra(EXTRA_COURSE_ID, courseId);
        intent.putExtra(EXTRA_EVENT_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
