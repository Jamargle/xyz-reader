package com.example.xyzreader.ui.articles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ItemsContract;
import com.example.xyzreader.data.UpdaterService;
import com.example.xyzreader.ui.details.ArticleDetailActivity;
import com.example.xyzreader.utils.ArticleLoader;

/**
 * An activity representing a list of Articles. This activity has different presentations for
 * handset and tablet-size devices. On handsets, the activity presents a list of items, which when
 * touched, lead to a {@link ArticleDetailActivity} representing item details. On tablets, the
 * activity presents a grid of items as cards.
 */
public class ArticleListActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor>,
        ArticleAdapter.OnArticleClickListener {

    private final BroadcastReceiver refreshingReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(
                final Context context,
                final Intent intent) {

            if (UpdaterService.BROADCAST_ACTION_STATE_CHANGE.equals(intent.getAction())) {
                final boolean isRefreshing = intent.getBooleanExtra(UpdaterService.EXTRA_REFRESHING, false);
                swipeRefreshLayout.setRefreshing(isRefreshing);
            }
        }

    };
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article_list);

        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        recyclerView = findViewById(R.id.recycler_view);
        getSupportLoaderManager().initLoader(0, null, this);

        if (savedInstanceState == null) {
            refresh();
        }
    }

    private void refresh() {
        startService(new Intent(this, UpdaterService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(refreshingReceiver, new IntentFilter(UpdaterService.BROADCAST_ACTION_STATE_CHANGE));
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(refreshingReceiver);
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

        final ArticleAdapter adapter = new ArticleAdapter(cursor, this);
        adapter.setHasStableIds(true);
        recyclerView.setAdapter(adapter);
        final int columnCount = getResources().getInteger(R.integer.list_column_count);

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(
                columnCount,
                StaggeredGridLayoutManager.VERTICAL));
    }

    @Override
    public void onLoaderReset(@NonNull final Loader<Cursor> loader) {
        recyclerView.setAdapter(null);
    }

    @Override
    public void onArticleClicked(final long articleDbId) {
        startActivity(new Intent(Intent.ACTION_VIEW,
                ItemsContract.Items.buildItemUri(articleDbId)));
    }

}
