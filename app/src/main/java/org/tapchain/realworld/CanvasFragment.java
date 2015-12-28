package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tapchain.TapChainAndroidEditor;
import org.tapchain.core.Actor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.EditorReturn;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/26.
 */
public class CanvasFragment extends Fragment {
    CanvasViewImpl2 view;
//    Activity act;
    static String CANVAS = "Canvas";
    TapChainEditor editor;

    public CanvasFragment() {
        super();
    }


//    public CanvasFragment setContext(Activity a) {
//        this.act = a;
//        if(view == null) {
//            view = new CanvasViewImpl2(act);
//            if(editor == null) {
//                editor = new TapChainAndroidEditor(view, act.getResources(), act);
//                editor.kickTapDraw(null);
//            }
//            view.setEditor(editor);
//        }
////        view.onAdd(TapChainEditor.FACTORY_KEY.ALL, "Number", 100, 300);
//        return this;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Activity act = getActivity();
        if (view == null) {
            view = new CanvasViewImpl2(act);
            if(editor == null) {
                editor = new TapChainAndroidEditor(view, act.getResources(), act);
                editor.kickTapDraw(null);
            }
            view.setEditor(editor);
        }
        return this.view;
    }

    public static CanvasFragment getCanvas(Activity act) {
        CanvasFragment f = (CanvasFragment) act.getFragmentManager()
                .findFragmentByTag(CANVAS);
        return f;
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag) {
        return onAdd(key, tag, null, null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return onAdd(key, tag, view.getPosition(x, y), null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float vx, float vy) {
        return onAdd(key, tag, view.getPosition(x, y), view.getVector(vx, vy));
    }


    public Actor onAdd(TapChainEditor.FACTORY_KEY key, String tag, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = view.getEditor().onAdd(key, tag, pos);
        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        view.getEditor().onFling(editorReturn.getTap(), pos, vec);
        return editorReturn.getActor();
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code) {
        return onAdd(key, code, null, null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return onAdd(key, code, view.getPosition(x, y), null);
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float vx, float vy) {
        return onAdd(key, code, view.getPosition(x, y), view.getVector(vx, vy));
    }

    public Actor onAdd(TapChainEditor.FACTORY_KEY key, int code, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = view.getEditor().onAdd(key, code, pos);
        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        view.getEditor().onFling(editorReturn.getTap(), pos, vec);
        return editorReturn.getActor();
    }

    public TapChainEditor getEditor() {
        return editor;
    }

    public static void create(MainActivity mainActivity) {
        FragmentFactory.create(mainActivity, CanvasFragment.class, R.id.fragment, CANVAS);
    }
}
