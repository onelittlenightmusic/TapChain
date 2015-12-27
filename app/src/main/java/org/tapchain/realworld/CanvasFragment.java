package org.tapchain.realworld;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tapchain.TapChainAndroidEditor;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/26.
 */
public class CanvasFragment extends Fragment {
    MainActivity.CanvasViewImpl2 view;
    MainActivity act;
    static String CANVAS = "Canvas";
    TapChainEditor editor;

    public CanvasFragment() {
        super();
//        setRetainInstance(true);
    }


    public CanvasFragment setContext(MainActivity a) {
        this.act = a;
        if(view == null) {
            view = act.new CanvasViewImpl2(act);
            if(editor == null) {
                editor = new TapChainAndroidEditor(view, act.getResources(), act);
                editor.kickTapDraw(null);
            }
            view.setEditor(editor);
        }
//        view.onAdd(TapChainEditor.FACTORY_KEY.ALL, "Number", 100, 300);
        return this;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view == null) {
            view = act.new CanvasViewImpl2(act);
            if(editor == null) {
                editor = new TapChainAndroidEditor(view, act.getResources(), act);
                editor.kickTapDraw(null);
            }
            view.setEditor(editor);
        }
        return this.view;
    }

    public static CanvasFragment getCanvas(MainActivity act) {
        CanvasFragment f = (CanvasFragment) act.getFragmentManager()
                .findFragmentByTag(CANVAS);
        return f;
    }
}
