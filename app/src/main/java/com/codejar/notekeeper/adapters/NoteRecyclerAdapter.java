package com.codejar.notekeeper.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codejar.notekeeper.R;
import com.codejar.notekeeper.activities.NoteActivity;
import com.codejar.notekeeper.data.NoteKeeperContract.CourseInfoEntry;
import com.codejar.notekeeper.data.NoteKeeperContract.NoteInfoEntry;

public class NoteRecyclerAdapter extends RecyclerView.Adapter<NoteRecyclerAdapter.ViewHolder> {

    private static final String TAG = NoteRecyclerAdapter.class.getSimpleName();
    private final Context mContext;
    Cursor mCursor;
    private final LayoutInflater mLayoutInflater;
    private int mNoteTitlePos;
    private int mCourseTitlePos;
    private int mNoteIdPos;

    public NoteRecyclerAdapter(Context context, Cursor cursor) {
        Log.d(TAG, "NoteRecyclerAdapter: ");
        mContext = context;
        mCursor = cursor;
        mLayoutInflater = LayoutInflater.from(mContext);
        populateColumnPositions();
    }

    private void populateColumnPositions() {
        Log.d(TAG, "populateColumnPositions: ");
        if (mCursor == null)
            return;
        ;
//            get column indexes from Cursor
        mNoteTitlePos = mCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mCourseTitlePos = mCursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_TITLE);
        mNoteIdPos = mCursor.getColumnIndex(NoteInfoEntry._ID);

    }

    public void changeCursor(Cursor cursor) {
        Log.d(TAG, "changeCursor: ");
        if (cursor != null){
        mCursor = cursor;
        populateColumnPositions();
        notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: ");
        View view = mLayoutInflater.inflate(R.layout.notes_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: "+position);
        mCursor.moveToPosition(position);
        String courseTitle = mCursor.getString(mCourseTitlePos);
        String noteTitle = mCursor.getString(mNoteTitlePos);
        long noteId =(int) mCursor.getInt(mNoteIdPos);

        holder.mCourseTitle.setText(courseTitle);
        holder.mNoteTitle.setText(noteTitle);
        holder.mId = noteId;
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: ");
        return mCursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public final TextView mCourseTitle;
        public final TextView mNoteTitle;
        public long mId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mCourseTitle = itemView.findViewById(R.id.txt_course);
            mNoteTitle = itemView.findViewById(R.id.txt_note);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "onClick: Note with id"+ mId);
                    Intent intent = new Intent(mContext, NoteActivity.class);
                    intent.putExtra(NoteActivity.NOTE_ID, mId);
                    mContext.startActivity(intent);
                }
            });
        }


    }
}
