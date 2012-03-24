package org.tapchain;

import java.util.ArrayList;
import java.util.HashMap;

import org.tapchain.AnimationChain.*;
import org.tapchain.Chain.ChainException;
import org.tapchain.TapChainEditor.TapChainEditorView;

import android.util.Log;


public class PieceFactory {
	ArrayList<PieceBlueprint> collect;
	HashMap<Class<? extends BasicPiece>, ArrayList<BasicPiece>> items = new 
	HashMap<Class<? extends BasicPiece>, ArrayList<BasicPiece>>();
	TapChainEditor editor = null;
	public PieceFactory(TapChainEditor e) {
		collect = new ArrayList<PieceBlueprint>();
		editor = e;
//		addClassTest(test_cls.class);
	}
	public PieceFactory Register(PieceBlueprint pbp) {
		collect.add(pbp);
		return this;
	}
	public BasicPiece getInstance(int num) {
		PieceBlueprint pbp = collect.get(num);
		AnimationChainManager maker = editor.GetManager();
		AnimationChainManager usermaker = editor.GetUserManager();
		BasicPiece rtn = null;
		if(pbp == null)
			return null;
		try {
			rtn = pbp.newInstance(usermaker, this);
			rtn.setLogLevel(true);
			TapChainEditorView _view = getView(num, maker);
			if(_view != null)
				maker.setView(rtn, _view);
			usermaker.Save();
			maker.Save();
			return rtn;
		} catch (ChainException e) {
			maker.Error(e);
		}
		return rtn;
	}
	public int getSize() {
		return collect.size();
	}
	public String getName(int n) {
		if(n >= getSize())
			return null;
		return collect.get(n).getName();
	}
	public TapChainEditorView getView(int n, AnimationChainManager maker) {
		if(n >= getSize()) {
			Log.w("PieceFactory", "Size Over, "+String.valueOf(n));
			return null;
		} else if(collect.get(n) == null) {
			Log.w("PieceFactory", "Class null, "+String.valueOf(n));
			return null;
		} else if(collect.get(n).view == null) {
			Log.w("PieceFactory", "View null, "+String.valueOf(n));
			return null;
		}
		try {
			return (TapChainEditorView) collect.get(n).view.newInstance(maker, this);
		} catch (ChainException e) {
			maker.Error(e);
		}
		return null;
	}
//	ArrayList<Class<?>> collect_test  = new ArrayList<Class<?>>();
//	public PieceFactory addClassTest(Class<?> _cls) {
//		collect_test.add(_cls);
//		return this;
//	}
//	public Object getInstanceTest(int num) {
//		Class<?> c = collect_test.get(num);
//		if(c == null)
//			return null;
//		try {
//			return c.newInstance();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		}
//		return null;
//	}
//	public static class test_cls {
//		test_cls() {
//		}
//		String getname() {
//			return "this is test_cls";
//		}
//	}
}
