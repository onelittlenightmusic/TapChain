package org.tapchain.realworld.test;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.tapchain.core.ActorManager;
import org.tapchain.core.IValue;
import org.tapchain.realworld.MainActivity;

import static java.lang.Thread.sleep;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
@RunWith(AndroidJUnit4.class)
public class TapChainLogicTest2 {
    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    MainActivity view;

    @Before
    public void setUp() throws Exception {
        view = mActivityRule.getActivity();
    }

    @Test
    public void testSomeFunction() {
        ActorManager e = view.getEditor().editTap();
        e.add((IValue<Integer> v) -> 1, 0)
                .pushTo((IValue<Integer> v, Integer i) -> i + v._get(), 1)
                .pushTo((IValue<Integer> v, Integer i) -> {
                    v._set(i);
                    Log.w("test", String.format("OK %d", i));
                }, 0)
                .save();
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