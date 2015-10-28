package org.tapchain;

public enum PaletteSort {
	FACTORY(0), HISTORY(1), RELATIVES(2);
	int tabNum;
	PaletteSort(int n) {
		tabNum = n;
	}
	public int getNum() {
		return tabNum;
	}
}