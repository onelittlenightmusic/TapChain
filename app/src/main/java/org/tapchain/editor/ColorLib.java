package org.tapchain.editor;

import java.util.HashMap;

import org.tapchain.core.LinkType;

public class ColorLib {
	public enum ColorCode {
		CLEAR, RED, GREEN, YELLOW, BLUE;
	}
	
	static HashMap<LinkType, ColorCode> colorMap =
			new HashMap<LinkType, ColorCode>() {
		{
			put(LinkType.PULL, ColorCode.BLUE);
			put(LinkType.PUSH, ColorCode.YELLOW);
			put(LinkType.FROM_PARENT, ColorCode.GREEN);
			put(LinkType.TO_CHILD, ColorCode.RED);
		}
	};
	
	public static ColorCode getLinkColor(LinkType al) {
		return colorMap.get(al);
	}
}
