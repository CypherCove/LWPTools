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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class ColorCacheView extends View {
	
	public static final int GINGERBREAD_HIGHLIGHT_COLOR = 0xffff9200;	
	public static final int HONEYCOMB_HIGHLIGHT_COLOR = 0x09c33b5e5;
	public static final int KITKAT_HIGHLIGHT_COLOR = 0x6e808080;
	
	OnColorChangedListener mListener;

	private int mPressedColor;
	private Paint mPaint;
    private float density;

    private static final int GAP_DP = 4;
    private static final int TARGET_HEIGHT_DP = 215;
    private static final int TARGET_WIDTH_DP = 280;

    private int mGap;

    private int mActualWidth;
    private int mActualHeight;

    private static final int ROWS = 4;
    private static final int COLUMNS = 5;

    private static final Integer[] DEFAULT_COLORS = {
    	0xff0099CC, 0xff9933CC, 0xff669900, 0xffFF8800, 0xffCC0000,
    	0xff33B5E5, 0xffAA66CC, 0xff99CC00, 0xffFFBB33, 0xffFF4444};

    ArrayList<Integer> mColors;

    private class Tracking{
    	public int row;
    	public int column;
    	public Tracking(int row, int column){
    		this.row = row;
    		this.column = column;
    	}
    }

    private Tracking mTracking = new Tracking(-1,-1); //set values negative when nothing is being tracked.
    private boolean mHoveringTrackedLocation = false;

    ColorCacheView(Context context){
    	this(context,null);
    }

    ColorCacheView(Context context, AttributeSet attrs){
    	this(context,attrs,0);
    }

    ColorCacheView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        density = metrics.density;

        mGap = (int)(GAP_DP * density);

        mPaint = new Paint(0);

        mColors = ColorCache.getCachedColors(context);
        if (mColors.get(0).equals(mColors.get(1))){ //only happens on first time use
        	//First time use. Populate default colors
        	ColorCache.submitNewColors(context, DEFAULT_COLORS);
        	mColors = ColorCache.getCachedColors(context);
        }
        
        mPressedColor = Build.VERSION.SDK_INT < 11 ? GINGERBREAD_HIGHLIGHT_COLOR : 
        	(Build.VERSION.SDK_INT < 19 ? HONEYCOMB_HIGHLIGHT_COLOR : KITKAT_HIGHLIGHT_COLOR);
    }
    
    public void setOnColorChangedListener(OnColorChangedListener listener){
    	this.mListener = listener;
    }
    
    private int getColorFromIndices(int row, int column){
    	try {
    		return mColors.get(row * COLUMNS + column);
    	} catch (IndexOutOfBoundsException e){
    		return Color.BLACK;
    	}
    }

    @Override 
    protected void onDraw(Canvas canvas) {
    	
    	int columnWidth = mActualWidth / COLUMNS;
    	int rowHeight = mActualHeight / ROWS;
    	
    	Rect rect = new Rect();
    	
    	for (int i=0; i<ROWS; i++){
    		for (int j=0; j<COLUMNS; j++){
    			if (mHoveringTrackedLocation && mTracking.row==i && mTracking.column==j){
    				mPaint.setColor(mPressedColor);
    			} else {
    				mPaint.setColor(getColorFromIndices(i,j));
    			}
    			
    			rect.top = i*rowHeight + mGap;
    			rect.bottom = (i+1)*rowHeight - mGap;
    			rect.left = j*columnWidth + mGap;
    			rect.right = (j+1)*columnWidth - mGap;
    			
    			canvas.drawRect(rect, mPaint);
    		}
    	}
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	requestFocus();
            	
            	mTracking.column = (int) (x / mActualWidth * COLUMNS);
            	mTracking.row = (int) (y / mActualHeight * ROWS);
            	
                if (mTracking.column >= COLUMNS || mTracking.row >= ROWS){
                	mTracking.row = -1;
                	mTracking.column = -1;
                }
                mHoveringTrackedLocation = true;
                break;
            case MotionEvent.ACTION_MOVE:
            	int currentColumn = (int) (x / mActualWidth * COLUMNS);
            	int currentRow = (int) (y / mActualHeight * ROWS);
            	mHoveringTrackedLocation = (mTracking.row==currentRow && mTracking.column==currentColumn);
                break;
            case MotionEvent.ACTION_UP:
                if (mHoveringTrackedLocation && mListener!=null) {
                	mListener.onColorChanged(getColorFromIndices(mTracking.row,mTracking.column), this);
                } 
                mHoveringTrackedLocation = false;
                break;
        }
        invalidate();
        return true;
    }        
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    	mActualWidth = MeasureSpec.getSize(widthMeasureSpec);

    	switch (widthMode){
    	case MeasureSpec.UNSPECIFIED:
    		mActualWidth = (int)(TARGET_WIDTH_DP*density);
    		break;
    	case MeasureSpec.AT_MOST:
    		mActualWidth = Math.min((int)(TARGET_WIDTH_DP*density), mActualWidth);
    		break;
    	}

    	
    	int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    	mActualHeight = MeasureSpec.getSize(heightMeasureSpec);
    	
    	switch (heightMode){
    	case MeasureSpec.UNSPECIFIED:
    		mActualHeight = (int)(TARGET_HEIGHT_DP*density);
    		break;
    	case MeasureSpec.AT_MOST:
    		mActualHeight = Math.min((int)(TARGET_HEIGHT_DP*density), mActualHeight);
    		break;
    	}
    	
    	setMeasuredDimension(mActualWidth, mActualHeight);
    }
    
}