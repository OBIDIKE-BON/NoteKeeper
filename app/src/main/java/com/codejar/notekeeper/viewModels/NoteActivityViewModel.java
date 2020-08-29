package com.codejar.notekeeper.viewModels;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    private static final String TAG = NoteActivityViewModel.class.getSimpleName();
    public String mOriginalCourseId;
    public String mOriginalNoteTitle;
    public String mOriginalNoteText;
    public int mOriginalNoteId;
    public boolean mIsNewModel= true;

    public static final String ORIGINAL_COURSE_ID="com.codejar.notekeeper.viewmodels.NoteActivityViewModel.ORIGINAL_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE="com.codejar.notekeeper.viewmodels.NoteActivityViewModel.ORIGINAL_NOTE_TITLE";
    public static final String ORIGINAL_NOTE_ID="com.codejar.notekeeper.viewmodels.NoteActivityViewModel.ORIGINAL_NOTE_ID";
    public static final String ORIGINAL_NOTE_TEXT="com.codejar.notekeeper.viewmodels.NoteActivityViewModel.ORIGINAL_NOTE_TEXT";

    public void saveState(Bundle outState) {
        Log.d(TAG, "saveState: ");
        outState.putString(ORIGINAL_COURSE_ID, mOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mOriginalNoteText);
        outState.putInt(ORIGINAL_NOTE_ID, mOriginalNoteId);
    }

    public void restoreState(Bundle inState) {
        Log.d(TAG, "restoreState: ");
        mOriginalCourseId= inState.getString(ORIGINAL_COURSE_ID);
        mOriginalNoteTitle= inState.getString(ORIGINAL_NOTE_TITLE);
        mOriginalNoteText= inState.getString(ORIGINAL_NOTE_TEXT);
        mOriginalNoteId=inState.getInt(ORIGINAL_NOTE_ID);
    }
}
