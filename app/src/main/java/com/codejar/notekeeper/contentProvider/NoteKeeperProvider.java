package com.codejar.notekeeper.contentProvider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import androidx.annotation.NonNull;

import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.CourseIdColumn;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Courses;
import com.codejar.notekeeper.contentProvider.NoteKeeperProviderContract.Notes;
import com.codejar.notekeeper.data.NoteKeeperContract.CourseInfoEntry;
import com.codejar.notekeeper.data.NoteKeeperContract.NoteInfoEntry;
import com.codejar.notekeeper.data.NotekeeperOpenHelper;

public class NoteKeeperProvider extends ContentProvider {
    private static final String TAG = NoteKeeperProvider.class.getSimpleName();
    private static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    private NotekeeperOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;
    public static final int NOTES = 1;
    private static final int NOTES_EXPANDED = 2;

    public static final int NOTE = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH + "/#", NOTE);
    }

    public NoteKeeperProvider() {
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        Log.d(TAG, "delete: ");
        long affectedRows = -1;
        SQLiteDatabase writableDatabase = mDbOpenHelper.getWritableDatabase();
        int uriMatcher = sUriMatcher.match(uri);
        switch (uriMatcher) {
            case NOTES:
                affectedRows = writableDatabase.delete(NoteInfoEntry.TABLE_NAME, selection,selectionArgs);
                break;
            case COURSES:
                affectedRows = writableDatabase.delete(CourseInfoEntry.TABLE_NAME, selection,selectionArgs);
                break;
            case NOTES_EXPANDED:
                // this table is read only
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + uriMatcher);
        }
        return getAffectedRows(affectedRows);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        Log.d(TAG, "getType: ");
        int matches = sUriMatcher.match(uri);
        String mimeType;
        switch (matches) {
            case COURSES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE +Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Notes.PATH_EXPANDED;
                break;
            case NOTE:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + MIME_VENDOR_TYPE + Courses.PATH;
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + matches);
        }
        return mimeType;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        Log.d(TAG, "insert: ");
        Uri rowUri = null;
        long rowId;
        SQLiteDatabase writableDatabase = mDbOpenHelper.getWritableDatabase();
        int uriMatcher = sUriMatcher.match(uri);
        switch (uriMatcher) {
            case NOTES:
                rowId = writableDatabase.insert(NoteInfoEntry.TABLE_NAME, null, values);
                rowUri = getRowUri(rowId, Notes.CONTENT_URI);
                break;
            case COURSES:
                rowId = writableDatabase.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = getRowUri(rowId, Courses.CONTENT_URI);
                break;
            case NOTES_EXPANDED:
                // this table is read only
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + uriMatcher);
        }
        return rowUri;
    }

    private Uri getRowUri(long rowId, Uri contentUri) {
        Log.d(TAG, "getRowUri: ");
        return ContentUris.withAppendedId(contentUri, rowId);
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NotekeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "query: ");
        Cursor cursor = null;
        SQLiteDatabase readableDatabase = mDbOpenHelper.getReadableDatabase();
        int matches = sUriMatcher.match(uri);
        switch (matches) {
            case COURSES:
                cursor = readableDatabase.query(CourseInfoEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES:
                cursor = readableDatabase.query(NoteInfoEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = getExpandedNotes(readableDatabase, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTE:
                long rowId = ContentUris.parseId(uri);
                String criteria = NoteInfoEntry._ID + " = ?";
                String[] criteriaArgs = {Long.toString(rowId)};
                cursor = readableDatabase.query(NoteInfoEntry.TABLE_NAME, projection,
                        criteria, criteriaArgs, null, null, null);
                break;
        }
        if (cursor != null) {
            cursor.moveToNext();
        }
        return cursor;
    }

    private Cursor getExpandedNotes(SQLiteDatabase readableDatabase, String[] projection,
                                    String selection, String[] selectionArgs, String sortOrder) {
        Log.d(TAG, "getExpandedNotes: ");

        String jointTable = NoteInfoEntry.TABLE_NAME + " JOIN " +
                CourseInfoEntry.TABLE_NAME + " ON " +
                NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

        String[] columns = new String[projection.length];

        for (int idx = 0; idx < projection.length; idx++) {
            columns[idx] = projection[idx].equals(BaseColumns._ID)
                    || projection[idx].equals(CourseIdColumn.COLUMN_COURSE_ID)
                    ? NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }

        return readableDatabase.query(jointTable, columns, selection,
                selectionArgs, null, null, sortOrder);
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        Log.d(TAG, "update: ");
        long affectedRows = -1;
        SQLiteDatabase writableDatabase = mDbOpenHelper.getWritableDatabase();
        int uriMatcher = sUriMatcher.match(uri);
        switch (uriMatcher) {
            case NOTES:
                affectedRows = writableDatabase.update(NoteInfoEntry.TABLE_NAME, values,selection,selectionArgs);
                break;
            case COURSES:
                affectedRows = writableDatabase.update(CourseInfoEntry.TABLE_NAME, values,selection,selectionArgs);
                break;
            case NOTES_EXPANDED:
                // this table is read only
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + uriMatcher);
        }
        return getAffectedRows(affectedRows);
    }

    private int getAffectedRows(long affectedRows) {
        Log.d(TAG, "getAffectedRows: ");
        return (int) affectedRows;
    }
}
