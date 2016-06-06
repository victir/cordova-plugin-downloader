#cordova-plugin-filedownloader plugin to download files for Android devices
When you need to download file, with history about downloads in device.

#Install
```
  ionic plugin add https://github.com/victir/cordova-plugin-downloader.git
  or
  cordova plugin add https://github.com/victir/cordova-plugin-downloader.git
```
#Example
```

  url = 'https://codereviewvideos.com/blog/wp-content/uploads/2015/05/ionic-logo-horizontal-transparent.png'

  window.cordova.plugins.FileDownloader.startDownloading url, fileName
    , (data)->
      alert data
    , (error) ->
      alert 'error'
```
