

package com.azad.musicplayer.viewholders;

import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import com.azad.musicplayer.*;
import java.io.File;

public class DirectoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView name;
    private ImageButton menu;
    private File directory;
    private ListsClickListener clickListener;
    private MainActivity activity;

    public DirectoryViewHolder(View view, MainActivity activity, ListsClickListener clickListener) {
        super(view);
        this.activity = activity;
        this.clickListener = clickListener;
        name = (TextView)view.findViewById(R.id.textViewFolderItemFolder);
        menu = (ImageButton)view.findViewById(R.id.buttonMenu);
        view.setOnClickListener(this);
        menu.setOnClickListener(this);
        menu.setFocusable(false);
    }

    public void update(File directory) {
        this.directory = directory;
        name.setText(directory.getName());
    }

    @Override
    public void onClick(View view) {
        if(view.equals(menu)) {
            final PopupMenu popup = new PopupMenu(activity, menu);
            popup.getMenuInflater().inflate(R.menu.contextmenu_browserdirectory, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    clickListener.onCategoryMenuClick(directory, item.getItemId());
                    return true;
                }
            });
            popup.show();
        } else {
            clickListener.onCategoryClick(directory);
        }
    }
}
