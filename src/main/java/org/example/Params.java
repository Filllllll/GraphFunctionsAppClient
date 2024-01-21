package org.example;

import org.json.JSONObject;

public enum Params {
    FUNC_NAME,
    T_EMIT,
    T_STEP,
    T_END,
    T_0,
    PARAM1,
    PARAM2,
    X,
    Y,
    Z;

    public static Double getDoubleValueOfJson(Params param, JSONObject json) {
        return Double.valueOf((String) (json.get(param.name())));
    }

    public static Float getFloatValueOfJson(Params param, JSONObject json) {
        return Float.valueOf((String) (json.get(param.name())));
    }
}