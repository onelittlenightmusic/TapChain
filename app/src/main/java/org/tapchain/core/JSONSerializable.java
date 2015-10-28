package org.tapchain.core;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by hiro on 2015/09/24.
 */
public interface JSONSerializable {
    JSONObject toJSON() throws JSONException;
}
