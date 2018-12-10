package com.lx.rxexample.conwatcher;

/**
 * Created by lx on 2017/9/14.
 */

public class ConWatcher implements Watcher{
    @Override
    public void upDate(String str) {
        System.out.println(str);
    }
}
