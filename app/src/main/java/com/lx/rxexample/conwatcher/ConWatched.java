package com.lx.rxexample.conwatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lx on 2017/9/14.
 */

public class ConWatched implements Watched{
    private List<Watcher> list = new ArrayList<>();

    @Override
    public void addWatcher(Watcher watcher) {
        list.add(watcher);
    }

    @Override
    public void removeWatcher(Watcher watcher) {
        list.remove(watcher);
    }

    @Override
    public void notifyWatcher(String str) {
        for (Watcher watcher : list) {
            watcher.upDate(str);
        }
    }
}
