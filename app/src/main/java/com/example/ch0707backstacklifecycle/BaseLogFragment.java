package com.example.ch0707backstacklifecycle;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * 统一日志标签的基类 Fragment。
 *
 * 目的只有一个：把 Fragment 生命周期与"视图生命周期"逐条打印出来，
 * 让 A（在返回栈里）和 B（前台）的时序可以对照。
 *
 * 注意区分两组回调：
 *   Fragment 生命周期：onAttach → onCreate → onStart → onResume → onPause → onStop → onDestroy → onDetach
 *   视图生命周期：onCreateView → onViewCreated → ... → onDestroyView
 * 进返回栈时，视图被销毁（onDestroyView），但 Fragment 实例只是回到 CREATED，并没有被销毁。
 */
public abstract class BaseLogFragment extends Fragment {

    /** 全局统一日志标签，Logcat 里直接过滤这个词 */
    public static final String TAG = "BackStackLifecycle";

    /** 日志里的前缀，用来区分是哪个 Fragment */
    protected abstract String logName();

    /** 子类要加载的布局 */
    protected abstract int layoutId();

    protected void log(String event) {
        Log.d(TAG, "[" + logName() + "] " + event);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        log("onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        log("onCreate");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        log("onCreateView（视图开始创建）");
        return inflater.inflate(layoutId(), container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        log("onViewCreated（视图已就绪，可以绑定控件）");
    }

    @Override
    public void onStart() {
        super.onStart();
        log("onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        log("onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        log("onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        log("onStop");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 这一条是"视图生命周期结束"的标志：视图没了，但 Fragment 实例还在返回栈里
        log("onDestroyView（视图销毁，但 Fragment 实例仍在返回栈中）");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        log("onDetach");
    }
}
