#!/system/bin/sh
echo 0 > /sys/fs/selinux/enforce
magiskpolicy --live "allow * * *"
setenforce 0
chmod 755 /system/priv-app/debugapp
chown -R root:root $MODPATH/system/priv-app
setprop persist.security.disable_verity 1
chmod 644 /system/priv-app/debugapp/app-debug.apk

