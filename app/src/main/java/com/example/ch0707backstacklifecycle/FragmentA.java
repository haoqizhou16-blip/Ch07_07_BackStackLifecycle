package com.example.ch0707backstacklifecycle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

/**
 * Fragment A：返回栈的实验对象。
 *
 * 本页同时注册两个观察者，唯一区别是 LifecycleOwner：
 *   ① observe(getViewLifecycleOwner(), …)  ← 正确：跟着"视图生命周期"走
 *   ② observe(this, …)                     ← 错误：跟着"Fragment 生命周期"走
 *
 * 两个都写在 onViewCreated() 里，所以每次视图重建都会执行一次注册。
 * 正确写法会随旧视图销毁而自动解绑；错误写法的旧观察者留在 LiveData 里不释放，
 * 于是"返回次数越多，同一条数据被回调的次数越多"。
 */
public class FragmentA extends BaseLogFragment {

    private TextView tvLastMessage;
    private SharedViewModel viewModel;

    @Override
    protected String logName() {
        return "A";
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_a;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvLastMessage = view.findViewById(R.id.tvLastMessage);
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // ① 正确写法
        viewModel.onObserveRegistered("A[viewLifecycleOwner]");
        viewModel.getMessage().observe(getViewLifecycleOwner(), value -> {
            Log.d(TAG, "[A][viewLifecycleOwner] 收到 = " + value);
            if (tvLastMessage != null) {
                tvLastMessage.setText(value);
            }
        });

        // ② 错误写法（对照组）：this 是 Fragment，而不是视图
        viewModel.onObserveRegistered("A[this]");
        viewModel.getMessage().observe(this, value -> {
            View root = getView();
            Log.d(TAG, "[A][this] 收到 = " + value
                    + "，getView() = " + (root == null ? "null（视图已销毁，仍在回调）" : "非空"));
        });

        view.findViewById(R.id.btnGoToB).setOnClickListener(v -> goToB());
        // 在 A 页也能自己发数据，方便现场对比两个观察者的回调次数
        view.findViewById(R.id.btnPost).setOnClickListener(v -> viewModel.postOne());
    }

    private void goToB() {
        Log.d(TAG, "[A] 点击跳转：replace + addToBackStack(\"B\")，A 的视图将被销毁");
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainer, new FragmentB(), "B")
                .addToBackStack("B")
                .commit();
    }
}
