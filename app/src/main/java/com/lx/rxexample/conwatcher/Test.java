package com.lx.rxexample.conwatcher;

/**
 * Created by lx on 2017/9/14.
 */

public class Test {
    public static void main(String[] args) throws Exception{
        Watched watched = new ConWatched();

        Watcher watcher1 = new ConWatcher();
        Watcher watcher2 = new ConWatcher();
        Watcher watcher3 = new ConWatcher();

        watched.addWatcher(watcher1);
        watched.addWatcher(watcher2);
        watched.addWatcher(watcher3);

        watched.notifyWatcher("发送");
    }
}
