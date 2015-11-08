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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class IntentPref extends Pref{
	
	PreferenceScreen prefScreen;
	int summary;
	Class<? extends Activity> targetActivityClass;
	String url;
	int urlResource;
	
	/**
	 * Creates an Intent that opens a URL.
	 * @param summaryMode
	 * @param title
	 * @param summaryIfApplicable
	 * @param urlResource Android String resource of URL
	 */
	public IntentPref(SummaryMode summaryMode, int title, int summaryIfApplicable, 
			int urlResource){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.urlResource = urlResource;
	}
	
	/**
	 * Creates an Intent that opens a URL.
	 * @param summaryMode
	 * @param title
	 * @param summaryIfApplicable
	 * @param url
	 */
	public IntentPref(SummaryMode summaryMode, int title, int summaryIfApplicable, 
			String url){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.url = url;
	}
	
	/**
	 * Creates an Intent that opens an activity
	 * @param summaryMode
	 * @param title
	 * @param summaryIfApplicable
	 * @param targetActivityClass
	 */
	public IntentPref(SummaryMode summaryMode, int title, int summaryIfApplicable, 
			Class<? extends Activity> targetActivityClass){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.targetActivityClass = targetActivityClass;
	}
	
	@SuppressWarnings("deprecation")
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setKey(key);
		prefScreen.setTitle(title);
		
		Intent intent;
		if (targetActivityClass==null){
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(url==null ? context.getString(urlResource) : url));
		} else {
			intent = new Intent(context, targetActivityClass);
		}
		prefScreen.setIntent(intent);
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
		return prefScreen;
	}

	@Override
	public void updateSummary(Context context, SharedPreferences sharedPrefs) {
		if (prefScreen==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case String:
				prefScreen.setSummary(summary);
				break;
			default:
				logUnsupportedSummaryWarning();
				break;
			}
		}
	}

	@Override
	public void resetToDefault() {
		
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return null;
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		
	}

	@Override
	public ValueType getValueType() {
		return ValueType.NONE;
	}
}
