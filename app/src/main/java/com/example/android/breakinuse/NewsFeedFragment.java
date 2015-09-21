package com.example.android.breakinuse;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.breakinuse.newsProvider.NewsContract;
import com.example.android.breakinuse.recyclerViewAdapter.NewsFeedAdapter;

public class NewsFeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private NewsFeedAdapter mNewsFeedAdapter;
    private static final int LOADER_ID_ALL = 0;
    private static final String TAG = NewsFeedFragment.class.getName();
    private RecyclerView mRecyclerView;
    private Context mContext;
    private TextView mNewsFeedTextView;
    private boolean mShouldLoadMore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_news_feed, container, false);
        mNewsFeedTextView = (TextView) rootView.findViewById(R.id.newsFeed_textView);
        mContext = getActivity();
        mShouldLoadMore = true;

        mNewsFeedAdapter = new NewsFeedAdapter(mContext,
                mContext.getContentResolver().query(NewsContract.NewsFeed.NEWSFEED_READURI,null,null,null,null));
        getLoaderManager().initLoader(LOADER_ID_ALL, null, this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.favouriteNewsFeed_recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mNewsFeedAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (mShouldLoadMore) {

                    if ( (layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.getItemCount()) {

                        mShouldLoadMore = false;

                        //TODO handledatainsertionfornext page;
                        Log.v(TAG, "Last Item Wow !");

                    }

                }

            }

        });

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

        } else {

            mNewsFeedTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

        }

//        mNewsFeedAdapter = new NewsFeedAdapter(getActivity(),data);
//        mRecyclerView.setAdapter(mNewsFeedAdapter);

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

}
