package com.mssm.demoversion.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.mssm.demoversion.base.BaseApplication;

/**
 * @author Easyhood
 * @desciption 图片展示View
 * @since 2023/7/11
 **/
public class AdvanceImageView extends RelativeLayout {
    private ImageView imageView;

    public AdvanceImageView(Context context) {
        super(context);
        initView();
    }

    public AdvanceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AdvanceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }


    private void initView() {
        imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        addView(imageView, new RelativeLayout.LayoutParams(-1, -1));
    }

    public void setImage(String path) {
        if (getContext() == null || path == null) {
            return;
        }
        if (imageView != null && imageView.getContext() != null) {
            Glide.with(BaseApplication.getContext()).load(path).into(imageView);
        }
    }

}
