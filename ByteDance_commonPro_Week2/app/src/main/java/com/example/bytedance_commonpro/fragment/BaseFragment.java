package com.example.bytedance_commonpro.fragment;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

// BaseFragment.java
public abstract class BaseFragment extends Fragment {
    @Nullable
    protected abstract View getTopInsetView(); // 子类返回需要避让状态栏的 View

    protected void applyTopWindowInset(@NonNull View rootView) {
        View topView = getTopInsetView();
        if (topView == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            topView.setPadding(
                    topView.getPaddingLeft(),
                    systemBars.top, // ← 状态栏高度（刘海屏会更大）
                    topView.getPaddingRight(),
                    topView.getPaddingBottom()
            );
            return insets;
        });
    }
}
