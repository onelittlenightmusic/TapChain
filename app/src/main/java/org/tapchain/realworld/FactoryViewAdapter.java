package org.tapchain.realworld;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.core.IBlueprint;
import org.tapchain.editor.TapChain;

/**
 * Created by hiro on 2016/01/02.
 */
public class FactoryViewAdapter extends ArrayAdapter<IBlueprint<Actor>> implements Factory.ValueChangeNotifier {
    private MainActivity act;
    private GridView _parent;
    Factory<Actor> f;

    public FactoryViewAdapter(Context c, GridView parent, TapChain.FACTORY_KEY k) {
        super(c, R.layout.gridview_layout);
        act = (MainActivity) c;
        _parent = parent;
        f = ((MainActivity)c).getTapChain().getFactory(k);
        addAll(f);
        act.getTapChain().getFactory(k).setNotifier(this);
    }

    @Override
    public void notifyChange() {
        act.runOnUiThread(() -> {
            clear();
            addAll(f);
            FactoryViewAdapter.this.notifyDataSetChanged();
    });
    }

    @Override
    public void invalidate() {
        act.runOnUiThread(() -> {
            clear();
            addAll(f);
            _parent.invalidate();
        });
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
