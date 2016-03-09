
package com.azad.musicplayer;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.azad.musicplayer.adapters.SearchResultsAdapter;
import com.azad.musicplayer.models.BrowserSong;

import java.io.File;
import java.util.ArrayList;

public class SearchActivity extends ActionBarActivity implements OnClickListener, OnKeyListener {
	private EditText editTextSearch;
	private ImageButton buttonSearch;
	private RecyclerView recyclerViewSearch;
	private MusicPlayerApplication application;
	private String lastSearch;
	private InputMethodManager inputMethodManager;
    private ContentResolver mediaResolver;
	private final static String[] projection = {
			MediaStore.Audio.Media.ARTIST,
			MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.TRACK,
			MediaStore.Audio.Media.DATA
	};

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        inputMethodManager = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        mediaResolver = getContentResolver();
        
        setContentView(R.layout.activity_search);
        
        editTextSearch = (EditText)findViewById(R.id.editTextSearch);
        editTextSearch.setOnKeyListener(this);
		editTextSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View view, boolean hasFocus) {
				if(!hasFocus) {
					inputMethodManager.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
				}
			}
		});
        buttonSearch = (ImageButton)findViewById(R.id.buttonSearch);
        buttonSearch.setOnClickListener(this);
        recyclerViewSearch = (RecyclerView)findViewById(R.id.recyclerViewSearch);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerViewSearch.setLayoutManager(llm);
        
        application = (MusicPlayerApplication)getApplication();
        lastSearch = application.getLastSearch();
        
        setResult(0, getIntent());
	}

	@Override
	public void onResume() {
		super.onResume();
		editTextSearch.requestFocus();
		inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        return true;
    }
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu) {
		if(lastSearch==null || lastSearch.equals("")) {
			menu.findItem(R.id.menu_repeatLastSearch).setVisible(false);
        }
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_repeatLastSearch:
			if(lastSearch==null) return true;
			editTextSearch.setText(lastSearch);
			search();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onClick(View view) {
		if(view.equals(buttonSearch)) {
			editTextSearch.clearFocus();
			search();
		}
	}
	
	private void search() {
		String text = editTextSearch.getText().toString();
		search(text);
		application.setLastSearch(text);
	}
	
	private void search(String str) {
		str = str.replace("\"", "");
        str = str.replace("%", "");
		str = str.trim();
		ArrayList<BrowserSong> results = new ArrayList<>();

		String where = MediaStore.Audio.Media.ARTIST + " LIKE \"%" + str + "%\" OR " + MediaStore.Audio.Media.TITLE + " LIKE \"%" + str + "%\"";
		Cursor musicCursor = mediaResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, where, null, null);
        int titleColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
        int artistColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
        int uriColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
        int trackColumn = musicCursor.getColumnIndex(MediaStore.Audio.Media.TRACK);
        if(musicCursor!=null && musicCursor.moveToFirst()) {
			do {
				String title = musicCursor.getString(titleColumn);
				String artist = musicCursor.getString(artistColumn);
				String uri = musicCursor.getString(uriColumn);
				String trackNumber = musicCursor.getString(trackColumn);
				results.add(new BrowserSong(uri, artist, title, trackNumber, null));
			} while (musicCursor.moveToNext());
		}
		musicCursor.close();

		if (results.size() == 0) {
			Utils.showMessageDialog(this, R.string.noResultsFoundTitle, R.string.noResultsFoundMessage);
		} else {
			recyclerViewSearch.setAdapter(new SearchResultsAdapter(this, results));
		}
	}

    public void songSelected(BrowserSong song) {
        Intent intent = getIntent();
        intent.putExtra("song", song);
        File songFile = new File(song.getUri());
        if(!songFile.exists()) {
            Utils.showMessageDialog(this, R.string.notFound, R.string.songNotFound);
            return;
        }
        setResult(1, intent);
        finish();
    }

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		// Manage "enter" key on keyboard
		if(event.getAction()==KeyEvent.ACTION_DOWN && keyCode==KeyEvent.KEYCODE_ENTER) {
			search();
			return true;
		}
		return false;
	}
}
