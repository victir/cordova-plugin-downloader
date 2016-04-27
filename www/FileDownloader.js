var exec = require('cordova/exec');

exports.startDownloading = function (fileURL, fileName, success, error) {
  exec(success, error, "FileDownloader", "startDownloading", [fileURL, fileName]);
};
