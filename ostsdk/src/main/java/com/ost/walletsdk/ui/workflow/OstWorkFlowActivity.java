package com.ost.walletsdk.ui.workflow;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.ost.walletsdk.OstSdk;
import com.ost.walletsdk.R;
import com.ost.walletsdk.models.entities.OstUser;
import com.ost.walletsdk.ui.BaseActivity;
import com.ost.walletsdk.ui.ChildFragmentStack;
import com.ost.walletsdk.ui.OstResourceProvider;
import com.ost.walletsdk.ui.WebViewFragment;
import com.ost.walletsdk.ui.WorkFlowPinFragment;
import com.ost.walletsdk.ui.loader.OstLoaderFragment;
import com.ost.walletsdk.ui.managedevices.Device;
import com.ost.walletsdk.ui.managedevices.DeviceListRecyclerViewAdapter;
import com.ost.walletsdk.ui.recovery.AbortRecoveryFragment;
import com.ost.walletsdk.ui.recovery.InitiateRecoveryFragment;
import com.ost.walletsdk.ui.recovery.RecoveryFragment;
import com.ost.walletsdk.ui.resetpin.ResetPinFragment;
import com.ost.walletsdk.ui.sdkInteract.SdkInteract;
import com.ost.walletsdk.ui.sdkInteract.WorkFlowListener;
import com.ost.walletsdk.ui.test.TestThemeFragment;
import com.ost.walletsdk.ui.uicomponents.uiutils.content.ContentConfig;
import com.ost.walletsdk.ui.util.DialogFactory;
import com.ost.walletsdk.ui.util.FragmentUtils;
import com.ost.walletsdk.ui.util.KeyBoard;
import com.ost.walletsdk.ui.walletsetup.WalletSetUpFragment;
import com.ost.walletsdk.workflows.OstContextEntity;
import com.ost.walletsdk.workflows.OstWorkflowContext;
import com.ost.walletsdk.workflows.errors.OstError;
import com.ost.walletsdk.workflows.errors.OstErrors;
import com.ost.walletsdk.workflows.interfaces.OstPinAcceptInterface;
import com.ost.walletsdk.workflows.interfaces.OstVerifyDataInterface;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ost.walletsdk.ui.recovery.RecoveryFragment.DEVICE_ADDRESS;
import static com.ost.walletsdk.ui.recovery.RecoveryFragment.SHOW_BACK_BUTTON;


public class OstWorkFlowActivity extends BaseActivity implements WalletSetUpFragment.OnFragmentInteractionListener,
        DeviceListRecyclerViewAdapter.OnDeviceListInteractionListener,
        SdkInteract.WorkFlowCallbacks,
        WorkFlowPinFragment.OnFragmentInteractionListener,
        ResetPinFragment.OnFragmentInteractionListener,
        RecoveryFragment.OnFragmentInteractionListener,
        OstLoaderCompletionDelegate {

    public static final String WORKFLOW_ID = "workflowId";
    public static final String WORKFLOW_NAME = "workflowName";
    public static final String ACTIVATE_USER = "activate_user";
    public static final String USER_ID = "user_id";
    public static final String EXPIRED_AFTER_SECS = "expired_after_secs";
    public static final String SPENDING_LIMIT = "spending_limit";
    public static final String INITIATE_RECOVERY = "initiate_recovery";
    public static final String ABORT_RECOVERY = "abort_recovery";
    public static final String CREATE_SESSION = "create_session";
    public static final String RESET_PIN = "reset_pin";
    public static final String UPDATE_BIOMETRIC_PREFERENCE = "update_biometric_pref";
    public static final String ENABLE = "enable";
    public static final String GET_DEVICE_MNEMONICS = "get_device_mnemonics";
    public static final String AUTHORIZE_DEVICE_WITH_MNEMONICS = "authorize_device_with_mnemonics";
    public static final String SHOW_QR = "show_qr";
    public static final String AUTHORIZE_DEVICE_VIA_QR = "authorize_device_via_qr";
    public static final String AUTHORIZE_TXN_VIA_QR = "authorize_txn_via_qr";

    private static final String LOG_TAG = "OstWorkFlowActivity";
    private static final String CROSS_BUTTON_CLICK_CODE = "owfa_gb";

    WorkFlowListener mWorkFlowListener;
    private boolean mUiWorkflowFinished = false;
    private Intent mIntent;
    String mWorkflowId;
    String mWorkFlowName;
    String mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ost_work_flow);
        mIntent = getIntent();
        mWorkflowId = getIntent().getStringExtra(WORKFLOW_ID);
        mWorkFlowName = getIntent().getStringExtra(WORKFLOW_NAME);
        mUserId = getIntent().getStringExtra(USER_ID);
        mWorkFlowListener = SdkInteract.getInstance().getWorkFlowListener(mWorkflowId);
        if (null == mWorkFlowListener || null == mWorkFlowName) {
            FragmentUtils.addFragment(R.id.layout_container,
                    TestThemeFragment.newInstance(),
                    this);
            return;
        }

        try {
            ensureValidState();
        } catch (OstError error) {
            mWorkFlowListener.flowInterrupt(getWorkflowContext(), error);
            finishWorkflow();
            return;
        } catch (Throwable th) {
            OstError error = new OstError("owfa_onc_1", OstErrors.ErrorCode.UNCAUGHT_EXCEPTION_HANDELED);
            error.setStackTrace( th.getStackTrace() );
            mWorkFlowListener.flowInterrupt(getWorkflowContext(), error);
            finishWorkflow();
            return;
        }

        initiateWorkFlow();

        if (null != mWorkFlowListener) {
            mWorkFlowListener.setWorkflowCallbacks(this);
        }
    }

    void initiateWorkFlow() {
        // Don't do anything here
    }

    void ensureValidState() throws OstError {
        if (null == OstUser.getById(mUserId) || null == OstUser.getById(mUserId).getCurrentDevice() ) {
            throw new OstError("owfa_oc_is_1", OstErrors.ErrorCode.DEVICE_NOT_SETUP);
        }
    }

    OstWorkflowContext getWorkflowContext() {
        return new OstWorkflowContext(OstWorkflowContext.WORKFLOW_TYPE.UNKNOWN);
    }

    @Override
    protected View getRootView() {
        return findViewById(R.id.layout_container);
    }


    @Override
    public void goBack() {
        Fragment topFragment = FragmentUtils.getTopFragment(this, R.id.layout_container);
        boolean consumed = false;
        if (topFragment instanceof ChildFragmentStack) {
            consumed = ((ChildFragmentStack)topFragment).popBack();
        }
        if (!consumed) {
            if (this.getSupportFragmentManager().getBackStackEntryCount() > 1) {
                FragmentUtils.goBack(this);
            } else {
                //hide keyboard if open
                KeyBoard.hideKeyboard(OstWorkFlowActivity.this);
                //interrupt workflow
                if (null != mWorkFlowListener) mWorkFlowListener.flowInterrupt(getWorkflowContext(), new OstError(CROSS_BUTTON_CLICK_CODE, OstErrors.ErrorCode.WORKFLOW_CANCELLED));
                super.goBack();
            }
        }
    }

    @Override
    public void activateAcknowledged(String workflowId) {

    }

    @Override
    public void onPinEntered(String pin) {
        // Do do anything
    }

    @Override
    public void openWebView(String url) {
        WebViewFragment fragment = WebViewFragment.newInstance(url);
        FragmentUtils.addFragment(R.id.layout_container,
                fragment,
                this);
    }

    @Override
    public void onDeviceSelectToRevoke(Device device) {
        OstSdk.revokeDevice(mUserId, device.getDeviceAddress(), mWorkFlowListener);
    }

    @Override
    public void onDeviceSelectedForRecovery(Device device) {
        Bundle bundle = mIntent.getExtras();
        bundle.putString(DEVICE_ADDRESS, device.getDeviceAddress());
        bundle.putBoolean(SHOW_BACK_BUTTON, true);
        FragmentUtils.addFragment(R.id.layout_container,
                InitiateRecoveryFragment.newInstance(bundle),
                this);
    }

    @Override
    public void onDeviceSelectedToAbortRecovery(Device device) {
        Bundle bundle = mIntent.getExtras();
        bundle.putString(DEVICE_ADDRESS, device.getDeviceAddress());
        bundle.putBoolean(SHOW_BACK_BUTTON, true);
        FragmentUtils.addFragment(R.id.layout_container,
                AbortRecoveryFragment.newInstance(bundle),
                this);
    }

    @Override
    public boolean getPin(String workflowId, OstWorkflowContext ostWorkflowContext, String userId, OstPinAcceptInterface ostPinAcceptInterface) {
        showProgress(false);
        showGetPinFragment(workflowId, userId, ostWorkflowContext, ostPinAcceptInterface);
        return false;
    }

    @Override
    public boolean invalidPin(String workflowId, OstWorkflowContext ostWorkflowContext, String userId, OstPinAcceptInterface ostPinAcceptInterface) {
        showProgress(false);
        Dialog dialog = DialogFactory.createSimpleOkErrorDialog(OstWorkFlowActivity.this,
                "Incorrect PIN",
                "Please enter your valid PIN to authorize", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showGetPinFragment(workflowId, userId, ostWorkflowContext, ostPinAcceptInterface);
                    }
                });
        dialog.setCancelable(false);
        dialog.show();
        return false;
    }

    @Override
    public boolean pinValidated(String workflowId, OstWorkflowContext ostWorkflowContext, String userId) {
        return false;
    }

    @Override
    public boolean flowComplete(String workflowId, OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity) {
        getWorkflowLoader().onSuccess(ostWorkflowContext, ostContextEntity, getContentConfig(ostWorkflowContext.getWorkflowType()), OstWorkFlowActivity.this);
        return false;
    }

    @Override
    public boolean flowInterrupt(String workflowId, OstWorkflowContext ostWorkflowContext, OstError ostError) {
        getWorkflowLoader().onFailure(ostWorkflowContext, ostError, getContentConfig(ostWorkflowContext.getWorkflowType()),OstWorkFlowActivity.this);

        return false;
    }

    @Override
    public boolean requestAcknowledged(String workflowId, OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity) {
        boolean waitForFinalization = OstResourceProvider.getLoaderManager().waitForFinalization(ostWorkflowContext.getWorkflowType());
        if (waitForFinalization) {
            getWorkflowLoader().onAcknowledge(getContentConfig(ostWorkflowContext.getWorkflowType()));
            return false;
        }

        showProgress(false);
        setUiWorkfLowFinished();
        finish();
        return false;
    }

    private JSONObject getContentConfig(OstWorkflowContext.WORKFLOW_TYPE workflowType) {
        String contentConfigKey = null;
        if (OstWorkflowContext.WORKFLOW_TYPE.ACTIVATE_USER.equals(workflowType)) {
            contentConfigKey = "activate_user";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.RESET_PIN.equals(workflowType)) {
            contentConfigKey = "reset_pin";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.ADD_SESSION.equals(workflowType)) {
            contentConfigKey = "add_session";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.GET_DEVICE_MNEMONICS.equals(workflowType)) {
            contentConfigKey = "view_mnemonics";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.UPDATE_BIOMETRIC_PREFERENCE.equals(workflowType)) {
            contentConfigKey = "biometric_preference";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.ABORT_DEVICE_RECOVERY.equals(workflowType)) {
            contentConfigKey = "abort_recovery";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.SHOW_DEVICE_QR.equals(workflowType)) {
            contentConfigKey = "show_add_device_qr";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.AUTHORIZE_DEVICE_WITH_QR_CODE.equals(workflowType)) {
            contentConfigKey = "scan_qr_to_authorize_device";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.AUTHORIZE_DEVICE_WITH_MNEMONICS.equals(workflowType)) {
            contentConfigKey = "add_current_device_with_mnemonics";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.INITIATE_DEVICE_RECOVERY.equals(workflowType)) {
            contentConfigKey = "initiate_recovery";
        } else if (OstWorkflowContext.WORKFLOW_TYPE.REVOKE_DEVICE.equals(workflowType)) {
            contentConfigKey = "revoke_device";
        } else {
            return null;
        }

        return ContentConfig.getInstance().getStringConfig(contentConfigKey);
    }

    @Override
    public boolean verifyData(String workflowId, OstWorkflowContext ostWorkflowContext, OstContextEntity ostContextEntity, OstVerifyDataInterface ostVerifyDataInterface) {
        return false;
    }

    private void finishWorkflow() {
        setUiWorkfLowFinished();
        finish();
    }

    private void setUiWorkfLowFinished() {
        mUiWorkflowFinished = true;
    }

    private void showGetPinFragment(String workflowId, String userId, OstWorkflowContext ostWorkflowContext, OstPinAcceptInterface ostPinAcceptInterface) {
        JSONObject stringConfigJsonObject = getContentString(ostWorkflowContext);
        WorkFlowPinFragment fragment = WorkFlowPinFragment.newInstance("Get Pin", getResources().getString(R.string.pin_sub_heading_get_pin), showBackButton());
        fragment.contentConfig = stringConfigJsonObject;
        fragment.setPinCallback(ostPinAcceptInterface);
        fragment.setUserId(userId);
        fragment.setWorkflowId(workflowId);
        fragment.setWorkflowContext(ostWorkflowContext);

        FragmentUtils.addFragment(R.id.layout_container,
                fragment,
                this);
    }

    boolean showBackButton() {
        return false;
    }

    JSONObject getContentString(OstWorkflowContext ostWorkflowContext) {
        try {
            return new JSONObject("{\n" +
                    "        \"title_label\": {\n" +
                    "          \"text\": \"Get PIN\"\n" +
                    "        },\n" +
                    "        \"lead_label\": {\n" +
                    "          \"text\": \"If you forget your PIN, you cannot recover your wallet\"\n" +
                    "        },\n" +
                    "        \"info_label\":{\n" +
                    "          \"text\":  \"\"\n" +
                    "        }\n" +
                    "      }");
        } catch (JSONException e) {
            return null;
        }
    }

    @Override
    public void popTopFragment() {
        FragmentUtils.goBack(this);
    }

    @Override
    public void invalidPin(long workflowId, OstWorkflowContext ostWorkflowContext, String userId, OstPinAcceptInterface ostPinAcceptInterface) {
        ostPinAcceptInterface.cancelFlow();
    }

    protected boolean isCrossButtonClicked(OstError ostError) {
        return CROSS_BUTTON_CLICK_CODE.equals(ostError.getInternalErrorCode());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        OstError error = new OstError("owfa_ond_2", OstErrors.ErrorCode.WORKFLOW_VIEW_DESTROYED);
        if (!mUiWorkflowFinished) {
            Log.d(LOG_TAG, "Workflow view destroyed");
            if (null != mWorkFlowListener) mWorkFlowListener.flowInterrupt(getWorkflowContext(), error);
        }
    }

    @Override
    protected OstLoaderFragment createDialogFragment() {
        return OstResourceProvider.getLoaderManager().getLoader(getWorkflowContext().getWorkflowType());
    }

    @Override
    public void dismissWorkflow() {
        //showProgress(false);
        finishWorkflow();
    }
}