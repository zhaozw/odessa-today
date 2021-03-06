package com.kvest.odessatoday.ui.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import com.android.volley.toolbox.NetworkImageView;
import com.kvest.odessatoday.R;
import com.kvest.odessatoday.TodayApplication;

/**
 * Created with IntelliJ IDEA.
 * User: Kvest
 * Date: 20.12.14
 * Time: 21:41
 * To change this template use File | Settings | File Templates.
 */
public class PhotoSlideAdapter extends PagerAdapter {
    private Context context;
    private String[] photoURLs;
    private ViewGroup.LayoutParams layoutParams;

    private int noImageResId, loadingImageResId;

    public PhotoSlideAdapter(Context context, String[] photoURLs) {
        this.context = context;
        this.photoURLs = photoURLs;
        this.layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        initResources(context);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        NetworkImageView imageView = new NetworkImageView(context);
        imageView.setScaleType(NetworkImageView.ScaleType.FIT_CENTER);
        imageView.setDefaultImageResId(loadingImageResId);
        imageView.setErrorImageResId(noImageResId);

        //start loading image
        imageView.setImageUrl(photoURLs[position], TodayApplication.getApplication().getVolleyHelper().getImageLoader());

        container.addView(imageView, layoutParams);

        return imageView;
    }

    @Override
    public int getCount() {
        return photoURLs.length;
    }

    @Override
    public boolean isViewFromObject(View view, Object object){
        return view.equals(object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    private void initResources(Context context) {
        // The attributes you want retrieved
        int[] attrs = {R.attr.NoImage, R.attr.LoadingImage};

        // Parse style, using Context.obtainStyledAttributes()
        TypedArray ta = context.obtainStyledAttributes(attrs);

        try {
            // Fetching the resources defined in the style
            noImageResId = ta.getResourceId(ta.getIndex(0), -1);
            loadingImageResId = ta.getResourceId(ta.getIndex(1), -1);
        } finally {
            ta.recycle();
        }
    }
}
