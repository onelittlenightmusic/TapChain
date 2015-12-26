package org.tapchain.realworld;

import android.app.Activity;
import android.widget.GridView;

import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/27.
 */
public class ActorSelector extends GridView {
    ActorSelector(final Activity act, TapChainEditor.FACTORY_KEY key, int color) {
        super(act);
        setBackgroundColor(color);
        setColumnWidth(100);
        setVerticalSpacing(0);
        setHorizontalSpacing(0);
        setNumColumns(GridView.AUTO_FIT);
        setAdapter(new MainActivity.ViewAdapter(act, this, key));
    }
}
