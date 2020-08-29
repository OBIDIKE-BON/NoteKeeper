//package com.codejar.notekeeper;
//
//import org.junit.Test;
//
//import static org.junit.Assert.*;
//
//public class DataManagerTest {
//
//    @Test
//    public void createNewNote() {
//        DataManager dm = DataManager.getInstance();
//        CourseInfo course = dm.getCourse("android_async");
//        String noteTitle ="AsyncTask con";
//        String noteText = "AsyncTask fails during configuration changes";
//
//        int noteIndex= dm.createNewNote();
//        NoteInfo note= dm.getNotes().get(noteIndex);
//        note.setCourse(course);
//        note.setTitle(noteTitle);
//        note.setText(noteText);
//
//        NoteInfo compareNote= dm.getNotes().get(noteIndex);
//        assertEquals(course, compareNote.getCourse());
//        assertEquals(noteTitle, compareNote.getTitle());
//        assertEquals(noteText, compareNote.getText());
//    }
//}