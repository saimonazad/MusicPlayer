

package com.azad.musicplayer;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.azad.musicplayer.models.*;

public class ImagesCache {
    private int imagesSize;
    final private LruCache<String,Bitmap> cache;

    public ImagesCache(Context context) {
        imagesSize = (int)context.getResources().getDimension(R.dimen.songImageSize);
        cache = new LruCache<>(Constants.IMAGES_CACHE_SIZE);
    }

    public void clearCache() {
        synchronized(cache) {
            cache.evictAll();
        }
    }

    public void getImageAsync(PlayableItem item, ImageView imageView) {
        Bitmap image;
        synchronized(cache) {
            image = cache.get(item.getPlayableUri());
        }
        if(image==null) {
            ImageLoaderTask imageLoader = new ImageLoaderTask(item, imageView);
            imageLoader.execute();
        } else {
            imageView.setImageBitmap(image);
            imageView.setVisibility(View.VISIBLE);
        }
    }

    public Bitmap getImageSync(PlayableItem item) {
        synchronized(cache) {
            Bitmap image = cache.get(item.getPlayableUri());
            if(image==null) {
                Bitmap originalImage = item.getImage();
                if(originalImage==null) return null;
                image = Bitmap.createScaledBitmap(originalImage, imagesSize, imagesSize, true);
                cache.put(item.getPlayableUri(), image);
            }
            return image.copy(image.getConfig(), true); // Necessary to avoid recycled bitmap to be used.
        }
    }

    private class ImageLoaderTask extends AsyncTask<Void, Void, Bitmap> {
        private PlayableItem item;
        private ImageView imageView;

        public ImageLoaderTask(PlayableItem item, ImageView imageView) {
            this.item = item;
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
            Bitmap originalImage = item.getImage();
            if(originalImage==null) return null;
            Bitmap image = Bitmap.createScaledBitmap(originalImage, imagesSize, imagesSize, true);
            synchronized(cache) {
                cache.put(item.getPlayableUri(), image);
            }
            image = image.copy(image.getConfig(), true); // Necessary to avoid recycled bitmap to be used.
            return image;
        }

        @Override
        protected void onPostExecute(Bitmap image) {
            if(image!=null) {
                imageView.setImageBitmap(image);
                imageView.setVisibility(View.VISIBLE);
            }
        }
    }
}
