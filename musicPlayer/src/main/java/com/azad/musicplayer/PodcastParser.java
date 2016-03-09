

package com.azad.musicplayer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.azad.musicplayer.models.PodcastEpisode;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilderFactory;

public class PodcastParser {
	private String title;
	private String link;
	private String imageUrl;
	private ArrayList<PodcastEpisode> episodes;
	
	public boolean parse(String podcastUrl) {
		episodes = new ArrayList<>();
		try {
			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new URL(podcastUrl).openStream());
		    Element root = document.getDocumentElement();
		    
		    NodeList channels = root.getChildNodes();
		    for(int i=0; i<channels.getLength(); i++) {
		    	Node channelNode = channels.item(i);
		    	if(channelNode.getNodeType()==Node.ELEMENT_NODE) {
		    		NodeList items = channelNode.getChildNodes();
		    		for(int j=0; j<items.getLength(); j++) {
		    			Node itemNode = items.item(j);
		    			if(itemNode.getNodeType()==Node.ELEMENT_NODE) {
		    				Element item = (Element)itemNode;
		    				if(item.getTagName().equals("title")) {
		    					title = item.getTextContent();
		    				} else if(item.getTagName().equals("link")) {
		    					link = item.getTextContent();
		    				} else if(item.getTagName().equals("itunes:image")) {
		    					imageUrl = item.getAttribute("href");
		    				} else if(item.getTagName().equals("item")) {
		    					NodeList itemsValues = itemNode.getChildNodes();
			    				String itemTitle=null, itemUrl=null, itemGuid=null, itemPubDate=null, duration=null, type=null;
			    				for(int k=0; k<itemsValues.getLength(); k++) {
			    					Node itemsValuesNode = itemsValues.item(k);
			    					if(itemsValuesNode.getNodeType()==Node.ELEMENT_NODE) {
			    						Element e = (Element)itemsValuesNode;
			    						String tag = e.getTagName();
			    						if(tag.equals("title")) itemTitle = e.getTextContent();
			    						if(tag.equals("guid")) itemGuid = e.getTextContent();
			    						if(tag.equals("pubDate")) itemPubDate = e.getTextContent();
			    						if(tag.equals("itunes:duration")) duration = e.getTextContent();
			    						if(tag.equals("enclosure")) {
			    							itemUrl = e.getAttribute("url");
			    							type = e.getAttribute("type");
			    						}
			    					}
			    				}
			    				
			    				long dateLong = 0;
			    				if(itemTitle==null || itemGuid==null || itemUrl==null || type==null) {
			    					continue; // This is essential information! Can't be missing!
			    				}
			    				try {
			    					DateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.ENGLISH);
				    				Date d = format.parse(itemPubDate);
				    				dateLong = d.getTime();
			    				} catch(Exception e) {}
			    				
			    				PodcastEpisode episode = new PodcastEpisode(itemUrl, null, itemTitle, itemGuid, null, PodcastEpisode.STATUS_NEW, dateLong, duration, type);
			    				episodes.add(episode);
		    				}
		    			}
		    		}
		    	}
		    }
			return true;
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getLink() {
		return link;
	}
	
	public String getImageUrl() {
		return imageUrl;
	}
	
	public ArrayList<PodcastEpisode> getEpisodes() {
		return episodes;
	}
	
	public byte[] downloadImage(int listImageSize) {
		try {
			URLConnection connection = new URL(imageUrl).openConnection();
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			InputStream input = connection.getInputStream();
			Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeStream(input), listImageSize, listImageSize, true);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
			return output.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
