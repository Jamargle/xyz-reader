package com.example.xyzreader.ui.viewcomponents;

import android.content.Context;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public final class DynamicHeightNetworkImageView extends NetworkImageView {

    private float aspectRatio = 1.5f;

    public DynamicHeightNetworkImageView(final Context context) {
        super(context);
    }

    public DynamicHeightNetworkImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public DynamicHeightNetworkImageView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setAspectRatio(final float aspectRatio) {
        this.aspectRatio = aspectRatio;
        requestLayout();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, (int) (measuredWidth / aspectRatio));
    }
}
