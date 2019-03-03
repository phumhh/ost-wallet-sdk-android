package com.ost.mobilesdk.workflows.services;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ost.mobilesdk.OstConstants;
import com.ost.mobilesdk.OstSdk;
import com.ost.mobilesdk.models.entities.OstBaseEntity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public abstract class OstPollingService extends IntentService {

    public static final String EXTRA_USER_ID = "com.ost.mobilesdk.workflows.extra.USER_ID";
    public static final String EXTRA_ENTITY_ID = "com.ost.mobilesdk.workflows.extra.ENTITY_ID";
    public static final String EXTRA_ENTITY_FAILURE_STATUS = "com.ost.mobilesdk.workflows.extra.ENTITY_FAILURE_STATUS";
    public static final String EXTRA_ENTITY_SUCCESS_STATUS = "com.ost.mobilesdk.workflows.extra.ENTITY_SUCCESS_STATUS";
    public static final String EXTRA_POLL_COUNT = "com.ost.mobilesdk.workflows.extra.POLL_COUNT";
    public static final String ENTITY_UPDATE_MESSAGE = "com.ost.mobilesdk.workflows.extra.ENTITY_UPDATE";
    public static final String EXTRA_ENTITY_TYPE = "com.ost.mobilesdk.workflows.extra.ENTITY_TYPE";
    public static final String EXTRA_IS_POLLING_TIMEOUT = "com.ost.mobilesdk.workflows.extra.IS_POLLING_TIMEOUT";
    public static final String EXTRA_IS_VALID_RESPONSE = "com.ost.mobilesdk.workflows.extra.IS_VALID_RESPONSE";

    private static final int POLL_MAX_COUNT = 10;

    private static final String TAG = "OstPollingService";
    private static final long POLLING_INTERVAL = OstConstants.BLOCK_GENERATION_TIME * 1000;
    private static final long INITIAL_POLLING_INTERVAL = 6 * OstConstants.BLOCK_GENERATION_TIME * 1000;

    public OstPollingService() {
        super(TAG);
    }

    static void startPolling(Context context, Intent intent, String userId, String entityId,
                             String successStatus, String failureStatus) {
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        intent.putExtra(EXTRA_ENTITY_SUCCESS_STATUS, successStatus);
        intent.putExtra(EXTRA_ENTITY_FAILURE_STATUS, failureStatus);

        Calendar calendar = Calendar.getInstance();
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + INITIAL_POLLING_INTERVAL, pendingIntent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String userId = intent.getStringExtra(EXTRA_USER_ID);
            final String entityId = intent.getStringExtra(EXTRA_ENTITY_ID);
            final String entitySuccessStatus = intent.getStringExtra(EXTRA_ENTITY_SUCCESS_STATUS);
            final String entityFailureStatus = intent.getStringExtra(EXTRA_ENTITY_FAILURE_STATUS);
            final int pollCount = intent.getIntExtra(EXTRA_POLL_COUNT, POLL_MAX_COUNT);

            if (!isValidUserId(userId)) {
                Log.e(TAG, String.format("Invalid User Id: %s", userId));
                return;
            }

            if (!validateParams(entityId, entitySuccessStatus, entityFailureStatus)) {
                Log.e(TAG, String.format("Invalid Entity Params for Entity: %s, EntityId: %s, " +
                                "From Status: %s, To Status: %s", getEntityName(), entityId,
                        entitySuccessStatus, entityFailureStatus));
                return;
            }

            Log.i(TAG, String.format("Polling... of entity %s", getEntityName()));
            JSONObject response = null;
            try {
                response = poll(userId, entityId);
            } catch (IOException e) {
                Log.e(TAG, String.format("IOException: %s", e.getCause()));
            }
            Log.d(TAG, String.format("Response of %s poll is %s", getEntityName(), response.toString()));

            Log.i(TAG, String.format("Checking response validity of %s entity", getEntityName()));
            boolean isValidResponse = isResponseValid(response);
            Log.d(TAG, String.format("Response of %s entity validity: %b", getEntityName(), isValidResponse));
            if (!isValidResponse) {
                sendUpdateMessage(userId, entityId, false, false);
                return;
            }

            Log.i(TAG, String.format("Checking %s entity update status", getEntityName()));
            String status = updatedGivenStatus(response, entitySuccessStatus, entityFailureStatus);
            if (null != status) {
                Log.d(TAG, String.format("Is %s entity updated status %s", getEntityName(), status));
                sendUpdateMessage(userId, entityId, false, status.equalsIgnoreCase(entitySuccessStatus));
                return;
            }

            int newPollCount = pollCount - 1;
            if (newPollCount < 0) {
                Log.d(TAG, String.format("Poll count reach to zero for %s", getEntityName()));
                sendUpdateMessage(userId, entityId, true);
            } else {
                Log.d(TAG, String.format("New Alarm set for %s", getEntityName()));
                setAlarm(userId, entityId, entitySuccessStatus, entityFailureStatus, newPollCount);
            }
        }
    }

    private void sendUpdateMessage(String userId, String entityId, boolean pollingTimeout) {
        sendUpdateMessage(userId, entityId, pollingTimeout, true);
    }

    private void sendUpdateMessage(String userId, String entityId, boolean pollingTimeout, boolean validResponse) {
        Intent updateIntent = new Intent(ENTITY_UPDATE_MESSAGE);
        // You can also include some extra data.
        updateIntent.putExtra(EXTRA_USER_ID, userId);
        updateIntent.putExtra(EXTRA_ENTITY_ID, entityId);
        updateIntent.putExtra(EXTRA_ENTITY_TYPE, getEntityName());
        updateIntent.putExtra(EXTRA_IS_POLLING_TIMEOUT, pollingTimeout);
        updateIntent.putExtra(EXTRA_IS_VALID_RESPONSE, validResponse);

        LocalBroadcastManager.getInstance(this).sendBroadcast(updateIntent);
    }

    private boolean isValidUserId(String userId) {
        return null != OstSdk.getUser(userId);
    }

    private String updatedGivenStatus(JSONObject response, String entitySuccessStatus, String entityFailureStatus) {
        JSONObject jsonData = response.optJSONObject(OstConstants.RESPONSE_DATA);
        JSONObject entityObject = jsonData.optJSONObject(getEntityName());
        String currentStatus;
        try {
            OstBaseEntity ostBaseEntity = parseEntity(entityObject);
            currentStatus = ostBaseEntity.getStatus();
        } catch (JSONException e) {
            Log.d(TAG, "JSONException", e);
            return null;
        }
        Log.d(TAG, String.format("Entity Success status: %s, Entity Failure status %s, Entity Current status %s",
                entitySuccessStatus, entityFailureStatus, currentStatus));

        if (entitySuccessStatus.equalsIgnoreCase(currentStatus)) {
            return entitySuccessStatus;
        }
        if (entityFailureStatus.equalsIgnoreCase(currentStatus)) {
            return entityFailureStatus;
        }
        Log.d(TAG, String.format("No update received for %s entity", getEntityName()));
        return null;
    }

    protected abstract OstBaseEntity parseEntity(JSONObject entityObject) throws JSONException;

    private boolean isResponseValid(JSONObject response) {
        try {
            if (!response.getBoolean(OstConstants.RESPONSE_SUCCESS)) {
                return false;
            }
            JSONObject jsonData = response.getJSONObject(OstConstants.RESPONSE_DATA);
            jsonData.has(getEntityName());
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    private void setAlarm(String userId, String entityId, String fromStatus, String toStatus, int pollCount) {

        Intent intent = getServiceIntent(this);
        intent.putExtra(EXTRA_USER_ID, userId);
        intent.putExtra(EXTRA_ENTITY_ID, entityId);
        intent.putExtra(EXTRA_ENTITY_SUCCESS_STATUS, fromStatus);
        intent.putExtra(EXTRA_ENTITY_FAILURE_STATUS, toStatus);
        intent.putExtra(EXTRA_POLL_COUNT, pollCount);

        Calendar calendar = Calendar.getInstance();
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 0, intent, 0);
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis() + POLLING_INTERVAL, pendingIntent);
    }

    protected abstract Intent getServiceIntent(Context context);

    protected abstract String getEntityName();

    protected abstract JSONObject poll(String userId, String entityId) throws IOException;

    protected abstract boolean validateParams(String entityId, String fromStatus, String toStatus);
}