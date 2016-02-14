package org.tapchain.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.tapchain.editor.ITap;
import org.tapchain.editor.IActorTap;
import org.tapchain.editor.IPathTap;

public class TapLib {
	static Map<IPoint, List<IActorTap>> map = new TreeMap<IPoint, List<IActorTap>>();
	static Map<IPoint, List<IPathTap>> mapPath = new TreeMap<IPoint, List<IPathTap>>();

	public static Collection<IActorTap> getTaps(IPoint pt) {
		return map.get(pt);
	}
	
	public static Collection<IActorTap> getRangeTaps(IPoint min, IPoint max) {
		List<IActorTap> rtn = new LinkedList<IActorTap>();
		SortedMap<IPoint, List<IActorTap>> s = ((TreeMap<IPoint, List<IActorTap>>)map).subMap(min, max);
		float minx = min.x(), miny = min.y(), maxx = max.x(), maxy = max.y();
		for(Entry<IPoint, List<IActorTap>> tList: s.entrySet()) {
			IPoint k = tList.getKey();
			float thisx = k.x(), thisy = k.y();
			if(thisx >= minx && thisx <= maxx && thisy >= miny && thisy <= maxy)
				rtn.addAll(tList.getValue());
		}
		return rtn;
	}
	
	public static void setTap(ITap t) {
		if(t instanceof IActorTap)
			setTap((IActorTap)t);
		else if(t instanceof IPathTap)
			setTap((IPathTap)t);
	}
	
	public static synchronized void setTap(IActorTap t1) {
		removeTap(t1);
		IPoint newKey = t1._get().copy();
		LinkedList<IActorTap> tList = (LinkedList<IActorTap>) map.get(newKey);
		if(tList == null) {
			tList = new LinkedList<IActorTap>();
			map.put(newKey, tList);
		}
		tList.addFirst(t1);
		t1.setRecentPoint(newKey);
		Collection<IActorTap> tList2 = t1.getAccessoryTaps();
		if(tList2 != null)
			for(IActorTap t: tList2)
				setTap(t);
	}
	
	public static synchronized void removeTap(IActorTap t1) {
		IPoint oldKey = t1.getRecentPoint();
		if(oldKey != null) {
			Collection<IActorTap> tList = getTaps(oldKey);
			if(tList != null) {
				tList.remove(t1);
			}
		} else {
		}
	}

	public static Collection<ITap> getAllSystemPieces() {
		LinkedList<ITap> rtn = new LinkedList<ITap>();
		for(List<IActorTap> tList: map.values())
			rtn.addAll(tList);
		for(List<IPathTap> tList: mapPath.values())
			rtn.addAll(tList);
		return rtn;
	}

	public static void setTap(IPathTap tp) {
		removeTap(tp);
		IPoint newKey = tp._get().copy();
		LinkedList<IPathTap> tList = (LinkedList<IPathTap>) mapPath.get(newKey);
		if(tList == null) {
			tList = new LinkedList<IPathTap>();
			mapPath.put(newKey, tList);
		}
		tList.addFirst(tp);
		tp.setRecentPoint(newKey);
	}

	public static void removeTap(IPathTap tp) {
		IPoint oldKey = tp.getRecentPoint();
		if(oldKey != null) {
			Collection<IPathTap> tList = getTapPath(oldKey);
			if(tList != null) {
				tList.remove(tp);
			}
		}
	}

	private static Collection<IPathTap> getTapPath(IPoint oldKey) {
		return mapPath.get(oldKey);
	}
}
