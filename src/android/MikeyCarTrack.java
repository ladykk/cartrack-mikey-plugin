package com.spheresoftsolutions.cordova.plugin;

// The native Toast API
import android.widget.Toast;
// Cordova-required packages
import androidx.annotation.NonNull;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cartrack.blesdk.ctg.BleListener;
import com.cartrack.blesdk.ctg.BleService;
import com.cartrack.blesdk.ctg.BleTerminal;
import com.cartrack.blesdk.enumerations.*;
import com.google.android.material.snackbar.Snackbar;

public class MikeyCarTrack extends CordovaPlugin {
    private BleTerminal bleTerminal;
    private String terminalId = "";
    private int timeout = 10;
    private String authKey = "";
    private boolean isDebug = false;
    private String callbackId = "";

    @Override
    public boolean execute(String action, JSONArray args,
            final CallbackContext callbackContext) {
        try {
            switch (action) {
                case "initialize":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.isDebug = args.getBoolean(0);
                    this.initialized();
                    this.callbackId = callbackContext.getCallbackId();
                    PluginResult res = new PluginResult(PluginResult.Status.OK, feedbackResponse("onInitialize"));
                    res.setKeepCallback(true);
                    callbackContext.sendPluginResult(res);
                    return true;
                case "requestPermission":
                    boolean result = this.requestPermission();
                    if (result)
                        return this.response(callbackContext, true, "");
                    else
                        return this.response(callbackContext, false, "Some permissions are not granted.");
                case "toast":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.showToast(args.getString(0));
                    return this.response(callbackContext, true, "");
                case "snackBar":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.showSnackBar(args.getString(0));
                    return this.response(callbackContext, true, "");
                case "createTerminal":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.terminalId = args.getString(0);
                    this.createTerminal();
                    return this.response(callbackContext, true, "");
                case "setAuthKey":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.authKey = args.getString(0);
                    this.setAuthKey();
                    return this.response(callbackContext, true, "");
                case "getAuthKey":
                    return this.response(callbackContext, true, this.getAuthKey());
                case "removeAuthKey":
                    this.removeAuthKey();
                    return this.response(callbackContext, true, "");
                case "connect":
                    if (args.length() < 1)
                        return this.response(callbackContext, false, "Invalid number of arguments.");
                    this.timeout = args.getInt(0);
                    this.connect();
                    return this.response(callbackContext, true, "");
                case "disconnect":
                    this.disconnect();
                    return this.response(callbackContext, true, "");
                case "lock":
                    this.lock();
                    return this.response(callbackContext, true, "");
                case "unlock":
                    this.unlock();
                    return this.response(callbackContext, true, "");
                case "headlight":
                    this.headlight();
                    return this.response(callbackContext, true, "");
                case "horn":
                    this.horn();
                    return this.response(callbackContext, true, "");
                case "getLockState":
                    this.getLockState();
                    return this.response(callbackContext, true, "");
                case "unlockNoKeyFOB":
                    this.unlockNoKeyFOB();
                    return this.response(callbackContext, true, "");
                case "getVehicleStats":
                    this.getVehicleStats();
                    return this.response(callbackContext, true, "");
                case "getVehicleStatus":
                    this.getVehicleStatus();
                    return this.response(callbackContext, true, "");
                default:
                    this.showToast("Invalid action: " + action);
                    return this.response(callbackContext, false, "Invalid action: " + action);
            }
        } catch (JSONException e) {
            this.showToast("JSON Exception: " + e.getMessage());
            return this.response(callbackContext, false, "JSON Exception: " + e.getMessage());
        } catch (Exception e) {
            this.showToast("Exception: " + e.getMessage());
            return this.response(callbackContext, false, "Exception: " + e.getMessage());
        }
    }

    // [Cordova Plugin Methods]
    private boolean response(final CallbackContext callbackContext, Boolean result, String message) {
        PluginResult pluginResult;
        if (result) {
            pluginResult = new PluginResult(PluginResult.Status.OK, message);
        } else {
            pluginResult = new PluginResult(PluginResult.Status.ERROR, message);
        }
        callbackContext.sendPluginResult(pluginResult);
        return result;
    }

    private JSONObject feedbackResponse(String event, JSONObject data) {
        JSONObject feedback = new JSONObject();
        try {
            feedback.put("event", event);
            feedback.put("data", data);
        } catch (JSONException e) {
            this.showToast("JSON Exception: " + e.getMessage());
        }
        return feedback;
    }

    private JSONObject feedbackResponse(String event) {
        JSONObject feedback = new JSONObject();
        try {
            feedback.put("event", event);
        } catch (JSONException e) {
            this.showToast("JSON Exception: " + e.getMessage());
        }
        return feedback;
    }

    private void sendFeedback(JSONObject feedback) {
        PluginResult res = new PluginResult(PluginResult.Status.OK, feedback);
        res.setKeepCallback(true);
        this.webView.sendPluginResult(res, this.callbackId);
    }

    private void showToast(String text) {
        Toast toast = Toast.makeText(cordova.getActivity(), text, Toast.LENGTH_SHORT);
        toast.show();
    }

    private void showSnackBar(String text) {
        Snackbar.make(cordova.getActivity().getCurrentFocus(), text, Snackbar.LENGTH_LONG).show();
    }

    // [CarTrack Methods]
    private void initialized() {
        if (this.isDebug)
            this.showToast("CarTrack plugin initialized.");
        this.requestPermission();
    }

    private boolean requestPermission() {
        PermissionManager permissionManager = new PermissionManager(this.cordova.getActivity());
        boolean result = permissionManager.requestPermission();
        if (this.isDebug)
            this.showSnackBar(result ? "All permissions granted." : "Some permissions not granted.");
        return result;
    }

    private void createTerminal() {
        BleService.Companion.clear();
        BleService.Companion.configure(cordova.getContext());
        bleTerminal = BleService.Companion.getTerminal(terminalId);
        bleTerminal.setBleListener(bleListener);
        if (this.isDebug)
            this.showSnackBar("Terminal Created: " + terminalId);
    }

    private void connect() {
        bleTerminal.scanAndConnectToPeripheral(timeout * 1000L);
        if (this.isDebug)
            this.showToast("Connecting to Terminal: " + terminalId);
    }

    private void disconnect() {
        bleTerminal.disconnect();
        if (this.isDebug)
            this.showToast("Disconnecting from Terminal: " + terminalId);
    }

    private void setAuthKey() {
        bleTerminal.saveAuthKey(authKey);
    }

    private void removeAuthKey() {
        bleTerminal.removeAuthKey();
    }

    private String getAuthKey() {
        if (!this.authKey.equals(bleTerminal.getAuthKey())) {
            this.authKey = bleTerminal.getAuthKey();
        }
        this.showSnackBar("Auth Key: " + authKey);
        return this.authKey;
    }

    // [CarTrack Actions]
    private void lock() {
        bleTerminal.sendAction(BleAction.LOCK);
    }

    private void unlock() {
        bleTerminal.sendAction(BleAction.UNLOCK);
    }

    private void headlight() {
        bleTerminal.sendAction(BleAction.HEADLIGHT);
    }

    private void horn() {
        bleTerminal.sendAction(BleAction.HORN);
    }

    private void getLockState() {
        bleTerminal.sendAction(BleAction.GET_LOCK_STATE);
    }

    private void unlockNoKeyFOB() {
        bleTerminal.sendAction(BleAction.UNLOCK_NOKEYFOB);
    }

    private void getVehicleStats() {
        bleTerminal.sendAction(BleAction.VEHICLE_GET_CONFIG);
    }

    private void getVehicleStatus() {
        bleTerminal.sendAction(BleAction.VEHICLE_GET_STATUS);
    }

    BleListener bleListener = new BleListener() {
        @Override
        public void onSignalStrength(@NonNull BleSignalStrength bleSignalStrength, int rssiValue) {
            JSONObject data = new JSONObject();
            try {
                data.put("bleSignalStrength", bleSignalStrength.name());
                data.put("rssiValue", rssiValue);
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onSignalStrength", data));
            if (isDebug)
                showToast("Signal Strength: " + bleSignalStrength.name() + " [" + rssiValue + " dB]");
        }

        @Override
        public void onReconnect() {
            sendFeedback(feedbackResponse("onReconnect"));
            if (isDebug)
                showToast("Reconnecting to Terminal: " + terminalId);
        }

        @Override
        public void onTerminalConnected(@NonNull BleTerminal bleTerminal) {
            sendFeedback(feedbackResponse("onTerminalConnected"));
            if (isDebug)
                showToast("Terminal: " + bleTerminal.getTerminalId() + " Connected.");
        }

        @Override
        public void onTerminalCommandResult(BleAction bleAction) {
            JSONObject data = new JSONObject();
            try {
                data.put("bleAction", bleAction.name());
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onTerminalCommandResult", data));
            if (isDebug)
                showToast("Success executing " + bleAction.name());
        }

        @Override
        public void onTerminalDidGetVehicleStats(byte b, GetVehicleStats getVehicleStats) {
            JSONObject data = new JSONObject();
            try {
                data.put("odometer", getVehicleStats.getOdometer());
                data.put("engineHours", getVehicleStats.getEngineHours());
                data.put("engineRPM", getVehicleStats.getEngineRPM());
                data.put("fuelLevel", getVehicleStats.getFuelLevel());
                data.put("hazardsIsOn", getVehicleStats.getHazardsIsOn());
                data.put("indicators", getVehicleStats.getIndicators());
                data.put("brakesIsActive", getVehicleStats.getBrakesIsActive());
                data.put("handBrakeIsActive", getVehicleStats.getHandBrakeIsActive());
                data.put("lightsIsOn", getVehicleStats.getLightsIsOn());
                data.put("driverDoorIsOpen", getVehicleStats.getDriverDoorIsOpen());
                data.put("passengerDoorIsOpen", getVehicleStats.getPassengerDoorIsOpen());
                data.put("driverSeatbeltIsEngaged", getVehicleStats.getDriverSeatbeltIsEngage());
                data.put("passengerSeatbeltIsEngaged", getVehicleStats.getPassengerSeatbeltIsEngage());
                data.put("hornIsActive", getVehicleStats.getHornIsActive());
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onTerminalDidGetVehicleStats", data));
            if (isDebug)
                showToast("onTerminalDidGetVehicleStats");
        }

        @Override
        public void onTerminalDidGetVehicleStatus(byte b, GetVehicleStatus getVehicleStatus) {
            JSONObject data = new JSONObject();
            try {
                data.put("ignitionState", getVehicleStatus.getIgnitionState());
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onTerminalDidGetVehicleStatus", data));
            if (isDebug)
                showToast("onTerminalDidGetVehicleStatus");
        }

        @Override
        public void onTerminalDisconnected(@NonNull BleTerminal bleTerminal) {
            if (isDebug)
                showToast("Terminal: " + bleTerminal.getTerminalId() + " Disconnected.");
        }

        @Override
        public void onSaveAuthKeySuccess(@NonNull BleTerminal bleTerminal, @NonNull String authKey) {
            JSONObject data = new JSONObject();
            try {
                data.put("authKey", authKey);
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onSaveAuthKeySuccess", data));
            if (isDebug)
                showToast("Auth key saved.");
        }

        @Override
        public void onRemoveAuthKeySuccess() {
            sendFeedback(feedbackResponse("onRemoveAuthKeySuccess"));
            if (isDebug)
                showToast("Auth key removed.");
        }

        @Override
        public void onError(BleError bleError) {
            JSONObject data = new JSONObject();
            try {
                data.put("actionCode", bleError.getActionCode());
                data.put("errorCode", bleError.getErrorCode());
                data.put("localizedDescription", bleError.getLocalizedDescription());
            } catch (JSONException e) {
                showToast("JSON Exception: " + e.getMessage());
            }
            sendFeedback(feedbackResponse("onError", data));
            if (bleError.getActionCode() != -1) {
                showToast("Action code:" + bleError.getActionCode() + " Error Code:" + bleError.getErrorCode() + " "
                        + bleError.getLocalizedDescription());
            } else {
                showToast("Error Code:" + bleError.getErrorCode() + " " + bleError.getLocalizedDescription());
            }
        }
    };
}