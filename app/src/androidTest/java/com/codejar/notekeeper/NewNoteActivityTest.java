//package com.codejar.notekeeper;
//
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//
//import org.junit.BeforeClass;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//import static androidx.test.espresso.Espresso.onData;
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.Espresso.pressBack;
//import static androidx.test.espresso.action.ViewActions.click;
//import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
//import static androidx.test.espresso.action.ViewActions.typeText;
//import static androidx.test.espresso.assertion.ViewAssertions.matches;
//import static androidx.test.espresso.matcher.ViewMatchers.withId;
//import static androidx.test.espresso.matcher.ViewMatchers.withSpinnerText;
//import static androidx.test.espresso.matcher.ViewMatchers.withText;
//import static org.hamcrest.Matchers.allOf;
//import static org.hamcrest.Matchers.containsString;
//import static org.hamcrest.Matchers.equalTo;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.junit.Assert.assertEquals;
//
//
//@RunWith(AndroidJUnit4.class)
//public class NewNoteActivityTest {
//
//    static DataManager dm;
//
//    @BeforeClass
//    public static void setupClass(){
//        dm=DataManager.getInstance();
//    }
//
////    @Rule
////    public ActivityTestRule<NoteListActivity> mNewNoteTestRule=
////            new ActivityTestRule<>(NoteListActivity.class);
//    @Test
//    public void createNewNote(){
////     final ViewInteraction fabInteraction= onView(withId(R.id.fab));
////     fabInteraction.perform(click());
//
//        CourseInfo course = dm.getCourse("android_async");
//        String noteTitle ="ui typing test \n AsyncTask con ";
//        String noteText = "this is the content of ui typing test" +
//                " \n AsyncTask fails during configuration changes";
//
//
//     onView(withId(R.id.fab)).perform(click());
//     onView(withId(R.id.courses_spinner)).perform(click());
//     onData(allOf(instanceOf(CourseInfo.class),equalTo(course))).perform(click());
//     onView(withId(R.id.courses_spinner))
//             .check(matches(withSpinnerText(containsString(course.getTitle()))));
//     onView(withId(R.id.txt_note_title))
//             .perform(typeText(noteTitle))
//             .check(matches(withText(containsString(noteTitle))));
//     onView(withId(R.id.txt_note_text))
//             .perform(typeText(noteText),closeSoftKeyboard())
//             .check(matches(withText(containsString(noteText))));
//     pressBack();
//
//        int noteIndex=dm.getNotes().size()-1;
//        NoteInfo compareNote= dm.getNotes().get(noteIndex);
//        assertEquals(compareNote.getCourse(), course);
//        assertEquals(compareNote.getTitle(), noteTitle);
//        assertEquals(compareNote.getText(), noteText);
// }
//}