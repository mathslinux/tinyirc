package org.mathslinux.tinyirc;

import java.util.EventListener;

public interface IRCEventListener extends EventListener {
    public void onPrivmsg(String message);
    public void onLoginSuccess();
    public void onLoginFailed();
}