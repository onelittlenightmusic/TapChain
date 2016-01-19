package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tapchain.TapChainAndroidEditor;
import org.tapchain.core.Actor;
import org.tapchain.core.IPoint;
import org.tapchain.editor.EditorReturn;
import org.tapchain.editor.TapChainEditor;

/**
 * CanvasFragment controls creation of canvas view and canvas model.
 * CanvasFragment is set for retaining instance so that canvas model
 * is not recreated.
 * Created by hiro on 2015/12/26.
 */
public class CanvasFragment extends Fragment {
    CanvasViewImpl view;
//    Activity act;
    static String CANVAS = "Canvas";
    TapChainEditor editor;

    public CanvasFragment() {
        super();
    }

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
            view = new CanvasViewImpl(act);
            if(editor == null) {
                editor = new TapChainAndroidEditor(view, act.getResources(), act);

                try {
                    editor.editBlueprint()
                            .add((Class<? extends Actor>)Class.forName("org.tapchain.core.Actor$WordGenerator"), "A", false)
                            .setViewArg(getResources().getIdentifier("a" , "drawable", getActivity().getPackageName()))
                            .setTag("Word")
                            //                .setLogLevel()
                            .save();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }

                editor.invalidate();
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

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag) {
        return add(key, tag, null, null);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y) {
        return add(key, tag, view.getPosition(x, y), null);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, float x, float y, float vx, float vy) {
        return add(key, tag, view.getPosition(x, y), view.getVector(vx, vy));
    }


    public Actor add(TapChainEditor.FACTORY_KEY key, String tag, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = editor.addActorFromBlueprint(key, tag, pos);
        //TODO: resolve duplication
        IPoint resultPoint = editorReturn.getTap()._valueGet();
        if(!view.isInWindow(resultPoint.x(), resultPoint.y()))
            //Centering
            view._onFlingBackgroundTo(pos.x(), pos.y());

        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        view.onFling(editorReturn.getTap(), vec);
        return editorReturn.getActor();
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code) {
        return add(key, code, null, null);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y) {
        return add(key, code, view.getPosition(x, y), null);
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, float x, float y, float vx, float vy) {
        return add(key, code, view.getPosition(x, y), view.getVector(vx, vy));
    }

    public Actor add(TapChainEditor.FACTORY_KEY key, int code, IPoint pos, IPoint vec) {
        EditorReturn editorReturn = editor.addActorFromBlueprint(key, code, pos);

        //TODO: resolve duplication
        IPoint resultPoint = editorReturn.getTap()._valueGet();
        if(!view.isInWindow(resultPoint.x(), resultPoint.y()))
            //Centering
            view._onFlingBackgroundTo(pos.x(), pos.y());

        if (editorReturn == null)
            return null;
        if (vec == null)
            return editorReturn.getActor();
        view.onFling(editorReturn.getTap(), vec);
        return editorReturn.getActor();
    }

    public TapChainEditor getEditor() {
        return editor;
    }

    /**
     * Create CanvasFragment into android layout.
     * @param mainActivity Activity instance
     * @param id layout object id into which CanvasFragment is inserted
     */
    public static void create(MainActivity mainActivity, int id) {
        FragmentFactory.create(mainActivity, CanvasFragment.class, id, CANVAS);
    }
}
