/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ost.ostwallet.ui.workflow.authrorizedeviceqr;

import com.ost.ostwallet.ui.BaseView;

interface AuthorizeDeviceQRView extends BaseView {
    void gotoDashboard(long workflowId);

    void launchQRScanner();
}