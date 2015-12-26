package org.tapchain.realworld;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/26.
 */
public class CanvasFragment extends Fragment {
    MainActivity.CanvasViewImpl2 view;
    MainActivity act;
    String tag = "Canvas";

    public CanvasFragment() {
        super();
//        setRetainInstance(true);
    }


    public CanvasFragment setContext(MainActivity a) {
        this.act = a;
        view = act.new CanvasViewImpl2(act);
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
        if (view == null)
            view = act.new CanvasViewImpl2(act);
        return this.view;
    }

}
