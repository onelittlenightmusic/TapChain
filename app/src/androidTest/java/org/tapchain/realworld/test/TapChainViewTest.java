package org.tapchain.realworld.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.core.Actor;
import org.tapchain.core.LinkType;
import org.tapchain.editor.IActorTap;
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
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Star", 100, 300));
        sleepSecond();
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Mover"));
        assertTrue(true);
    }

    @Test
    public void testSomeFunction() {
//        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Number", 100, 300));
        sleepSecond();
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Multi", 250, 300));
        sleepSecond();
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Plus", 400, 200));
        assertTrue(true);
    }

    @Test
    public void top() {
//        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "Number", 100, 300));
        sleepSecond();
        Actor a;
        assertNotNull(
                a = view.add(FACTORY_KEY.ALL, "PushOut", 250, 300));
        sleepSecond();
        assertNotNull(
                view.add(FACTORY_KEY.ALL, "PassThru", 400, 400));
        sleepSecond();
        Actor b;
        assertNotNull(
                b = view.add(FACTORY_KEY.ALL, "Accumulate", 250, 500));
        sleepSecond();
        view.connect(a, LinkType.PULL, b);
        sleepSecond(10000);
        assertTrue(true);
    }

    @After
    public void end() {
        view.finishThisFromOutside();
        sleepSecond(2000);
    }

    public void sleepSecond() {
        sleepSecond(3000);
    }

    public void sleepSecond(int intervalMs) {
        try {
            sleep(intervalMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}