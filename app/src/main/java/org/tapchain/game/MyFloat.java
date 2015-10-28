package org.tapchain.game;


public class MyFloat implements IFloat {
	float f;
	public MyFloat() {
		f = 0f;
	}
	public MyFloat(Float f) {
		this.f = f;
	}
	public void set(Float fl) {
		f = fl;
	}
	public Float get() {
		return f;
	}
	
	@Override
	public String toString() {
		return Float.toString(f);
	}
}