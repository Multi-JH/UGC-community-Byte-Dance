package com.example.bytedance_commonpro.network;

import com.example.bytedance_commonpro.model.FeedResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * NetworkManager
 * 网络请求管理类
 * 负责创建和配置Retrofit实例，提供API接口的访问方法
 */
public class NetworkManager {
    //服务器API的基础URL地址，所有网络请求都会基于这个地址进行拼接
    private static final String BASE_URL = "https://college-training-camp.bytedance.com/";
    //使用静态变量确保全局只有一个实例，避免重复创建浪费资源
    private static ApiService apiService;
    /**
     * Retrofit API服务接口定义
     * 声明所有可用的网络请求方法
     */
    public interface ApiService {
        @GET("feed/")
        Call<FeedResponse> getFeed(
                @Query("count") int count,
                @Query("accept_video_clip") boolean acceptVideoClip
        );
    }
    /**
     * getApiService()
     * 获取API服务接口实例（单例）
     * @return ApiService 可用于发起网络请求的接口实例
     */
    public static ApiService getApiService() {
        if (apiService == null) {
            //创建全局唯一的ApiService实例，设置基础的URL，添加Gson转换器
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    //添加Gson转换器工厂，用于自动将JSON响应转换为Java对象
                    .addConverterFactory(GsonConverterFactory.create())
                    //构建Retrofit实例
                    .build();
            //创建ApiService接口的实现类，Retrofit使用动态代理技术自动生成接口的实现
            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}