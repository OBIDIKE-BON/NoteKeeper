//package com.codejar.notekeeper;
//
//import androidx.test.espresso.action.ViewActions;
//import androidx.test.espresso.contrib.DrawerActions;
//import androidx.test.espresso.contrib.NavigationViewActions;
//import androidx.test.espresso.contrib.RecyclerViewActions;
//import androidx.test.ext.junit.runners.AndroidJUnit4;
//import androidx.test.rule.ActivityTestRule;
//
//import com.codejar.notekeeper.activities.MainActivity;
//
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//
//
//import java.util.List;
//
//import static androidx.test.espresso.Espresso.onView;
//import static androidx.test.espresso.action.ViewActions.*;
//import static androidx.test.espresso.matcher.ViewMatchers.*;
//import static androidx.test.espresso.assertion.ViewAssertions.*;
//import static org.hamcrest.core.IsNot.not;
//
//@RunWith(AndroidJUnit4.class)
//public class NextThroughTest {
//    @Rule
//    public ActivityTestRule<MainActivity> mTestRule =
//            new ActivityTestRule<>(MainActivity.class);
//
//    @Test
//    public void nextThroughNote() {
//        onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
//        onView(withId(R.id.nav_view))
//                .perform().perform(NavigationViewActions.navigateTo(R.id.nav_notes));
//        onView(withId(R.id.list_items))
//                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
//        List<NoteInfo> notes = DataManager.getInstance().getNotes();
//
//        for (int i = 0; i < notes.size(); ++i) {
//            NoteInfo note = notes.get(i);
//            onView(withId(R.id.courses_spinner))
//                    .check(matches(withSpinnerText(note.getCourse().getTitle())));
//            onView(withId(R.id.txt_note_title))
//                    .check(matches(withText(note.getTitle())));
//            onView(withId(R.id.txt_note_text))
//                    .check(matches(withText(note.getText())));
//            if (i < notes.size() - 1) {
//                onView(withId(R.id.action_next))
//                        .check(matches(isEnabled()));
//                onView(withId(R.id.action_next))
//                        .perform(click());
//            }
//        }
//        onView(withId(R.id.action_next))
//                .check(matches(not(isEnabled())));
//
//        ViewActions.pressBack();
//    }
//
//}