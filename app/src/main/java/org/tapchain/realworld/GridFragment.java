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
    GridShowState show = GridShowState.HIDE;
    int _width = ViewGroup.LayoutParams.MATCH_PARENT,
            _height = ViewGroup.LayoutParams.MATCH_PARENT;
    boolean autohide = false;
    ImageView ShowingDisabled;
    TabHost tabH;
    ArrayList<TapChainEditor.FACTORY_KEY> factoryList = new ArrayList<>();

    public GridFragment() {
        super();
        setRetainInstance(true);
    }

//    public GridFragment setContext(MainActivity a) {
//        // Log.i("TapChain", "GridFragment#setContext called");
//        this.act = a;
//        return this;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle saved) {
        // Log.i("TapChain", "GridFragment#onCreateView called");
        Activity act = getActivity();
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
        addTab(tabH, "TS1", TapChainEditor.FACTORY_KEY.ALL,
                0xaa000000, R.drawable.plus);
        addTab(tabH, "TS2", TapChainEditor.FACTORY_KEY.LOG,
                0xaa220000, R.drawable.history);
        addTab(tabH, "TS3", TapChainEditor.FACTORY_KEY.RELATIVES,
                0xaa000022, R.drawable.relatives);
        ImageView img = new ImageView(act);
        img.setImageDrawable(getResources()
                .getDrawable(R.drawable.pulldown));
        tabWidget.addView(img);
        tabWidget.getChildAt(3).setOnClickListener(v -> toggle());
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

    public void addTab(TabHost h, String _tag,
                       final TapChainEditor.FACTORY_KEY key, final int color, int resource) {
        TabHost.TabSpec ts = h.newTabSpec(_tag);
        ts.setIndicator(""/* label */, getResources().getDrawable(resource));
        ts.setContent(tag -> {
            return new ActorSelector(getActivity(), key, color);
        });
        // ts1.setContent(new Intent(this,Tab1.class));
        h.addTab(ts);
        factoryList.add(key);

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

        return !(rx < x || rx > x + w || ry < y || ry > y + h);
    }

    public void show(GridShowState _show) {
        show = _show;
        FragmentTransaction ft = getActivity().getFragmentManager()
                .beginTransaction();
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

    public boolean toggle() {
        show((show == GridShowState.HIDE) ? GridShowState.HALF : GridShowState.HIDE);
        return show != GridShowState.HIDE;
    }

    public void kickAutohide() {
        if (autohide)
            show(GridShowState.HIDE);
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
        return (GridFragment) act.getFragmentManager()
                .findFragmentByTag(VIEW_SELECT);
    }

    public Pair<Integer, Integer> checkDisplayAndRotate() {
        DisplayMetrics metrix = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrix);
        if (metrix.widthPixels > metrix.heightPixels)
            return new Pair<>(metrix.widthPixels / 2,
                    ViewGroup.LayoutParams.MATCH_PARENT);
        return new Pair<>(ViewGroup.LayoutParams.MATCH_PARENT,
                metrix.heightPixels / 2);
    }

    public static void create(MainActivity mainActivity, int id) {
//        Fragment fragment =
                FragmentFactory.create(mainActivity, GridFragment.class, id, VIEW_SELECT);
//        if(fragment == null)
//            return;
//        GridFragment grid = (GridFragment)fragment;
//        grid.setContext(mainActivity);//.show(GridShow.HIDE);
    }
}
