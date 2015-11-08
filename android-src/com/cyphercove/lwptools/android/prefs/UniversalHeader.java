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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity.Header;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class UniversalHeader {

	Header header;
	
	public static final String HEADER_CUSTOM_LAYOUT_RESOURCE_KEY = "com.cyphercove.lwptools.android.prefs.UniversalHeader:headerCustomLayoutResrouce";
	public static final String HEADER_TYPE_KEY = "com.cyphercove.lwptools.android.prefs.UniversalHeader:headerType";
	public static final String ABOUT_DIALOG_VERSION_RESOURCE = "com.cyphercove.lwptools.android.prefs.UniversalHeader:aboutDialogVersionResource";
	public static final String ABOUT_DIALOG_HTML_MESSAGE_RESOURCE = "com.cyphercove.lwptools.android.prefs.UniversalHeader:aboutDialogHtmlMessageResource";
	public static final String ABOUT_DIALOG_ICON_RESOURCE = "com.cyphercove.lwptools.android.prefs.UniversalHeader:aboutDialogIconResource";
	public static final String ONE_TIME_INTENT_PREF_NAME_PRESSED = "com.cyphercove.lwptools.android.prefs.UniversalHeader:oneTimeIntentPrefNamePressed";
	
	public static final int TYPE_CATEGORY = 0;
	public static final int TYPE_NORMAL = 1;
	public static final int TYPE_ABOUT_DIALOG = 2;
	public static final int TYPE_ONE_TIME_INTENT = 3;
	
	private String legacyAction;
	String fragment;
	
	Intent intent;
	
	int titleRes;
	int summaryRes;
	int iconRes;
	int customLayoutRes;
	int headerType = TYPE_NORMAL;
	
	int aboutDialogVersionRes;
	int aboutDialogHtmlMessageRes;
	int aboutDialogIconRes;
	
	String oneTimeIntentPrefNamePressed;
	String oneTimeIntentUrl;
	
	/**
	 * Creates a header that is a category
	 */
	public UniversalHeader(int titleRes){
		this(titleRes, 0, 0, 0);
		headerType = TYPE_CATEGORY;
	}
	
	/**
	 * Creates a header that will open a fragment or preferences screen.
	 */
	public UniversalHeader(int titleRes, int summaryRes, int iconRes,
			String legacyAction, String fragment, int customLayoutRes){
		this(titleRes, summaryRes, iconRes, customLayoutRes);
		this.fragment = fragment;
		this.legacyAction = legacyAction;
		headerType = TYPE_NORMAL;
	}
	
	/**
	 * Creates a header that will open an intent
	 */
	public UniversalHeader(int titleRes, int summaryResource, int iconRes, 
			Intent intent, int customLayoutRes ){
		this(titleRes, summaryResource, iconRes, customLayoutRes);
		this.intent = intent;
		headerType = TYPE_NORMAL;
	}
	
	/**
	 * Creates a header that will open an Action.VIEW activity intent of the URL, and is only visible if it's never been pressed
	 */
	public UniversalHeader(int titleRes, int summaryResource, int iconRes, 
			String url, int customLayoutRes, String prefNamePressed){
		this(titleRes, summaryResource, iconRes, customLayoutRes);
		oneTimeIntentUrl = url;
		headerType = TYPE_ONE_TIME_INTENT;
		oneTimeIntentPrefNamePressed = prefNamePressed;
	}
	
	/**
	 * Creates a header that will open an about dialog
	 */
	public UniversalHeader(int titleRes, int summaryResource, int headerIconRes, 
			int formattableVersionStringRes, int bodyHtmlRes, int dialogIconRes, int customLayoutRes ){
		this(titleRes, summaryResource, headerIconRes, customLayoutRes);
		this.aboutDialogVersionRes = formattableVersionStringRes;
		this.aboutDialogHtmlMessageRes = bodyHtmlRes;
		this.aboutDialogIconRes = dialogIconRes;
		headerType = TYPE_ABOUT_DIALOG;
	}
	
	private UniversalHeader(int titleRes, int summaryResource, int iconRes, int customLayoutRes){
		this.titleRes = titleRes;
		this.summaryRes = summaryResource;
		this.iconRes = iconRes;
		this.customLayoutRes = customLayoutRes;
	}
	
	public String getLegacyAction(){
		return legacyAction;
	}
	
	@SuppressWarnings("deprecation")
	public void addLegacyHeader(final PreferenceActivity prefsActivity, int headerLayoutResource, PrefRoot root){
		switch(headerType){
		case TYPE_CATEGORY:
			PreferenceCategory category = new PreferenceCategory(prefsActivity);
			category.setTitle(titleRes);
			root.addPreference(category);
			break;
		case TYPE_NORMAL:
			PreferenceScreen prefScreen = prefsActivity.getPreferenceManager().createPreferenceScreen(prefsActivity);
			prefScreen.setTitle(titleRes);
			if (summaryRes!=0)
				prefScreen.setSummary(summaryRes);
			if (intent!=null){
				prefScreen.setIntent(intent);
			} else if (legacyAction!=null){
				Intent i;
				i = new Intent(prefsActivity,prefsActivity.getClass());
				i.setAction(legacyAction);
				prefScreen.setIntent(i);
			}
			if (customLayoutRes!=0){
				prefScreen.setLayoutResource(customLayoutRes);
			} else {
				prefScreen.setLayoutResource(headerLayoutResource);
			}
			root.addPreference(prefScreen);
			break;
		case TYPE_ABOUT_DIALOG:
			Preference aboutDialogPref = new Preference(prefsActivity);
			aboutDialogPref.setTitle(titleRes);
			if (summaryRes!=0)
				aboutDialogPref.setSummary(summaryRes);
			if (customLayoutRes!=0){
				aboutDialogPref.setLayoutResource(customLayoutRes);
			}else {
				aboutDialogPref.setLayoutResource(headerLayoutResource);
			}
			aboutDialogPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					showAboutDialog(prefsActivity,aboutDialogVersionRes,aboutDialogHtmlMessageRes,aboutDialogIconRes);
					return false;
				}
			});
			root.addPreference(aboutDialogPref);
			break;
		case TYPE_ONE_TIME_INTENT:
			final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(prefsActivity);
			if (sharedPrefs.getBoolean(oneTimeIntentPrefNamePressed, false))
				return; //it's been pressed before. don't create the pref.
			Preference oneTimeIntentPref = new Preference(prefsActivity);
			oneTimeIntentPref.setTitle(titleRes);
			if (summaryRes!=0)
				oneTimeIntentPref.setSummary(summaryRes);
			if (customLayoutRes!=0){
				oneTimeIntentPref.setLayoutResource(customLayoutRes);
			}else {
				oneTimeIntentPref.setLayoutResource(headerLayoutResource);
			}
			oneTimeIntentPref.setOnPreferenceClickListener(new OnPreferenceClickListener(){
				@Override
				public boolean onPreferenceClick(Preference arg0) {
					SharedPreferences.Editor editor = sharedPrefs.edit();
					editor.putBoolean(oneTimeIntentPrefNamePressed, true);
					editor.commit();
					Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(oneTimeIntentUrl));
					prefsActivity.startActivity(i);
					return false;
				}
			});
			root.addPreference(oneTimeIntentPref);
			break;
		}

	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Header createHeader(Context context){
		if (headerType==TYPE_ONE_TIME_INTENT){
			final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
			if (sharedPrefs.getBoolean(oneTimeIntentPrefNamePressed, false))
				return null; //it's been pressed before. don't create the header.
		}
		
		header = new Header();
		header.title = context.getString(titleRes);
		if (headerType==TYPE_ONE_TIME_INTENT){
			header.intent = new Intent(Intent.ACTION_VIEW);
			header.intent.setData(Uri.parse(oneTimeIntentUrl));
		} else if (intent!=null){
			header.intent = intent;
		} else if (fragment!=null){
			header.fragment = fragment;
		}
		header.iconRes = iconRes;
		header.summaryRes = summaryRes;
		
		Bundle extras = new Bundle();
		extras.putInt(HEADER_TYPE_KEY, headerType);
		
		if (customLayoutRes != 0){
			extras.putInt(HEADER_CUSTOM_LAYOUT_RESOURCE_KEY,customLayoutRes);
		}
		
		if (headerType==TYPE_ABOUT_DIALOG){
			extras.putInt(ABOUT_DIALOG_VERSION_RESOURCE, aboutDialogVersionRes);
			extras.putInt(ABOUT_DIALOG_HTML_MESSAGE_RESOURCE, aboutDialogHtmlMessageRes);
			extras.putInt(ABOUT_DIALOG_ICON_RESOURCE, aboutDialogIconRes);
		}
		
		if (headerType==TYPE_ONE_TIME_INTENT){
			extras.putString(ONE_TIME_INTENT_PREF_NAME_PRESSED, oneTimeIntentPrefNamePressed);
		}
		
		header.extras = extras;
		return header;
	}

	public static void showAboutDialog(final Context context, int versionStringFormattable, int messageHtmlFormattable, int iconRes){
		String title;
		try {
			title = String.format(context.getString(versionStringFormattable), 
					context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName);
		} catch (Exception e){
			title = "";
		}
		AlertDialog dialog = new AlertDialog.Builder(context)
		.setTitle(title)
		.setIcon(context.getResources().getDrawable(iconRes))
		.setMessage(Html.fromHtml(context.getString(messageHtmlFormattable)))
		.setPositiveButton(context.getText(android.R.string.ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
			}
		})
		.show();
		TextView view = ((TextView)dialog.findViewById(android.R.id.message));
		view.setMovementMethod(LinkMovementMethod.getInstance());
		view.setTextColor(view.getTextColors().getDefaultColor());
		view.setLinkTextColor(0xff1e84ff);
	}

}
