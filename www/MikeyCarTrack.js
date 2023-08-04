// Empty constructor
function MikeyCarTrack() {}

MikeyCarTrack.prototype.register = function (successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "register", []);
};

MikeyCarTrack.prototype.createTerminal = function (
  terminalId,
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "createTerminal",
    [terminalId]
  );
};

MikeyCarTrack.prototype.destroyTerminal = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "destroyTerminal",
    []
  );
};

MikeyCarTrack.prototype.saveAuthKey = function (
  authKey,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "saveAuthKey", [
    authKey,
  ]);
};

MikeyCarTrack.prototype.getAuthKey = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "getAuthKey",
    []
  );
};

MikeyCarTrack.prototype.hasAuthKey = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "hasAuthKey",
    []
  );
};

MikeyCarTrack.prototype.removeAuthKey = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "removeAuthKey",
    []
  );
};

MikeyCarTrack.prototype.connect = function (
  timeout,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "connect", [
    timeout,
  ]);
};

MikeyCarTrack.prototype.disconnect = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "disconnect",
    []
  );
};

MikeyCarTrack.prototype.lock = function (successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "lock", []);
};

MikeyCarTrack.prototype.unlock = function (successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "unlock", []);
};

MikeyCarTrack.prototype.unlockNoKey = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "unlockNoKey",
    []
  );
};

MikeyCarTrack.prototype.getLockState = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "getLockState",
    []
  );
};

MikeyCarTrack.prototype.headlight = function (successCallback, errorCallback) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "headlight",
    []
  );
};

MikeyCarTrack.prototype.horn = function (successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "horn", []);
};

MikeyCarTrack.prototype.getVehicleStats = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "getVehicleStats",
    []
  );
};

MikeyCarTrack.prototype.getIgnitionState = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "getIgnitionState",
    []
  );
};

MikeyCarTrack.prototype.checkBluetoothEnabled = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "checkBluetoothEnabled"
  );
};

// Installation constructor that binds ToastyPlugin to window
MikeyCarTrack.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.MikeyCarTrack = new MikeyCarTrack();
  return window.plugins.MikeyCarTrack;
};
cordova.addConstructor(MikeyCarTrack.install);
