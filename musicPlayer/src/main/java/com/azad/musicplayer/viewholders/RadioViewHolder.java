

package com.azad.musicplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.*;
import android.widget.*;

import com.azad.musicplayer.*;
import com.azad.musicplayer.models.*;

public class RadioViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView text;
    private ImageView image;
    private ImageButton menu;
    private View card;
    private Radio radio;
    private MainActivity activity;
    private ListsClickListener clickListener;

    public RadioViewHolder(View view, MainActivity activity, ListsClickListener clickListener) {
        super(view);
        text = (TextView)view.findViewById(R.id.textViewRadio);
        image = (ImageView)view.findViewById(R.id.imageViewRadio);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);
        card = view.findViewById(R.id.card);
        this.activity = activity;
        this.clickListener = clickListener;
        view.setOnClickListener(this);
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    public void update(Radio radio, Radio playingRadio) {
        this.radio = radio;
        text.setText(radio.getTitle());
        if(radio.equals(playingRadio)) {
            card.setBackgroundResource(R.drawable.card_playing);
            image.setImageResource(R.drawable.play_orange);
        } else {
            card.setBackgroundResource(R.drawable.card);
            image.setImageResource(R.drawable.radio);
        }
    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            final PopupMenu popup = new PopupMenu(activity, menu);
            popup.getMenuInflater().inflate(R.menu.contextmenu_editdelete, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    clickListener.onPlayableItemMenuClick(radio, item.getItemId());
                    return true;
                }
            });
            popup.show();
        } else {
            clickListener.onPlayableItemClick(radio);
        }
    }
}
