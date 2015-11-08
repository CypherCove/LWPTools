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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/** For selecting an integer from 0 to 359. */
public class RotaryPref extends Pref{

	public int def;
	PreferenceScreen prefScreen;
	int summary;
	int dialogTitle;
	boolean showValue;
	RotaryPrefValueFormatter formatter = new RotaryPrefValueFormatter(){
		@Override
		public String formatValue(int value) {
			return Integer.toString(value)+"\u00B0";
		}
		
	};
	
	/** Modifies the value (between 0 to 359) into the value shown in the summary and in the dialog. */
	public interface RotaryPrefValueFormatter {
		String formatValue(int value);
	}
		
	public RotaryPref(String key, int def, SummaryMode summaryMode, int title, int summaryIfApplicable, 
			int dialogTitle, boolean showValue){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.dialogTitle = dialogTitle;
		this.showValue = showValue;

	}
	
	public RotaryPref(String key, int def, SummaryMode summaryMode, int title, int summaryIfApplicable, 
			int dialogTitle, boolean showValue, RotaryPrefValueFormatter formatter){
		this(key,def,summaryMode,title,summaryIfApplicable,dialogTitle,showValue);
		this.formatter = formatter;
	}
	
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				new RotaryDialog(context, RotaryPref.this, formatter,
						dialogTitle, getValue(sharedPrefs), showValue).show();
				return false;
			}
		});
		
		updateSummary(context, sharedPrefs);
		
		if (indented)
			prefScreen.setLayoutResource(resources.androidPreferenceLayoutChild);
		
		if (listToAddTo!=null)
			listToAddTo.add(this);
		
		return prefScreen;
	}
	
	public void updateSummary(Context context, SharedPreferences sharedPrefs){
		if (prefScreen==null){
			logSummaryUpdatedTooSoonWarning();
		} else {
			switch (summaryMode){
			case NoneOrCustom:
				updateCustomSummary();
				break;
			case Value:
				prefScreen.setSummary(getFormattedValue(sharedPrefs));
				break;
			case ValueInFormattedString:
				if (summary ==0){
					prefScreen.setSummary(getFormattedValue(sharedPrefs).replace("%", ""));
				} else {
					prefScreen.setSummary(String.format(context.getString(summary),getFormattedValue(sharedPrefs).replace("%", "")));
				}
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

	public int getValue(SharedPreferences sharedPrefs){
		return sharedPrefs.getInt(key, def);
	}
	
	public String getFormattedValue(SharedPreferences sharedPrefs){
		return formatter.formatValue(sharedPrefs.getInt(key, def));
	}

	public void setValue(int value){
		Editor editor = getSharedPrefs().edit();
		editor.putInt(key, value);
		editor.commit();
	}

	@Override
	public String getValueAsString(SharedPreferences sharedPrefs) {
		return String.valueOf(sharedPrefs.getInt(key, def));
	}

	@Override
	public void setValueInBatch(Editor editor, String value) {
		editor.putInt(key, Integer.valueOf(value));
	}

	public void resetToDefault(){
		if (getSharedPrefs()==null)
			logPrefMethodCalledTooSoonWarning();
		Editor editor = getSharedPrefs().edit();
		editor.putInt(key, def);
		editor.commit();
	}

	public class RotaryDialog extends Dialog implements RotaryView.OnAngleChangedListener{
		int title;
		RotaryPref rotaryPref;
		int startingValue;
		boolean showValue;

		RotaryPrefValueFormatter formatter;
		TextView valueTextView;
		RotaryView rotaryView;

		public RotaryDialog(Context context, RotaryPref rotaryPref,RotaryPrefValueFormatter formatter,
				int title,int startingValue,boolean showValue){
			super(context);
			this.rotaryPref = rotaryPref;
			this.formatter = formatter;
			this.title = title;
			this.startingValue = startingValue;
			this.showValue = showValue;
		}

		@SuppressWarnings("deprecation")
        @Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			float density = getContext().getResources().getDisplayMetrics().density;

			boolean holo = Build.VERSION.SDK_INT >= 11;
			int gutterSize = (int)(16 * density);

			LayoutParams wrapWrap = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams fillZeroWeight1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					0);
			fillZeroWeight1.weight = 1;

			LinearLayout verticalLayout= new LinearLayout(getContext());
			verticalLayout.setOrientation(LinearLayout.VERTICAL);
			verticalLayout.setGravity(Gravity.CENTER);
			verticalLayout.setLayoutParams(wrapWrap);
			setContentView(verticalLayout);

			valueTextView = new TextView(getContext());
			valueTextView.setLayoutParams(wrapWrap);
			valueTextView.setText(formatter.formatValue(startingValue));
			valueTextView.setVisibility(showValue? View.VISIBLE : View.GONE);
			valueTextView.setPadding(0, gutterSize, 0, 0);
			verticalLayout.addView(valueTextView);

			//Doesn't need padding around it because it has 16dp padding built in (for touch reasons)
			rotaryView = new RotaryView(getContext());
			rotaryView.setLayoutParams(fillZeroWeight1);
			rotaryView.setValue(startingValue);
			rotaryView.setOnAngleChangedListener(RotaryDialog.this);
			verticalLayout.addView(rotaryView);

			if (holo){
				//add horizontal divider above button
				LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						(int)(1*density));
				dividerParams.weight = 0;
				View dividerView = new View(getContext());
				dividerView.setLayoutParams(dividerParams);
				dividerView.setBackgroundDrawable(
                        getContext().getResources()
                                .getDrawable(android.R.drawable.divider_horizontal_textfield));
				verticalLayout.addView(dividerView);
			}

			Button b = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
			b.setText(android.R.string.ok);
			LinearLayout.LayoutParams okButtonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			okButtonLayoutParams.weight = 0;
			b.setLayoutParams(okButtonLayoutParams);
			verticalLayout.addView(b);

			b.setOnClickListener(new View.OnClickListener() {
	        	public void onClick(View v){ 
	        		rotaryPref.setValue(rotaryView.getValue());
					dismiss();
	        	}
	        });
			
			setTitle(title);
		}

		@Override
		public void onAngleChanged(int newAngle, View source) {
			valueTextView.setText(formatter.formatValue(newAngle));
		}
	}

	@Override
	public ValueType getValueType() {
		return ValueType.INT;
	}

}
