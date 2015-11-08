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
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutDialogPref extends Pref{

	PreferenceScreen prefScreen;
	int versionStringFormattable;
	int versionFallbackString;
	int iconRes;
	int messageHtmlFormattable;
	int okString;
	
	public AboutDialogPref(int title, int versionStringFormattable, 
			int versionFallbackString, int iconRes, int messageHtmlFormattable, int okString){
		this.key = "";
		this.summaryMode = SummaryMode.NoneOrCustom;
		this.title = title;
		this.versionStringFormattable = versionStringFormattable;
		this.versionFallbackString = versionFallbackString;
		this.iconRes = iconRes;
		this.messageHtmlFormattable = messageHtmlFormattable;
		this.okString = okString;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, SharedPreferences sharedPrefs,
								   boolean indented, PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				showDialog(context);
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
	
	private void showDialog(final Context context){
		String title;
		try {
			title = String.format(context.getString(versionStringFormattable), 
					context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName);
		} catch (Exception e){
			title = context.getString(versionFallbackString);
		}
		AlertDialog dialog = new AlertDialog.Builder(context)
		.setTitle(title)
		.setIcon(context.getResources().getDrawable(iconRes))
		.setMessage(Html.fromHtml(context.getString(messageHtmlFormattable)))
		.setPositiveButton(okString, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		})
		.show();
		TextView view = ((TextView)dialog.findViewById(android.R.id.message));
		view.setMovementMethod(LinkMovementMethod.getInstance());
		view.setTextColor(view.getTextColors().getDefaultColor());
		view.setLinkTextColor(0xff1e84ff);
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
