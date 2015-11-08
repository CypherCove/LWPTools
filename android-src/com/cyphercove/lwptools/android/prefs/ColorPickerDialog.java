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
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class ColorPickerDialog extends Dialog implements OnColorChangedListener {

	public interface OnColorPrefChangedListener {
		void onColorPrefChanged(int color, String key);
	}

	private OnColorPrefChangedListener mListener;
    private String mKey;
    private int mInitialColor;
    private ColorPickerView cpv;
    private int titleTextResource;
    private View bottomColorView;
    private EditText hexEditText;

	public ColorPickerDialog(Context context,
			OnColorPrefChangedListener listener,
			int initialColor, String key,
			int titleTextResource ) {
		super(context);

		mListener = listener;
		mKey = key;
		mInitialColor = initialColor;
		this.titleTextResource=titleTextResource;
	}

	public boolean isExtraLargeScreen(Context context) {
	    return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
	    		>= Configuration.SCREENLAYOUT_SIZE_XLARGE;
	}

	public boolean isInWidescreenOrientation(Context context) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
	    return metrics.heightPixels < metrics.widthPixels;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		float density = getContext().getResources().getDisplayMetrics().density;

		boolean holo = Build.VERSION.SDK_INT >= 11;

		LayoutParams wrapWrap = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);

		/*
		 * linearLayout (vertical)
		 * 		cpv
		 * 		relativeLayout
		 * 			editText
		 * 			button
		 */

		LinearLayout baseLinearLayout = new LinearLayout(getContext());
		baseLinearLayout.setOrientation(LinearLayout.VERTICAL);
		baseLinearLayout.setLayoutParams(wrapWrap);
		setContentView(baseLinearLayout);

		cpv = new ColorPickerView(getContext());
		cpv.setColor(mInitialColor);
		LinearLayout.LayoutParams colorPickerViewParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		int margin4dip = (int)(4*density);
		colorPickerViewParams.setMargins(margin4dip,margin4dip,margin4dip,margin4dip); //color picker view has 4dip visual margins built in. Add 4 on that.
		colorPickerViewParams.gravity = Gravity.CENTER_HORIZONTAL;
		colorPickerViewParams.weight = 1;
		cpv.setLayoutParams(colorPickerViewParams);
		cpv.setOnColorChangedListener(this);
		cpv.setFocusableInTouchMode(true);//so it can take focus away from EditText to close the edit text keyboard.
		baseLinearLayout.addView(cpv);

		LinearLayout.LayoutParams bottomLinearLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		bottomLinearLayoutParams.weight = 0;

		RelativeLayout relativeLayout = new RelativeLayout(getContext());
		relativeLayout.setLayoutParams(bottomLinearLayoutParams);
		relativeLayout.setBackgroundColor(mInitialColor);
		this.bottomColorView = relativeLayout;
		baseLinearLayout.addView(relativeLayout);

		RelativeLayout.LayoutParams hexEditTextParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		hexEditTextParams.setMargins((int)(16*density), 0, 0, 0);
		hexEditTextParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		hexEditTextParams.addRule(RelativeLayout.CENTER_VERTICAL);

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

		hexEditText = new EditText(getContext());
		hexEditText.setText(String.format("%06X", (0xFFFFFF & mInitialColor)));
		hexEditText.setHint("RRGGBB");
		hexEditText.setGravity(Gravity.CENTER_HORIZONTAL);
		hexEditText.setLayoutParams(hexEditTextParams);
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
		relativeLayout.addView(hexEditText);

		hexEditText.addTextChangedListener(new TextWatcher(){

			boolean doNotUpdateCPV = false;
	        public void afterTextChanged(Editable s) {

	        	if (s.length()!=6 && (s.length()>0 && s.charAt(0)!='@')) //Must have been entered by user
	        		doNotUpdateCPV = false;

	        	//A valid value typed by the user
	        	if (s.length()==6 && !doNotUpdateCPV){
	            	try {
	            		int color = Color.parseColor("#"+s.toString());
	            		cpv.setColor(color);
	            		bottomColorView.setBackgroundColor(color);
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

		hexEditText.setShadowLayer(1*density, 0.5f*density, 0.5f*density, Color.DKGRAY);
		if (!holo){
			//remove background
			hexEditText.setBackgroundColor(Color.TRANSPARENT);
		}
        hexEditText.setTextColor(Color.WHITE); //always white text.

		RelativeLayout.LayoutParams buttonParams = new RelativeLayout.LayoutParams((int)(120*density),
				LayoutParams.WRAP_CONTENT);
		buttonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		buttonParams.addRule(RelativeLayout.CENTER_VERTICAL);

		Button b = holo ?  new Button(getContext(),null,android.R.attr.borderlessButtonStyle) : new Button(getContext()) ;
		b.setText(android.R.string.ok);
		if (holo){
			//need contrast with background since no background
			b.setShadowLayer(1*density, 0.5f*density, 0.5f*density, Color.DKGRAY);
		}
		b.setLayoutParams(buttonParams);
		int uniqueIdForButton = 1;
		while (uniqueIdForButton==hexEditText.getId())
			uniqueIdForButton++;
		b.setId(uniqueIdForButton);
		relativeLayout.addView(b);

		b.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v){ 
        		int newColor = cpv.getColor();
        		mListener.onColorPrefChanged(newColor,mKey);
        		ColorCache.submitNewColors(getContext(), newColor);
				dismiss();
        	}
        });
		
		if (holo){
			//add divider next to button
			addDividerViewNextToHoloButton(relativeLayout,density,b.getId());
		}

		setTitle(titleTextResource);
		
//		LayoutParams params = getWindow().getAttributes();
//        params.width = LayoutParams.WRAP_CONTENT;
//        getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);
	}
	
	@SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
	void addDividerViewNextToHoloButton(RelativeLayout parent, float pixPerDIP, int buttonId){
		RelativeLayout.LayoutParams dividerParams = new RelativeLayout.LayoutParams((int)(1*pixPerDIP), 
				LayoutParams.WRAP_CONTENT);
		dividerParams.topMargin = dividerParams.bottomMargin = (int)(4*pixPerDIP);
		dividerParams.addRule(RelativeLayout.LEFT_OF, buttonId);
		dividerParams.addRule(RelativeLayout.ALIGN_TOP, buttonId);
		dividerParams.addRule(RelativeLayout.ALIGN_BOTTOM,buttonId);
		View dividerView = new View(getContext());
		dividerView.setLayoutParams(dividerParams);
		dividerView.setBackgroundDrawable(
                getContext().getResources()
                        .getDrawable(android.R.drawable.divider_horizontal_textfield)
        );
		parent.addView(dividerView);
	}

	@Override
	public void onColorChanged(int newColor, View source) {
		bottomColorView.setBackgroundColor(newColor);
		
		String previousText = "@" + hexEditText.getText().toString();
		String newText = "@" + String.format("%06X", (0xFFFFFF & newColor));
		//The @ indicates the source of the text was the color picker, not the user typing.
		if (!previousText.equals(newText)){
			hexEditText.setText(newText);
		}
	}
	
}

