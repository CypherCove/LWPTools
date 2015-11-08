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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;

public class ColorPickerView extends View {
	
	OnColorChangedListener mListener;

    private Paint huePaint;
    private Paint saturationPaint;
    private Paint valuePaint;
    private Paint hueLinePaint;
    private Paint svCirclePaint;
    private final int[] hues= new int[] {0xFFFF0000, 0xFFFFFF00, 0xFF00FF00, 0xFF00FFFF, 0xFF0000FF, 0xFFFF00FF, 0xFFFF0000}; //rainbow
    private int[] saturations= new int[] { 0xFFFFFFFF, 0xFF000000 }; //white and arbitrary color that will be updated to fully saturated hue
    private final int[] values= new int[] { 0x00000000, 0xFF000000 };//clear black to opaque black;
    private float density;

    private static final int HUE_LINE_WIDTH_DP = 48;
    private static final int GAP_DP = 4;
    private static final int HSV_HEIGHT_DP = 215;
    private static final int WIDTH_DP = 280;

    private int hueLineWidth;
    private int hueStripWidth;
    private int gapWidth;
    private int gapHeight;
    private int hsvHeight;
    private int svWidth;

    private boolean trackingHue;
    private boolean trackingSV;
    private float[] hsvSelected={0f,0f,0f};

    public int getColor(){
    	return Color.HSVToColor(hsvSelected);
    }

    public void setColor(int color){
    	if (!trackingHue && !trackingSV){
        	Color.colorToHSV(color, hsvSelected);
        	updateHue(getHue(color));
        	invalidate();
    	}
    }

    ColorPickerView(Context context){
    	this(context,null);
    }

    ColorPickerView(Context context, AttributeSet attrs){
    	this(context,attrs,0);
    }

    ColorPickerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Color.colorToHSV(0xffff00ff, hsvSelected);
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        density = metrics.density;

        huePaint = new Paint(Paint.DITHER_FLAG);
        huePaint.setStyle(Paint.Style.STROKE);

        saturationPaint = new Paint(Paint.DITHER_FLAG); //may not need aa, since it's rectangular
        saturationPaint.setStyle(Paint.Style.STROKE);

        valuePaint = new Paint(Paint.DITHER_FLAG); //may not need aa, since it's rectangular
        valuePaint.setStyle(Paint.Style.STROKE);

        hueLinePaint = new Paint(0);
        hueLinePaint.setColor(0xFFFFFFFF);

        svCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        svCirclePaint.setColor(0xFFFFFFFF);
        svCirclePaint.setStyle(Paint.Style.STROKE);
        svCirclePaint.setStrokeWidth(2*density);
    }

    public void preparePaintWithNewSizes(){
    	Shader hueShader = new LinearGradient(0, 0, 0, hsvHeight, hues, null, Shader.TileMode.CLAMP);
        huePaint.setShader(hueShader);
        huePaint.setStrokeWidth(hueStripWidth);

        saturationPaint.setStrokeWidth(hsvHeight);
        updateHue(hsvSelected[0]);//this also prepares the saturationPaint

        Shader valueShader = new LinearGradient(0, 0, 0, hsvHeight, values, null, Shader.TileMode.CLAMP);
        valuePaint.setShader(valueShader);
        valuePaint.setStrokeWidth(svWidth);

        hueLinePaint.setStrokeWidth(hueLineWidth);

    }

    public void setOnColorChangedListener(OnColorChangedListener listener){
    	this.mListener = listener;
    }

    private void updateHue(float hue){
    	hsvSelected[0]=hue;
        float[] saturated={hue,1f,1f};
        saturations[1]=Color.HSVToColor(saturated); //update the opaque saturated color
    	Shader saturationShader = new LinearGradient(0, 0, svWidth, 0, saturations, null, Shader.TileMode.CLAMP);
        saturationPaint.setShader(saturationShader);
    }
    
    private float getHue(int color){
    	float[] hsv={0f,0f,0f};
    	Color.colorToHSV(color, hsv);
    	return hsv[0];
    }

    @Override 
    protected void onDraw(Canvas canvas) {
        
        canvas.translate(hueLineWidth/2, gapHeight); //move to top center of hue strip
        canvas.drawLine(0, 0, 0, hsvHeight, huePaint);	//draw the hue strip same height as svHeight
        float hueLineCenterHeight = hsvSelected[0]/360f*hsvHeight; 
        canvas.drawLine(0, hueLineCenterHeight-density, 0, hueLineCenterHeight+density, hueLinePaint); //draw hue line marker 2 dip wide
        
        canvas.translate(hueLineWidth/2+gapWidth, hsvHeight/2); //move to left center of sv rectangle
        canvas.drawLine(0, 0, svWidth, 0, saturationPaint);	//draw the saturation across rectangle
        
        canvas.translate(svWidth/2, -hsvHeight/2); //move to top center of sv rectangle
        canvas.drawLine(0, 0, 0, hsvHeight, valuePaint);  //draw the value down the rectangle
        
        canvas.translate(-svWidth/2, 0); //move to top left corner of sv rectangle
        canvas.drawCircle(hsvSelected[1]*svWidth, (1-hsvSelected[2])*hsvHeight, 3*density, svCirclePaint); //draw sv marker
        
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            	requestFocus();
            	
                trackingHue= (x>gapWidth && x <= hueLineWidth && y <= hsvHeight);
                trackingSV= !trackingHue && (x >= hueLineWidth+gapWidth+gapWidth) && (y <= hsvHeight);
                if (trackingHue) {
                    float hue=(y-gapHeight)/hsvHeight*360;//convert current hue position to main hue. 
                    updateHue(hue);
                    invalidate();
                } else if (trackingSV){
                	hsvSelected[1]=(x-hueLineWidth-gapWidth-gapWidth)/svWidth; //convert current x position into saturation
                	hsvSelected[2]=1-(y-gapHeight)/hsvHeight; //convert current y position into value
                	invalidate();
                }
                break;
            case MotionEvent.ACTION_MOVE:
            	if (trackingHue) {
            		//clamp tracking position
            		if (y<gapHeight) y=gapHeight;
                    else if (y>hsvHeight+gapHeight) y=hsvHeight+gapHeight;
            		
                    float hue =(y-gapHeight)/hsvHeight*360;//convert current hue position to main hue
                    updateHue(hue);
                    invalidate();
                } else if (trackingSV){
                	//clamp tracking position
                	if (x<hueLineWidth+gapWidth+gapWidth) x=hueLineWidth+gapWidth+gapWidth;
                    else if (x>hueLineWidth + gapWidth + svWidth + gapWidth) x=hueLineWidth + gapWidth + svWidth + gapWidth;
                	if (y<gapHeight) y = gapHeight;
                	else if (y>hsvHeight+gapHeight) y = hsvHeight+gapHeight;
                	
                	hsvSelected[1]=(x-hueLineWidth-gapWidth-gapWidth)/svWidth; //convert current x position into saturation
                	hsvSelected[2]=1-(y-gapHeight)/hsvHeight; //convert current y position into value
                	invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (trackingHue) {
                	trackingHue = false; 
                    invalidate();
                }
                if (trackingSV) {
                	trackingSV = false; 
                    invalidate();
                }
                break;
        }
        if (mListener!=null){
        	mListener.onColorChanged(getColor(), this);
        }
        
        return true;
    }        
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    	int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    	int widthTarget = MeasureSpec.getSize(widthMeasureSpec);

    	switch (widthMode){
    	case MeasureSpec.UNSPECIFIED:
    		widthTarget = (int)(WIDTH_DP*density);
    		break;
    	case MeasureSpec.AT_MOST:
    		widthTarget = Math.min((int)(WIDTH_DP*density), widthTarget);
    		break;
    	}

    	hueLineWidth=(int)(HUE_LINE_WIDTH_DP*density);
    	gapWidth=(int)(GAP_DP*density);
        hueStripWidth=hueLineWidth-2*gapWidth;
        svWidth=widthTarget-hueLineWidth-2*gapWidth;  //sv is the flexible part of width
        
    	int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    	int heightTarget = MeasureSpec.getSize(heightMeasureSpec);
    	
    	switch (heightMode){
    	case MeasureSpec.UNSPECIFIED:
    		heightTarget = (int)(HSV_HEIGHT_DP*density);
    		break;
    	case MeasureSpec.AT_MOST:
    		heightTarget = Math.min((int)(HSV_HEIGHT_DP*density), heightTarget);
    		break;
    	}
    	gapHeight = (int)(GAP_DP*density);
    	hsvHeight = heightTarget - gapHeight*2;
    	
    	preparePaintWithNewSizes();
    	
        setMeasuredDimension(widthTarget, heightTarget);
    }
    
}