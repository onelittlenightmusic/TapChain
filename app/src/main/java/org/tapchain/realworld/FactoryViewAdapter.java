package org.tapchain.realworld;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import org.tapchain.core.Actor;
import org.tapchain.core.ActorBlueprint;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprint;
import org.tapchain.editor.TapChainEditor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by hiro on 2016/01/02.
 */
public class FactoryViewAdapter extends ArrayAdapter<IBlueprint<Actor>> implements Factory.ValueChangeNotifier {
    private MainActivity act;
    private GridView _parent;
    Factory<Actor> f;

    public FactoryViewAdapter(Context c, GridView parent, TapChainEditor.FACTORY_KEY k) {
        super(c, R.layout.gridview_layout, ((MainActivity)c).getEditor().getFactory(k));
        act = (MainActivity) c;
        _parent = parent;
        f = ((MainActivity)c).getEditor().getFactory(k);
        act.getEditor().getFactory(k).setNotifier(this);
    }

    @Override
    public void notifyChange() {
        act.runOnUiThread(() -> {synchronized(f) {FactoryViewAdapter.this.notifyDataSetChanged();}});
    }

    @Override
    public void invalidate() {
        act.runOnUiThread(() -> {synchronized(f) {_parent.invalidate(); }});
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        IBlueprint<Actor> item = getItem(position);
        if (convertView == null
                || !convertView.getTag().equals(item.getTag())
                ) {
            convertView = new ActorImageButton(act, item);
        }
        return convertView;
    }
}
