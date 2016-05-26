package org.tapchain.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.tapchain.editor.ITapView;
import org.tapchain.editor.IActorTapView;
import org.tapchain.editor.IPathTapView;

public class TapLib {
	static Map<IPoint, List<IActorTapView>> map = new TreeMap<IPoint, List<IActorTapView>>();
	static Map<IPoint, List<IPathTapView>> mapPath = new TreeMap<IPoint, List<IPathTapView>>();

	public static Collection<IActorTapView> getTaps(IPoint pt) {
		return map.get(pt);
	}
	
	public static Collection<IActorTapView> getRangeTaps(IPoint min, IPoint max) {
		List<IActorTapView> rtn = new LinkedList<IActorTapView>();
		SortedMap<IPoint, List<IActorTapView>> s = ((TreeMap<IPoint, List<IActorTapView>>)map).subMap(min, max);
		float minx = min.x(), miny = min.y(), maxx = max.x(), maxy = max.y();
		for(Entry<IPoint, List<IActorTapView>> tList: s.entrySet()) {
			IPoint k = tList.getKey();
			float thisx = k.x(), thisy = k.y();
			if(thisx >= minx && thisx <= maxx && thisy >= miny && thisy <= maxy)
				rtn.addAll(tList.getValue());
		}
		return rtn;
	}
	
	public static void setTap(ITapView t) {
		if(t instanceof IActorTapView)
			setTap((IActorTapView)t);
		else if(t instanceof IPathTapView)
			setTap((IPathTapView)t);
	}
	
	public static synchronized void setTap(IActorTapView t1) {
		removeTap(t1);
		IPoint newKey = t1._get().copy();
		LinkedList<IActorTapView> tList = (LinkedList<IActorTapView>) map.get(newKey);
		if(tList == null) {
			tList = new LinkedList<IActorTapView>();
			map.put(newKey, tList);
		}
		tList.addFirst(t1);
		t1.setRecentPoint(newKey);
		Collection<IActorTapView> tList2 = t1.getAccessoryTaps();
		if(tList2 != null)
			for(IActorTapView t: tList2)
				setTap(t);
	}
	
	public static synchronized void removeTap(IActorTapView t1) {
		IPoint oldKey = t1.getRecentPoint();
		if(oldKey != null) {
			Collection<IActorTapView> tList = getTaps(oldKey);
			if(tList != null) {
				tList.remove(t1);
			}
		} else {
		}
	}

	public static Collection<ITapView> getAllSystemPieces() {
		LinkedList<ITapView> rtn = new LinkedList<ITapView>();
		for(List<IActorTapView> tList: map.values())
			rtn.addAll(tList);
		for(List<IPathTapView> tList: mapPath.values())
			rtn.addAll(tList);
		return rtn;
	}

	public static void setTap(IPathTapView tp) {
		removeTap(tp);
		IPoint newKey = tp._get().copy();
		LinkedList<IPathTapView> tList = (LinkedList<IPathTapView>) mapPath.get(newKey);
		if(tList == null) {
			tList = new LinkedList<IPathTapView>();
			mapPath.put(newKey, tList);
		}
		tList.addFirst(tp);
		tp.setRecentPoint(newKey);
	}

	public static void removeTap(IPathTapView tp) {
		IPoint oldKey = tp.getRecentPoint();
		if(oldKey != null) {
			Collection<IPathTapView> tList = getTapPath(oldKey);
			if(tList != null) {
				tList.remove(tp);
			}
		}
	}

	private static Collection<IPathTapView> getTapPath(IPoint oldKey) {
		return mapPath.get(oldKey);
	}
}
