/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package ost.com.ostsdkui.uicomponents;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import ost.com.ostsdkui.uicomponents.uiutils.Font;
import ost.com.ostsdkui.uicomponents.uiutils.FontFactory;
import ost.com.ostsdkui.uicomponents.uiutils.theme.ThemeConfig;


public class OstH2Label extends OstTextView {
    public OstH2Label(Context context) {
        super(context);
    }

    public OstH2Label(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OstH2Label(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    void defineUi(Context context, AttributeSet attrs, int defStyleAttr) {
        super.defineUi(context, attrs, defStyleAttr);
        ThemeConfig.getInstance().H2().apply(this);
    }

    @Override
    public void setTypeface(@Nullable Typeface tf) {
        Font font = FontFactory.getInstance(getContext(), FontFactory.FONT.LATO);
        super.setTypeface(font.getBold());
    }
}
