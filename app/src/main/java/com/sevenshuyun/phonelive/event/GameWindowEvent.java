package com.sevenshuyun.phonelive.event;

/**
 * Created by cxf on 2018/11/3.
 */

public class GameWindowEvent {
    private boolean mOpen;

    public GameWindowEvent(boolean open) {
        mOpen = open;
    }

    public boolean isOpen() {
        return mOpen;
    }
}
