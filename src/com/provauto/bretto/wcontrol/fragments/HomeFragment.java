package com.provauto.bretto.wcontrol.fragments;

import com.provauto.bretto.wcontrol.MainActivity;
import com.provauto.bretto.wcontrol.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

public class HomeFragment extends Fragment {
	
	private Activity mActivity;
	private boolean mLaid = false;
	
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	return inflater.inflate(R.layout.home_layout, container, false);
    }

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		final Fragment myFragment = this;

		this.getView().getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					
				@Override
				public void onGlobalLayout() {
					
					View sensorBtn = myFragment.getView().findViewById(R.id.sensorButton);
					View locationBtn = myFragment.getView().findViewById(R.id.locationButton);
					View saveBtn = myFragment.getView().findViewById(R.id.saveButton);
					
					// Second time onGlobalLayout() is called, remove listener
					// Avoid a strange behavior laying these buttons, they appear in the corner before be laid properly
					// Now, they are invisible, and become visible when they are in the right position (second time onGlobalLayout() is called)
					if (mLaid){
						// make sure it is not called anymore
						myFragment.getView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
						sensorBtn.setVisibility(View.VISIBLE);
						locationBtn.setVisibility(View.VISIBLE);
						saveBtn.setVisibility(View.VISIBLE);
						mLaid = false;
						return;
					}

					// Resize and relocate central buttons
					// First, buttons must have same total width than escreen
					// Then, relocate buttons in the middle of screen
					
					DisplayMetrics displaymetrics = new DisplayMetrics();
					mActivity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
					int screenWidth = displaymetrics.widthPixels;
					Log.d(MainActivity.APP_TAG,"screen width "+screenWidth);
/*					
					int difference = screenWidth - (sensorBtn.getWidth() + locationBtn.getWidth() + saveBtn.getWidth());
					Log.d(MainActivity.APP_TAG,"difference "+difference);
					
					// First, buttons must have same total width than escreen
					if (difference != 0){
						
						int offset = difference / 2;
						
						// Distribute difference between left and right buttons
						RelativeLayout.LayoutParams sensorParams = (RelativeLayout.LayoutParams) sensorBtn.getLayoutParams();
						Log.d(MainActivity.APP_TAG,"sensor width "+sensorParams.width);
						sensorParams.width += offset;
						Log.d(MainActivity.APP_TAG,"new sensor width "+sensorParams.width);
						sensorBtn.setLayoutParams(sensorParams);
						
						RelativeLayout.LayoutParams saveParams = (RelativeLayout.LayoutParams) saveBtn.getLayoutParams();
						Log.d(MainActivity.APP_TAG,"save width "+sensorParams.width);
						saveParams.width += offset;
						Log.d(MainActivity.APP_TAG,"new save width "+saveParams.width);
						saveBtn.setLayoutParams(saveParams);
						
						// It's not a even difference 
						if ((difference % 2) != 0) {
							// difference positive
							if (offset > 0){
								saveParams.width += 1;
							// difference negative
							} else {
								saveParams.width -= 1;
							}
							saveBtn.setLayoutParams(saveParams);
						}
					}
*/					
					// Then, relocate buttons in the middle of screen
					
					View assembleOn = myFragment.getView().findViewById(R.id.assembleOnButton);
					int heightAssemble = assembleOn.getHeight();

					int heightSensor = sensorBtn.getHeight();

					int sensorBtnX = 0;
					int sensorBtnY = ((heightAssemble * 2) - heightSensor) / 2;
					
					//int locationBtnX = sensorBtnX + sensorBtn.getWidth();
					int locationBtnX = (screenWidth/2) - (locationBtn.getWidth()/2);
					int locationBtnY = sensorBtnY;
					
					//int saveBtnX = locationBtnX + locationBtn.getWidth();
					int saveBtnX = screenWidth - saveBtn.getWidth();
					int saveBtnY = sensorBtnY;
					
					RelativeLayout.LayoutParams sensorParams = (RelativeLayout.LayoutParams) sensorBtn.getLayoutParams();
					sensorParams.leftMargin = sensorBtnX;
					sensorParams.topMargin = sensorBtnY;
					sensorBtn.setLayoutParams(sensorParams);
					
					RelativeLayout.LayoutParams locationParams = (RelativeLayout.LayoutParams) locationBtn.getLayoutParams();
					locationParams.leftMargin = locationBtnX;
					locationParams.topMargin = locationBtnY;
					locationBtn.setLayoutParams(locationParams);
					
					RelativeLayout.LayoutParams saveParams = (RelativeLayout.LayoutParams) saveBtn.getLayoutParams();
					saveParams.leftMargin = saveBtnX;
					saveParams.topMargin = saveBtnY;
					saveBtn.setLayoutParams(saveParams);
					
					mLaid = true;
				}
				
		});
		
	} // end of onActivityCreated
  
}
