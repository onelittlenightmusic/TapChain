package org.tapchain.realworld.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.editor.TapChainEditor.FACTORY_KEY;
import org.tapchain.realworld.MainActivity;

import static java.lang.Thread.sleep;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    MainActivity view;

    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Before
    public void start() {
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        view = getActivity();
    }

//    @Test
//    public void testSomeFunction2() {
////        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
//        assertNotNull(
//                view.add(FACTORY_KEY.ALL, "Star", 100, 300));
//        sleepSecond();
//        assertNotNull(
//                view.add(FACTORY_KEY.ALL, "Mover"));
//        assertTrue(true);
//    }
//
//    @Test
//    public void testSomeFunction() {
////        onView(withId(TapChainView.tapOffset+1)).perform(ViewActions.scrollTo());
//        assertNotNull(
//                view.add(FACTORY_KEY.ALL, "Number", 100, 300));
//        sleepSecond();
//        assertNotNull(
//                view.add(FACTORY_KEY.ALL, "Multi", 250, 300));
//        sleepSecond();
//        assertNotNull(
//                view.add(FACTORY_KEY.ALL, "Plus", 400, 200));
//        assertTrue(true);
//    }

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
//        try {
////            Log.w("test", ((ParameterizedType)a.getClass().getMethod("getInputDummy").getGenericReturnType()).getRawType().toString());
//            Log.w("test", a.getClass().getMethod("getOutputDummy").getGenericReturnType().toString());
//            Log.w("test", a.getClass().getMethod("getParentDummy").getGenericReturnType().toString());
//            Log.w("test", a.getClass().getMethod("getValueDummy").getGenericReturnType().toString());
//            Log.w("test", Actor.getParameters(a.getClass(), Actor.Controllable.class).toString());
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        }
//        runAction((ta, tb) -> 1, "A", new Object(){});
        sleepSecond(10000);
        assertTrue(true);
    }

    @After
    public void end() {
        if (view != null)
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

    interface TwoArgumentFunction<T, K, V> {
        T f(K arg1, V arg2);
    }
    public <T, K, V> T runAction(final TwoArgumentFunction<T, K, V> action, final K arg1, final V arg2) {
        return action.f(arg1, arg2);
    }
}