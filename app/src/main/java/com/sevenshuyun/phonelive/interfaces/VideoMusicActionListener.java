package com.sevenshuyun.phonelive.interfaces;

import com.sevenshuyun.phonelive.adapter.MusicAdapter;
import com.sevenshuyun.phonelive.bean.MusicBean;

/**
 * Created by cxf on 2018/12/7.
 */

public interface VideoMusicActionListener {
    void onPlayMusic(MusicAdapter adapter, MusicBean bean, int position);

    void onStopMusic();

    void onUseClick(MusicBean bean);

    void onCollect(MusicAdapter adapter, int musicId, int isCollect);
}
