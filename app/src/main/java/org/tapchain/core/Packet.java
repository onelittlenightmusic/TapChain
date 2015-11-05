package org.tapchain.core;


import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class Packet<T> implements IPacket<T>, Delayed {
	T obj = null;
	IPiece source = null;
	long delay = 0;
	String tag = "";

	Packet(T _obj, IPiece _source) {
		this(_obj, _source, 0);
	}

	Packet(T _obj, IPiece _source, long delayMs) {
		obj = _obj;
		source = _source;
		delay = System.currentTimeMillis() + delayMs;
	}

	@Override
	public T getObject() {
		return obj;
	}

	@Override
	public IPiece getSource() {
		return source;
	}

	@Override
	public long getDelay(TimeUnit unit) {
		return unit.convert(delay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
	}

	@Override
	public int compareTo(Delayed another) {
		long thisTime = delay;
		long thatTime = ((Packet)another).delay;
		return (thisTime > thatTime) ? 1 : ((thisTime == thatTime) ? 0 : -1);
	}

	static Packet Error = new Packet(null, null) {{ tag = "Error"; }};

    public String getTag() {
        return tag;
    }

    public void setTag(String _tag) {
        tag = _tag;
    }

}