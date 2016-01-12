package org.tapchain.realworld;

import org.tapchain.IntentHandler;

/**
 * Created by hiro on 2016/01/05.
 */
public interface IIntentHandler {
    void addIntentHandler(int requestCode, IntentHandler h);
}
