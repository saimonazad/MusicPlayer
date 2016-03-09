

package com.azad.musicplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.azad.musicplayer.*;
import com.azad.musicplayer.models.*;

public class PlaylistViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private MainActivity activity;
    private ListsClickListener clickListener;
    private Playlist playlist;
    private TextView name;
    private ImageView menu;

    public PlaylistViewHolder(View view, MainActivity activity, final ListsClickListener clickListener) {
        super(view);
        this.activity = activity;
        this.clickListener = clickListener;
        name = (TextView)view.findViewById(R.id.textViewName);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);
        view.setOnClickListener(this);
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    public void update(Playlist playlist) {
        this.playlist = playlist;
        name.setText(playlist.getName());
    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            final PopupMenu popup = new PopupMenu(activity, menu);
            popup.getMenuInflater().inflate(R.menu.contextmenu_editdelete, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    clickListener.onCategoryMenuClick(playlist, item.getItemId());
                    return true;
                }
            });
            popup.show();
        } else {
            clickListener.onCategoryClick(playlist);
        }
    }
}
