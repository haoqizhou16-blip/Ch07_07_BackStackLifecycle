package com.example.ch0707backstacklifecycle;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

/**
 * Fragment B：用 replace + addToBackStack 加进来的。
 * 它的作用是①证明返回栈生效 ②在这里发数据，制造 A 的观察者回调差异。
 */
public class FragmentB extends BaseLogFragment {

    private TextView tvPostCount;
    private SharedViewModel viewModel;

    @Override
    protected String logName() {
        return "B";
    }

    @Override
    protected int layoutId() {
        return R.layout.fragment_b;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvPostCount = view.findViewById(R.id.tvPostCount);
        // Activity 作用域：拿到的和 A 是同一个 ViewModel 实例
        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        updateCounterText();

        view.findViewById(R.id.btnPost).setOnClickListener(v -> {
            viewModel.postOne();
            updateCounterText();
        });

        view.findViewById(R.id.btnBackToA).setOnClickListener(v -> {
            Log.d(TAG, "[B] 点击返回：popBackStack()，出栈回到 A");
            getParentFragmentManager().popBackStack();
        });
    }

    private void updateCounterText() {
        if (tvPostCount != null) {
            tvPostCount.setText(getString(R.string.post_count_format, viewModel.getCounter()));
        }
    }
}
