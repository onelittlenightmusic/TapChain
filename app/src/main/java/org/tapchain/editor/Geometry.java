package org.tapchain.editor;

import java.util.Arrays;
import java.util.TreeSet;
import org.tapchain.core.IPoint;

public class Geometry {
	TreeSet<IPoint> map = new TreeSet<IPoint>();
	public Geometry() {
	}
	
	public Geometry(IPoint... allocables) {
		this();
		addAllocables(allocables);
	}
	
	public Geometry addAllocables(IPoint... allocs) {
		map.addAll(Arrays.asList(allocs));
		return this;
	}
	
	public TreeSet<IPoint> getAllocables() {
		return map;
	}
	
	public boolean isAllocable(IPoint p) {
		return map.contains(p);
	}
}
