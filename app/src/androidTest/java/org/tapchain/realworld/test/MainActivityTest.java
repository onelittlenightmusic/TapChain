package org.tapchain.realworld.test;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.realworld.MainActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import org.tapchain.realworld.R;
import org.tapchain.realworld.SelectGridView;

import static android.support.test.espresso.matcher.ViewMatchers.withTagValue;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void testSomeFunction() {
        onView(withId(R.id.menu_toggle)).perform(click()).perform(click()).perform(click()).perform(click()).perform(click()).perform(click());
//        onView(withTagValue(is((Object) "Generator"))).perform(click());
//        onView(withTagValue(is((Object) "Filter"))).perform(click());
//        onView(withTagValue(is((Object) "Consumer"))).perform(click());
    }

//    @Test
//    public void top() {
////        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
////        view.connect(a, LinkType.PULL, b);
////        onView(withId(R.id.dustbox)).perform(click()).perform(click()).perform(click()).perform(click()).perform(click()).perform(click());
////        onView(allOf(withId(10001), isDisplayed(), hasItem)).perform(click()).perform(click()).perform(click()).perform(click()).perform(click()).perform(click());
//        onData(anything()).inAdapterView(is(instanceOf(SelectGridView.class))).atPosition(0).perform(click());
////        onData(anything()).inAdapterView(withTagValue(equalTo("Generator"))).perform(click()).perform(click()).perform(click()).perform(click()).perform(click()).perform(click());
//    }



}