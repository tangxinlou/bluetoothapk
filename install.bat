
rem adb install -r -t --bypass-low-target-sdk-block -i "android" --force-queryable  app\build\outputs\apk\debug\app-debug.apk
rem adb shell setenforce 0
rem adb shell mkdir /system/priv-app/debugapp
rem adb shell chmod 755 /system/priv-app/debugapp
rem adb shell ls -la /system/priv-app/debugapp
rem adb push  app\build\outputs\apk\debug\app-debug.apk  /system/priv-app/debugapp
rem adb shell chown root:root /system/priv-app/debugapp/app-debug.apk
rem adb shell chmod 644 /system/priv-app/debugapp/app-debug.apk
rem adb shell ls -la /system/priv-app/debugapp/app-debug.apk
rem adb shell setprop persist.security.disable_verity 1
rem adb disable-verity
adb push  app\build\outputs\apk\debug\app-debug.apk  /sdcard/app-debug.apk
adb shell "su -c 'cp /sdcard/app-debug.apk /data/adb/modules/PixelMod/system/priv-app/debug'"
adb shell "su -c 'ls -la /data/adb/modules/PixelMod/system/priv-app/debug'"
pause
