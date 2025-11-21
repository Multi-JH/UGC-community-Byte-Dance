package com.example.bytedance_commonpro.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.bytedance_commonpro.R;
import com.google.android.material.tabs.TabLayout;

public class HomeFragment extends Fragment {

    private TabLayout tabLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // 加载首页布局
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // 初始化视图，找到 TabLayout 等控件
        initViews(view);
        // 设置TabLayout， 配置 Tab 默认选中、监听器等
        setupTabLayout();

        return view;
    }

    private void initViews(View view) {
        // 获取TabLayout
        tabLayout = view.findViewById(R.id.tab_layout);
    }

    private void setupTabLayout() {

        // 设置默认选中"社区"
        TabLayout.Tab communityTab = tabLayout.getTabAt(3);
        if (communityTab != null) {
            communityTab.select();
        }

        // 设置Tab选择监听器
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // 当Tab被选中时调用
                // 这里可以处理选中状态的变化
                updateTabAppearance(tab, true);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // 当Tab取消选中时调用
                updateTabAppearance(tab, false);
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // 当已选中的Tab再次被点击时调用,这里可以处理刷新等操作
            }
        });
    }
    private void updateTabAppearance(TabLayout.Tab tab, boolean isSelected) {
        // 这里可以自定义Tab选中和未选中的样式
        // 由于需求是无需支持点击，所以这里主要是视觉上的处理
        //获取当前 Tab 的 自定义视图（Custom View），没有getCustomView() 返回 null。
        View customView = tab.getCustomView();

        if (customView instanceof ImageView) {
            ImageView iconView = (ImageView) customView;
            int color;
            if (isSelected) {
                // 选中状态下的图标颜色
                color = ContextCompat.getColor(requireContext(), R.color.text_selected);
            } else {
                // 未选中状态下的图标颜色
                color = ContextCompat.getColor(requireContext(), android.R.color.darker_gray);
            }
            iconView.setColorFilter(color);
        } else {
            // 如果是文本Tab
            // TabLayout会自动处理文本颜色的变化
        }
    }
}
