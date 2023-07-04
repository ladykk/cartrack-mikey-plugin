import Foundation
import CartrackBleLockSDK

@objc(MikeyCarTrack)
class MikeyCarTrack: CDVPlugin {
    private var callbackId: String? = nil;
    private var bleTerminal: BleTerminal? = nil;
    
    enum ResponseStatus: String {
        case Success = "success"
        case Pending = "pending"
        case Error = "error"
    }
    
    enum ResponseError: String {
        case InvalidArguments = "InvalidArguments"
        case NoBleTerminal = "NoBleTerminal"
        case BluetoothUnauthorized = "BluetoothUnauthorized"
        case BluetoothDisabled = "BluetoothDisabled"
        case BluetoothUnsupported = "BluetoothUnsupported"
        case SaveAuthKeyFailed = "SaveAuthKeyFailed"
        case KeyNotFound = "KeyNotFound"
        case KeyInvalid = "KeyInvalid"
        case TerminalNotFound = "TerminalNotFound"
        case NotConnected = "NotConnected"
        case UnknownError = "UnknownError"
    }
    
    enum ResponseEvent: String {
        case Register = "register"
        case CreateTerminal = "createTerminal"
        case DestroyTerminal = "destroyTerminal"
        case SaveAuthKey = "saveAuthKey"
        case GetAuthKey = "getAuthKey"
        case HasAuthKey = "hasAuthKey"
        case RemoveAuthKey = "removeAuthKey"
        case Connect = "connect"
        case Disconnect = "disconnect"
        case Lock = "lock"
        case Unlock = "unlock"
        case UnlockNoKey = "unlockNoKey"
        case GetLockState = "getLockState"
        case Headlight = "headlight"
        case Horn = "horn"
        case GetVehicleStats = "getVehicleStats"
        case GetIgnitionState = "getIgnitionState"
        case SignalUpdate = "signalUpdate"
        case Unknown = "unknown"
    }
    
    // [Plugins]
    private func dictToJson(dict: Dictionary<String, String>) -> String {
        let jsonData = try? JSONSerialization.data(withJSONObject: dict);
        return String(data: jsonData!, encoding: String.Encoding.ascii)!;
    }
    
    private func boolToString(input: Bool) -> String {
        return input ? "True" : "False"
    }
    
    private func bleErrorToResponseError(error: BleError) -> ResponseError {
        switch (error) {
        case BleError.bluetoohUnauthorized:
            return ResponseError.BluetoothUnauthorized
        case BleError.bluetoothDisabled:
            return ResponseError.BluetoothDisabled
        case BleError.bluetoohUnsupported:
            return ResponseError.BluetoothUnsupported
        case BleError.saveAuthKeyFailed:
            return ResponseError.SaveAuthKeyFailed
        case BleError.keyInvalid:
            return ResponseError.KeyInvalid
        case BleError.terminalNotFound:
            return ResponseError.TerminalNotFound
        case BleError.notConnected:
            return ResponseError.NotConnected
        default:
            return ResponseError.UnknownError
        }
    }
    
    private func bleActionToResponseEvent(action: BleAction) -> ResponseEvent {
        switch (action) {
        case BleAction.lock:
            return ResponseEvent.Lock
        case BleAction.unlock:
            return ResponseEvent.Unlock
        case BleAction.headlight:
            return ResponseEvent.Headlight
        case BleAction.horn:
            return ResponseEvent.Horn
        case BleAction.lockState:
            return ResponseEvent.GetLockState
        case BleAction.unlockNoKeyFob:
            return ResponseEvent.UnlockNoKey
        case BleAction.ignitionState:
            return ResponseEvent.GetIgnitionState
        default:
            return ResponseEvent.Unknown
        }
    }
    
    private func lockStateToString(lockState: LockState) -> String {
        switch (lockState) {
        case LockState.locked:
            return "Locked"
        case LockState.unlocked:
            return "Unlocked"
        default:
            return "Unknown"
        }
    }
    
    private func ignitionStateToString(ignitionState: IgnitionState) -> String {
        switch (ignitionState) {
        case .off:
            return "Off"
        case .on:
            return "On"
        default:
            return "Unknown"
        }
    }
    
    private func bleSignalStrengthToString(bleSignalStrength: BleSignalStrength) -> String {
        switch (bleSignalStrength) {
        case .strong:
            return "Strong"
        case .good:
            return "Good"
        case .weak:
            return "Weak"
        default:
            return "Unknown"
        }
    }
    
    private func indicatorsToString(indicators: IndicatorState) -> String {
        switch (indicators) {
        case .both:
            return "Both"
        case .left:
            return "Left"
        case.right:
            return "Right"
        default:
            return "Off"
        }
    }
    
    private func vehicleStatsToDict(vehicleStats: VehicleStats) -> Dictionary<String, String> {
        return [
            "odometer": String(vehicleStats.odometer * 1000),
            "engineHours": String(vehicleStats.engineHours),
            "engineRPM": String(vehicleStats.engineRPM * 1000),
            "fuelLevel": String(vehicleStats.fuelLevel),
            "hazardsIsOn": self.boolToString(input: vehicleStats.hazardsIsOn),
            "indicators": self.indicatorsToString(indicators: vehicleStats.indicators),
            "brakesIsActive": self.boolToString(input: vehicleStats.brakesIsActive),
            "handBrakeIsActive": self.boolToString(input: vehicleStats.handBrakeIsActive),
            "lightsIsOn": self.boolToString(input: vehicleStats.lightsIsOn),
            "driverDoorIsOpen": self.boolToString(input: vehicleStats.driverDoorIsOpen),
            "passengerDoorIsOpen": self.boolToString(input: vehicleStats.passengerDoorIsOpen),
            "driverSeatbeltIsEngage": self.boolToString(input: vehicleStats.driverSeatbeltIsEngage),
            "passengerSeatbeltIsEngage": self.boolToString(input: vehicleStats.passengerSeatbeltIsEngage),
            "hornIsActive": self.boolToString(input: vehicleStats.hornIsActive)
        ]
    }
                
    private func response(
        event: ResponseEvent,
        status: ResponseStatus,
        error: ResponseError? = nil,
        data: Dictionary<String, String>? = nil,
        keepCallback: Bool = false
    ) -> CDVPluginResult {
        // Create Response Message
        var response: Dictionary<String, String> = [:];
        response["event"] = event.rawValue;
        response["status"] = status.rawValue;
        if(error != nil) {response["error"] = error?.rawValue};
        if(data != nil) {response["data"] = self.dictToJson(dict: data!)};
        
        // Create Plugin Result
        let pluginResult = CDVPluginResult(
            status: status == ResponseStatus.Error ? CDVCommandStatus_ERROR : CDVCommandStatus_OK,
            messageAs: self.dictToJson(dict: response));
        
        // Set Keep Callback
        if(keepCallback) {pluginResult?.keepCallback = true}
        
        return pluginResult!;
    }
    
    private func success(event: ResponseEvent, callbackId: String, data: Dictionary<String, String>? = nil, keepCallback: Bool = false) {
        self.commandDelegate.send(
            self.response(event: event, status: .Success, data: data, keepCallback: keepCallback),
            callbackId: callbackId
        );
    }
    
    private func pending(event: ResponseEvent, callbackId: String, keepCallback: Bool = false) {
        self.commandDelegate.send(
            self.response(event: event, status: .Pending, keepCallback: keepCallback),
            callbackId: callbackId
        );
    }
    
    private func error(event: ResponseEvent, callbackId: String, error: ResponseError, keepCallback: Bool = false) {
        self.commandDelegate.send(
            self.response(event: event, status: .Error, error: error, keepCallback: keepCallback),
            callbackId: callbackId
        );
    }
    
    private func NoBleTerminalError(event: ResponseEvent, callbackId: String) -> Bool {
        if (bleTerminal == nil) {
            self.error(event: event, callbackId: callbackId, error: .NoBleTerminal)
            return true;
        } else {
            return false;
        }
    }
    
    private func InvalidArgumentsError(event: ResponseEvent, callbackId: String, source: Int, target: Int) -> Bool {
        if (source < target) {
            self.error(event: event, callbackId: callbackId, error: .InvalidArguments)
            return true;
        } else {
            return false;
        }
    }

    @objc(register:)
    func register(command: CDVInvokedUrlCommand) {
        self.callbackId = command.callbackId;
        self.success(event: .Register, callbackId: command.callbackId, keepCallback: true)
    }
    
    // [Connection]
    @objc(createTerminal:)
    func createTerminal(command: CDVInvokedUrlCommand) {
        if (InvalidArgumentsError(event: .CreateTerminal, callbackId: command.callbackId, source: command.arguments.count, target: 1)) { return }
       
        let terminalID: String? = command.arguments[0] as? String;
        
        // Get terminal
        bleTerminal = BleService.getTerminal(terminalID: terminalID!);
        bleTerminal?.delegate = self;
        
        self.success(event: .CreateTerminal, callbackId: command.callbackId)
    }
    
    @objc(destroyTerminal:)
    func destroyTerminal(command: CDVInvokedUrlCommand) {
        bleTerminal = nil;
        self.success(event: .DestroyTerminal, callbackId: command.callbackId);
    }
    
    @objc(saveAuthKey:)
    func saveAuthKey(command: CDVInvokedUrlCommand) {
        if (InvalidArgumentsError(event: .SaveAuthKey, callbackId: command.callbackId, source: command.arguments.count, target: 1)) { return }
        
        if (NoBleTerminalError(event: .SaveAuthKey, callbackId: command.callbackId)) { return }
        
        let authKey: String? = command.arguments[0] as? String;
        bleTerminal?.saveAuthKey(authKey: authKey!);
        
        self.pending(event: .SaveAuthKey, callbackId: command.callbackId)
    }
    
    @objc(getAuthKey:)
    func getAuthKey(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .GetAuthKey, callbackId: command.callbackId)) { return }
        
        let authKey: String = self.bleTerminal?.authKey ?? "";
        
        self.success(event: .GetAuthKey, callbackId: command.callbackId, data: ["authKey": authKey])
    }
    
    @objc(hasAuthKey:)
    func hasAuthKey(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .HasAuthKey, callbackId: command.callbackId)) { return }
        
        let hasAuthKey: Bool = bleTerminal?.hasKey ?? false;
        
        self.success(event: .HasAuthKey, callbackId: command.callbackId, data: ["hasAuthKey": self.boolToString(input: hasAuthKey)])
    }
    
    @objc(removeAuthKey:)
    func removeAuthKey(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .RemoveAuthKey, callbackId: command.callbackId)) { return }
        
        bleTerminal?.removeAuthKey()
        
        self.success(event: .RemoveAuthKey, callbackId: command.callbackId)
    }
    
    @objc(connect:)
    func connect(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Connect, callbackId: command.callbackId)) { return }
        
        var timeout: Float = 10;
        if (command.arguments.count > 0) {
            timeout = (command.arguments[0] as? Float)!
        }
        
        bleTerminal?.connect(timeout: timeout);
        
        self.pending(event: .Connect, callbackId: command.callbackId)
    }
    
    @objc(disconnect:)
    func disconnect(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Disconnect, callbackId: command.callbackId)) { return }
        
        bleTerminal?.disconnect();
        self.pending(event: .Disconnect, callbackId: command.callbackId)
    }
    
    @objc(lock:)
    func lock(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Lock, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.lock)
        
        self.pending(event: .Lock, callbackId: command.callbackId)
    }
    
    @objc(unlock:)
    func unlock(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Unlock, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.unlock)
        
        self.pending(event: .Unlock, callbackId: command.callbackId)
    }
    
    @objc(unlockNoKey:)
    func unlockNoKey(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .UnlockNoKey, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.unlockNoKeyFob)
        
        self.pending(event: .UnlockNoKey, callbackId: command.callbackId)
    }
    
    @objc(getLockState:)
    func getLockState(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .GetLockState, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.lockState)
        
        self.pending(event: .GetLockState, callbackId: command.callbackId)
    }
    
    @objc(headlight:)
    func headlight(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Headlight, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.headlight)
        
        self.pending(event: .Headlight, callbackId: command.callbackId)
    }
    
    @objc(horn:)
    func horn(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .Horn, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.horn)
        
        self.pending(event: .Horn, callbackId: command.callbackId)
    }
    
    @objc(getVehicleStats:)
    func getVehicleStats(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .GetVehicleStats, callbackId: command.callbackId)) { return }
        
        bleTerminal?.getVehicleStats()
        
        self.pending(event: .GetVehicleStats, callbackId: command.callbackId)
    }
    
    @objc(getIgnitionState:)
    func getIgnitionState(command: CDVInvokedUrlCommand) {
        if (NoBleTerminalError(event: .GetIgnitionState, callbackId: command.callbackId)) { return }
        
        bleTerminal?.sendAction(.ignitionState)
        
        self.pending(event: .GetIgnitionState, callbackId: command.callbackId)
    }
    
    
}

extension MikeyCarTrack: BleTerminalDelegate {
    func bleTerminalDidSavedKey(terminal: CartrackBleLockSDK.BleTerminal, error: CartrackBleLockSDK.BleError?) {
        if (self.callbackId != nil) {
            if let error = error {
                self.error(event: .SaveAuthKey, callbackId: self.callbackId!, error: self.bleErrorToResponseError(error: error), keepCallback: true)
            } else {
                self.success(event: .SaveAuthKey, callbackId: self.callbackId!, keepCallback: true);
            }
        }
    }
    
    func bleTerminalDidConnect(terminal: CartrackBleLockSDK.BleTerminal, error: CartrackBleLockSDK.BleError?) {
        if (self.callbackId != nil) {
            if let error = error {
                self.error(event: .Connect, callbackId: self.callbackId!, error: self.bleErrorToResponseError(error: error), keepCallback: true)
            } else {
                self.success(event: .Connect, callbackId: self.callbackId!, keepCallback: true)
            }
        }
    }
    
    func bleTerminalDidAction(terminal: CartrackBleLockSDK.BleTerminal, action: CartrackBleLockSDK.BleAction, error: CartrackBleLockSDK.BleError?) {
        if (self.callbackId != nil) {
            if let error = error {
                self.error(event: bleActionToResponseEvent(action: action), callbackId: callbackId!, error: self.bleErrorToResponseError(error: error), keepCallback: true)
            } else {
                let event = bleActionToResponseEvent(action: action)
                switch (event) {
                case .Lock, .Unlock, .UnlockNoKey, .Headlight, .Horn:
                    self.success(event: event, callbackId: callbackId!, keepCallback: true);
                    break;
                case .GetLockState:
                    let lockState: String = lockStateToString(lockState: terminal.lockState)
                    self.success(event: .GetLockState, callbackId: callbackId!, data: ["lockState": lockState], keepCallback: true)
                    break;
                case .GetIgnitionState:
                    let ignitionState: String = ignitionStateToString(ignitionState: terminal.ignitionState)
                    self.success(event: .GetIgnitionState, callbackId: callbackId!, data: ["ignitionState": ignitionState], keepCallback: true)
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    func bleTerminalDisconnected(terminal: CartrackBleLockSDK.BleTerminal) {
        if (self.callbackId != nil) {
            self.success(event: .Disconnect, callbackId: callbackId!, keepCallback: true)
        }
    }
    
    func bleTerminalSignalUpdate(rssi: Int, strength: CartrackBleLockSDK.BleSignalStrength) {
        if (self.callbackId != nil) {
            self.success(event: .SignalUpdate, callbackId: callbackId!, data: ["strength": self.bleSignalStrengthToString(bleSignalStrength: strength), "rssi": String(rssi)], keepCallback: true)
        }
    }
    
    func bleTerminalDidGetVehicleStats(terminal: CartrackBleLockSDK.BleTerminal, vehicleStats: CartrackBleLockSDK.VehicleStats?, error: CartrackBleLockSDK.BleError?) {
        if (self.callbackId != nil && vehicleStats != nil) {
            if let error = error {
                self.error(event: .GetVehicleStats, callbackId: callbackId!, error: self.bleErrorToResponseError(error: error))
            } else {
                self.success(event: .GetVehicleStats, callbackId: callbackId!, data: self.vehicleStatsToDict(vehicleStats: vehicleStats!), keepCallback: true)
            }
        }
    }
}
