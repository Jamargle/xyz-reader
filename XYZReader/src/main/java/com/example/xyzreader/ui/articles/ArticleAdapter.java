package com.example.xyzreader.ui.articles;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.xyzreader.R;
import com.example.xyzreader.ui.viewcomponents.DynamicHeightNetworkImageView;
import com.example.xyzreader.utils.ArticleLoader;
import com.example.xyzreader.utils.ImageLoaderHelper;

import static com.example.xyzreader.utils.DateParsingUtil.getFormattedPublishedDateAfterStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.getFormattedPublishedDateBeforeStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.isPreviousStartOfEpoch;
import static com.example.xyzreader.utils.DateParsingUtil.parsePublishedDate;

public final class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {

    private final Cursor cursor;
    private final OnArticleClickListener listener;

    ArticleAdapter(
            final Cursor cursor,
            final OnArticleClickListener listener) {

        this.cursor = cursor;
        this.listener = listener;
    }

    @Override
    public long getItemId(final int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull final ViewGroup parent,
            final int viewType) {

        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_article, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                listener.onArticleClicked(getItemId(holder.getAdapterPosition()));
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(
            final ViewHolder holder,
            final int position) {

        cursor.moveToPosition(position);

        holder.bindArticle(
                cursor.getString(ArticleLoader.Query.THUMB_URL),
                cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO),
                cursor.getString(ArticleLoader.Query.TITLE),
                cursor.getString(ArticleLoader.Query.AUTHOR),
                cursor.getString(ArticleLoader.Query.PUBLISHED_DATE));
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    interface OnArticleClickListener {

        void onArticleClicked(long articleDbId);

    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private DynamicHeightNetworkImageView thumbnailView;
        private TextView titleView;
        private TextView subtitleView;

        ViewHolder(final View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
        }

        void bindArticle(
                final String imageUrl,
                final float imageAspectRatio,
                final String title,
                final String author,
                final String publishedDate) {

            thumbnailView.setImageUrl(
                    imageUrl,
                    ImageLoaderHelper.getInstance(itemView.getContext()).getImageLoader());
            thumbnailView.setAspectRatio(imageAspectRatio);
            titleView.setText(title);

            if (isPreviousStartOfEpoch(parsePublishedDate(publishedDate))) {
                subtitleView.setText(Html.fromHtml(
                        getFormattedPublishedDateBeforeStartOfEpoch(publishedDate)
                                + "<br/>" + " by "
                                + author));
            } else {
                subtitleView.setText(Html.fromHtml(
                        getFormattedPublishedDateAfterStartOfEpoch(publishedDate)
                                + "<br/>" + " by "
                                + author));
            }
        }

    }

}