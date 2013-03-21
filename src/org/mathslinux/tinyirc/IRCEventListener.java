package org.mathslinux.tinyirc;

import java.util.EventListener;

public interface IRCEventListener extends EventListener {
    public void onPrivmsg(String message);
}