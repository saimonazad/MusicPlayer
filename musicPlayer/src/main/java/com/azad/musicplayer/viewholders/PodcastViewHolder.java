
package com.azad.musicplayer.viewholders;

import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.azad.musicplayer.*;
import com.azad.musicplayer.models.*;

public class PodcastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private MainActivity activity;
    private ListsClickListener clickListener;
    private TextView title;
    private ImageView image;
    private ImageView menu;
    private Podcast podcast;

    public PodcastViewHolder(View view, MainActivity activity, ListsClickListener clickListener) {
        super(view);
        this.activity = activity;
        this.clickListener = clickListener;
        title = (TextView)view.findViewById(R.id.textViewFolderItemFolder);
        image = (ImageView)view.findViewById(R.id.imageViewItemImage);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);
        view.setOnClickListener(this);
    }

    public void update(final Podcast podcast) {
        this.podcast = podcast;
        title.setText(podcast.getName());
        Bitmap podcastImage = podcast.getImage();
        if(podcastImage!=null) {
            image.setImageBitmap(podcastImage);
        }
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            final PopupMenu popup = new PopupMenu(activity, menu);
            popup.getMenuInflater().inflate(R.menu.contextmenu_editdelete, popup.getMenu());
            popup.getMenu().removeItem(R.id.menu_edit);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    clickListener.onCategoryMenuClick(podcast, item.getItemId());
                    return true;
                }
            });
            popup.show();
        } else {
            clickListener.onCategoryClick(podcast);
        }
    }
}
