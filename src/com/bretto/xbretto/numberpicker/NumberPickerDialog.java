/*
 * Copyright (C) 2010-2012 Mike Novak <michael.novakjr@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bretto.xbretto.numberpicker;

import com.bretto.xbretto.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class NumberPickerDialog extends AlertDialog implements OnClickListener {
    private OnNumberSetListener mListener;
    
    // May there be one or two pickers, depends on layout
    private NumberPicker mNumberPicker1;
    private NumberPicker mNumberPicker2;

    private int mInitialValue;

    public NumberPickerDialog(Context context, int theme, int initialValue, int layout, String description) {
        super(context, theme);
        mInitialValue = initialValue;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(layout, null);
        setView(view);
        
        if (description != null) {
        	TextView textView = (TextView)view.findViewById(R.id.title_time);
        	textView.setText(description);
        }

        mNumberPicker1 = (NumberPicker) view.findViewById(R.id.time_picker);
        mNumberPicker1.setCurrent(mInitialValue);
        
        // Try to inflate second one
        mNumberPicker2 = (NumberPicker) view.findViewById(R.id.speed_picker);
        if (mNumberPicker2 != null) {
        	mNumberPicker2.setCurrent(mInitialValue);
        }
    }

    public void setRange(int picker, int start, int end) {
    	if (picker == 1) {
    		mNumberPicker1.setRange(start, end);
    	} else if ((picker == 2) && (mNumberPicker2 != null)) {
    		mNumberPicker2.setRange(start, end);
    	}
    }

    public void setWrap(int picker, boolean wrap) {
    	if (picker == 1) {
    		mNumberPicker1.setWrap(wrap);
    	} else if ((picker == 2) && (mNumberPicker2 != null)) {
    		mNumberPicker2.setWrap(wrap);
    	}
    }

    public void setRange(int picker, int start, int end, String[] displayedValues) {
    	if (picker == 1) {
    		mNumberPicker1.setRange(start, end, displayedValues);
    	} else if ((picker == 2) && (mNumberPicker2 != null)) {
    		mNumberPicker2.setRange(start, end, displayedValues);
    	}
    }

    public void setOnNumberSetListener(OnNumberSetListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mListener != null) {
        	Integer number1 = mNumberPicker1.getCurrent();
        	
        	Integer number2 = null;
        	if (mNumberPicker2 != null) {
        		number2 = mNumberPicker2.getCurrent();
        	}
            mListener.onNumberSet(this, number1, number2);
        }
    }

    public interface OnNumberSetListener {
        public void onNumberSet(NumberPickerDialog pickerDialog, Integer selectedNumber1, Integer selectedNumber2);
    }
}
