package com.example.ch0707backstacklifecycle;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

/**
 * 宿主 Activity：只放一个容器 + 观察返回栈深度。
 *
 * 首次添加 A 时故意"不入栈"，所以 A 上按返回键会直接退出 Activity；
 * 从 A 跳到 B 时 addToBackStack("B")，栈深度变 1，返回键才由 FragmentManager 拦截并回退。
 */
public class MainActivity extends AppCompatActivity {

    private TextView tvStackDepth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(BaseLogFragment.TAG, "[Activity] onCreate");
        setContentView(R.layout.activity_main);

        tvStackDepth = findViewById(R.id.tvStackDepth);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            FragmentManager fm = getSupportFragmentManager();
            int depth = fm.getBackStackEntryCount();
            String top = depth > 0
                    ? fm.getBackStackEntryAt(depth - 1).getName()
                    : "（空，返回键将退出 Activity）";
            Log.d(BaseLogFragment.TAG, "[FragmentManager] 返回栈深度 = " + depth + "，栈顶 = " + top);
            tvStackDepth.setText(getString(R.string.stack_depth_format, depth) + "　栈顶：" + top);
        });

        if (savedInstanceState == null) {
            Log.d(BaseLogFragment.TAG, "[Activity] 首次添加 Fragment A（不入栈）");
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, new FragmentA(), "A")
                    .commit();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(BaseLogFragment.TAG, "[Activity] onDestroy");
    }
}
