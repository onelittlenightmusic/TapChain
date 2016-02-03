package org.tapchain.realworld;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import org.tapchain.core.Actor;
import org.tapchain.core.Factory;
import org.tapchain.editor.TapChainEditor;

/**
 * Created by hiro on 2016/01/02.
 */
public class FactoryViewAdapter extends BaseAdapter implements Factory.ValueChangeNotifier {
    private Factory<Actor> f;
    private TapChainEditor.FACTORY_KEY key;
    private MainActivity act;
    private GridView _parent;

    public FactoryViewAdapter(Context c, GridView parent, TapChainEditor.FACTORY_KEY k) {
        super();
        act = (MainActivity) c;
        key = k;
        _parent = parent;
        f = Factory.copy(act.getEditor().getFactory(key));
        act.getEditor().getFactory(key).setNotifier(this);
    }

    @Override
    public void notifyChange() {
        act.runOnUiThread(() -> {
            f = Factory.copy(act.getEditor().getFactory(key));
            FactoryViewAdapter.this.notifyDataSetChanged();
        });
    }

    @Override
    public void invalidate() {
        act.runOnUiThread(() -> {
            f = Factory.copy(act.getEditor().getFactory(key));
            _parent.invalidate();
        });
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null || convertView.getTag() == null
                || !convertView.getTag().equals(f.get(position).getTag())) {
            convertView = new ActorImageButton(act, f, key, position);
        }
        convertView.setId(300 + position);
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 300 + position;
    }

    @Override
    public Object getItem(int position) {
        return f.get(position).getTag();
    }

    @Override
    public int getCount() {
        return f.getSize();
    }
}
