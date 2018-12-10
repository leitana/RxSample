package com.lx.rxexample.conwatcher;

/**
 * Created by lx on 2017/9/14.
 */

public interface Watched {
    public void addWatcher(Watcher watcher);

    public void removeWatcher(Watcher watcher);

    public void notifyWatcher(String str);
}
