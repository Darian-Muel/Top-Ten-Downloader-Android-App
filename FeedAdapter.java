package com.example.top10downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

public class FeedAdapter extends ArrayAdapter {
    private static final String TAG = "FeedAdapter";
    //inflating a xml resource means taking a xml represention and producing the acutal widgets from it
    private final int layoutResource;
    //layoutInflater takes an XML file as input and builds the View objects from it.
    private final LayoutInflater layoutInflater;
    private List<FeedEntry> applications;
    private Bitmap finalPic = null;
    private Context mContext;


    public FeedAdapter(@NonNull Context context, int resource, List<FeedEntry> applications) {
        super(context, resource);
        mContext = context;
        //this refers to current instance of the class
        //resource is ID of the layout resource that getView() would inflate to create the view.
        this.layoutResource = resource;
        //context refers to the activity where the adapter is created.
        //creates a layoutinflater from the given context
        this.layoutInflater = LayoutInflater.from(context);
        /*
        object is a collection of objects that provide data to the adapter. If it is a list,
        the adapter stores it as is. If it is an array, the adapter converts it to a list.
        It is not required to pass this value when instantiating the adapter. It can be set
        later using addAll() method.
         */
        this.applications = applications;
    }

    @Override
    public int getCount() {
        return applications.size();
    }


    @NonNull
    @Override
    //called when setAdapter is used
    // getView() method is responsible for creating the views.
    //convertView is used to reuse old view.
    //parentView is the ListView which contains the item's view which getView() generates.
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        setupImageLoader();
        ViewHolder viewHolder;
        // Check if an existing view is being reused, otherwise inflate the view
        /*
        The adapters are built to reuse Views, when a View is scrolled so that it is no longer visible,
        it can be used for one of the new Views appearing. This reused View is the convertView.
        If this is null it means that there is no recycled View and we have to create a new one,
        otherwise we should use it to avoid creating a new.
         */
        if ((convertView == null)) {
            Log.d(TAG, "getView: called with null convertView");
            //layoutResource is going to be coming from list_record
            convertView = layoutInflater.inflate(layoutResource, parent, false);
            viewHolder = new ViewHolder(convertView);
            //store references of the views
            convertView.setTag(viewHolder);


        }
        else {
            Log.d(TAG, "getView: provided a convertView");
            //We recycle a View that already exists
            viewHolder = (ViewHolder) convertView.getTag();

        }
        FeedEntry currentApp = applications.get(position);

        int defaultImage = mContext.getResources().getIdentifier("@drawable/image_failed", null, mContext.getPackageName());

        ImageLoader imageLoader = ImageLoader.getInstance();
        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .cacheOnDisc(true).resetViewBeforeLoading(true)
                .showImageForEmptyUri(defaultImage)
                .showImageOnFail(defaultImage)
                .showImageOnLoading(defaultImage).build();
        imageLoader.displayImage(currentApp.getImageURL(), viewHolder.ivPoster, options);



        viewHolder.tvName.setText(currentApp.getName());
        viewHolder.tvArtist.setText(currentApp.getArtist());
        viewHolder.tvSummary.setText(currentApp.getSummary());
        viewHolder.tvRank.setText(currentApp.getRank());







        return convertView;
    }

//  class to use the ViewHolder pattern
    private class ViewHolder {
        final TextView tvName;
        final TextView tvArtist;
        final TextView tvSummary;
        final TextView tvRank;
        final ImageView ivPoster;

        ViewHolder(View v) {
            this.tvName = v.findViewById(R.id.tvName);
            this.tvArtist = v.findViewById(R.id.tvArtist);
            this.tvSummary = v.findViewById(R.id.tvSummary);
            this.tvRank = v.findViewById(R.id.tvRank);
            this.ivPoster = v.findViewById(R.id.ivPostor);
        }
    }
    private void setupImageLoader(){
        // UNIVERSAL IMAGE LOADER SETUP
        DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                .cacheOnDisc(true).cacheInMemory(true)
                .imageScaleType(ImageScaleType.EXACTLY)
                .displayer(new FadeInBitmapDisplayer(300)).build();

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                mContext)
                .defaultDisplayImageOptions(defaultOptions)
                .memoryCache(new WeakMemoryCache())
                .discCacheSize(100 * 1024 * 1024).build();

        ImageLoader.getInstance().init(config);
        // END - UNIVERSAL IMAGE LOADER SETUP

    }
}







