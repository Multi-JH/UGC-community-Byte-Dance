package com.example.bytedance_commonpro.network;


import com.example.bytedance_commonpro.model.FeedResponse;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class NetworkManager {
    private static final String BASE_URL = "https://college-training-camp.bytedance.com/";
    //API 的基础地址
    private static ApiService apiService;
    //静态字段，避免重复创建

    public interface ApiService {
        @GET("feed/")
        Call<FeedResponse> getFeed(
                @Query("count") int count,
                @Query("accept_video_clip") boolean acceptVideoClip
                // 后端可根据 accept_video_clip 决定是否返回视频内容
        );
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            //创建全局唯一的ApiService实例，设置基础的URL，添加Gson转换器
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())//自动将JSON数据转换成对象
                    .build();
            apiService = retrofit.create(ApiService.class);//动态生成接口实现类
        }
        return apiService;
    }
}