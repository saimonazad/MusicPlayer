

package com.azad.musicplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.*;

import com.azad.musicplayer.*;
import com.azad.musicplayer.models.*;

public class SongViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView artist;
    private TextView title;
    private ImageView image;
    private ImageButton menu;
    private View card;
    private PlayableItem song;
    private ImagesCache imagesCache;
    private ListsClickListener clickListener;

    public SongViewHolder(View view, ImagesCache imagesCache, ListsClickListener clickListener) {
        super(view);
        title = (TextView)view.findViewById(R.id.textViewSongItemTitle);
        artist = (TextView)view.findViewById(R.id.textViewSongItemArtist);
        image = (ImageView)view.findViewById(R.id.imageViewItemImage);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);
        card = view.findViewById(R.id.card);
        this.imagesCache = imagesCache;
        this.clickListener = clickListener;
        view.setOnClickListener(this);
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    public void update(PlayableItem song, PlayableItem playingSong) {
        this.song = song;
        String trackNumber = "";
        if(song instanceof BrowserSong) {
            BrowserSong browserSong = (BrowserSong)song;
            if(browserSong.getTrackNumber()>0) trackNumber = browserSong.getTrackNumber() + ". ";
        } else {
            menu.setVisibility(View.GONE);
        }
        title.setText(trackNumber + song.getTitle());
        artist.setText(song.getArtist());
        if(song.equals(playingSong)) {
            card.setBackgroundResource(R.drawable.card_playing);
            image.setImageResource(R.drawable.play_orange);
        } else {
            card.setBackgroundResource(R.drawable.card);
            image.setImageResource(R.drawable.audio);
            if(song.hasImage()) {
                imagesCache.getImageAsync(song, image);
            }
        }
    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            clickListener.onPlayableItemMenuClick(song, 0);
        } else {
            clickListener.onPlayableItemClick(song);
        }
    }
}
