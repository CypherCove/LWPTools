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

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.preference.PreferenceActivity.Header;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class UniversalHeaderAdapter extends ArrayAdapter<Header> {

	private LayoutInflater mInflater;
	int mHeaderLayoutResource;
	
	public UniversalHeaderAdapter(Context context, List<Header> objects, int headerLayoutResource) {
		super(context, 0, objects);
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mHeaderLayoutResource = headerLayoutResource;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		Header header = getItem(position);
		int headerType = header.extras.getInt(UniversalHeader.HEADER_TYPE_KEY);
		View view = null;

		switch (headerType) {
		case UniversalHeader.TYPE_CATEGORY:
			view = mInflater.inflate(android.R.layout.preference_category, parent, false);
			((TextView) view.findViewById(android.R.id.title)).setText(header.getTitle(getContext()
					.getResources()));
			break;

		case UniversalHeader.TYPE_NORMAL:
		case UniversalHeader.TYPE_ABOUT_DIALOG:
		case UniversalHeader.TYPE_ONE_TIME_INTENT:
			int customLayoutResource = header.extras==null? 0 : header.extras.getInt(UniversalHeader.HEADER_CUSTOM_LAYOUT_RESOURCE_KEY);
			
			view = mInflater.inflate(customLayoutResource==0?mHeaderLayoutResource:customLayoutResource, parent, false);
			((ImageView) view.findViewById(android.R.id.icon)).setImageResource(header.iconRes);
			TextView titleView = ((TextView) view.findViewById(android.R.id.title));
			titleView.setText(header.getTitle(getContext().getResources()));
			TextView summaryView = ((TextView) view.findViewById(android.R.id.summary));
			if (header.summaryRes!=0){
				summaryView.setText(header.getSummary(getContext().getResources()));
			} else {
				summaryView.setVisibility(View.GONE);
			}

			break;
		}

		return view;
	}

}
