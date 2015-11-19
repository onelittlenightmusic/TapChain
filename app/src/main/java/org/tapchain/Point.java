package org.tapchain;

import org.tapchain.core.IPoint;
import org.tapchain.core.IValue;
import org.tapchain.core.WorldPoint;
import org.tapchain.core.IPoint.WPEffect;

public class Point implements IPoint {
		public float x, y;
		private WPEffect effect = WPEffect.POS;

		public Point() {
			super();
		}

		public Point(int _x, int _y) {
			x = _x;
			y = _y;
		}
		
		public Point(Point pt) {
			x = pt.x;
			y = pt.y;
			effect = pt.effect;
		}

		public Point(float x2, float y2) {
			x = x2;
			y = y2;
		}

		@Override
		public IPoint plusNew(IPoint pos) {
			if(pos==null) return this;
			return new Point(this).plus(pos.x(), pos.y());
		}
		@Override
		public IPoint plus(float dx, float dy) {
			x += dx;
			y += dy;
			return this;
		}
		@Override
		public IPoint plus(IPoint worldPoint) {
			return plus(worldPoint.x(), worldPoint.y());
		}
		public IPoint divide(float n) {
			return new Point(x()/n, y()/n);
		}
		@Override
		public IPoint multiplyNew(float a) {
			return new Point(this).multiply(a);
		}
		@Override
		public IPoint multiply(float a) {
			multiply(a, a);
			return this;
		}
		public IPoint multiply(float ax, float ay) {
			set(ax*x(), ay*y());
			return this;
		}
		public IPoint scaler(IPoint b) {
			return multiply(b.x(), b.y());
		}
		@Override
		public IPoint subNew(IPoint b) {
			if(b==null) return new Point(this);
			return new Point(this).sub(b);
		}
		
		public IPoint sub(IPoint b) {
			setRawDif(-b.x(), -b.y());
			return this;
		}
		@Override
		public String toString() {
			return String.format("%f/%f", x(), y());
		}

    @Override
    public IPoint setOffset(IValue<IPoint> pt) {
        return setOffset(pt, false);
    }

    @Override
		public float x() {
			return x;
		}

		@Override
		public float y() {
			return y;
		}

		@Override
		public IPoint setDif() {
			this.effect = WPEffect.DIF;
//			Log.w("test", String.format("%s setDif", toString()));
			return this;
		}

		@Override
		public IPoint unsetDif() {
			this.effect = WPEffect.POS;
			return this;
		}

		public WPEffect getEffect() {
			return effect;
		}
		
		private IPoint setDif(WPEffect effect) {
			if(effect == WPEffect.DIF)
				return this.setDif();
			return this.unsetDif();
		}
		
		public static IPoint minus(IPoint wp) {
			if(wp==null) return new Point();
			return new Point(-wp.x(), -wp.y());
		}
		
		public IPoint minus(float i, float j) {
			return subNew(new Point(i, j));
		}
		
		public float getAbs() {
			return (float) Math.sqrt(x() * x() + y() * y());
		}
		
		@Override
		public IPoint round(int denominator) {
			if(x() < 0)
				x -= denominator;
			x -= x() % denominator;
			if(y() < 0)
				y -= denominator;
			y -= y() % denominator;
			return this;
		}
		@Override
		public boolean equals(IPoint p) {
			return x() == p.x() && y() == p.y();
		}
		@Override
		public IPoint set(IPoint p) {
			switch(p.getEffect()) {
			case POS:
				set(p.x(), p.y());
				break;
			case DIF:
				setRawDif(p.x(), p.y());
				break;
			}
			return this;
		}
		
		public IPoint set(float px, float py) {
				setRaw(px,py);
			return this;
		}
		@Override
		public IPoint copy() {
			return new Point(this);
		}
		
		@Override
		public IPoint setOffset(IValue<IPoint> pt, boolean keep) {
			return this;
		}

		
		@Override
		public float rawX() {
			return x;
		}

		@Override
		public float rawY() {
			return y;
		}
		
		@Override
		public float theta() {
		    float angle = (float) Math.toDegrees(Math.atan2(x(), y()));

		    if(angle < 0){
		        angle += 360;
		    }
		    return angle;
		}

		@Override
		public IPoint unsetOffset(IValue<IPoint> pt, boolean keep) {
			return this;
		}

		public IPoint setRaw(float px, float py) {
			this.x = px;
			this.y = py;
			return this;
		}
		
		public IPoint setRawDif(float dx, float dy) {
			this.x += dx;
			this.y += dy;
			return this;
		}

		@Override
		public IPoint ein() {
			return this.multiply(1f/getAbs());
		}

		@Override
		public Point clear() {
			// TODO Auto-generated method stub
			return this;
		}
}
