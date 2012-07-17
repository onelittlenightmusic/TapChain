package org.tapchain.core;

import android.content.Intent;

public interface IntentHandler {
	public void onIntent(int resultCode, Intent data);
}