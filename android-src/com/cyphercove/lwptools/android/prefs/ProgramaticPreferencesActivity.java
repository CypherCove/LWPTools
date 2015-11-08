/*******************************************************************************
 * Copyright 2015 Cypher Cove, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.cyphercove.lwptools.android.prefs;

import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.ListAdapter;

public abstract class ProgramaticPreferencesActivity extends PreferenceActivity{
	
	private List<UniversalHeader> mUniversalHeaders;
	private List<Header> mHeaders;

	private OnRebuildPrefScreenListener rebuildPrefScreenOnResumeListener = null;

	@Override
	public void onCreate(Bundle savedInstanceState){
		mUniversalHeaders = onCreateUniversalHeaders(); //Need the list before super.onCreate, because that calls onBuildHeaders, where the list is used.

		super.onCreate(savedInstanceState);

		//Load legacy headers, or the appropriate preferences screen if not on Honeycomb or later.
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			String action = getIntent().getAction();
			if (action != null){
				constructLegacyFragmentAlternativeFromActionName(action);
			} else { //Is showing a headers view
				constructLegacyHeadersScreen();
			}
		}

	}

	@Override
	public void onResume(){
		super.onResume();
		if (rebuildPrefScreenOnResumeListener!=null){
			rebuildPrefScreenOnResumeListener.onRebuildPrefScreen();
		}
	}

	@Override
	public boolean isValidFragment(String fragmentName){
		return true;
	}

	public void enableRebuildPrefScreenOnResume(OnRebuildPrefScreenListener onRebuildPrefScreenListener){
		rebuildPrefScreenOnResumeListener = onRebuildPrefScreenListener;
	}

	public void disableRebuildPrefScreenOnResume(){
		rebuildPrefScreenOnResumeListener = null;
	}

	protected abstract void constructLegacyFragmentAlternativeFromActionName(String actionName);

	protected OnRebuildPrefScreenListener getLegacyOnRebuildPrefScreenListener(final String actionName){
		return new OnRebuildPrefScreenListener(){
			@Override
			public void onRebuildPrefScreen() {
				constructLegacyFragmentAlternativeFromActionName(actionName);
			}

		};
	}

	/**
	 *
	 * @return whether any headers were used.
	 */
	@SuppressWarnings("deprecation")
	private final void constructLegacyHeadersScreen(){
		PrefRoot root = new PrefRoot(getPreferenceManager(), this, getSharedPreferences(), getIndentLayoutResource(),
				getChooseColorStringResource(), getAdvancedColorPickerStringResources(), null);

		for (UniversalHeader hf : mUniversalHeaders){
			hf.addLegacyHeader(this, getHeaderLayoutResource(), root);
		}

		setPreferenceScreen(root.root);
	}

	@Override
	public final void onBuildHeaders(List<Header> target) {
		for (UniversalHeader hf : mUniversalHeaders){
			Header h = hf.createHeader(this);
			if (h!=null)
				target.add( h );
		}
		mHeaders = target;
	}

	@Override
	public void setListAdapter(ListAdapter adapter) {
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
			int i, count;

			if (mHeaders == null) {
				mHeaders = new ArrayList<Header>();
				// When the saved state provides the list of headers,
				// onBuildHeaders is not called
				// so we build it from the adapter given, then use our own adapter

				count = adapter.getCount();
				for (i = 0; i < count; ++i)
					mHeaders.add((Header) adapter.getItem(i));
			}

			super.setListAdapter(new UniversalHeaderAdapter(this, mHeaders, getHeaderLayoutResource()));
		} else {
			super.setListAdapter(adapter);
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onHeaderClick(Header header, int position){
		if (header.extras!=null && header.extras.getInt(UniversalHeader.HEADER_TYPE_KEY)== UniversalHeader.TYPE_ABOUT_DIALOG){
			int versionStringResource = header.extras.getInt(UniversalHeader.ABOUT_DIALOG_VERSION_RESOURCE);
			int bodyResource = header.extras.getInt(UniversalHeader.ABOUT_DIALOG_HTML_MESSAGE_RESOURCE);
			int iconResource = header.extras.getInt(UniversalHeader.ABOUT_DIALOG_ICON_RESOURCE);
			UniversalHeader.showAboutDialog(this, versionStringResource, bodyResource,
					iconResource);
		}
		if (header.extras!=null && header.extras.getInt(UniversalHeader.HEADER_TYPE_KEY)== UniversalHeader.TYPE_ONE_TIME_INTENT){
			String prefNamePressed = header.extras.getString(UniversalHeader.ONE_TIME_INTENT_PREF_NAME_PRESSED);
			PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(prefNamePressed, true).commit();
		}
		super.onHeaderClick(header, position);
	}

	protected abstract SharedPreferences getSharedPreferences();
	/**
	 * Override and create a list of header prefs here.
	 * @return Provide a List of HeaderPrefs to populate in Headers list.
	 */
	protected abstract List<UniversalHeader> onCreateUniversalHeaders();
	protected abstract int getIndentLayoutResource();
	protected abstract int getChooseColorStringResource();
	protected abstract AdvancedColorPickerDialog.TextResources getAdvancedColorPickerStringResources();
	
	/**
	 * @return A layout XML resource that must have a TextViews with IDs @+android:id/title and @+android:id/summary, 
	 * and an ImageView with ID @+android:id/icon.
	 */
	protected abstract int getHeaderLayoutResource();

}
