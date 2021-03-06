/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ost.walletsdk.ui.uicomponents.uiutils;

import android.content.res.Resources;

public class SizeUtil {

    public static float getTextSize(Resources res, int resPath) {
        return res.getDimension(resPath)/ res.getDisplayMetrics().density;
    }

    public int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
