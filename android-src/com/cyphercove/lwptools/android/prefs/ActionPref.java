package com.cyphercove.lwptools.android.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

import java.util.ArrayList;

/**
 * A pref that simply calls its onPressed method when tapped. Must be subclassed.
 */
public abstract class ActionPref extends Pref{

	PreferenceScreen prefScreen;
	int summary;

	public ActionPref(SummaryMode summaryMode, int title, int summaryIfApplicable){
		this.key = "";
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
			boolean indented, int androidPreferenceLayoutChild, final int messageString,
			ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				onPressed();
                return true;
			}
		});
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
		return prefScreen;
	}

    protected abstract void onPressed();
	
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
