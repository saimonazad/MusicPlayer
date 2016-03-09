

package com.azad.musicplayer.viewholders;

import com.azad.musicplayer.models.*;

public interface ListsClickListener {
    void onHeaderClick();
    void onPlayableItemClick(PlayableItem item);
    void onPlayableItemMenuClick(PlayableItem item, int menuId);
    void onCategoryClick(Object item);
    void onCategoryMenuClick(Object item, int menuId);
}
