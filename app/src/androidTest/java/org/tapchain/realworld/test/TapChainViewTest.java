package org.tapchain.realworld.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.editor.TapChainEditor.FACTORY_KEY;
import org.tapchain.realworld.TapChainView;

import static java.lang.Thread.sleep;

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
        view.add(FACTORY_KEY.ALL, 0, 100, 300);
        sleepSecond();
        view.add(FACTORY_KEY.ALL, 4);
        assertTrue(true);
    }

    @Test
    public void testSomeFunction() {
//        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
        view.add(FACTORY_KEY.ALL, 13, 100, 300);
        sleepSecond();
        view.add(FACTORY_KEY.ALL, 14, 250, 300);
        sleepSecond();
        view.add(FACTORY_KEY.ALL, 26, 400, 200);
        assertTrue(true);
    }
    @After
    public void end() {
    }

    public void sleepSecond() {
        try {
            sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}