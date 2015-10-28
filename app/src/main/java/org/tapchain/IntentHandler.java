package org.tapchain;

import android.content.Intent;

import org.tapchain.core.Chain;

public interface IntentHandler {
	public void onIntent(int resultCode, Intent data) throws Chain.ChainException;
}