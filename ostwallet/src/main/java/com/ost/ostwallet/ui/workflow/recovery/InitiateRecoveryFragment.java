/*
 * Copyright 2019 OST.com Inc
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 */

package com.ost.ostwallet.ui.workflow.recovery;


import android.app.Fragment;
import android.os.Bundle;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link InitiateRecoveryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class InitiateRecoveryFragment extends RecoveryFragment {

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment CreateSessionFragment.
     */
    public static InitiateRecoveryFragment newInstance(String deviceAddress) {
        InitiateRecoveryFragment fragment = new InitiateRecoveryFragment();
        Bundle args = new Bundle();
        args.putString(DEVICE_ADDRESS, deviceAddress);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public RecoveryPresenter getPresenter() {
        return InitiateRecoveryPresenter.getInstance();
    }
}