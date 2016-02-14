package org.tapchain.realworld;

import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2015/12/27.
 */
public class SelectGridView extends GridView
{
    SelectGridView(final Activity act, TapChainEditor.FACTORY_KEY key, int color) {
        super(act);
        setBackgroundColor(color);
        setColumnWidth(100);
        setVerticalSpacing(0);
        setHorizontalSpacing(0);
        setNumColumns(GridView.AUTO_FIT);
        setAdapter(new FactoryViewAdapter(act, this, key));
    }
}
