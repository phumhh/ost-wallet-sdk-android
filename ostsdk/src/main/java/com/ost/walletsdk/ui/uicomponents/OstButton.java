/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ost.walletsdk.ui.uicomponents;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import static android.view.View.TEXT_ALIGNMENT_CENTER;


public abstract class OstButton extends AppCompatButton {
    public OstButton(Context context) {
        super(context);
        defineUi(context, null, 0);
    }

    public OstButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        defineUi(context, attrs, 0);
    }

    public OstButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        defineUi(context, attrs, defStyleAttr);
    }

    void defineUi(Context context, AttributeSet attrs, int defStyleAttr) {
        setAllCaps(false);
        setLetterSpacing((float) -0.02);
        setLineSpacing(0, (float) 0.3);
        setGravity(Gravity.CENTER);
        setTextAlignment(TEXT_ALIGNMENT_CENTER);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}