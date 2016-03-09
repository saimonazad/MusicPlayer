

package com.azad.musicplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;
import com.azad.musicplayer.*;

public class HeaderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView text;
    private ListsClickListener clickListener;

    public HeaderViewHolder(View view, ListsClickListener clickListener) {
        super(view);
        this.clickListener = clickListener;
        text = (TextView)view.findViewById(R.id.textViewHeader);
        view.setOnClickListener(this);
    }

    public void update(String msg) {
        text.setText(msg);
    }

    @Override
    public void onClick(View view) {
        clickListener.onHeaderClick();
    }
}
