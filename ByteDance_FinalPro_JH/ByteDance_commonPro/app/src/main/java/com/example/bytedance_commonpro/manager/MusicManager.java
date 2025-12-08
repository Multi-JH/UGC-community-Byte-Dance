package com.example.bytedance_commonpro.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;

import com.example.bytedance_commonpro.model.Music;

import java.io.IOException;
/**
 * MusicManager 音乐管理器类
 * 负责管理应用中的背景音乐播放，提供全局音乐控制功能
 * 采用单例模式，确保整个应用中只有一个音乐播放器实例
 */
public class MusicManager {
    //单例实例，确保全局只有一个MusicManager
    private static MusicManager instance;
    //Android媒体播放器，用于播放音乐文件
    private MediaPlayer mediaPlayer;
    private boolean isMuted = false;
    private boolean isPrepared = false;
    private String currentMusicUrl = "";
    private SharedPreferences preferences;

    private Music currentMusic;
    /**
     * MusicManager(Context context)
     * 初始化音乐管理器和媒体播放器
     * 用于获取SharedPreferences
     */
    private MusicManager(Context context) {
        //初始化SharedPreferences，用于保存用户设置
        preferences = context.getSharedPreferences("music_prefs", Context.MODE_PRIVATE);
        isMuted = preferences.getBoolean("is_muted", false);
        //初始化媒体播放器
        initMediaPlayer();
    }

    /**
     * 获取MusicManager的单例实例（线程安全）
     * 使用synchronized确保多线程环境下只创建一个实例
     *
     * @param context 应用上下文
     * @return MusicManager的单例实例
     */
    public static synchronized MusicManager getInstance(Context context) {
        if (instance == null) {
            instance = new MusicManager(context);
        }
        return instance;
    }

    /**
     * initMediaPlayer() 初始化媒体播放器
     * 配置MediaPlayer的各种监听器和默认设置
     */
    private void initMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setLooping(true);

        //设置错误监听器，当播放器发生错误时调用
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            //发生错误时，将准备状态设为false，防止错误的播放
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                isPrepared = false;
                return false;
            }
        });

        //设置准备完成监听器，当音乐文件加载完成并准备好播放时调用
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                isPrepared = true;

                //根据静音状态设置音量
                if (isMuted) {
                    mediaPlayer.setVolume(0f, 0f);
                } else {
                    //使用当前音乐的原始音量设置
                    if (currentMusic != null) {
                        float volume = currentMusic.getVolume() / 100f;
                        mediaPlayer.setVolume(volume, volume);
                    } else {
                        mediaPlayer.setVolume(1f, 1f);
                    }
                }

                //设置播放位置，使用当前音乐的起始播放位置
                if (currentMusic != null) {
                    int seekTime = currentMusic.getSeekTime();
                    if (seekTime > 0) {
                        mediaPlayer.seekTo(seekTime);
                    }
                }
                //自动播放（如果非静音）
                if (!isMuted) {
                    mediaPlayer.start();
                }
            }
        });
    }

    /**
     * playMusic(Music music) 播放指定音乐
     * 设计理念：
     * - 如果已经是当前音乐，则恢复播放（不重新加载）
     * - 如果是不同音乐，则停止当前并加载新音乐
     */
    public void playMusic(Music music) {
        if (music == null || music.getUrl() == null || music.getUrl().isEmpty()) {
            return;
        }

        //如果已经是当前音乐，则恢复播放
        if (currentMusicUrl.equals(music.getUrl()) && isPrepared) {
            if (!isMuted && !mediaPlayer.isPlaying()) {
                //恢复播放
                mediaPlayer.start();
            }
            return;
        }
        //如果是不同的音乐，停止当前播放并设置新音乐
        stop();
        currentMusic = music;
        currentMusicUrl = music.getUrl();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(music.getUrl());
            mediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            isPrepared = false;
        }
    }
    /**
     * play() 恢复播放当前音乐
     * 如果音乐已准备好且未静音，则开始播放
     */
    public void play() {
        if (mediaPlayer != null && isPrepared && !isMuted) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }
    /**
     * pause() 暂停当前音乐的播放
     * 如果音乐正在播放，则暂停它
     */
    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }
    /**
     * stop() 停止当前音乐的播放
     * 停止播放并重置播放器状态
     */
    public void stop() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.reset();
            isPrepared = false;
        }
    }
    /**
     * change_Mute() 切换静音状态
     * 如果当前是静音，则取消静音并恢复播放
     * 如果当前不是静音，则设置为静音并暂停播放
     */
    public void change_Mute() {
        isMuted = !isMuted;

        //保存是否静音状态
        preferences.edit().putBoolean("is_muted", isMuted).apply();

        if (mediaPlayer != null && isPrepared) {
            if (isMuted) {
                mediaPlayer.setVolume(0f, 0f);
                pause();
            } else {
                //恢复音乐原始音量
                if (currentMusic != null) {
                    float volume = currentMusic.getVolume() / 100f;
                    mediaPlayer.setVolume(volume, volume);
                } else {
                    mediaPlayer.setVolume(1f, 1f);
                }
                play();
            }
        }
    }
    /**
     * isMuted() 获取当前是否静音
     */
    public boolean isMuted() {
        return isMuted;
    }
    /**
     * resetOnColdStart()
     * 在APP冷启动时重置音乐管理器
     * 清除所有状态，恢复到初始设置
     * 通常在应用重新启动时调用
     */
    public void resetOnColdStart() {
        isMuted = false;
        currentMusic = null;
        currentMusicUrl = "";
        preferences.edit().putBoolean("is_muted", false).apply();
        stop();
    }
}