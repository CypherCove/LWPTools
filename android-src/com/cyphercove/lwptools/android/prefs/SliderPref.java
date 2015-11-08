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

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class SliderPref extends Pref{

	public int def;
	int max;
	PreferenceScreen prefScreen;
	int summary;
	int lowerLabel;
	int upperLabel;
	int dialogTitle;
	boolean showValue;
	SliderPrefValueFormatter formatter = new SliderPrefValueFormatter(){
		@Override
		public String formatValue(int value) {
			return Integer.toString(value)+"%";
		}
		
	};
	
	/** Modifies the value (between 0 to 1) into the value shown in the summary and in the dialog*/
	public interface SliderPrefValueFormatter {
		 String formatValue(int value);
	}
		
	public SliderPref(String key, int def, SummaryMode summaryMode, int title, int summaryIfApplicable, 
			int dialogTitle, int lowerLabel, int upperLabel, int max, boolean showValue){
		this.key = key;
		this.def = def;
		this.summaryMode = summaryMode;
		this.title = title;
		this.summary = summaryIfApplicable;
		this.dialogTitle = dialogTitle;
		this.lowerLabel = lowerLabel;
		this.upperLabel = upperLabel;
		this.max = max;
		this.showValue = showValue;
	}
	
	public SliderPref(String key, int def, SummaryMode summaryMode, int title, int summaryIfApplicable, 
			int dialogTitle, int lowerLabel, int upperLabel, int max, boolean showValue, SliderPrefValueFormatter sliderPrefValueFormatter){
		this(key,def,summaryMode,title,summaryIfApplicable,dialogTitle,lowerLabel,upperLabel,max,showValue);
        if (sliderPrefValueFormatter != null) this.formatter = sliderPrefValueFormatter;
	}

	@Override
	public PreferenceScreen create(PreferenceManager manager, final Context context, final SharedPreferences sharedPrefs,
								   boolean indented, final PrefResources resources, ArrayList<Pref> listToAddTo){
		this.setSharedPrefs(sharedPrefs);
		prefScreen = manager.createPreferenceScreen(context);
		pref = prefScreen;
		prefScreen.setTitle(title);
		prefScreen.setOnPreferenceClickListener(new OnPreferenceClickListener(){
			@Override
			public boolean onPreferenceClick(Preference arg0) {
				new SliderDialog(context, SliderPref.this, formatter,
						dialogTitle, lowerLabel, upperLabel, getValue(sharedPrefs), max, showValue).show();
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
				if (summary == 0){
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
        if(getSharedPrefs()==null){
            throw new UnsupportedOperationException(
                    "Cannot use this setValue method before initialized. Use the version with the " +
                            "SharedPreferences argument.");
        }
		this.setValue(value, getSharedPrefs());
	}

    public void setValue(int value, SharedPreferences sharedPrefs){
        Editor editor = sharedPrefs.edit();
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

	public class SliderDialog extends Dialog implements SeekBar.OnSeekBarChangeListener{
		int title;
		int lowerLabel;
		int upperLabel;
		SliderPref sliderPref;
		int startingValue;
		int max;
		boolean showValue;

		SliderPrefValueFormatter formatter;
		TextView valueTextView;
		SeekBar seekBar;

		private static final int DIVIDER_COLOR = 0x26FFFFFF;
		private static final int PRE_HOLO_MIN_DIALOG_PADDING_DP = 8;
		private static final int PRE_HOLO_MAX_WIDTH_DP = 480 - 2*PRE_HOLO_MIN_DIALOG_PADDING_DP;

		public SliderDialog(Context context, SliderPref sliderPref,SliderPrefValueFormatter formatter,
				int title,int lowerLabel,
				int upperLabel, int startingValue,int max, boolean showValue){
			super(context);
			this.sliderPref = sliderPref;
			this.formatter = formatter;
			this.title = title;
			this.lowerLabel = lowerLabel;
			this.upperLabel = upperLabel;
			this.startingValue = startingValue;
			this.max = max;
			this.showValue = showValue;
		}

		@Override
		protected void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			float density = getContext().getResources().getDisplayMetrics().density;

			boolean holo = Build.VERSION.SDK_INT >= 11;
			int gutterSize = (int)(16 * density);
			int gapSize = (int)(8 * density);

			LayoutParams wrapWrap = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
			LayoutParams fillWrap = new LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT);
			LayoutParams fillWrapWeight1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT,1);

			LinearLayout verticalLayout= new LinearLayout(getContext());
			verticalLayout.setOrientation(LinearLayout.VERTICAL);
			verticalLayout.setGravity(Gravity.CENTER);
			verticalLayout.setLayoutParams(fillWrap);
			setContentView(verticalLayout);

			valueTextView = new TextView(getContext());
			valueTextView.setLayoutParams(wrapWrap);
			valueTextView.setText(formatter.formatValue(startingValue));
			valueTextView.setVisibility(showValue? View.VISIBLE : View.GONE);
			valueTextView.setPadding(gutterSize, gutterSize, gutterSize, 0);
			verticalLayout.addView(valueTextView);

			seekBar = new SeekBar(getContext());
			seekBar.setLayoutParams(fillWrap);
			seekBar.setMax(max);
			seekBar.setProgress(startingValue);
			seekBar.setOnSeekBarChangeListener(SliderDialog.this);
			seekBar.setPadding(gutterSize, gapSize, gutterSize, 0);
			verticalLayout.addView(seekBar);

			LinearLayout horizontalLayout= new LinearLayout(getContext());
			horizontalLayout.setOrientation(LinearLayout.HORIZONTAL);
			horizontalLayout.setGravity(Gravity.CENTER_VERTICAL);
			horizontalLayout.setLayoutParams(fillWrap);
			horizontalLayout.setPadding(gutterSize, gapSize, gutterSize, gapSize);
			verticalLayout.addView(horizontalLayout);

				TextView lowerLabelView = new TextView(getContext());
				lowerLabelView.setLayoutParams(fillWrapWeight1);
				lowerLabelView.setText(lowerLabel);
				lowerLabelView.setGravity(Gravity.LEFT);
				horizontalLayout.addView(lowerLabelView);

				TextView upperLabelView = new TextView(getContext());
				upperLabelView.setLayoutParams(fillWrapWeight1);
				upperLabelView.setText(upperLabel);
				upperLabelView.setGravity(Gravity.RIGHT);
				horizontalLayout.addView(upperLabelView);

			if (holo){
				//add horizontal divider above button
				LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
						(int)(1*density));
				dividerParams.weight = 0;
				View dividerView = new View(getContext());
				dividerView.setLayoutParams(dividerParams);
				dividerView.setBackgroundColor(DIVIDER_COLOR);
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
	        		sliderPref.setValue(seekBar.getProgress());
					dismiss();
	        	}
	        });
			
			setTitle(title);
		}
		
		@Override
		public void onStart(){
			super.onStart();
			
			if (Build.VERSION.SDK_INT < 11){
				float density = getContext().getResources().getDisplayMetrics().density;
				@SuppressWarnings("deprecation")
				int screenWidth =((WindowManager) getContext()
						.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
				int width = 
						(screenWidth >= (PRE_HOLO_MAX_WIDTH_DP+2*PRE_HOLO_MIN_DIALOG_PADDING_DP)*density) ?
								(int)(PRE_HOLO_MAX_WIDTH_DP*density) : LayoutParams.MATCH_PARENT;
				getWindow().setLayout(width, LayoutParams.WRAP_CONTENT);
			}
		}

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			valueTextView.setText(formatter.formatValue(progress));
			
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {

		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
			
		}
	}

	@Override
	public ValueType getValueType() {
		return ValueType.INT;
	}

}
