package org.tapchain;

import android.content.Intent;

import org.tapchain.core.ChainException;

public interface IntentHandler {
	public void onIntent(int resultCode, Intent data) throws ChainException;
}