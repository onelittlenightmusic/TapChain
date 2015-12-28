package org.tapchain.realworld;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Created by hiro on 2015/12/29.
 */
public class FragmentFactory {
    public static Fragment create(MainActivity mainActivity, Class<? extends Fragment> fragmentClass, int id, String tag) {
        FragmentManager fm = mainActivity.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = fm.findFragmentByTag(tag);
        if(fragment == null) {
            fragment = Fragment.instantiate(mainActivity, fragmentClass.getName());
            ft.replace(id, fragment, tag);
        } else {
            ft.attach(fragment);
        }
        ft.commit();
        return fragment;
    }

}
