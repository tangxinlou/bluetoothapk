rem adb shell "logcat -v threadtime | tee /sdcard/all_logs.txt"
adb logcat -G 256m
adb logcat -g
adb shell "su -c 'logcat -c '"
rem adb shell "su -c 'logcat | grep -e \"AndroidRuntime\" -e \"tangxinlou debug\"'"
adb shell "su -c 'logcat | grep -E \"AndroidRuntime^|tangxinlou debug\"'"
