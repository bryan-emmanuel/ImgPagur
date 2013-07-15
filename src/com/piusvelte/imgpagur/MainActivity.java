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
import java.util.Locale;
import java.util.Map;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;

public class MainActivity extends SherlockFragmentActivity implements ImgList.ImgListListener, TabListener {

	ViewPager viewPager;
	ImgPagurPagerAdapter adapter;
	List<ImgFeed> imgFeeds = new ArrayList<ImgFeed>();

	public class ImgFeed {

		String title;
		String link;

		public ImgFeed(String title, String link) {
			this.title = title;
			this.link = link;
		}

	}
	
	ActionBar actionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//TODO setTheme
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		SharedPreferences sharedPrefs = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE);

		for (Map.Entry<String, String> entry : ((Map<String, String>) sharedPrefs.getAll()).entrySet()) {
			imgFeeds.add(new ImgFeed(entry.getKey(), entry.getValue()));
		}

		viewPager = (ViewPager) findViewById(R.id.pager);

		adapter = new ImgPagurPagerAdapter(getSupportFragmentManager());
		
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		viewPager.setAdapter(adapter);
		viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		
		});

		for (int i = 0, c = adapter.getCount(); i < c; i++) {
			actionBar.addTab(actionBar.newTab()
					.setText(adapter.getPageTitle(i))
					.setTabListener(this));
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE).edit();
		editor.clear();
		for (ImgFeed imgFeed : imgFeeds) {
			editor.putString(imgFeed.title, imgFeed.link);
		}
		editor.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.menu_remove) {
			int currentItem = viewPager.getCurrentItem();
			if (currentItem < (adapter.getCount() - 1)) {
				imgFeeds.remove(currentItem);
				adapter.notifyDataSetChanged();
				actionBar.removeTabAt(currentItem);
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public class ImgPagurPagerAdapter extends FragmentPagerAdapter {

		public ImgPagurPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {
			Fragment fragment = new ImgList();
			if (arg0 < imgFeeds.size()) {
				Bundle extras = new Bundle();
				extras.putString(ImgList.EXTRA_LINK, imgFeeds.get(arg0).link);
				fragment.setArguments(extras);
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// add 1 to include a new tab for adding a new feed
			return imgFeeds.size() + 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			if (position < imgFeeds.size()) {
				return imgFeeds.get(position).title.replace("_", " ").toUpperCase(l);
			} else {
				return getString(R.string.tab_new).toUpperCase(l);
			}
		}

	}

	@Override
	public void addFeed(String title, String link) {
		int updateTabAt = adapter.getCount() - 1;
		imgFeeds.add(new ImgFeed(title.replace(" ", "_"), link));
		adapter.notifyDataSetChanged();
		actionBar.getTabAt(updateTabAt).setText(adapter.getPageTitle(updateTabAt));
		actionBar.addTab(actionBar.newTab()
				.setText(adapter.getPageTitle(++updateTabAt))
				.setTabListener(this));
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction ft) {
		viewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

}
