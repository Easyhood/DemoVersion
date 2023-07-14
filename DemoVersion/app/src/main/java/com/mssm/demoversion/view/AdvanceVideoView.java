package com.mssm.demoversion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mssm.demoversion.util.cache.PreloadManager;
import com.bumptech.glide.request.RequestOptions;
import com.mssm.demoversion.R;

import xyz.doikki.videoplayer.player.VideoView;

/**
 * @author Easyhood
 * @desciption 视频展示View
 * @since 2023/7/11
 **/
public class AdvanceVideoView extends RelativeLayout {
    public ImageView imageView;
    private VideoView videoView;
    private RelativeLayout videoRela;
    private String path;
    public int currentPosition;


    public AdvanceVideoView(Context context) {
        super(context);
        initView();
    }

    public AdvanceVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AdvanceVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private PreloadManager mPreloadManager;

    private void initView() {
        videoRela = new RelativeLayout(getContext());
        addView(videoRela, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    public void setImage(String path, VideoView videoView, PreloadManager preloadManager) {
        this.path = path;
        this.videoView = videoView;
        this.mPreloadManager = preloadManager;
        Glide.with(getContext()).setDefaultRequestOptions(
                new RequestOptions()
                        .frame(0)
        ).load(path).into(imageView);
    }

    /**
     * 将View从父控件中移除
     */
    public static void removeViewFormParent(View v) {
        if (v == null) return;
        ViewParent parent = v.getParent();
        if (parent instanceof RelativeLayout) {
            ((RelativeLayout) parent).removeView(v);
        }
    }

    public void setVideo() {
//        if (videoView != null) {
//            videoView.release();
//            videoRela.removeView(videoView);
//            videoView = null;
//        }
        removeViewFormParent(videoView);
        videoView.release();
        videoView.setPlayerBackgroundColor(getContext().getResources().getColor(R.color.main_color));
        // videoView = new VideoView(getContext());
        videoView.setScreenScaleType(VideoView.SCREEN_SCALE_MATCH_PARENT);
//        HttpProxyCacheServer proxy = BaseApplication.getProxy(getContext());
//        String proxyUrl = proxy.getProxyUrl(path);
//        videoView.setVideoPath(proxyUrl);
        //videoView.setVideoPath(path);
        //  videoView.setBackgroundColor(Color.TRANSPARENT);
        String playUrl = mPreloadManager.getPlayUrl(path);
        videoView.setUrl(playUrl);
        LayoutParams layoutParams = new LayoutParams(-1, -1);
        //设置videoview占满父view播放
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        videoRela.addView(videoView, layoutParams);
        videoView.start();
//        videoView.setOnPreparedListener(mediaPlayer -> {
//            new Handler().postDelayed(() -> {
//                imageView.setVisibility(GONE);
//            }, 400);//防止videoview播放视频前有个闪烁的黑屏
//        });
    }

    public void setDestroy() {
        if (videoView != null) {
            videoView.release();
        }
    }

    public void setPause() {
        if (videoView != null) {
            videoView.pause();
            //currentPosition = videoView.getCurrentPosition();
            // imageView.setVisibility(VISIBLE);
        }
    }

    public void setRestart() {
        if (videoView != null) {
            videoView.resume();
            // videoView.seekTo(currentPosition);
            //videoView.start();
        }
    }


}
