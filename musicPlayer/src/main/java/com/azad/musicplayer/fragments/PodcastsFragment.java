
package com.azad.musicplayer.fragments;

import java.io.File;
import java.util.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import com.azad.musicplayer.*;
import com.azad.musicplayer.adapters.*;
import com.azad.musicplayer.models.*;
import com.azad.musicplayer.viewholders.*;

public class PodcastsFragment extends MusicPlayerFragment {
	private Podcast currentPodcast; // if null, show podcasts' list
    private ListsClickListener clickListener = new ListsClickListener() {
        @Override
        public void onHeaderClick() {
            currentPodcast = null;
            updateListView();
        }

        @Override
        public void onPlayableItemClick(PlayableItem item) {
            final PodcastEpisode podcastEpisode = (PodcastEpisode)item;
            int status = podcastEpisode.getStatus();
            if(podcastEpisode.getStatus()==PodcastEpisode.STATUS_NEW) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                dialog.setTitle(podcastEpisode.getTitle());
                dialog.setMessage(R.string.chooseDownloadMethod);
                dialog.setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Utils.isWifiConnected()) {
                            downloadEpisode(podcastEpisode);
                        } else {
                            downloadEpisodeConfirm(podcastEpisode);
                        }
                    }
                });
                dialog.setNegativeButton(R.string.streaming, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        podcastEpisode.setStreaming();
                        ((MainActivity)getActivity()).playPodcastEpisodeStreaming(podcastEpisode);
                        updateListView();
                    }
                });
                dialog.show();
            } else if(status==PodcastEpisode.STATUS_DOWNLOADED) {
                ((MainActivity)getActivity()).playItem(podcastEpisode);
                updateListView();
            }
        }

        @Override
        public void onPlayableItemMenuClick(PlayableItem item, int menuId) {
            switch(menuId) {
                case R.id.menu_delete:
                    deletePodcastEpisode((PodcastEpisode) item);
                    break;
            }
        }

        @Override
        public void onCategoryClick(Object item) {
            if(item instanceof Podcast) {
                openPodcast((Podcast)item);
            }
        }

        @Override
        public void onCategoryMenuClick(Object item, int menuId) {
            switch(menuId) {
                case R.id.menu_delete:
                    deletePodcast((Podcast)item);
                    break;
            }
        }
    };
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) return null;
		View view = inflater.inflate(R.layout.layout_fragments, container, false);
		initialize(view);
        setFloatingButtonVisible(true);
		updateListView();
		return view;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		updateListView(true);
	}
	
	@Override
	public void updateListView() {
		updateListView(false);
	}
	
	public void updateListView(boolean reloadFromDatabase) {
		ArrayList<Object> items = new ArrayList<>();
		if(currentPodcast==null) {
			ArrayList<Podcast> podcasts = Podcast.getPodcasts();
			items.addAll(podcasts);
            setFloatingButtonImage(R.drawable.newcontent);
            setEmptyViewText(R.string.noPodcasts);
		} else {
            items.add(currentPodcast.getName());
			if(reloadFromDatabase) currentPodcast.loadItemsFromDatabase();
            setFloatingButtonImage(R.drawable.refresh);
            setEmptyViewText(R.string.podcastEmpty);
            items.addAll(currentPodcast.getEpisodes());
		}
		PodcastEpisode currentPodcastEpisode = null;
		MainActivity activity = (MainActivity)getActivity();
		if(activity.getCurrentPlayingItem() instanceof PodcastEpisode) currentPodcastEpisode = (PodcastEpisode)activity.getCurrentPlayingItem();

        Parcelable state = layoutManager.onSaveInstanceState();
		recyclerView.setAdapter(new MusicPlayerAdapter(activity, items, currentPodcastEpisode, emptyView, clickListener));
        layoutManager.onRestoreInstanceState(state);
	}
	
	private void openPodcast(Podcast podcast) {
		currentPodcast = podcast;
		updateListView();
	}
	
	private void downloadEpisodeConfirm(final PodcastEpisode episode) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.podcast);
		builder.setMessage(R.string.podcastDownloadNoWifiConfirm);
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				downloadEpisode(episode);
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
	
	private void downloadEpisode(PodcastEpisode episode) {
		String podcastsDirectory = preferences.getString(Constants.PREFERENCE_PODCASTSDIRECTORY, Podcast.DEFAULT_PODCASTS_PATH);
		
		if(!new File(podcastsDirectory).exists()) {
			Utils.showMessageDialog(getActivity(), R.string.error, R.string.podcastDirectoryNotExist);
			return;
		}
		
		episode.setStatus(PodcastEpisode.STATUS_DOWNLOADING);
		Intent downloaderIntent = new Intent(getActivity(), PodcastEpisodeDownloaderService.class);
		downloaderIntent.putExtra("idItem", episode.getId());
		downloaderIntent.putExtra("title", episode.getTitle());
		downloaderIntent.putExtra("url", episode.getUrl());
		downloaderIntent.putExtra("type", episode.getType());
		downloaderIntent.putExtra("podcastsDirectory", podcastsDirectory);
		getActivity().startService(downloaderIntent);
		updateListView();
	}
	
	private void addPodcast() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.addPodcast);
		final View view = getActivity().getLayoutInflater().inflate(R.layout.layout_addpodcast1, null);
		builder.setView(view);
		
		final EditText editTextUrl = (EditText)view.findViewById(R.id.editTextPodcastUrl);
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				String url = editTextUrl.getText().toString();
				if(url.equals("") || url.equals("http://")) {
					Toast.makeText(getActivity(), R.string.errorInvalidURL, Toast.LENGTH_SHORT).show();
					return;
				}
				new GetPodcastInformationTask(url).execute();
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
	
	private void addPodcast2(final String url, final String name, final byte[] image) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.addPodcast);
		final View view = getActivity().getLayoutInflater().inflate(R.layout.layout_addpodcast2, null);
		builder.setView(view);
		TextView textViewUrl = (TextView)view.findViewById(R.id.textViewUrl);
		textViewUrl.setText(url);
		final EditText editTextName = (EditText)view.findViewById(R.id.editTextPodcastName);
		if(name!=null) editTextName.setText(name);
		
		ImageView imageViewPodcastImage = (ImageView)view.findViewById(R.id.imageViewPodcastImage);
		if(image==null) {
			imageViewPodcastImage.setVisibility(View.GONE);
		} else {
			Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
			imageViewPodcastImage.setImageBitmap(bitmap);
		}
		
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				if(!editTextName.getText().toString().equals("")) {
					Podcast.addPodcast(getActivity(), url, editTextName.getText().toString(), image);
					updateListView();
				} else {
					Utils.showMessageDialog(getActivity(), R.string.error, R.string.podcastNameError);
				}
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		AlertDialog dialog = builder.create();
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		dialog.show();
	}
	
	private void deletePodcast(final Podcast podcast) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(R.string.removePodcastConfirm);
		builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				podcast.remove();
				updateListView();
			}
		});
		builder.setNegativeButton(R.string.cancel, null);
		builder.show();
	}

    private void deletePodcastEpisode(PodcastEpisode podcastEpisode) {
        podcastEpisode.getPodcast().deleteEpisode(podcastEpisode);
        updateListView();
    }
	
	public void removeAllEpisodes() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.removeAllEpisodes);
		if(currentPodcast==null) {
			builder.setMessage(R.string.removeAllEpisodesConfirm);
		} else {
			builder.setMessage(getActivity().getResources().getString(R.string.removeEpisodesConfirm, currentPodcast.getName()));
		}
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Podcast.deleteEpisodes(currentPodcast, false);
				Toast.makeText(getActivity(), R.string.removeEpisodesCompletion, Toast.LENGTH_SHORT).show();
				updateListView(true);
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}
	
	public void removeDownloadedEpisodes() {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.removeDownloadedEpisodes);
		if(currentPodcast==null) {
			builder.setMessage(R.string.removeAllDownloadedEpisodesConfirm);
		} else {
			builder.setMessage(getActivity().getResources().getString(R.string.removeDownloadedEpisodesConfirm, currentPodcast.getName()));
		}
		builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				Podcast.deleteEpisodes(currentPodcast, true);
				Toast.makeText(getActivity(), R.string.removeDownloadedEpisodesCompletion, Toast.LENGTH_SHORT).show();
				updateListView(true);
			}
		});
		builder.setNegativeButton(R.string.no, null);
		builder.show();
	}

    @Override
    public void onFloatingButtonClick() {
        if(currentPodcast==null) addPodcast();
        else new UpdatePodcastTask(currentPodcast).execute();
    }

    private class UpdatePodcastTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progressDialog;
		private Podcast podcast;
		public UpdatePodcastTask(Podcast podcast) {
			this.podcast = podcast;
		}
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setIndeterminate(true);
	        progressDialog.setCancelable(false);
	        progressDialog.setMessage(getActivity().getString(R.string.updatingPodcast));
			progressDialog.show();
	    }
		@Override
		protected Boolean doInBackground(Void... params) {
			return podcast.update();
		}
		@Override
		protected void onPostExecute(final Boolean success) {
			if(!success) {
				Utils.showMessageDialog(getActivity(), R.string.error, R.string.podcastDownloadError);
			}
			updateListView();
			if(progressDialog.isShowing()) progressDialog.dismiss();
		}
	}
	
	private class GetPodcastInformationTask extends AsyncTask<Void, Void, Boolean> {
		private ProgressDialog progressDialog;
		private String url, name;
		private byte[] image;
		public GetPodcastInformationTask(String url) {
			this.url = url;
		}
		@Override
		protected void onPreExecute() {
			progressDialog = new ProgressDialog(getActivity());
	        progressDialog.setIndeterminate(true);
	        progressDialog.setCancelable(false);
	        progressDialog.setMessage(getActivity().getString(R.string.updatingPodcast));
			progressDialog.show();
	    }
		@Override
		protected Boolean doInBackground(Void... params) {
			PodcastParser parser = new PodcastParser();
			boolean ok = parser.parse(url);
			if(!ok) return false;
			name = parser.getTitle();
            int listImageSize = (int)getResources().getDimension(R.dimen.listImageSize);
			image = parser.downloadImage(listImageSize);
			return true;
		}
		@Override
		protected void onPostExecute(final Boolean success) {
			if(progressDialog.isShowing()) progressDialog.dismiss();
			if(success) {
				addPodcast2(url, name, image);
			} else {
				Utils.showMessageDialog(getActivity(), R.string.error, R.string.podcastDownloadError);
			}
		}
	}

	@Override
	public boolean onBackPressed() {
		if(currentPodcast!=null) {
			openPodcast(null);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void gotoPlayingItemPosition(PlayableItem playingItem) {
		PodcastEpisode playingEpisode = (PodcastEpisode)playingItem;
		openPodcast(playingEpisode.getPodcast());

        int position = ((MusicPlayerAdapter)recyclerView.getAdapter()).getPlayableItemPosition(playingItem);
        recyclerView.scrollToPosition(position);
	}
}
