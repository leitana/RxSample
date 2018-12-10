package com.lx.rxexample;

import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.lx.rxexample.base.BaseActivity;
import com.orhanobut.logger.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends BaseActivity {
    protected final String TAG = this.getClass().getSimpleName();
    private Context mContext = MainActivity.this;

    @BindView(R.id.tv)
    TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Logger.init();
        ButterKnife.bind(this);
//        test1();
//        test2();
//        test3();
//        test4();
//        test5();
        test7();
    }

    private void test1() {
        //创建一个上游 Observable：被观察者
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                emitter.onNext(1);
                emitter.onNext(2);
                emitter.onNext(3);
                emitter.onComplete();
            }
        });
        //创建一个下游 Observer
        Observer<Integer> observer = new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {
                Logger.d("subscribe");
            }

            @Override
            public void onNext(Integer value) {
                Logger.d("next" + value);
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("error");
            }

            @Override
            public void onComplete() {
                Logger.d("complete");
            }
        };
        observable.subscribe(observer);
    }

    private void test2() {
        Observable<Integer> observable = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Logger.d("Observable thread is :" + Thread.currentThread().getName());
                Logger.d("emit 1");
                e.onNext(1);
            }
        });
        //Consumer只关心next发送的信息
        Consumer<Integer> consumer = new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Exception {
                Logger.d("Observer thread is :" + Thread.currentThread().getName());
                Logger.d("onNext :" + integer);
            }
        };

        //上下游默认是在同一个线程工作.
//        observable.subscribe(consumer);

        /**
         * 简单来说
         * subscribeOn() 指定的是上游发送事件的线程,
         * observeOn() 指定的是下游接收事件的线程.
         *
         *
         * Schedulers.io() 代表io操作的线程, 通常用于网络,读写文件等io密集型的操作
         * Schedulers.computation() 代表CPU计算密集型的操作, 例如需要大量计算的操作
         * Schedulers.newThread() 代表一个常规的新线程
         * AndroidSchedulers.mainThread() 代表Android的主线程
         */
        observable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(consumer);
    }

    /**
     * map操作符
     * 一对一转换
     */
    private void test3() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).map(new Function<Integer, String>() {
            @Override
            public String apply(Integer integer) throws Exception {
                return "map操作符" + integer;
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Logger.d(s);
            }
        });
    }

    /**
     * flapMap关键字
     * 先发射1,2,3.经过转换
     * 变成发射"1","2","3".
     * 这里不用map是因为
     *  因为flatmap能转化发射源，既“
     *  Observable<RegisterResponse>” -> "Observable<LoginResponse>"
     *  ,配合Retrofit就能在完成注册事件后继续完成登录事件。
     *  map操作符只能把“Observable<RegisterResponse>”
     *  里面的“RegisterResponse”转化成“LoginResponse”，
     *  而“LoginResponse”只是一个model对象，不能作为发射源完成登录操作
     */
    private void test4() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Logger.d("Integer：" + integer + "on" + Thread.currentThread().getName());
                    }
                }).observeOn(Schedulers.newThread())
                .flatMap(new Function<Integer, ObservableSource<String>>() {
                    @Override
                    public ObservableSource<String> apply(final Integer integer) throws Exception {
                        return Observable.create(new ObservableOnSubscribe<String>() {
                            @Override
                            public void subscribe(ObservableEmitter<String> e) throws Exception {
                                e.onNext("flatmap:" + integer);
                            }
                        });
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Logger.d(s);
                    }
                });
    }

    /**
     * zip操作符
     */
    private void test5() {
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                e.onNext(1);
                Logger.d("observable1:1");
//                Thread.sleep(1000);

                e.onNext(2);
                Logger.d("observable1:2");
//                Thread.sleep(1000);

                e.onNext(3);
                Logger.d("observable1:3");
//                Thread.sleep(1000);

                e.onNext(4);
                Logger.d("observable1:4");
//                Thread.sleep(1000);

                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable<String> observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                e.onNext("A");
                Logger.d("observable2:A");
//                Thread.sleep(1000);

                e.onNext("B");
                Logger.d("observable2:B");
//                Thread.sleep(1000);

                e.onNext("C");
                Logger.d("observable2:C");
//                Thread.sleep(1000);

                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>(){

            @Override
            public void onSubscribe(Disposable d) {
                Logger.d("onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Logger.d("onNext"+value);
            }

            @Override
            public void onError(Throwable e) {
                Logger.d("onError");
            }

            @Override
            public void onComplete() {
                Logger.d("onComplete");
            }
        });
    }
    private void test7() {
        try {
            //Return an AssetManager instance for your application's package
            InputStream is = mContext.getAssets().open("再别康桥.txt");
            int size = is.available();
            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            // Convert the buffer into a string.
            String text = new String(buffer, "utf-8");
            tv.setText(text);
            // Finally stick the string into the text view.
        } catch (IOException ex) {
            // Should never happen!
//            throw new RuntimeException(e);
            ex.printStackTrace();
        }
    }
//    private void test6(){
//        Flowable.create(new FlowableOnSubscribe<String>() {
//            @Override
//            public void subscribe(FlowableEmitter<String> e) throws Exception {
//
//            }
//        }, BackpressureStrategy.ERROR)
//    }
}
