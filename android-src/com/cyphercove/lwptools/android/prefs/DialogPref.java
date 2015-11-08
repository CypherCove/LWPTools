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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;

public class DialogPref extends Pref{

	PreferenceScreen prefScreen;
	int summary;
	
	public interface DialogPrefListener {
		public void onOK();
		public void onCancel();
	}
	
	public DialogPref(SummaryMode summaryMode, int title, int summaryIfApplicable){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
	}

	@Override
	public Preference create(PreferenceManager manager, Context context, SharedPreferences sharedPrefs, boolean indented, PrefResources resources, ArrayList<Pref> listToAddTo) {
		Log.w("PrefRoot", "Cannot add DialogPref using this method. Blank preference will be used.");
		Preference preference = new Preference(context);
		preference.setTitle(title);
		return preference;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
			boolean indented, final PrefResources resources, final int messageString,
			final DialogPrefListener listener, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				new AlertDialog.Builder(context)
		        .setTitle(title)
		        .setMessage(context.getString(messageString))
		        .setPositiveButton(context.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	listener.onOK();
		            }
		        })
		        .setNegativeButton(context.getText(android.R.string.cancel), new DialogInterface.OnClickListener() {
		            public void onClick(DialogInterface dialog, int whichButton) {
		            	listener.onCancel();
		            }
		        })
		        .show();
				return true;
				
			}
		});
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
		return prefScreen;
	}
	
	@Override
	public void resetToDefault() {
		//do nothing
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
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return "";
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
	}


	@Override
	public ValueType getValueType() {
		return ValueType.NONE;
	}
}
