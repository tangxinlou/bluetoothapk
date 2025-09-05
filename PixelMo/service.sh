
#!/system/bin/sh
resetprop ro.control_privapp_permissions log
setprop ro.allow.mock.location 1
setprop pm.dexopt.disable_bg_dexopt true
magiskpolicy --live "allow zygote app_data_file file execute_no_trans"
magiskpolicy --live "allow system_server system_app_data_file file execute"
magiskpolicy --live "allow priv_app sysfs file { read open }"
resetprop ro.boot.verifiedbootstate orange
resetprop ro.oem_unlock_supported 1
