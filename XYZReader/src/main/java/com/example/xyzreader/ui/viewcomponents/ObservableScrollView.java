/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.xyzreader.ui.viewcomponents;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * A custom ScrollView that can accept a scroll listener.
 */
public final class ObservableScrollView extends ScrollView {

    @Nullable
    private Callback callback;

    public ObservableScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        if (callback != null) {
            callback.onScrollChanged();
        }
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        super.onLayout(changed, l, t, r, b);
        final int scrollY = getScrollY();
        // hack to call onScrollChanged on screen rotate
        if (scrollY > 0 && callback != null) {
            callback.onScrollChanged();
        }
    }

    @Override
    public int computeVerticalScrollRange() {
        return super.computeVerticalScrollRange();
    }

    public void setCallback(final Callback listener) {
        callback = listener;
    }

    public interface Callback {

        void onScrollChanged();

    }

}