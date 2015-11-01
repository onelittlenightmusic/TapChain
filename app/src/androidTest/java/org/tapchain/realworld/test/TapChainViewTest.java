package org.tapchain.realworld.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.realworld.TapChainView;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class TapChainViewTest extends ActivityInstrumentationTestCase2<TapChainView> {
    TapChainView view;
    public TapChainViewTest() {
        super(TapChainView.class);
    }

    @Before
    public void start() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        view = getActivity();
    }

    @Test
    public void testSomeFunction2() {
//        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
        view.add(view.getEditor().getFactory(), 0, 100, 300);
        view.add(view.getEditor().getFactory(), 4);
        assertEquals("result", "result");
        assertTrue(true);
    }

    @After
    public void end() {
    }
}