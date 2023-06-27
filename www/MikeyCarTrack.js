// Empty constructor
function MikeyCarTrack() {}

MikeyCarTrack.prototype.initialize = function (
  debug,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "initialize", [
    debug,
  ]);
};

MikeyCarTrack.prototype.requestPermission = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "requestPermission",
    []
  );
};

MikeyCarTrack.prototype.toast = function (
  message,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "toast", [
    message,
  ]);
};

MikeyCarTrack.prototype.snackBar = function (
  message,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "snackBar", [
    message,
  ]);
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

MikeyCarTrack.prototype.setAuthKey = function (
  authKey,
  successCallback,
  errorCallback
) {
  cordova.exec(successCallback, errorCallback, "MikeyCarTrack", "setAuthKey", [
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

MikeyCarTrack.prototype.unlockNoKeyFOB = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "unlockNoKeyFOB",
    []
  );
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

MikeyCarTrack.prototype.getVehicleStatus = function (
  successCallback,
  errorCallback
) {
  cordova.exec(
    successCallback,
    errorCallback,
    "MikeyCarTrack",
    "getVehicleStatus",
    []
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
