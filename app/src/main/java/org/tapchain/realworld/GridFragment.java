package org.tapchain.realworld;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;

import org.tapchain.editor.TapChainEditor;

import java.util.ArrayList;

/**
 * Created by hiro on 2015/12/26.
 */
public class GridFragment extends Fragment {
    static final String VIEW_SELECT = "SELECT";
    GridShow show = GridShow.HIDE;
    int _width = ViewGroup.LayoutParams.MATCH_PARENT,
            _height = ViewGroup.LayoutParams.MATCH_PARENT;
    boolean autohide = false;
    ImageView ShowingDisabled;
    Activity act = null;
    TabHost tabH;
    ArrayList<TapChainEditor.FACTORY_KEY> factoryList = new ArrayList<>();

    public GridFragment() {
        super();
        setRetainInstance(true);
    }

    public GridFragment setContext(MainActivity a) {
        // Log.i("TapChain", "GridFragment#setContext called");
        this.act = a;
        return this;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {
        // Log.i("TapChain", "GridFragment#onCreateView called");
        LinearLayout tabView;
        HorizontalScrollView scrollTitle;
        TabWidget tabWidget;
        FrameLayout tabContent;
        FrameLayout darkMask;

        tabH = new TabHost(act, null);

        tabView = new LinearLayout(act);
        tabView.setOrientation(LinearLayout.VERTICAL);
        tabH.addView(tabView);

        scrollTitle = new HorizontalScrollView(act);
        tabView.addView(scrollTitle);

        // the tabhost needs a tabwidget, that is a container for the
        // visible tabs
        tabWidget = new TabWidget(act);
        tabWidget.setId(android.R.id.tabs);
        tabWidget.setPadding(0, 10, 0, 0);
        scrollTitle.addView(tabWidget);

        // the tabhost needs a frame layout for the views associated with
        // each visible tab
        tabContent = new FrameLayout(act);
        tabContent.setId(android.R.id.tabcontent);
        tabView.addView(tabContent, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // setup must be called if the tabhost is programmatically created.
        tabH.setup();
        addTab(tabH, "TS1", "[ + ]", TapChainEditor.FACTORY_KEY.ALL,
                0xaa000000, R.drawable.plus);
        addTab(tabH, "TS2", "[ V ]", TapChainEditor.FACTORY_KEY.LOG,
                0xaa220000, R.drawable.history);
        addTab(tabH, "TS3", "[ <=> ]", TapChainEditor.FACTORY_KEY.RELATIVES,
                0xaa000022, R.drawable.relatives);
        ImageView img = new ImageView(act);
        img.setImageDrawable(getResources()
                .getDrawable(R.drawable.pulldown));
        tabWidget.addView(img);
        tabWidget.getChildAt(3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GridFragment f = getGrid(act);
                if (f != null)
                    f.toggle();
            }
        });
        darkMask = new FrameLayout(act);
        darkMask.addView(tabH);
        darkMask.setLayoutParams(new FrameLayout.LayoutParams(_width,
                _height));
        ShowingDisabled = new ImageView(act);
        ShowingDisabled.setBackgroundColor(0x80000000);
        darkMask.addView(ShowingDisabled);
        enable();
        return darkMask;
    }

    public void addTab(TabHost h, String _tag, String label,
                       final TapChainEditor.FACTORY_KEY key, final int color, int resource) {
        TabHost.TabSpec ts = h.newTabSpec(_tag);
        ts.setIndicator(""/* label */, getResources().getDrawable(resource));
        ts.setContent(new TabHost.TabContentFactory() {
            public View createTabContent(String tag) {
                return new ActorSelector(act, key, color);
            }
        });
        // ts1.setContent(new Intent(this,Tab1.class));
        h.addTab(ts);
        factoryList.add(key);
        return;

    }


    public void setSize(int w, int h) {
        _width = w;
        _height = h;
        getView().setLayoutParams(
                new LinearLayout.LayoutParams(_width, _height));

    }

    public boolean contains(int rx, int ry) {
        int[] l = new int[2];
        getView().getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = getView().getWidth();
        int h = getView().getHeight();

        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }

    public void show(GridShow _show) {
        show = _show;
        FragmentTransaction ft = act.getFragmentManager()
                .beginTransaction();
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        if (this != act.getFragmentManager().findFragmentByTag(VIEW_SELECT)) {
            ft.replace(R.id.fragment2, this, VIEW_SELECT);
        }
        switch (_show) {
            case SHOW:
                setSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ft.show(this);
                break;
            case HALF:
                Pair<Integer, Integer> p1 = checkDisplayAndRotate();
                setSize(p1.first, p1.second);
                ft.show(this);
                break;
            case HIDE:
                ft.hide(this);
        }
        ft.commit();
    }

    public void show() {
        show(show);
    }

    public String getShowState() {
        return show.toString();
    }

    public boolean toggle() {
        show((show == GridShow.HIDE) ? GridShow.HALF : GridShow.HIDE);
        return show != GridShow.HIDE;
    }

    public void setAutohide() {
        autohide = !autohide;
    }

    public void kickAutohide() {
        if (autohide)
            show(GridShow.HIDE);
    }

    public void enable() {
        ShowingDisabled.setVisibility(View.INVISIBLE);
    }

    public TapChainEditor.FACTORY_KEY getCurrentFactory() {
        int tabNum = tabH.getCurrentTab();
        return factoryList.get(tabNum);
    }

    public void disable() {
        ShowingDisabled.setVisibility(View.VISIBLE);
    }

    public void setCurrentFactory(int tabNum) {
        tabH.setCurrentTab(tabNum);
    }

    public static GridFragment getGrid(Activity act) {
        // [APIv11]
        GridFragment f = (GridFragment) act.getFragmentManager()
                .findFragmentByTag(VIEW_SELECT);
        return f;
    }

    public Pair<Integer, Integer> checkDisplayAndRotate() {
        DisplayMetrics metrix = new DisplayMetrics();
        act.getWindowManager().getDefaultDisplay().getMetrics(metrix);
        if (metrix.widthPixels > metrix.heightPixels)
            return new Pair<>(metrix.widthPixels * 1 / 2,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        return new Pair<>(ViewGroup.LayoutParams.MATCH_PARENT,
                metrix.heightPixels * 1 / 2);
    }

}