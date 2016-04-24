var exec = require('cordova/exec');

exports.startDownloading = function (fileURL, success, error) {
    exec(success, error, "FileDownloader", "startDownloading", [fileURL]);
};

