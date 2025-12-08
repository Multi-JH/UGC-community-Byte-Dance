package com.example.bytedance_commonpro.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.bytedance_commonpro.R;

public class MessageFragment extends Fragment {
    /**
     * 生命周期（onCreateView） 创建Fragment的视图
     * 当Fragment需要绘制其用户界面时调用此方法
     * 这里加载并返回消息页面的布局文件
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //加载消息页面布局
        View view = inflater.inflate(R.layout.fragment_message, container, false);
        return view;
    }
}
