/*
 * ImgPagur
 * Copyright (C) 2013 Bryan Emmanuel
 * 
 * This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  Bryan Emmanuel piusvelte@gmail.com
 */
package com.piusvelte.imgpagur;

import java.util.ArrayList;
import java.util.List;

import org.mcsoxford.rss.MediaThumbnail;
import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public class RowImagesLoader extends AsyncTaskLoader<List<ImgListItem>> {
	
	String link = null;
	List<ImgListItem> rowImages = null;

	public RowImagesLoader(Context context, String link) {
		super(context);
		this.link = link;
	}

	@Override
	public List<ImgListItem> loadInBackground() {
		rowImages = new ArrayList<ImgListItem>();
		if (link != null) {
			RSSReader reader = new RSSReader();
			try {
				RSSFeed feed = reader.load(link);
				List<RSSItem> items = feed.getItems();
				for (RSSItem item : items) {
					List<MediaThumbnail> thumbnails = item.getThumbnails();
					if ((thumbnails != null) && (thumbnails.size() > 0)) {
						rowImages.add(new ImgListItem(thumbnails.get(0).getUrl().toString(), item.getTitle(), item.getContent()));
					}
				}
			} catch (RSSReaderException e) {
				e.printStackTrace();
			}
			reader.close();
		}
		return rowImages;
	}

	@Override
	public void deliverResult(List<ImgListItem> rowImages) {
		this.rowImages = rowImages;
		if (isStarted()) {
			super.deliverResult(rowImages);
		}
	}

	@Override
	protected void onStartLoading() {
		if (takeContentChanged() || (rowImages == null)) {
			forceLoad();
		} else if (rowImages != null) {
			deliverResult(rowImages);
		}
	}

	@Override
	protected void onReset() {
		super.onReset();
		onStopLoading();
		rowImages = null;
	}

	@Override
	protected void onStopLoading() {
		cancelLoad();
	}

}
