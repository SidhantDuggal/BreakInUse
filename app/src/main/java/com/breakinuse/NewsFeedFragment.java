package com.breakinuse;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.breakinuse.dataSync.DownloadNewsFeedTask;
import com.breakinuse.newsProvider.NewsContract;
import com.breakinuse.recyclerViewAdapter.NewsFeedAdapter;
import com.breakinuse.utilities.Utility;

public class NewsFeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        com.breakinuse.dataSync.DownloadNewsFeedTask.OnDownloadTaskFinishedListener {

    private NewsFeedAdapter mNewsFeedAdapter;
    private static final int LOADER_ID_ALL = 0;
    private static final String TAG = NewsFeedFragment.class.getName();
    private RecyclerView mRecyclerView;
    private Context mContext;
    private TextView mNewsFeedTextView;
    private boolean mShouldLoadMore;
    private ProgressBar mLoadMoreIndicator;
    private boolean isCurrentlySelected;
    private SwipeRefreshLayout mSwipeContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news_feed, container, false);
        mNewsFeedTextView = (TextView) rootView.findViewById(R.id.newsFeed_textView);
        mLoadMoreIndicator = (ProgressBar) rootView.findViewById(R.id.newsFeed_loadMoreIndicator);
        mLoadMoreIndicator.setVisibility(View.GONE);
        mContext = getActivity();
        mShouldLoadMore = true;

        mNewsFeedAdapter = new NewsFeedAdapter(mContext,
                mContext.getContentResolver().query(NewsContract.NewsFeed.NEWSFEED_READURI,null,null,null,null));
        getLoaderManager().initLoader(LOADER_ID_ALL, null, this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.newsFeed_recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mNewsFeedAdapter);
        final Fragment fragment = this;

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (isCurrentlySelected) {

                    if (mShouldLoadMore) {

                        if ((layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.getItemCount()) {

                            if (!Utility.isNetworkAvailable(mContext)) {

                                Utility.makeToast(mContext,
                                        "We are not able to detect an internet connection. Please resolve this befor trying again.",
                                        Toast.LENGTH_SHORT);
                                mShouldLoadMore = true;

                            } else {

                                mShouldLoadMore = false;
                                new DownloadNewsFeedTask(mContext,mLoadMoreIndicator,fragment).execute();
                                mLoadMoreIndicator.setVisibility(View.VISIBLE);


                            }

                        }

                    }

                }

            }

        });


        mSwipeContainer = (SwipeRefreshLayout) rootView.findViewById(R.id.newsFeed_swipeContainer);
        mSwipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {

                Utility.updateNewsFeed(mContext);

            }

        });
        mSwipeContainer.setColorSchemeResources(android.R.color.holo_red_light);

        return rootView;

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        CursorLoader cursorLoader;

        switch(id){

            case LOADER_ID_ALL:

                cursorLoader = new CursorLoader(getActivity(),NewsContract.NewsFeed.NEWSFEED_READURI,
                                        null,
                                        null,
                                        null,
                                        null);
                break;

            default:

                throw new UnsupportedOperationException("Unknown Loader ID used: " + String.valueOf(id));

        }

        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if ((data != null) && (data.moveToFirst())){

            mRecyclerView.setVisibility(View.VISIBLE);
            mNewsFeedAdapter.swapCursor(data);
            mNewsFeedTextView.setVisibility(View.GONE);
            if (!mShouldLoadMore){

                mShouldLoadMore = true;

            }
            mSwipeContainer.setRefreshing(false);

        } else {

            mNewsFeedTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mSwipeContainer.setRefreshing(false);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mNewsFeedAdapter.swapCursor(null);

    }

    @Override
    public void onResume() {

        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID_ALL, null, this);

    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {

        super.setUserVisibleHint(isVisibleToUser);
        isCurrentlySelected = isVisibleToUser;

    }

    @Override
    public void onDownloadTaskFinished(String taskStatus) {

        if (taskStatus.equals("caughtException")){

            mShouldLoadMore = true;

        }

    }

}
