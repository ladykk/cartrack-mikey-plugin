package com.spheresoftsolutions.cordova.plugin;

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

import java.util.HashMap;
import java.util.Map;

public class MikeyCarTrack extends CordovaPlugin {
    private String callbackId = null;
    private BleTerminal bleTerminal = null;

    public enum ResponseStatus {
        Success("success"),
        Pending("pending"),
        Error("error");

        private final String rawValue;
        ResponseStatus(final String value) {
            this.rawValue = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.rawValue;
        }

        public Boolean equals(ResponseStatus status) {
            return this.rawValue.equals(status.rawValue);
        }
    }

    public enum ResponseError {
        InvalidArguments("InvalidArguments"),
        NoBleTerminal("NoBleTerminal"),
        BluetoothUnauthorized("BluetoothUnauthorized"),
        BluetoothDisabled("BluetoothDisabled"),
        BluetoothUnsupported("BluetoothUnsupported"),
        SaveAuthKeyFailed("SaveAuthKeyFailed"),
        KeyNotFound("KeyNotFound"),
        KeyInvalid("KeyInvalid"),
        TerminalNotFound("TerminalNotFound"),
        NotConnected("NotConnected"),
        MissingPermission("MissingPermission"),
        UnknownError("UnknownError");

        private final String rawValue;
        ResponseError(final String value) {
            this.rawValue = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.rawValue;
        }
    }

    public enum ResponseEvent {
        Register("register"),
        CreateTerminal("createTerminal"),
        DestroyTerminal("destroyTerminal"),
        SaveAuthKey("saveAuthKey"),
        GetAuthKey("getAuthKey"),
        HasAuthKey("hasAuthKey"),
        RemoveAuthKey("removeAuthKey"),
        Connect("connect"),
        Disconnect("disconnect"),
        Lock("lock"),
        Unlock("unlock"),
        UnlockNoKey("unlockNoKey"),
        GetLockState("getLockState"),
        Headlight("headlight"),
        Horn("horn"),
        GetVehicleStats("getVehicleStats"),
        GetIgnitionState("getIgnitionState"),
        SignalUpdate("signalUpdate"),
        Unknown("unknown");

        private final String rawValue;
        public static final ResponseEvent[] events = new ResponseEvent[]{
                Register,
                CreateTerminal,
                DestroyTerminal,
                SaveAuthKey,
                GetAuthKey,
                HasAuthKey,
                RemoveAuthKey,
                Connect,
                Disconnect,
                Lock,
                Unlock,
                UnlockNoKey,
                GetLockState,
                Headlight,
                Horn,
                GetVehicleStats,
                GetIgnitionState,
                SignalUpdate,
                Unknown
        };


        ResponseEvent(final String value) {
            this.rawValue = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.rawValue;
        }

        public static ResponseEvent getResponseEventFromAction(String action) {
            for (ResponseEvent event : events) {
                if (event.rawValue.equals(action)) {
                    return event;
                }
            }
            return ResponseEvent.Unknown;
        }
    }

    // [Plugins]
    private String mapToJson(Map<String, String> map) {
        JSONObject json = new JSONObject();
        for (String key : map.keySet()) {
            try {
                json.put(key, map.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return json.toString();
    }

    private String boolToString(Boolean input) {
        return input ? "True" : "False";
    }

    private ResponseError bleErrorToResponseError(BleError error) {
        if (BleError.BluetoothUnauthorized.INSTANCE.equals(error)) return ResponseError.BluetoothUnauthorized;
        else if (BleError.BluetoothDisabled.INSTANCE.equals(error)) return ResponseError.BluetoothDisabled;
        else if (BleError.BluetoothUnsupported.INSTANCE.equals(error)) return ResponseError.BluetoothUnsupported;
        else if (BleError.SaveAuthKeyFailed.INSTANCE.equals(error)) return ResponseError.SaveAuthKeyFailed;
        else if (BleError.KeyNotFound.INSTANCE.equals(error)) return ResponseError.KeyNotFound;
        else if (BleError.KeyInvalid.INSTANCE.equals(error)) return ResponseError.KeyInvalid;
        else if (BleError.TerminalNotFound.INSTANCE.equals(error)) return ResponseError.TerminalNotFound;
        else if (BleError.NotConnected.INSTANCE.equals(error)) return ResponseError.NotConnected;
        else return ResponseError.UnknownError;
    }

    private ResponseEvent bleErrorToResponseEvent(BleError error) {
        if (BleAction.LOCK.ordinal() == error.getActionCode()) return ResponseEvent.Lock;
        else if (BleAction.UNLOCK.ordinal() == error.getActionCode()) return ResponseEvent.Unlock;
        else if (BleAction.HEADLIGHT.ordinal() == error.getActionCode()) return ResponseEvent.Headlight;
        else if (BleAction.HORN.ordinal() == error.getActionCode()) return ResponseEvent.Horn;
        else if (BleAction.GET_LOCK_STATE.ordinal() == error.getActionCode()) return ResponseEvent.GetLockState;
        else if (BleAction.UNLOCK_NOKEYFOB.ordinal() == error.getActionCode()) return ResponseEvent.UnlockNoKey;
        else if (BleAction.VEHICLE_GET_STATUS.ordinal() == error.getActionCode()) return ResponseEvent.GetVehicleStats;
        else if (BleAction.VEHICLE_GET_CONFIG.ordinal() == error.getActionCode()) return ResponseEvent.GetIgnitionState;
        else return ResponseEvent.Unknown;
    }

    private ResponseEvent bleActionToResponseEvent(BleAction action) {
        if (BleAction.LOCK.equals(action)) return ResponseEvent.Lock;
        else if (BleAction.UNLOCK.equals(action)) return ResponseEvent.Unlock;
        else if (BleAction.HEADLIGHT.equals(action)) return ResponseEvent.Headlight;
        else if (BleAction.HORN.equals(action)) return ResponseEvent.Horn;
        else if (BleAction.GET_LOCK_STATE.equals(action)) return ResponseEvent.GetLockState;
        else if (BleAction.UNLOCK_NOKEYFOB.equals(action)) return ResponseEvent.UnlockNoKey;
        else if (BleAction.VEHICLE_GET_STATUS.equals(action)) return ResponseEvent.GetVehicleStats;
        else if (BleAction.VEHICLE_GET_CONFIG.equals(action)) return ResponseEvent.GetIgnitionState;
        else return ResponseEvent.Unknown;
    }

    private String lockStateToString(LockState lockState) {
        if (LockState.LOCKED.equals(lockState)) return "Locked";
        else if (LockState.UNLOCKED.equals(lockState)) return "Unlocked";
        else if (LockState.UNKNOWN.equals(lockState)) return "Unknown";
        else return "Unknown";
    }

    private String ignitionStateToString(Boolean ignitionState) {
        if (ignitionState == null) return "Unknown";
        else if (ignitionState) return "On";
        else return "Off";
    }

    private String bleSignalStrengthToString(BleSignalStrength bleSignalStrength) {
        if (BleSignalStrength.STRONG.equals(bleSignalStrength)) return "Strong";
        else if (BleSignalStrength.GOOD.equals(bleSignalStrength)) return "Good";
        else if (BleSignalStrength.WEAK.equals(bleSignalStrength)) return "Weak";
        else return "Unknown";
    }

    private String indicatorsToString(String indicators) {
        switch (indicators) {
            case "Left":
                return "Left";
            case "Right":
                return "Right";
            case "Both":
                return "Both";
            default:
                return "Off";
        }
    }

    private Map<String, String> vehicleStatsToMap(GetVehicleStats vehicleStats) {
        Map<String, String> data = new HashMap<>();
        data.put("odometer", String.valueOf(vehicleStats.getOdometer() != null ? vehicleStats.getOdometer() * 1000 : 0));
        data.put("engineHours", String.valueOf(vehicleStats.getEngineHours()));
        data.put("engineRPM", String.valueOf(vehicleStats.getEngineRPM()));
        data.put("fuelLevel", String.valueOf(vehicleStats.getFuelLevel()));
        data.put("hazardsIsOn", boolToString(vehicleStats.getHazardsIsOn()));
        data.put("indicators", indicatorsToString(vehicleStats.getIndicators() != null ? vehicleStats.getIndicators() : ""));
        data.put("brakesIsActive", boolToString(vehicleStats.getBrakesIsActive()));
        data.put("handBrakeIsActive", boolToString(vehicleStats.getHandBrakeIsActive()));
        data.put("lightsIsOn", boolToString(vehicleStats.getLightsIsOn()));
        data.put("driverDoorIsOpen", boolToString(vehicleStats.getDriverDoorIsOpen()));
        data.put("passengerDoorIsOpen", boolToString(vehicleStats.getPassengerDoorIsOpen()));
        data.put("driverSeatbeltIsEngage", boolToString(vehicleStats.getDriverSeatbeltIsEngage()));
        data.put("passengerSeatbeltIsEngage", boolToString(vehicleStats.getPassengerSeatbeltIsEngage()));
        data.put("hornIsActive", boolToString(vehicleStats.getHornIsActive()));
        return data;
    }

    private PluginResult response(ResponseEvent event, ResponseStatus status, ResponseError error, Map<String, String> data, Boolean keepCallback) {
        // Create Response Message
        Map<String, String> response = new HashMap<>();
        response.put("event", event.toString());
        response.put("status", status.toString());
        if (error != null) {
            response.put("error", error.toString());
        }
        if (data != null) {
            response.put("data", mapToJson(data));
        }

        // Create Plugin Result
        PluginResult pluginResult = new PluginResult(
                ResponseStatus.Error.equals(status) ? PluginResult.Status.ERROR : PluginResult.Status.OK,
                mapToJson(response)
        );

        // Set Keep Callback
        if (keepCallback) {
            pluginResult.setKeepCallback(true);
        }

        return pluginResult;
    }

    private Boolean success(final CallbackContext callbackContext, ResponseEvent event) {
        callbackContext.sendPluginResult(response(event, ResponseStatus.Success, null, null, false));
        return true;
    }
    private Boolean success(final CallbackContext callbackContext, ResponseEvent event, Map<String, String> data) {
        callbackContext.sendPluginResult(response(event, ResponseStatus.Success, null, data, false));
        return true;
    }
    private Boolean success(ResponseEvent event) {
        this.webView.sendPluginResult(response(event, ResponseStatus.Success, null, null, true), this.callbackId);
        return true;
    }
    private void success(ResponseEvent event, Map<String, String> data) {
        this.webView.sendPluginResult(response(event, ResponseStatus.Success, null, data, true), this.callbackId);
    }

    private Boolean pending(final CallbackContext callbackContext, ResponseEvent event) {
        callbackContext.sendPluginResult(response(event, ResponseStatus.Pending, null, null, false));
        return true;
    }

    private Boolean error(final CallbackContext callbackContext, ResponseEvent event, ResponseError error) {
        callbackContext.sendPluginResult(response(event, ResponseStatus.Error, error, null, false));
        return true;
    }
    private void error(ResponseEvent event, ResponseError error) {
        this.webView.sendPluginResult(response(event, ResponseStatus.Error, error, null, true), callbackId);
    }

    private Boolean NoBleTerminalError(final CallbackContext callbackContext, ResponseEvent event) {
        if (bleTerminal == null) {
            this.error(callbackContext, event, ResponseError.NoBleTerminal);
            return true;
        } else {
            return false;
        }
    }

    private Boolean InvalidArgumentsError(final CallbackContext callbackContext, ResponseEvent event, Integer source, Integer target) {
        if (source < target) {
            this.error(callbackContext, event, ResponseError.InvalidArguments);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean execute(String action, JSONArray args,
            final CallbackContext callbackContext) {
        try {
            switch (action) {
                case "register":
                    return this.register(callbackContext);
                case "createTerminal":
                    return this.createTerminal(args, callbackContext);
                case "destroyTerminal":
                    return this.destroyTerminal(callbackContext);
                case "saveAuthKey":
                    return this.saveAuthKey(args, callbackContext);
                case "getAuthKey":
                    return this.getAuthKey(callbackContext);
                case "hasAuthKey":
                    return this.hasAuthKey(callbackContext);
                case "removeAuthKey":
                    return this.removeAuthKey(callbackContext);
                case "connect":
                    return this.connect(args, callbackContext);
                case "disconnect":
                    return this.disconnect(callbackContext);
                case "lock":
                    return this.lock(callbackContext);
                case "unlock":
                    return this.unlock(callbackContext);
                case "headlight":
                    return this.headlight(callbackContext);
                case "horn":
                    return this.horn(callbackContext);
                case "getLockState":
                    return this.getLockState(callbackContext);
                case "unlockNoKey":
                    return this.unlockNoKey(callbackContext);
                case "getVehicleStats":
                    return this.getVehicleStats(callbackContext);
                case "getIgnitionState":
                    return this.getIgnitionState(callbackContext);
                default:
                    return this.error(callbackContext, ResponseEvent.Unknown, ResponseError.UnknownError);
            }
        } catch (JSONException e) {
            this.error(callbackContext, ResponseEvent.getResponseEventFromAction(action), ResponseError.InvalidArguments);
            return false;
        } catch (Exception e) {
            this.error(callbackContext, ResponseEvent.getResponseEventFromAction(action), ResponseError.UnknownError);
            return false;
        }
    }

    private boolean requestPermission() {
        PermissionManager permissionManager = new PermissionManager(this.cordova.getActivity());
        return permissionManager.requestPermission();
    }

    private Boolean register(final CallbackContext callbackContext) {
        this.callbackId = callbackContext.getCallbackId();
        this.requestPermission();
        return this.success(ResponseEvent.Register);
    }

    // [Connection]
    private Boolean createTerminal(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (InvalidArgumentsError(callbackContext, ResponseEvent.CreateTerminal, args.length(), 1)) return false;

        if (!this.requestPermission()) {
            this.error(callbackContext, ResponseEvent.CreateTerminal, ResponseError.MissingPermission);
            return false;
        }

        String terminalId = args.getString(0);

        // Get terminal
        BleService.Companion.clear();
        BleService.Companion.configure(cordova.getContext());
        bleTerminal = BleService.Companion.getTerminal(terminalId);
        bleTerminal.setBleListener(listener);

        return this.success(callbackContext, ResponseEvent.CreateTerminal);
    }

    private Boolean destroyTerminal(final CallbackContext callbackContext) {
        BleService.Companion.clear();
        bleTerminal = null;

        return this.success(callbackContext, ResponseEvent.DestroyTerminal);
    }

    private Boolean saveAuthKey(JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (InvalidArgumentsError(callbackContext, ResponseEvent.SaveAuthKey, args.length(), 1)) return false;
        if (NoBleTerminalError(callbackContext, ResponseEvent.SaveAuthKey)) return false;

        String authKey = args.getString(0);
        bleTerminal.saveAuthKey(authKey);

        return this.pending(callbackContext, ResponseEvent.SaveAuthKey);
    }

    private Boolean getAuthKey(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.GetAuthKey)) return false;

        Map<String ,String> data = new HashMap<>();
        data.put("authKey", bleTerminal.getAuthKey());

        return this.success(callbackContext, ResponseEvent.GetAuthKey, data);
    }

    private Boolean hasAuthKey(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.HasAuthKey)) return false;

        Map<String ,String> data = new HashMap<>();
        data.put("hasAuthKey", boolToString(bleTerminal.hasKey()));

        return this.success(callbackContext, ResponseEvent.HasAuthKey, data);
    }

    private Boolean removeAuthKey(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.RemoveAuthKey)) return false;

        bleTerminal.removeAuthKey();

        return this.success(callbackContext, ResponseEvent.RemoveAuthKey);
    }

    private Boolean connect(JSONArray args, final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Connect)) return false;
        if (!this.requestPermission()) {
            this.error(callbackContext, ResponseEvent.Connect, ResponseError.MissingPermission);
            return false;
        }

        int timeout = 10;
        if (args.length() > 0) {
            try {
                timeout = args.getInt(0);
            } catch (JSONException ignored) {}
        }

        bleTerminal.scanAndConnectToPeripheral(timeout * 1000L);

        return this.pending(callbackContext, ResponseEvent.Connect);
    }

    private Boolean disconnect(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Disconnect)) return false;

        bleTerminal.disconnect();

        return this.pending(callbackContext, ResponseEvent.Disconnect);
    }

    private Boolean lock(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Lock)) return false;

        bleTerminal.sendAction(BleAction.LOCK);

        return this.pending(callbackContext, ResponseEvent.Lock);
    }

    private Boolean unlock(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Unlock)) return false;

        bleTerminal.sendAction(BleAction.UNLOCK);

        return this.pending(callbackContext, ResponseEvent.Unlock);
    }

    private Boolean unlockNoKey(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.UnlockNoKey)) return false;

        bleTerminal.sendAction(BleAction.UNLOCK_NOKEYFOB);

        return this.pending(callbackContext, ResponseEvent.UnlockNoKey);
    }

    private Boolean getLockState(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.GetLockState)) return false;

        bleTerminal.sendAction(BleAction.GET_LOCK_STATE);

        return this.pending(callbackContext, ResponseEvent.GetLockState);
    }

    private Boolean headlight(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Headlight)) return false;

        bleTerminal.sendAction(BleAction.HEADLIGHT);

        return this.pending(callbackContext, ResponseEvent.Headlight);
    }

    private Boolean horn(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.Horn)) return false;

        bleTerminal.sendAction(BleAction.HORN);

        return this.pending(callbackContext, ResponseEvent.Horn);
    }

    private Boolean getVehicleStats(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.GetVehicleStats)) return false;

        bleTerminal.sendAction(BleAction.VEHICLE_GET_CONFIG);

        return this.pending(callbackContext, ResponseEvent.GetVehicleStats);
    }

    private Boolean getIgnitionState(final CallbackContext callbackContext) {
        if (NoBleTerminalError(callbackContext, ResponseEvent.GetIgnitionState)) return false;

        bleTerminal.sendAction(BleAction.VEHICLE_GET_STATUS);

        return this.pending(callbackContext, ResponseEvent.GetIgnitionState);
    }


    public final class Listener implements BleListener {
        public void onTerminalConnected(@NonNull BleTerminal bleTerminal) {
            if (callbackId != null) {
                success(ResponseEvent.Connect);
            }
        }

        public void onTerminalCommandResult(@NonNull BleAction bleAction) {
            if (callbackId != null) {
                if (BleAction.LOCK.equals(bleAction) || BleAction.UNLOCK.equals(bleAction) || BleAction.UNLOCK_NOKEYFOB.equals(bleAction) || BleAction.HEADLIGHT.equals(bleAction) || BleAction.HORN.equals(bleAction)) {
                    success(bleActionToResponseEvent(bleAction));
                } else if (BleAction.GET_LOCK_STATE.equals(bleAction)) {
                    Map<String, String> data = new HashMap<>();
                    data.put("lockState", lockStateToString(bleTerminal.getLockState()));
                    success(bleActionToResponseEvent(bleAction), data);
                }
            }
        }

        public void onTerminalDidGetVehicleStats(byte b, @NonNull GetVehicleStats getVehicleStats) {
            if (callbackId != null) {
                success(ResponseEvent.GetVehicleStats, vehicleStatsToMap(getVehicleStats));
            }
        }

        public void onTerminalDidGetVehicleStatus(byte b, @NonNull GetVehicleStatus getVehicleStatus) {
            if (callbackId != null) {
                Map<String, String> data = new HashMap<>();
                data.put("ignitionState", ignitionStateToString(getVehicleStatus.getIgnitionState()));
                success(ResponseEvent.GetIgnitionState, data);
            }
        }

        public void onTerminalDisconnected(@NonNull BleTerminal bleTerminal) {
            if (callbackId != null) {
                success(ResponseEvent.Disconnect);
            }
        }

        public void onSaveAuthKeySuccess(@NonNull BleTerminal bleTerminal, @NonNull String s) {
            if (callbackId != null) {
                success(ResponseEvent.SaveAuthKey);
            }
        }

        public void onSignalStrength(@NonNull BleSignalStrength bleSignalStrength, int i) {
            if (callbackId != null) {
                Map<String, String> data = new HashMap<>();
                data.put("strength", bleSignalStrengthToString(bleSignalStrength));
                data.put("rssi", String.valueOf(i));

                success(ResponseEvent.SignalUpdate, data);
            }
        }

        public void onSignalStrength(@NonNull BleSignalStrength bleSignalStrength) {
            if (callbackId != null) {
                Map<String, String> data = new HashMap<>();
                data.put("strength", bleSignalStrengthToString(bleSignalStrength));
                data.put("rssi", String.valueOf(0));

                success(ResponseEvent.SignalUpdate, data);
            }
        }

        public void onRemoveAuthKeySuccess() {
            // REMARK: iOS SDK don't have this options.
        }

        public void onReconnect() {
            // REMARK: iOS SDK don't have this options.
        }

        public void onError(@NonNull BleError bleError) {

            if (callbackId != null) {
                error(bleErrorToResponseEvent(bleError), bleErrorToResponseError(bleError));
            }
        }
    };

    private final BleListener listener = new Listener();
}