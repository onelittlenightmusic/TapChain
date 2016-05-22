package org.tapchain.realworld;

import android.app.Activity;
import android.widget.GridView;

import org.tapchain.editor.TapChain;

/**
 * Created by hiro on 2015/12/27.
 */
public class SelectGridView extends GridView
{
    SelectGridView(final Activity act, TapChain.FACTORY_KEY key, int color) {
        super(act);
        setBackgroundColor(color);
        setColumnWidth(100);
        setVerticalSpacing(0);
        setHorizontalSpacing(0);
        setNumColumns(GridView.AUTO_FIT);
        setAdapter(new FactoryViewAdapter(act, this, key));
    }
}
