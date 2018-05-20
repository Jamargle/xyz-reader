package com.example.xyzreader.ui.details;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.Loader;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.xyzreader.R;
import com.example.xyzreader.ui.articles.ArticleListActivity;
import com.example.xyzreader.ui.viewcomponents.DrawInsetsFrameLayout;
import com.example.xyzreader.ui.viewcomponents.ObservableScrollView;
import com.example.xyzreader.utils.ArticleLoader;
import com.example.xyzreader.utils.ImageLoaderHelper;

import static com.example.xyzreader.utils.DateParsingUtil.getFormattedPublishedDateAfterStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.getFormattedPublishedDateBeforeStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.isPreviousStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.parsePublishedDate;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String ARG_ITEM_ID = "item_id";
    private static final String TAG = "ArticleDetailFragment";
    private static final float PARALLAX_FACTOR = 1.25f;
    private static final int DEFAULT_MUTED_COLOR = 0xFF333333;

    private Cursor cursor;
    private long itemId;
    private View rootView;
    private int mutedColor = DEFAULT_MUTED_COLOR;
    private ObservableScrollView scrollView;
    private DrawInsetsFrameLayout drawInsetsFrameLayout;
    private ColorDrawable statusBarColorDrawable;

    private int topInset;
    private View photoContainerView;
    private ImageView photoView;
    private int mScrollY;
    private boolean isCard = false;
    private int mStatusBarFullOpacityBottom;

    public static ArticleDetailFragment newInstance(final long itemId) {
        final ArticleDetailFragment fragment = new ArticleDetailFragment();
        final Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        fragment.setArguments(arguments);
        return fragment;
    }

    static float progress(
            final float v,
            final float min,
            final float max) {

        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(
            final float val,
            final float min,
            final float max) {

        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            itemId = getArguments().getLong(ARG_ITEM_ID);
        }

        isCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(
            @NonNull final LayoutInflater inflater,
            final ViewGroup container,
            final Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        initDrawInsetsLayout();
        initScrollView();
        initPhotoView();
        statusBarColorDrawable = new ColorDrawable(0);

        rootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (getActivity() == null) {
                    return;
                }
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();
        return rootView;
    }

    public int getUpButtonFloor() {
        if (photoContainerView == null || photoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return isCard
                ? (int) photoContainerView.getTranslationY() + photoView.getHeight() - mScrollY
                : photoView.getHeight() - mScrollY;
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(
            final int i,
            final Bundle bundle) {

        return ArticleLoader.newInstanceForItemId(getActivity(), itemId);
    }

    @Override
    public void onLoadFinished(
            @NonNull final Loader<Cursor> cursorLoader,
            final Cursor cursor) {

        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        this.cursor = cursor;
        if (this.cursor != null && !this.cursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            this.cursor.close();
            this.cursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Cursor> cursorLoader) {
        cursor = null;
        bindViews();
    }

    private void initDrawInsetsLayout() {
        drawInsetsFrameLayout = rootView.findViewById(R.id.draw_insets_frame_layout);
        drawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(final Rect insets) {
                topInset = insets.top;
            }
        });
    }

    private void initScrollView() {
        scrollView = rootView.findViewById(R.id.scrollview);
        scrollView.setCallback(new ObservableScrollView.Callback() {
            @Override
            public void onScrollChanged() {
                mScrollY = scrollView.getScrollY();
                if (getActivity() != null) {
                    ((ArticleDetailActivity) getActivity()).onUpButtonFloorChanged(
                            itemId,
                            ArticleDetailFragment.this);
                }
                photoContainerView.setTranslationY((int) (mScrollY - mScrollY / PARALLAX_FACTOR));
                updateStatusBar();
            }
        });
    }

    private void initPhotoView() {
        photoView = rootView.findViewById(R.id.photo);
        photoContainerView = rootView.findViewById(R.id.photo_container);
    }

    private void updateStatusBar() {
        int color = 0;
        if (photoView != null && topInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - topInset * 3,
                    mStatusBarFullOpacityBottom - topInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mutedColor) * 0.9),
                    (int) (Color.green(mutedColor) * 0.9),
                    (int) (Color.blue(mutedColor) * 0.9));
        }
        statusBarColorDrawable.setColor(color);
        drawInsetsFrameLayout.setInsetBackground(statusBarColorDrawable);
    }

    private void bindViews() {
        if (rootView == null) {
            return;
        }

        if (cursor != null) {
            rootView.setAlpha(0);
            rootView.setVisibility(View.VISIBLE);
            rootView.animate().alpha(1);
        } else {
            rootView.setVisibility(View.GONE);
        }

        bindTitleLabel();
        bindDateAndAuthorLabel();
        bindBodyView();
        bindImageView();
    }

    private void bindTitleLabel() {
        final TextView titleView = rootView.findViewById(R.id.article_title);
        if (cursor == null) {
            titleView.setText("N/A");
        } else {
            titleView.setText(cursor.getString(ArticleLoader.Query.TITLE));
        }
    }

    private void bindDateAndAuthorLabel() {
        final TextView bylineView = rootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        if (cursor == null) {
            bylineView.setText("N/A");
        } else {
            final String publishedDate = cursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            final String author = cursor.getString(ArticleLoader.Query.AUTHOR);

            if (isPreviousStartOfEpoch(parsePublishedDate(publishedDate))) {
                bylineView.setText(Html.fromHtml(
                        getFormattedPublishedDateBeforeStartOfEpoch(publishedDate)
                                + " by <font color='#ffffff'>"
                                + author
                                + "</font>"));
            } else {
                bylineView.setText(Html.fromHtml(
                        getFormattedPublishedDateAfterStartOfEpoch(publishedDate)
                                + "<br/>" + " by "
                                + author
                                + "</font>"));
            }
        }
    }

    private void bindBodyView() {
        final TextView bodyView = rootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (cursor == null) {
            bodyView.setText("N/A");
        } else {
            bodyView.setText(Html.fromHtml(
                    cursor.getString(ArticleLoader.Query.BODY)
                            .replaceAll("(\r\n|\n)", "<br />")));
        }
    }

    private void bindImageView() {
        if (cursor == null) {
            return;
        }
        ImageLoaderHelper.getInstance(getActivity()).getImageLoader().get(
                cursor.getString(ArticleLoader.Query.PHOTO_URL),
                new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(
                            final ImageLoader.ImageContainer imageContainer,
                            final boolean b) {

                        final Bitmap bitmap = imageContainer.getBitmap();
                        if (bitmap != null) {
                            final Palette p = new Palette.Builder(bitmap).maximumColorCount(12).generate();
                            mutedColor = p.getDarkMutedColor(DEFAULT_MUTED_COLOR);
                            photoView.setImageBitmap(imageContainer.getBitmap());
                            rootView.findViewById(R.id.meta_bar)
                                    .setBackgroundColor(mutedColor);
                            updateStatusBar();
                        }
                    }

                    @Override
                    public void onErrorResponse(VolleyError volleyError) {

                    }
                });
    }

}
