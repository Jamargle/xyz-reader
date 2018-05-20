package com.example.xyzreader.ui.details;

import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.utils.ArticleLoader;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private Cursor cursor;
    private long startId;
    private long selectedItemId;
    private int selectedItemUpButtonFloor = Integer.MAX_VALUE;
    private int topInset;
    private ViewPager pager;
    private MyPagerAdapter pagerAdapter;
    private View upButton;

    private final ViewPager.SimpleOnPageChangeListener viewPagerListener = setViewPagerListener();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
        setContentView(R.layout.activity_article_detail);

        getSupportLoaderManager().initLoader(0, null, this);

        initViewPager();
        initUpButton();

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                startId = ItemsContract.Items.getItemId(getIntent().getData());
                selectedItemId = startId;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        pager.addOnPageChangeListener(viewPagerListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        pager.removeOnPageChangeListener(viewPagerListener);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(
            final int i,
            final Bundle bundle) {

        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(
            @NonNull final Loader<Cursor> cursorLoader,
            final Cursor cursor) {

        this.cursor = cursor;
        pagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (startId > 0) {
            this.cursor.moveToFirst();
            while (!this.cursor.isAfterLast()) {
                if (this.cursor.getLong(ArticleLoader.Query._ID) == startId) {
                    final int position = this.cursor.getPosition();
                    pager.setCurrentItem(position, false);
                    break;
                }
                this.cursor.moveToNext();
            }
            startId = 0;
        }
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Cursor> cursorLoader) {
        cursor = null;
        pagerAdapter.notifyDataSetChanged();
    }

    public void onUpButtonFloorChanged(
            final long itemId,
            final ArticleDetailFragment fragment) {

        if (itemId == selectedItemId) {
            selectedItemUpButtonFloor = fragment.getUpButtonFloor();
            updateUpButtonPosition();
        }
    }

    private ViewPager.SimpleOnPageChangeListener setViewPagerListener() {
        return new ViewPager.SimpleOnPageChangeListener() {

            @Override
            public void onPageSelected(final int position) {
                if (cursor != null) {
                    cursor.moveToPosition(position);
                    selectedItemId = cursor.getLong(ArticleLoader.Query._ID);
                }
                updateUpButtonPosition();
            }

            @Override
            public void onPageScrollStateChanged(final int state) {
                upButton.animate()
                        .alpha((state == ViewPager.SCROLL_STATE_IDLE) ? 1f : 0f)
                        .setDuration(300);
            }
        };
    }

    private void initViewPager() {
        pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        pager = findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        pager.setPageMarginDrawable(new ColorDrawable(0x22000000));
    }

    private void initUpButton() {
        upButton = findViewById(R.id.action_up);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                onSupportNavigateUp();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            final View upButtonContainer = findViewById(R.id.up_container);
            upButtonContainer.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @Override
                public WindowInsets onApplyWindowInsets(
                        final View view,
                        final WindowInsets windowInsets) {

                    view.onApplyWindowInsets(windowInsets);
                    topInset = windowInsets.getSystemWindowInsetTop();
                    upButtonContainer.setTranslationY(topInset);
                    updateUpButtonPosition();
                    return windowInsets;
                }
            });
        }
    }

    private void updateUpButtonPosition() {
        final int upButtonNormalBottom = topInset + upButton.getHeight();
        upButton.setTranslationY(Math.min(selectedItemUpButtonFloor - upButtonNormalBottom, 0));
    }

    private final class MyPagerAdapter extends FragmentStatePagerAdapter {

        MyPagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(
                final ViewGroup container,
                final int position,
                final Object object) {

            super.setPrimaryItem(container, position, object);
            final ArticleDetailFragment fragment = (ArticleDetailFragment) object;
            if (fragment != null) {
                selectedItemUpButtonFloor = fragment.getUpButtonFloor();
                updateUpButtonPosition();
            }
        }

        @Override
        public Fragment getItem(final int position) {
            cursor.moveToPosition(position);
            return ArticleDetailFragment.newInstance(cursor.getLong(ArticleLoader.Query._ID));
        }

        @Override
        public int getCount() {
            return (cursor != null) ? cursor.getCount() : 0;
        }

    }

}
