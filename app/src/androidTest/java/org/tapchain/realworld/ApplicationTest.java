package org.tapchain.realworld;

import android.app.Application;
import android.test.ApplicationTestCase;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    // ★ 各テストケースを呼ぶ前にコールされるセットアップ関数
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // アクティビティを取得
//        getActivity();
    }
    public void testSomeFunction() {
//        Espresso.onView(ViewMatchers.withId(0x00001235));
        assertEquals("result", "result");
        assertTrue(true);
    }
}