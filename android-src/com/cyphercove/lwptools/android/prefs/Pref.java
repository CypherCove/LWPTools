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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.ArrayList;

public abstract class Pref {
	public String key;
	public SummaryMode summaryMode;
	public int title;
	public Preference pref;
	
	protected CustomSummary customSummary;

	/**The type of the underlying preference that is saved. */
	public enum ValueType {
		NONE, BOOLEAN, INT, STRING
	}

	public abstract void resetToDefault();
	public abstract void updateSummary(Context context, SharedPreferences sharedPrefs);
	public abstract String getValueAsString(SharedPreferences sharedPrefs);
	public abstract void setValueInBatch(SharedPreferences.Editor editor, String value);
	public abstract ValueType getValueType();

	/** Prefs of ValueType.INT should override and return the proper value. */
	public int getValueAsInt(SharedPreferences sharedPrefs){
		return 0;
	}

	/** Prefs of ValueType.BOOLEAN should override and return the proper value. */
	public boolean getValueAsBoolean(SharedPreferences sharedPrefs){
		return false;
	}
	
	public void updateSummaryWithKey(Context context, SharedPreferences sharedPrefs,String keyToMatch){
		if (key.equals(keyToMatch) || (customSummary!=null && customSummary.matchKey(keyToMatch)) ) 
			updateSummary(context,sharedPrefs);
	}
	
	protected void updateCustomSummary(){
		if (pref==null){
			logSummaryUpdatedTooSoonWarning();
			return;
		}
		if (customSummary!=null){
			pref.setSummary(customSummary.getSummary());
		}
	}
	
	public interface CustomSummary{
		public CharSequence getSummary();
		/** If updateSummaryWithKey called, summary will only be updated if this returns true. This can be used to prevent unnecessary updates, 
		 * which may be necessary if getSummary() performs an hierarchy refresh.*/
		boolean matchKey(String key);
	}
	
	public enum SummaryMode {
		NoneOrCustom,
		Value,
		ValueInFormattedString,
		ArrayValue,
		ArrayValueInFormattedString,
		String,
		Color,
		ColorWithString,
		LabeledColor,
		LabeledColorWithString,
		AdvancedColor,
		AdvancedColorWithString
	}
	
	protected void logUnsupportedSummaryWarning(){
		Log.w(this.getClass().getName(),"Unsupported summary mode. Summary left blank.");
	}
	
	protected void logSummaryUpdatedTooSoonWarning(){
		Log.w(this.getClass().getName(),"Cannot update summary before adding to hierarchy. Summary left blank.");
	}
	
	protected void logPrefMethodCalledTooSoonWarning(){
		Log.w(this.getClass().getName(),"Cannot modify or get preference before Create is called.");
	}

	public abstract Preference create(PreferenceManager manager, Context context, SharedPreferences sharedPrefs,
							 boolean indented, PrefResources resources, ArrayList<Pref> listToAddTo);
	
	public void setCustomSummary(CustomSummary customSummary){
		this.customSummary = customSummary;
	}
	
	public void setEnabled(boolean enabled){
		if (pref!=null)
			pref.setEnabled(enabled);
		else
			logPrefMethodCalledTooSoonWarning();
	}
	
	private Pref dependencyPref;
	private boolean inclusiveDependency;
	private String[] stringDependencies;
	private int[] intDependencies;
	private boolean booleanDependency;
	private SharedPreferences sharedPrefs;
	
	/**
	 * @param dependencyPref A pref whose current value this pref's enabled status depends on. The pref must be of
	 *                       ValueType.STRING for this to work properly.
	 * @param dependentPrefs Optional list to add pref to.
	 * @param inclusive True means the pref is enabled if the dependencyPref equals one of the dependencies. False means disabled.
	 */
	public void setDependency(Pref dependencyPref, boolean inclusive,ArrayList<Pref> dependentPrefs,String... dependencies){
		intDependencies = null;

		this.dependencyPref = dependencyPref;
		inclusiveDependency = inclusive;
		stringDependencies = dependencies;

		checkDependency();

		if (dependentPrefs!=null)
			dependentPrefs.add(this);
	}

	/**
	 * @param dependencyPref A pref whose current value this pref's enabled status depends on. The pref must be of
	 *                       ValueType.INT for this to work properly.
	 * @param dependentPrefs Optional list to add pref to.
	 * @param inclusive True means the pref is enabled if the dependencyPref equals one of the dependencies. False means disabled.
	 */
	public void setDependency(Pref dependencyPref, boolean inclusive,ArrayList<Pref> dependentPrefs,int... dependencies){
		stringDependencies = null;

		this.dependencyPref = dependencyPref;
		inclusiveDependency = inclusive;
		intDependencies = dependencies;

		checkDependency();

		if (dependentPrefs!=null)
			dependentPrefs.add(this);
	}

	/**
	 * @param dependentPrefs Optional list to add pref to.
	 * @param dependencyPref
	 * @param dependency This pref will be enabled when dependencyPref = dependency.
	 */
	public void setDependency(CheckBoxPref dependencyPref,ArrayList<Pref> dependentPrefs, boolean dependency){
		this.dependencyPref = dependencyPref;
		booleanDependency = dependency;

		checkDependency();

		if (dependentPrefs!=null)
			dependentPrefs.add(this);
	}

	/**Update enabled status of pref. All prefs should be checked in the order they appear, so grandchildren are disabled appropriately.*/
	public boolean checkDependency(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();

		if (dependencyPref==null){
			Log.w(this.getClass().getName(),"checkDependency called before setting a dependency.");
			return false;
		}

		//Assuming prefs are checked in order: Always disable child if parent is disabled
		if (!dependencyPref.pref.isEnabled()) {
			pref.setEnabled(false);
			return false;
		}

		switch (dependencyPref.getValueType()){
			case NONE:
				return true; //A none preference should never be the dependency pref
			case STRING:
				String stringValue = dependencyPref.getValueAsString(getSharedPrefs());
				for (String s : stringDependencies){
					if (s.equals(stringValue)){
						pref.setEnabled(inclusiveDependency);
						return inclusiveDependency;
					}
				}
				pref.setEnabled(!inclusiveDependency);
				return !inclusiveDependency;
			case INT:
				int intValue = dependencyPref.getValueAsInt(getSharedPrefs());
				for (int i : intDependencies){
					if (i==intValue){
						pref.setEnabled(inclusiveDependency);
						return inclusiveDependency;
					}
				}
				pref.setEnabled(!inclusiveDependency);
				return !inclusiveDependency;
			case BOOLEAN:
				boolean booleanValue = dependencyPref.getValueAsBoolean(getSharedPrefs());
				if (booleanValue==booleanDependency){
					pref.setEnabled(true);
					return true;
				}
				pref.setEnabled(false);
				return false;
		}
		
		Log.w(this.getClass().getName(),"checkDependency called before setting a dependency.");
		return false;
	}

	public SharedPreferences getSharedPrefs() {
		return sharedPrefs;
	}
	protected void setSharedPrefs(SharedPreferences sharedPrefs) {
		this.sharedPrefs = sharedPrefs;
	}
	
}
