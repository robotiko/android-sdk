package com.sensorberg.sdk.testUtils;

import com.sensorberg.sdk.internal.Clock;

public class NoClock implements Clock{
    public static final Clock CLOCK = new NoClock();

    @Override
    public long now() {
        return 0;
    }

    @Override
    public long elapsedRealtime() {
        return 0;
    }
}
