

package com.azad.musicplayer.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;

import com.azad.musicplayer.MainActivity;
import com.azad.musicplayer.R;
import com.azad.musicplayer.Utils;
import com.azad.musicplayer.adapters.MusicPlayerAdapter;
import com.azad.musicplayer.models.PlayableItem;
import com.azad.musicplayer.models.Playlist;
import com.azad.musicplayer.models.PlaylistSong;
import com.azad.musicplayer.models.Playlists;
import com.azad.musicplayer.ui.DragDropTouchListener;
import com.azad.musicplayer.viewholders.ListsClickListener;

import java.util.ArrayList;

public class PlaylistFragment extends MusicPlayerFragment {
    private Playlist currentPlaylist = null;
    private ListsClickListener clickListener = new ListsClickListener() {
        @Override
        public void onHeaderClick() {
            showPlaylist(null);
        }

        @Override
        public void onPlayableItemClick(PlayableItem item) {
            activity.playItem(item);
            updateListView();
        }

        @Override
        public void onPlayableItemMenuClick(PlayableItem item, int menuId) {

        }

        @Override
        public void onCategoryClick(Object item) {
            showPlaylist((Playlist)item);
        }

        @Override
        public void onCategoryMenuClick(Object item, int menuId) {
            switch(menuId) {
                case R.id.menu_edit:
                    editPlaylist((Playlist)item);
                    break;
                case R.id.menu_delete:
                    deletePlaylist((Playlist)item);
                    break;
            }
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if(container==null) return null;
		View view = inflater.inflate(R.layout.layout_fragments, container, false);
        initialize(view);
        enableSort(view, R.id.imageViewItemImage, new DragDropTouchListener.OnItemMovedListener() {
            @Override
            public void onItemMoved(int from, int to) {
                sortPlaylist(from, to);
            }

            @Override
            public void onItemDeleted(Object item) {
                if(currentPlaylist==null) {
                    deletePlaylist((Playlist)item);
                } else {
                    deleteSongFromPlaylist((PlaylistSong)item);
                }
            }
        });
        updateListView();
		return view;
	}
	
	private void addPlaylist(String name) {
		Playlists.addPlaylist(name);
		updateListView();
	}
	
	public void editPlaylist(final Playlist playlist) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		int title = playlist==null ? R.string.newPlaylist : R.string.editPlaylist;
		builder.setTitle(getResources().getString(title));
		final View view = getActivity().getLayoutInflater().inflate(R.layout.layout_editplaylist, null);
		builder.setView(view);
		
		final EditText editTextName = (EditText)view.findViewById(R.id.editTextPlaylistName);
		if(playlist!=null) editTextName.setText(playlist.getName());
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String name = editTextName.getText().toString();
				if(name==null || name.equals("")) {
					Utils.showMessageDialog(getActivity(), R.string.error, R.string.errorPlaylistName);
					return;
				}
				if(playlist==null) {
					addPlaylist(name);
				} else {
					playlist.editName(name);
					updateListView();
				}
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
	
	public void deletePlaylist(final Playlist playlist) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.deletePlaylistConfirm);
        builder.setCancelable(false);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Playlists.deletePlaylist(playlist);
                updateListView();
            }
		});
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                updateListView();
            }
        });
		builder.show();
	}
	
	@Override
	public void updateListView() {
		MainActivity activity = (MainActivity)getActivity();
		PlaylistSong playingSong = null;
        if(activity.getCurrentPlayingItem() instanceof PlaylistSong) {
        	playingSong = (PlaylistSong)(activity.getCurrentPlayingItem());
        }
		ArrayList<Object> values = new ArrayList<>();
		if(currentPlaylist==null) { // Show all playlists
            setFloatingButtonVisible(true);
            setEmptyViewText(R.string.playlistsEmpty);
			ArrayList<Playlist> playlists = Playlists.getPlaylists();
			values.addAll(playlists);
		} else {
            values.add(currentPlaylist.getName());
            setFloatingButtonVisible(false);
            setEmptyViewText(R.string.playlistEmpty);
			values.addAll(currentPlaylist.getSongs());
		}

        Parcelable state = layoutManager.onSaveInstanceState();
        recyclerView.setAdapter(new MusicPlayerAdapter(activity, values, playingSong, emptyView, clickListener));
        layoutManager.onRestoreInstanceState(state);
	}
	
	private void deleteSongFromPlaylist(PlaylistSong song) {
		song.getPlaylist().deleteSong(song);
	}
	
	private void sortPlaylist(int from, int to) {
		if(currentPlaylist==null) { // Sorting playlists
			Playlists.sortPlaylists(from, to);
		} else { // Sorting songs in playlist
            if(from==0 || to==0) return;
			currentPlaylist.sort(from-1, to-1);
		}
	}
	
	public void showPlaylist(Playlist playlist) {
		currentPlaylist = playlist;
		updateListView();
	}

	@Override
	public boolean onBackPressed() {
		if(currentPlaylist!=null) {
			showPlaylist(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void gotoPlayingItemPosition(PlayableItem playingItem) {
		PlaylistSong song = (PlaylistSong)playingItem;
		showPlaylist(song.getPlaylist());

        int position = ((MusicPlayerAdapter)recyclerView.getAdapter()).getPlayableItemPosition(playingItem);
        recyclerView.scrollToPosition(position);
	}

    @Override
    public void onFloatingButtonClick() {
        editPlaylist(null);
    }
}
