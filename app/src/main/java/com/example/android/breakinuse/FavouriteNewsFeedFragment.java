package com.example.android.breakinuse;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.example.android.breakinuse.recyclerViewAdapter.FavouriteNewsFeedAdapter;
import com.example.android.breakinuse.utilities.Utility;

import java.util.Set;

public class FavouriteNewsFeedFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private FavouriteNewsFeedAdapter mFavouriteNewsFeedAdapter;
    private static final int LOADER_ID_FAVOURITES = 1;
    private static final String TAG = FavouriteNewsFeedFragment.class.getName();
    private RecyclerView mRecyclerView;
    private Context mContext;
    private TextView mFavouriteNewsFeedTextView;
    private boolean mShouldLoadMore;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_favourite_news_feed, container, false);
        mFavouriteNewsFeedTextView = (TextView)rootView.findViewById(R.id.favouriteNewsFeed_textView);
        mContext = getActivity();
        mShouldLoadMore = true;

        Set<String> favouriteTopicsSet = Utility.getFavouriteTopicsSet(mContext);
        if (!favouriteTopicsSet.isEmpty()){

            String[] selectionArgs = new String[favouriteTopicsSet.size()];
            StringBuilder selection = new StringBuilder();

            int index = 0;
            for (String iterator : favouriteTopicsSet){

                selectionArgs[index++] = iterator;
                selection.append("?");
                selection.append(",");

            }
            selection = new StringBuilder(selection.substring(0,selection.length()-1));
            mFavouriteNewsFeedAdapter =  new FavouriteNewsFeedAdapter(mContext,
                    mContext.getContentResolver().query(NewsContract.NewsFeed.FAVOURITE_NEWSFEED_READURI,
                            null,
                            NewsContract.NewsFeed.COLUMN_SECTIONID + " IN (" + selection.toString() + ")",
                            selectionArgs,
                            null));



        } else {

            mFavouriteNewsFeedAdapter = new FavouriteNewsFeedAdapter(mContext,
                    mContext.getContentResolver().query(NewsContract.NewsFeed.FAVOURITE_NEWSFEED_READURI,
                        null,
                        NewsContract.NewsFeed.COLUMN_ARTICLEID + " = ? ",
                        new String[]{"DummyFavouriteNewsFeedArticleID"},
                        null));

        }
        getLoaderManager().initLoader(LOADER_ID_FAVOURITES, null, this);

        final LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.favouriteNewsFeed_recyclerView);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mFavouriteNewsFeedAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                super.onScrolled(recyclerView, dx, dy);

                if (mShouldLoadMore) {

                    if ((layoutManager.getChildCount() + layoutManager.findFirstVisibleItemPosition()) >= layoutManager.getItemCount()) {

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

            case LOADER_ID_FAVOURITES:

                Set<String> favouriteTopicsSet = Utility.getFavouriteTopicsSet(mContext);
                if (!favouriteTopicsSet.isEmpty()){

                    String[] selectionArgs = new String[favouriteTopicsSet.size()];
                    StringBuilder selection = new StringBuilder();

                    int index = 0;
                    for (String iterator : favouriteTopicsSet){

                        selectionArgs[index++] = iterator;
                        selection.append("?");
                        selection.append(",");

                    }
                    selection = new StringBuilder(selection.substring(0,selection.length()-1));
                    cursorLoader =  new CursorLoader(mContext,
                            NewsContract.NewsFeed.FAVOURITE_NEWSFEED_READURI,
                            null,
                            NewsContract.NewsFeed.COLUMN_SECTIONID + " IN (" + selection.toString() +")",
                            selectionArgs,
                            null);



                } else {

                    cursorLoader = new CursorLoader(mContext,
                            NewsContract.NewsFeed.FAVOURITE_NEWSFEED_READURI,
                            null,
                            NewsContract.NewsFeed.COLUMN_ARTICLEID + " = ? ",
                            new String[]{"DummyFavouriteNewsFeedArticleID"},
                            null);

                }

                break;

            default:

                throw new UnsupportedOperationException("Unknown Loader ID used: " + String.valueOf(id));

        }

        return cursorLoader;

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if ((data != null) && (data.moveToFirst())){

            mFavouriteNewsFeedTextView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mFavouriteNewsFeedAdapter.swapCursor(data);
            if (!mShouldLoadMore){

                mShouldLoadMore = true;

            }

        } else {

            mFavouriteNewsFeedTextView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);

        }

//        mFavouriteNewsFeedAdapter = new FavouriteNewsFeedAdapter(getActivity(),data);
//        mRecyclerView.setAdapter(mFavouriteNewsFeedAdapter);

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mFavouriteNewsFeedAdapter.swapCursor(null);
    }

    @Override
    public void onResume() {

        super.onResume();
        getLoaderManager().restartLoader(LOADER_ID_FAVOURITES, null, this);

    }

}