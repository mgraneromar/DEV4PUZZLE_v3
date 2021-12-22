package com.example.dev4puzzle_v3;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ImageFirebaseAdapter extends BaseAdapter {
    private Context mContext;
    private ArrayList<String> images;

    public ImageFirebaseAdapter(Context c, ArrayList<String> images) {
        mContext = c;
        this.images = images;

        Log.v("MainActivity", "ImageFirebaseAdapter: " + images.size());
    }

    public int getCount() {
        return images.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // crea un nuevo ImageView para cada elemento al que hace referencia el Adaptador
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            final LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.grid_element, null);
        }

        Log.v("MainActivity", "adapter position: " + position + " - url: " + images.get(position));

        final ImageView imageView = convertView.findViewById(R.id.gridImageview);
        imageView.setImageBitmap(null);
        // ejecutar código relacionado con la imagen después de que se haya diseñado la vista
        Glide.with(mContext).load(images.get(position))
                .centerCrop()
                .into(imageView);

        return convertView;
    }

}