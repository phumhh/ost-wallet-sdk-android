/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ost.walletsdk.workflows.interfaces;

import com.ost.walletsdk.workflows.OstContextEntity;
import com.ost.walletsdk.workflows.OstWorkflowContext;
import com.ost.walletsdk.workflows.errors.OstError;

import org.json.JSONObject;

/**
 * OstWorkFlowCallback implemented by SDK user to perform prerequisites task.
 * These tasks are assigned by SDK workflows with help of callbacks.
 *
 * @see com.ost.walletsdk.OstSdk
 */
public interface OstWorkFlowCallback {
    /**
     * Register device passed as parameter
     *
     * @param apiParams                    Register Device API parameters
     * @param ostDeviceRegisteredInterface To pass response
     */
    void registerDevice(JSONObject apiParams, OstDeviceRegisteredInterface ostDeviceRegisteredInterface);

    /**
     * Pin needed to check the authenticity of the user.
     * Developers should show pin dialog on this callback
     *
     * @param userId                Id of user whose password and pin are needed.
     * @param ostPinAcceptInterface To pass pin
     */
    void getPin(OstWorkflowContext ostWorkflowContext, String userId, OstPinAcceptInterface ostPinAcceptInterface);

    /**
     * Inform SDK user about invalid pin
     * Developers should show invalid pin error and ask for pin again on this callback
     *
     * @param userId                Id of user whose password and pin are needed.
     * @param ostPinAcceptInterface to pass another pin
     */
    void invalidPin(OstWorkflowContext ostWorkflowContext, String userId, OstPinAcceptInterface ostPinAcceptInterface);

    /**
     * Inform SDK user that entered pin is validated.
     * Developers should dismiss pin dialog on this callback
     *
     * @param userId Id of user whose pin and password has been validated.
     */
    void pinValidated(OstWorkflowContext ostWorkflowContext, String userId);

    /**
     * Inform SDK user the the flow is complete
     *
     * @param ostWorkflowContext workflow type
     * @param ostContextEntity status of the flow
     * @see OstContextEntity
     */
    void flowComplete(OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity);

    /**
     * Inform SDK user that flow is interrupted with errorCode
     * Developers should dismiss pin dialog (if open) on this callback
     *
     * @param ostWorkflowContext workflow type
     * @param ostError reason of interruption
     */
    void flowInterrupt(OstWorkflowContext ostWorkflowContext, OstError ostError);


    /**
     * Inform SDK user about workflow core api call
     * @param ostWorkflowContext info about workflow type
     * @param ostContextEntity info about entity
     */
    void requestAcknowledged(OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity);

    /**
     * Ask SDK user to verify data to proceed
     *
     * @param ostWorkflowContext       info about workflow type
     * @param ostContextEntity         info about entity
     * @param ostVerifyDataInterface to acknowledge workflow to proceed
     */
    void verifyData(OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity, OstVerifyDataInterface ostVerifyDataInterface);
}