package com.example.ch0707backstacklifecycle;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Activity 作用域的 ViewModel：A、B 两个 Fragment 共用同一个实例，
 * 所以在 B 里发数据，A 里的观察者也会收到（前提是观察者还活着）。
 *
 * 这里额外统计"累计注册次数"：凡是 observe() 就 +1，而这个计数**不会**因为视图销毁而减少。
 * 它和"实际回调次数"的差值，就是挂在 LiveData 上没被释放的观察者数量。
 */
public class SharedViewModel extends ViewModel {

    private static final String TAG = BaseLogFragment.TAG;

    private final MutableLiveData<String> message = new MutableLiveData<>();
    private int counter;
    /** 累计注册的观察者次数（只增不减） */
    private int observeCount;

    public LiveData<String> getMessage() {
        return message;
    }

    /** 每次 observe() 之前调一下，用来记录"注册了但可能永不释放"的观察者 */
    public void onObserveRegistered(String who) {
        observeCount++;
        Log.d(TAG, "[ViewModel] 注册观察者 " + who + "，累计注册次数 = " + observeCount);
    }

    public void postOne() {
        counter++;
        String value = "第 " + counter + " 条数据";
        Log.d(TAG, "[ViewModel] setValue(" + value + ")，历史累计注册观察者 = " + observeCount);
        message.setValue(value);
    }

    public int getCounter() {
        return counter;
    }

    public int getObserveCount() {
        return observeCount;
    }
}
