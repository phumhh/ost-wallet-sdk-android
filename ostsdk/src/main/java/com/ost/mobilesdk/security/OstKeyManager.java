package com.ost.mobilesdk.security;

import com.ost.mobilesdk.workflows.OstUserPinInfoHolder;
import com.ost.mobilesdk.workflows.errors.OstError;
import com.ost.mobilesdk.workflows.errors.OstErrors.ErrorCode;

public class OstKeyManager {
    private static final String TAG = "OstKeyManager";


    private final String mUserId;
    private KeyMetaStruct mKeyMetaStruct;

    public OstKeyManager(String userId) {
        this.mUserId = userId;
        mKeyMetaStruct = InternalKeyManager2.getKeyMataStruct(userId);
        if (null == mKeyMetaStruct ) {

            //Create new KeyManagerInstance so that keys are created.
            InternalKeyManager2 ikm = new InternalKeyManager2(userId);

            //Fetch KeyMataStruct.
            mKeyMetaStruct = InternalKeyManager2.getKeyMataStruct(userId);
            ikm = null;
        }
    }

    public String getApiKeyAddress() {
        return mKeyMetaStruct.getApiAddress();
    }

    byte[] getMnemonics(String address) {
        InternalKeyManager2 ikm = new InternalKeyManager2(mUserId);
        byte[] mnemonics = ikm.getMnemonics(address);
        ikm = null;
        return mnemonics;
    }

    public String createSessionKey() {
        InternalKeyManager2 ikm = new InternalKeyManager2(mUserId);
        String address = ikm.createSessionKey();
        ikm = null;

        if ( null == address ) {
            OstError error = new OstError("km_okm_csk_1", ErrorCode.SESSION_KEY_GENERATION_FAILED);
            throw error;
        }

        return address;
    }

    public String getDeviceAddress() {
        return mKeyMetaStruct.getDeviceAddress();
    }

    public byte[] getMnemonics() {
        String deviceAddress = getDeviceAddress();
        return getMnemonics(deviceAddress);
    }

    public boolean validatePin(OstUserPinInfoHolder pinInfoHolder) {
        InternalKeyManager ikm = new InternalKeyManager(mUserId);
        if ( !ikm.isUserPassphraseValidationAllowed() ) {
            OstError error = new OstError("km_okm_vpin_1", ErrorCode.MAX_PASSPHRASE_VERIFICATION_LIMIT_REACHED);
            throw error;
        }
        boolean isValid = ikm.validateUserPassphrase(pinInfoHolder.getPassphrasePrefix(), pinInfoHolder.getUserPassphrase(), pinInfoHolder.getScriptSalt());
        ikm = null;
        return isValid;

    }

}