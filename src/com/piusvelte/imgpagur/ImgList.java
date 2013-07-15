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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListFragment;
import com.squareup.picasso.Picasso;

public class ImgList extends SherlockListFragment implements LoaderManager.LoaderCallbacks<List<ImgListItem>>, OnClickListener {

	public interface ImgListListener {

		public void addFeed(String title, String link);

	}
	
	public class ImgListAdapter extends ArrayAdapter<ImgListItem> {

		public ImgListAdapter(Context context, int textViewResourceId, List<ImgListItem> rowImages) {
			super(context, textViewResourceId, rowImages);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View row;
			if (convertView == null) {
				row = (View) (LayoutInflater.from(parent.getContext())).inflate(R.layout.image_item, null);
			} else {
				row = (View) convertView;
			}
			ImgListItem imgListItem = getItem(position);
			ImageView image = (ImageView) row.findViewById(R.id.image);
			TextView textView = (TextView) row.findViewById(R.id.title);
			textView.setText(imgListItem.getTitle());
			textView = (TextView) row.findViewById(R.id.content);
			textView.setText(imgListItem.getContent());
			Picasso.with(getContext()).load(imgListItem.getThumbnail()).resizeDimen(R.dimen.list_image, R.dimen.list_image).into(image);
			return row;
		}

	}

	private List<ImgListItem> imgListItems = new ArrayList<ImgListItem>();
	private ImgListListener callback;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			callback = (ImgListListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement AccountsListener");
		}
	}

	private TextView textEmpty;
	private EditText editTitle;
	private EditText editLink;
	private Button buttonSubmit;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View rootView = inflater.inflate(R.layout.image_list, container, false);
		textEmpty = (TextView) rootView.findViewById(android.R.id.empty);
		editTitle = (EditText) rootView.findViewById(R.id.title);
		editLink = (EditText) rootView.findViewById(R.id.link);
		buttonSubmit = (Button) rootView.findViewById(R.id.submit);
		buttonSubmit.setOnClickListener(this);
		return rootView;
	}

	private ImgListAdapter adapter;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		adapter = new ImgListAdapter(getActivity(), R.layout.image_item, imgListItems);
		setListAdapter(adapter);
		Bundle extras = getArguments();
		if (extras != null) {
			String link = extras.getString(EXTRA_LINK);
			if (link != null) {
				editTitle.setVisibility(View.GONE);
				editLink.setVisibility(View.GONE);
				buttonSubmit.setVisibility(View.GONE);
				getLoaderManager().initLoader(0, extras, this);
				return;
			}
		}
		textEmpty.setVisibility(View.GONE);
	}

	public static final String EXTRA_LINK = "link";

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		super.onListItemClick(list, view, position, id);
		startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(adapter.getItem(position).getThumbnail())));
	}

	@Override
	public Loader<List<ImgListItem>> onCreateLoader(int arg0, Bundle arg1) {
		if (arg1 != null) {
			return new RowImagesLoader(getActivity(), arg1.getString(EXTRA_LINK));
		} else {
			return null;
		}
	}

	@Override
	public void onLoadFinished(Loader<List<ImgListItem>> arg0,
			List<ImgListItem> arg1) {
		imgListItems.clear();
		imgListItems.addAll(arg1);
		if (imgListItems.isEmpty()) {
			textEmpty.setText(R.string.no_images);
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onLoaderReset(Loader<List<ImgListItem>> arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.submit) {
			String title = editTitle.getText().toString();
			String link = editLink.getText().toString();
			if ((title != null)
					&& (title.length() > 0)) {
				if ((link != null)
						&& (link.length() > 0)) {
					if (callback != null) {
						editTitle.setVisibility(View.GONE);
						editLink.setVisibility(View.GONE);
						v.setVisibility(View.GONE);
						callback.addFeed(title, link);
						Bundle extras = new Bundle();
						extras.putString(EXTRA_LINK, link);
						textEmpty.setVisibility(View.VISIBLE);
						getLoaderManager().initLoader(0, extras, this);
					}
				} else {
					Toast.makeText(getActivity(), "A link is required", Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(getActivity(), "A title is required", Toast.LENGTH_SHORT).show();
			}
		}
	}

}
