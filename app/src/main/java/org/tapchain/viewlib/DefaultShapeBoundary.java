package org.tapchain.viewlib;

import org.tapchain.core.IPoint;

public enum DefaultShapeBoundary implements IShapeBoundary {
	RECT, CIRCLE;

	@Override
	public boolean contains(IPoint ipoint, IPoint... ps) {
		boolean rtn = false;
		float ix = ipoint.x(), iy = ipoint.y();
		float cx = ps[0].x(), cy = ps[0].y(), rx = ps[1].x(), ry = ps[1].y();
		switch(this) {
		case CIRCLE:
			rtn = (ix - cx)*(ix - cx)/rx/rx+(iy - cy)*(iy - cy)/ry/ry > 1;
		case RECT:
			float left = cx - rx, right = cx + rx, top = cy - ry, bottom = cy + ry;
			rtn = !((left > ix) || (right < ix) || (top > iy) || (bottom < iy));
			break;
		default:
		}
		return false;
	}

}
