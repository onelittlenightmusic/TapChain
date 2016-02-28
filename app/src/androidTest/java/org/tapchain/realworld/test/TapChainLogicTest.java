package org.tapchain.realworld.test;

import android.support.test.espresso.action.ViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.core.Actor;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IValue;
import org.tapchain.core.LinkType;
import org.tapchain.editor.TapChainEditor;
import org.tapchain.realworld.MainActivity;
import org.tapchain.realworld.R;
import org.tapchain.realworld.SelectGridView;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static java.lang.Thread.sleep;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class TapChainLogicTest {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    MainActivity view;

    @Before
    public void setUp() throws Exception {
        view = mActivityRule.getActivity();
    }

//    @Test
//    public void testSomeFunction() {
//        ActorManager e = view.getEditor().editTap();
//        e.add(() -> 1, 0)
//                .pushTo((IValue<Integer> v, Integer i) -> i + 1, 0)
//                .pushTo((IValue<Integer> v, Integer i) -> {
//                    v._set(i);
//                    Log.w("test", String.format("OK %d", i));
//                }, 0)
//                .save();
//    }

//    @Test
//    public void checkFibonacci() {
//        view.add(TapChainEditor.FACTORY_KEY.ALL, "Number", 100, 300);
//        sleepSecond();
//        Actor a;
//        a = view.add(TapChainEditor.FACTORY_KEY.ALL, "PushOut", 250, 300);
//        sleepSecond();
//        view.add(TapChainEditor.FACTORY_KEY.ALL, "PassThru", 400, 400);
//        sleepSecond();
//        Actor b;
//        b = view.add(TapChainEditor.FACTORY_KEY.ALL, "Accumulate", 250, 500);
//        sleepSecond();
//        view.link(a, LinkType.PULL, b);
//    }

    @Test
    public void checkFibonacci() {
        view.add(TapChainEditor.FACTORY_KEY.ALL, "Number", 100, 300);
        sleepSecond();
        Actor a;
        a = view.add(TapChainEditor.FACTORY_KEY.ALL, "PushOut", 250, 300);
        sleepSecond();
        view.add(TapChainEditor.FACTORY_KEY.ALL, "PassThru", 400, 400);
        sleepSecond();
        Actor b;
        b = view.add(TapChainEditor.FACTORY_KEY.ALL, "Accumulate", 250, 500);
        sleepSecond();
        view.connect(a, LinkType.PULL, b);
    }

    public void sleepSecond() {
        sleepSecond(1500);
    }

    public void sleepSecond(int intervalMs) {
        try {
            sleep(intervalMs);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}