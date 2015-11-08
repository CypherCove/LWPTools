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
import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdvancedColorPickerDialog extends Dialog implements OnColorChangedListener {

	public static interface OnAdvancedColorPrefChangedListener {
		void onAdvancedColorPrefChanged(String advancedColor, String key);
	}

	public static class TextResources {
		public int title;
		public int mode;
		public int constantColor;
		public int batteryBasedColor;
		public int plugBasedColor;
		public int selectedConstant;
		public int selectedEmpty;
		public int selectedMiddle;
		public int selectedFull;
		public int selectedUnplugged;
		public int selectedPlugged;
	}

    private static final int RADIO_BUTTON_HEIGHT_DIP = 48;

	private OnAdvancedColorPrefChangedListener mListener;
	private String mKey;
	private TextResources mTextResources;
	private AdvancedColor mCurrentColor;
	private LinkedHashMap<String, Integer> mAdditionalModes; //list of modes, along with their user-visible string resource ints
	private String mAdditionalMode; //is non-null when not an advanced color
	private int mNumberBatteryBasedColors;
	ViewFlipper mViewFlipper;
	private ColorPickerView mColorPickerView;
    private EditText hexEditText;
	private RadioGroup mColorSwatchRadioGroup;
	private TextView mAdditionalModeTextView;
	private int mCurrentColorIndex;
	private ArrayList<RadioButton> mRadioColorSwatchesInGroup = new ArrayList<RadioButton>(3);
	private HashMap<RadioButton, ShapeDrawable> mRadioColorSwatchDrawablesInGroup = new HashMap<RadioButton, ShapeDrawable>();
	private int mAlternateTitleResource = 0;

	/**
	 *
	 * @param numberBatteryBasedColors Must be 2 or 3.
	 * @param additionalModes If non-null, additional non-color modes available in the spinner.
	 * @param alternateTitleResource If non-zero, a string resource used for the dialog's title.
	 */
	public AdvancedColorPickerDialog(Context context,
			OnAdvancedColorPrefChangedListener listener,
			String initialAdvancedColorAsString, int numberBatteryBasedColors,
			String key, final TextResources textResources, LinkedHashMap<String, Integer> additionalModes, int alternateTitleResource) {
		super(context);

		mListener = listener;
		mKey = key;

		if (additionalModes!=null){
			mAdditionalModes = additionalModes;
			for (String modeKey : additionalModes.keySet()){
				if (modeKey.equals(initialAdvancedColorAsString)){
					mCurrentColor = new AdvancedColor(AdvancedColor.ColorType.CONSTANT,Color.BLACK);//just default to a constant black
					mAdditionalMode = modeKey;
					break;
				}
			}
		}
		if (mCurrentColor==null)
			mCurrentColor = AdvancedColor.fromString(initialAdvancedColorAsString);

		mNumberBatteryBasedColors = numberBatteryBasedColors;

		if (numberBatteryBasedColors!=2 && numberBatteryBasedColors!=3){
			throw new IllegalArgumentException("Number of battery based colors must be 2 or 3.");
		}

		mCurrentColor.setThreeColorBatteryLevelMode(mNumberBatteryBasedColors==3);

		mTextResources = textResources;

		mAdditionalModes = additionalModes;

		mAlternateTitleResource = alternateTitleResource;
	}

	public boolean isExtraLargeScreen(Context context) {
		return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
				>= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	public boolean isInWidescreenOrientation(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return metrics.heightPixels < metrics.widthPixels;
	}

	@SuppressWarnings("deprecation")
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Context context = getContext();

		final float density = context.getResources().getDisplayMetrics().density;

		boolean holo = Build.VERSION.SDK_INT >= 11;

		LayoutParams matchWrapLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		/*
		 * 	linearLayout (vertical)
		 * 		radio group (horizontal)
		 * 			spinner, radio, radio, radio, textView
		 * 		view pager
		 *     		linear layout (horizontal)
		 *     			cpv, button
		 *     		linear layout (horizontal)
		 *     			ccv, button
		 * 			linear layout (horizontal)
		 *     			hex edit text, button
		 * 		relativeLayout
		 * 			button
		 */

		LinearLayout baseLinearLayout = new LinearLayout(context);
		baseLinearLayout.setOrientation(LinearLayout.VERTICAL);
		LayoutParams baseLayoutParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		baseLinearLayout.setLayoutParams(baseLayoutParams);
		setContentView(baseLinearLayout);


		mColorSwatchRadioGroup = new RadioGroup(context);
		mColorSwatchRadioGroup.setLayoutParams(matchWrapLayoutParams);
		mColorSwatchRadioGroup.setOrientation(LinearLayout.HORIZONTAL);
		mColorSwatchRadioGroup.setOnCheckedChangeListener(mColorSwatchRadioGroupCheckedChangeListener);
		if (holo)
			addRadioGroupDividers(mColorSwatchRadioGroup, density);
		Spinner spinner = createSpinner(context, holo, density);
		mColorSwatchRadioGroup.addView(spinner); //Add the spinner inside this radio group so it's on the same line
		mColorSwatchRadioGroup.addView(prepareAdditionalModeTextView(density));
		updateRadioGroupEntries(holo, density); //Add the radio buttons
		baseLinearLayout.addView(mColorSwatchRadioGroup);

		mViewFlipper = new ViewFlipper(context);
		LinearLayout.LayoutParams viewFlipperParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		viewFlipperParams.weight = 1;
		mViewFlipper.setLayoutParams(viewFlipperParams);
		final Animation inAnimation = AnimationUtils.loadAnimation(context,android.R.anim.slide_in_left);
		final Animation outAnimation = AnimationUtils.loadAnimation(context,android.R.anim.slide_out_right);
		//defer setting these animations to avoid having the inAnimation play when the view first loads

		FrameLayout.LayoutParams viewFlipperChildParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);

		LinearLayout colorPickerLinearLayout = new LinearLayout(context);
		colorPickerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		colorPickerLinearLayout.setLayoutParams(viewFlipperChildParams);

		mColorPickerView = new ColorPickerView(getContext());
		mColorPickerView.setColor(mCurrentColor.getColors()[0]);
		LinearLayout.LayoutParams colorPickerViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		int margin4dip = (int)(4*density);
		colorPickerViewParams.setMargins(margin4dip,margin4dip,margin4dip,margin4dip); //color picker view has 4dip visual margins built in. Add 4 on that.
		colorPickerViewParams.weight = 1;
		mColorPickerView.setLayoutParams(colorPickerViewParams);
		mColorPickerView.setOnColorChangedListener(this);
		mColorPickerView.setFocusableInTouchMode(true);//so it can take focus away from EditText to close the edit text keyboard.
		colorPickerLinearLayout.addView(mColorPickerView);

		Button toColorCacheButton = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
		LinearLayout.LayoutParams viewFlipperAdvanceButtonParams = new LinearLayout.LayoutParams((int)(40*density),
				LayoutParams.MATCH_PARENT);
		int margin8dip = (int)(8*density);
		viewFlipperAdvanceButtonParams.setMargins(0,margin8dip,margin8dip,margin8dip);
		viewFlipperAdvanceButtonParams.weight = 0;
		toColorCacheButton.setLayoutParams(viewFlipperAdvanceButtonParams);
		toColorCacheButton.setText(">");
		toColorCacheButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				mViewFlipper.setInAnimation(inAnimation);
				mViewFlipper.setOutAnimation(outAnimation);
				mViewFlipper.showNext();
			}
		});
		colorPickerLinearLayout.addView(toColorCacheButton);

		LinearLayout colorCacheLinearLayout = new LinearLayout(context);
		colorCacheLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		colorCacheLinearLayout.setLayoutParams(viewFlipperChildParams);

		ColorCacheView colorCacheView = new ColorCacheView(context);
		LinearLayout.LayoutParams colorCacheViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		colorCacheViewParams.setMargins(margin4dip,margin4dip,margin4dip,margin4dip);//color picker view has 4dip visual margins built in. Add 4 on that.
		colorCacheViewParams.weight = 1;
		colorCacheView.setLayoutParams(colorCacheViewParams);
		colorCacheView.setOnColorChangedListener(this);
		colorCacheView.setFocusableInTouchMode(true);//so it can take focus away from EditText to close the edit text keyboard.
		colorCacheLinearLayout.addView(colorCacheView);

		Button toHexEditTextButton = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
		toHexEditTextButton.setLayoutParams(viewFlipperAdvanceButtonParams);
		toHexEditTextButton.setText(">");
		toHexEditTextButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				mViewFlipper.showNext();
			}
		});
		colorCacheLinearLayout.addView(toHexEditTextButton);

		LinearLayout hexEditTextLinearLayout = new LinearLayout(context);
		hexEditTextLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		FrameLayout.LayoutParams hexEditTexLinearLayoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		hexEditTextLinearLayout.setLayoutParams(hexEditTexLinearLayoutParams);
		hexEditTextLinearLayout.setGravity(Gravity.CENTER);

		LinearLayout hexEditTextInnerLinearLayout = new LinearLayout(context);
		hexEditTextInnerLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
		hexEditTextInnerLinearLayout.setGravity(Gravity.CENTER);
		LinearLayout.LayoutParams hexEditTexInnerLinearLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		hexEditTexInnerLinearLayoutParams.weight = 1;
		hexEditTextInnerLinearLayout.setLayoutParams(hexEditTexInnerLinearLayoutParams);

		LinearLayout.LayoutParams hexEditTextParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		TextView hexEditTextLeadTextView = new TextView(context);
		hexEditTextLeadTextView.setLayoutParams(hexEditTextParams);
		hexEditTextLeadTextView.setText("#");
		hexEditTextInnerLinearLayout.addView(hexEditTextLeadTextView);

		prepareHexEditText(context, density);
		hexEditText.setLayoutParams(hexEditTextParams);
		hexEditTextInnerLinearLayout.addView(hexEditText);

		hexEditTextLinearLayout.addView(hexEditTextInnerLinearLayout);

		Button toColorPickerButton = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
		toColorPickerButton.setLayoutParams(viewFlipperAdvanceButtonParams);
		toColorPickerButton.setText(">");
		toColorPickerButton.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				mViewFlipper.showNext();
			}
		});
		hexEditTextLinearLayout.addView(toColorPickerButton);

		mViewFlipper.addView(colorPickerLinearLayout, 0);
		mViewFlipper.addView(colorCacheLinearLayout, 1);
		mViewFlipper.addView(hexEditTextLinearLayout, 2);

		baseLinearLayout.addView(mViewFlipper);

		if (holo){
			//add horizontal divider above button
			LinearLayout.LayoutParams dividerParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
					(int)(1*density));
			dividerParams.weight = 0;
			View dividerView = new View(getContext());
			dividerView.setLayoutParams(dividerParams);
			dividerView.setBackgroundDrawable(context.getResources().getDrawable(android.R.drawable.divider_horizontal_textfield));

			baseLinearLayout.addView(dividerView);
		}

		Button b = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
		b.setText(android.R.string.ok);
		LinearLayout.LayoutParams okButtonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		okButtonLayoutParams.weight = 0;
		b.setLayoutParams(okButtonLayoutParams);
		baseLinearLayout.addView(b);

		b.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v){
				String output = mAdditionalMode==null? mCurrentColor.toString() : mAdditionalMode;

				mListener.onAdvancedColorPrefChanged(output,mKey);

				if (mAdditionalMode == null){
					//reverse order of colors
					int[] colors = mCurrentColor.getColors();
					Integer[] reversedColors = new Integer[colors.length];
					for (int i=0; i<colors.length; i++){
						reversedColors[colors.length-1-i] = colors[i];
					}

					ColorCache.submitNewColors(getContext(), reversedColors);
				}


        		dismiss();
			}
		});

		setTitle(mAlternateTitleResource != 0? mAlternateTitleResource : mTextResources.title);

		//Set spinner to correct level.
		int initialSpinnerIndex = 0; //default single color
		if (mAdditionalMode!=null){
			int spinnerIndexForEntry = 3;
			for (Map.Entry<String,Integer> entry : mAdditionalModes.entrySet()){
				if (entry.getKey().equals(mAdditionalMode)){
					initialSpinnerIndex = spinnerIndexForEntry;
					break;
				}
				spinnerIndexForEntry++;
			}
		} else if (mCurrentColor.getColorType()== AdvancedColor.ColorType.BATTERY_LEVEL_BASED)
			initialSpinnerIndex = 1;
		else if (mCurrentColor.getColorType()== AdvancedColor.ColorType.PLUGGED_STATE_BASED)
			initialSpinnerIndex = 2;
		spinner.setSelection(initialSpinnerIndex);

		onColorTypeChanged(); //ensure views are up to date
	}


	private void prepareHexEditText(Context context, float density) {

		InputFilter[] inputFilters = new InputFilter[1];
		inputFilters[0] = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end,Spanned dest, int dstart, int dend) {
            	if (source.length()==7 && source.charAt(0)=='@')
            		return null;

            	//reject change if any digit is not a hex digit
            	for (int i = 0; i < source.length(); i++) {
            		char c = source.charAt(i);
            		if (c != '0' && c != '1' && c != '2' && c != '3' && c != '4' && c != '5' && c != '6' && c != '7'
            			&& c != '8' && c != '9' && c != 'a' && c != 'b' && c != 'c' && c != 'd' && c != 'e' && c != 'f'
            				&& c != 'A' && c != 'B' && c != 'C' && c != 'D' && c != 'E' && c != 'F'){
            			return "";
            		}
            	}

            	return null;
            }

        };

		hexEditText = new EditText(context);
		hexEditText.setText(colorToHexString(mCurrentColor.getColors()[0]));
		hexEditText.setHint("RRGGBB");
		hexEditText.setGravity(Gravity.CENTER_HORIZONTAL);
		hexEditText.setFilters(inputFilters);
		hexEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		hexEditText.setOnFocusChangeListener(new OnFocusChangeListener() //auto-hide keyboard when focus lost
        {
            public void onFocusChange(View v, boolean hasFocus)
            {
                if (v == hexEditText) {
                    if (!hasFocus)
                    	((InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        		hexEditText.getWindowToken(), 0);
                }
            }
        });

		hexEditText.addTextChangedListener(new TextWatcher(){

			boolean doNotUpdateCPV = false;
	        public void afterTextChanged(Editable s) {

	        	if (s.length()!=6 && (s.length()>0 && s.charAt(0)!='@')) //Must have been entered by user
	        		doNotUpdateCPV = false;

	        	//A valid value typed by the user
	        	if (s.length()==6 && !doNotUpdateCPV){
	            	try {
	            		int color = Color.parseColor("#"+s.toString());
	            		onColorChanged(color, hexEditText);
	            	} catch (IllegalArgumentException e){
	            		//Error. Clear field
	            		s.replace(0, s.length(), "");
	            	}
	            	return;
	            }

	        	//If the CPV is the source of the change
	        	//Check this after checking if CPV can be updated
	        	//so CPV doesn't cause itself to be updated (infinite recursion)
            	if (s.length()>0 && s.charAt(0)=='@'){
            		doNotUpdateCPV=true;
            		s.replace(0, s.length(), s.subSequence(1, 7));
            		return;
            	}

            	//Typed value needs truncation
            	if (s.length()>6){
            		s.replace(0, s.length(), s.subSequence(0, 6));
            		return;
            	}


	        }
	        public void beforeTextChanged(CharSequence s, int start, int count, int after){}
	        public void onTextChanged(CharSequence s, int start, int before, int count){}
	    });
	}

	private Spinner createSpinner(final Context context, final boolean holo, final float density) {
		Spinner spinner = new Spinner(context);
		LinearLayout.LayoutParams spinnerParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				(int)(RADIO_BUTTON_HEIGHT_DIP*density));
		spinnerParams.weight = 0;
		spinner.setLayoutParams(spinnerParams);
		ArrayList<String> spinnerArray = new ArrayList<String>();
		spinnerArray.add(context.getString(mTextResources.constantColor));
		spinnerArray.add(context.getString(mTextResources.batteryBasedColor));
		spinnerArray.add(context.getString(mTextResources.plugBasedColor));

		//Map of additional modes where key is spinner index
		final HashMap<Integer, String> additionalModesMap = mAdditionalModes!=null ?
				new HashMap<Integer, String>(mAdditionalModes.size()) :
					new HashMap<Integer, String>();
		int index = 3;
		if (mAdditionalModes != null){
			for (Map.Entry<String, Integer> entry : mAdditionalModes.entrySet()){
				spinnerArray.add(context.getString(entry.getValue()));
				additionalModesMap.put(index++, entry.getKey());
			}
		}

		//Subclass array adapter so the button view is always a text view that says "mode"
		ArrayAdapter<String> spinnerArrayAdapter =
				new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, spinnerArray){
			@Override
			public View getView(int position, View convertView, ViewGroup parent){
				if (convertView==null){
					//Do not inflate built-in Android layout here. It was triggering an Android layout bug.
					convertView = new TextView(context);
					if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB){
						((TextView)convertView).setTextAppearance(context, android.R.attr.spinnerStyle);
						int leftRightPadding = (int)(density * 4);
						convertView.setPadding(leftRightPadding, 0, leftRightPadding, 0);
					}
				}
				((TextView)convertView).setText(mTextResources.mode);
				return convertView;
			}
		};
		spinner.setAdapter(spinnerArrayAdapter);

		spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				switch (position){
				case 0:
					onConstantColorSelected();
					break;
				case 1:
					onBatteryBasedColorSelected();
					break;
				case 2:
					onPlugBasedColorSelected();
					break;
				default:
					onNonColorSelected(additionalModesMap.get(position));
				}
			}

			public void onNothingSelected(AdapterView<?> parent) {}
		});
		return spinner;
	}


	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void addRadioGroupDividers(RadioGroup radioGroup, float density) {
		radioGroup.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
		ShapeDrawable dividerDrawable = new ShapeDrawable();
		dividerDrawable.setIntrinsicWidth((int)(1*density));
		dividerDrawable.getPaint().setColor(Color.TRANSPARENT);
		radioGroup.setDividerDrawable(dividerDrawable);
	}

	private void updateRadioGroupEntries(boolean holo, float density) {
		if (mAdditionalMode != null){
			while (mRadioColorSwatchesInGroup.size() > 0){
				RadioButton lastButton =
						mRadioColorSwatchesInGroup.get(mRadioColorSwatchesInGroup.size()-1);
				mColorSwatchRadioGroup.removeView(lastButton);
				mRadioColorSwatchesInGroup.remove(lastButton);
				mRadioColorSwatchDrawablesInGroup.remove(lastButton);
			}
			mAdditionalModeTextView.setText(mAdditionalModes.get(mAdditionalMode));
			mAdditionalModeTextView.setVisibility(View.VISIBLE);
			return;
		}
		mAdditionalModeTextView.setVisibility(View.GONE);

		int numberOfSwatchesNeeded = 1;
		int colorType = mCurrentColor.getColorType();
		if (colorType== AdvancedColor.ColorType.BATTERY_LEVEL_BASED)
			numberOfSwatchesNeeded = mNumberBatteryBasedColors;
		else if (colorType== AdvancedColor.ColorType.PLUGGED_STATE_BASED)
			numberOfSwatchesNeeded = 2;

		while (mRadioColorSwatchesInGroup.size() < numberOfSwatchesNeeded){
			RadioButton newButton = generateColorSwatchRadioButton(holo, density);
			mColorSwatchRadioGroup.addView(newButton);
			mRadioColorSwatchesInGroup.add(newButton);
		}

		while (mRadioColorSwatchesInGroup.size() > numberOfSwatchesNeeded){
			RadioButton lastButton =
					mRadioColorSwatchesInGroup.get(mRadioColorSwatchesInGroup.size()-1);
			mColorSwatchRadioGroup.removeView(lastButton);
			mRadioColorSwatchesInGroup.remove(lastButton);
			mRadioColorSwatchDrawablesInGroup.remove(lastButton);
		}

		//set id's to index + 1
		for (int i=0; i<mRadioColorSwatchesInGroup.size(); i++){
			mRadioColorSwatchesInGroup.get(i).setId(i+1);
		}

		switch (colorType){
		case AdvancedColor.ColorType.CONSTANT:
			mRadioColorSwatchesInGroup.get(0).setText(mTextResources.selectedConstant);
			break;
		case AdvancedColor.ColorType.BATTERY_LEVEL_BASED:
			mRadioColorSwatchesInGroup.get(0).setText(mTextResources.selectedEmpty);
			for (int i=1; i<mNumberBatteryBasedColors-1; i++){
				mRadioColorSwatchesInGroup.get(i).setText(mTextResources.selectedMiddle);
			}
			mRadioColorSwatchesInGroup.get(mNumberBatteryBasedColors-1).setText(mTextResources.selectedFull);
			break;
		case AdvancedColor.ColorType.PLUGGED_STATE_BASED:
			mRadioColorSwatchesInGroup.get(0).setText(mTextResources.selectedUnplugged);
			mRadioColorSwatchesInGroup.get(1).setText(mTextResources.selectedPlugged);
			break;
		}

		//make constant color non-clickable (or reset it if reusued as one of other types)
		mRadioColorSwatchesInGroup.get(0).setClickable(colorType!= AdvancedColor.ColorType.CONSTANT);

		//initialize background colors
		int[] colors = mCurrentColor.getColors();
		for (int i=0; i<mRadioColorSwatchesInGroup.size(); i++){
			mRadioColorSwatchDrawablesInGroup.get(mRadioColorSwatchesInGroup.get(i)).getPaint().setColor(colors[i]);
		}

		mColorSwatchRadioGroup.check(1); //1 is id of first element (id's were set to index+1)
	}

	@SuppressWarnings("deprecation")
	private RadioButton generateColorSwatchRadioButton(boolean holo, float density) {
		//Instantiate with a style that doesn't assign a button drawable (checkbox or radio)
		RadioButton button = holo ? new RadioButton(getContext(),null,android.R.attr.borderlessButtonStyle) :
			new RadioButton(getContext(),null,android.R.attr.textAppearanceSmall);
        button.setTextColor(Color.WHITE); //always white text
		RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, (int)(RADIO_BUTTON_HEIGHT_DIP*density)); //height 48dip (typical button)
		params.weight = 1;
		button.setLayoutParams(params);
		button.setGravity(Gravity.CENTER);
		//button.setButtonDrawable(null); //remove radio circle drawable
		button.setOnCheckedChangeListener(mColorSwatchOnCheckedChangeListener);
		button.setOnClickListener(mEmptyOnClickListener); //so it will make sound.
		button.setShadowLayer(1*density, 0.5f*density, 0.5f*density, Color.DKGRAY);//need contrast with background since no background

		StateListDrawable states = new StateListDrawable();
		ShapeDrawable pressedColorDrawable = new ShapeDrawable(new RectShape());
		pressedColorDrawable.getPaint().setColor(
				Build.VERSION.SDK_INT < 11 ? ColorCacheView.GINGERBREAD_HIGHLIGHT_COLOR :
		        	(Build.VERSION.SDK_INT < 19 ? ColorCacheView.HONEYCOMB_HIGHLIGHT_COLOR : ColorCacheView.KITKAT_HIGHLIGHT_COLOR));
		states.addState(new int[] {android.R.attr.state_pressed}, pressedColorDrawable);
		ShapeDrawable unpressedColorDrawable = new ShapeDrawable(new RectShape());
		mRadioColorSwatchDrawablesInGroup.put(button, unpressedColorDrawable);
		states.addState(new int[] { }, unpressedColorDrawable);

		button.setBackgroundDrawable(states);

		return button;
	}

	private TextView prepareAdditionalModeTextView(float density) {
		mAdditionalModeTextView = new TextView(getContext(),null,android.R.attr.textAppearance);
		RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(0, (int)(RADIO_BUTTON_HEIGHT_DIP*density)); //height 48dip (typical button)
		params.weight = 1;
		mAdditionalModeTextView.setLayoutParams(params);
		mAdditionalModeTextView.setGravity(Gravity.CENTER);
		mAdditionalModeTextView.setTextColor(Color.WHITE);
		return mAdditionalModeTextView;
	}

	private RadioGroup.OnCheckedChangeListener mColorSwatchRadioGroupCheckedChangeListener =
			new RadioGroup.OnCheckedChangeListener(){

		public void onCheckedChanged(RadioGroup group, int checkedId) {
			mCurrentColorIndex = checkedId-1;
			int newColor = mCurrentColor.getColors()[mCurrentColorIndex];
			if (mColorPickerView!=null){ //it's null before it's created.
				mColorPickerView.setColor(newColor);
			}
			if (hexEditText!=null){
				hexEditText.setText("@" + colorToHexString(newColor));//The @ indicates the source of the text was not the user typing.
			}
		}
	};

	private RadioButton.OnCheckedChangeListener mColorSwatchOnCheckedChangeListener =
			new RadioButton.OnCheckedChangeListener(){

		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			//buttonView.setTypeface(null, isChecked ? Typeface.BOLD : Typeface.NORMAL);
			if (isChecked){
				buttonView.setPaintFlags(buttonView.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
			} else {
				buttonView.setPaintFlags(buttonView.getPaintFlags() & ~Paint.UNDERLINE_TEXT_FLAG);
			}
		}

	};

	//Set this on radio buttons to make them make sound
	private View.OnClickListener mEmptyOnClickListener = new View.OnClickListener(){
		public void onClick(View v) {}
	};

	private void onConstantColorSelected() {
		int currentColorType = mCurrentColor.getColorType();
		if (mAdditionalMode!=null || currentColorType != AdvancedColor.ColorType.CONSTANT){
			mAdditionalMode = null;
			mCurrentColor.setColorType(AdvancedColor.ColorType.CONSTANT);
			onColorTypeChanged();
		}
	}

	private void onBatteryBasedColorSelected() {
		int currentColorType = mCurrentColor.getColorType();
		if (mAdditionalMode!=null || currentColorType != AdvancedColor.ColorType.BATTERY_LEVEL_BASED){
			mAdditionalMode = null;
			mCurrentColor.setColorType(AdvancedColor.ColorType.BATTERY_LEVEL_BASED);
			onColorTypeChanged();
		}
	}

	private void onPlugBasedColorSelected() {
		int currentColorType = mCurrentColor.getColorType();
		if (mAdditionalMode!=null || currentColorType != AdvancedColor.ColorType.PLUGGED_STATE_BASED){
			mAdditionalMode = null;
			mCurrentColor.setColorType(AdvancedColor.ColorType.PLUGGED_STATE_BASED);
			onColorTypeChanged();
		}
	}
	

	private void onNonColorSelected(String newMode) {
		if (mAdditionalMode==null || !mAdditionalMode.equals(newMode)){
			mAdditionalMode = newMode;
			onColorTypeChanged();
		}
		
	}

	private void onColorTypeChanged(){
		updateRadioGroupEntries((Build.VERSION.SDK_INT >= 11),
				getContext().getResources().getDisplayMetrics().density);
		if (mAdditionalMode == null){
			mViewFlipper.setVisibility(View.VISIBLE);
		} else {
			mViewFlipper.setVisibility(View.INVISIBLE);
		}
	}

	@Override
	public void onColorChanged(int newColor, View source) {
		RadioButton button = mRadioColorSwatchesInGroup.get(mCurrentColorIndex);
		mRadioColorSwatchDrawablesInGroup.get(button).getPaint().setColor(newColor);
		button.invalidate();
		mCurrentColor.getColors()[mCurrentColorIndex] = newColor;
		
		if (source != (View)mColorPickerView) {
			mColorPickerView.setColor(newColor);//apply color to color picker view, but avoid infinite recursion if source is self
		}
		
		if (source != (View)hexEditText){
			String previousText = hexEditText.getText().toString();
			String newText = colorToHexString(newColor);
			
			if (!previousText.equals(newText)){
				hexEditText.setText("@" + newText);//The @ indicates the source of the text was not the user typing.
			}
		}
	}
	
	private String colorToHexString(int color){
		return String.format("%06X", (0xFFFFFF & color));
	}

}

