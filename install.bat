
rem adb install -r -t --bypass-low-target-sdk-block -i "android" --force-queryable  app\build\outputs\apk\debug\app-debug.apk
 adb shell setenforce 0
 adb shell mkdir /system/priv-app/debugapp
 adb shell chmod 755 /system/priv-app/debugapp
 adb shell ls -la /system/priv-app/debugapp
 adb push  app\build\outputs\apk\debug\app-debug.apk  /system/priv-app/debugapp
 adb shell chown root:root /system/priv-app/debugapp/app-debug.apk
 adb shell chmod 644 /system/priv-app/debugapp/app-debug.apk
 adb shell ls -la /system/priv-app/debugapp/app-debug.apk
 adb shell setprop persist.security.disable_verity 1
 adb disable-verity
pause
