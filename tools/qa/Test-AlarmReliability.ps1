param(
    [Parameter(Mandatory=$true)][string]$Serial,
    [Parameter(Mandatory=$true)][string]$OutputDirectory,
    [switch]$DisposableEmulator,
    [switch]$IncludeRealTiming,
    [switch]$IncludeReboot,
    [switch]$IncludePin,
    [string]$Adb = "$env:LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe"
)
$ErrorActionPreference = 'Stop'
if (!$DisposableEmulator -or $Serial -notmatch '^emulator-\d+$') {
    throw 'This destructive-environment QA suite requires an explicitly disposable emulator.'
}
$package = 'com.nullpointer.nourseCompose'
$testClass = "$package.medication.MedicationAlarmReliabilityTest"
function Invoke-Adb { & $Adb -s $Serial @args }
$api = [int]((Invoke-Adb shell getprop ro.build.version.sdk) -join '')
if ($api -lt 33) { throw 'This suite targets API 33 and later.' }
$avd = (Invoke-Adb emu avd name) -join ''
if ($IncludePin -and $avd -notmatch '^Codex_Alarm_API_') {
    throw 'PIN changes are permitted only on a dedicated Codex_Alarm_API_ QA AVD.'
}
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
$results = [System.Collections.Generic.List[object]]::new()
$permissionDump = (Invoke-Adb shell dumpsys package $package) -join "`n"
$originalPost = $permissionDump -match 'android.permission.POST_NOTIFICATIONS:\s*granted=true'
function Get-AppOpMode([string]$Operation) {
    $value = (Invoke-Adb shell cmd appops get $package $Operation) -join ''
    $m = [regex]::Match($value, [regex]::Escape($Operation) + ':\s*(\w+)')
    if ($m.Success) { return $m.Groups[1].Value }
    return 'default'
}
$originalExact = Get-AppOpMode 'SCHEDULE_EXACT_ALARM'
$originalFullScreen = if ($api -ge 34) { Get-AppOpMode 'USE_FULL_SCREEN_INTENT' } else { $null }
function Run-Case([string]$Method) {
    $output = (Invoke-Adb shell am instrument -w -r -e reliabilitySuite true -e class "$testClass#$Method" "$package.test/androidx.test.runner.AndroidJUnitRunner" 2>&1) -join "`n"
    $output | Set-Content -LiteralPath (Join-Path $OutputDirectory "$Method.log") -Encoding utf8
    # am instrument can return process status 0 for failures and skipped tests.
    $passed = $output -match 'OK \(1 test\)' -and $output -notmatch 'INSTRUMENTATION_STATUS_CODE: -3|FAILURES!!!|INSTRUMENTATION_FAILED'
    $status = if ($passed) { 'passed' } else { 'failed-or-incomplete' }
    $results.Add([pscustomobject]@{api=$api; serial=$Serial; case=$Method; status=$status})
    Write-Host "$Method : $status"
    return $passed
}
function Wait-ForBoot([string]$PreviousBootId) {
    Invoke-Adb wait-for-device | Out-Null
    $deadline = (Get-Date).AddMinutes(4)
    do {
        Start-Sleep -Seconds 3
        $boot = (Invoke-Adb shell getprop sys.boot_completed) -join ''
        $id = (Invoke-Adb shell cat /proc/sys/kernel/random/boot_id) -join ''
    } while (($boot -ne '1' -or $id -eq $PreviousBootId) -and (Get-Date) -lt $deadline)
    if ($boot -ne '1' -or $id -eq $PreviousBootId) { throw 'Actual reboot was not confirmed.' }
    Invoke-Adb shell input keyevent 224 | Out-Null
    Invoke-Adb shell wm dismiss-keyguard | Out-Null
    # Boot broadcasts are deferred beyond sys.boot_completed. Starting instrumentation
    # too early force-stops the target and removes its pending BOOT_COMPLETED delivery.
    Start-Sleep -Seconds 90
}
try {
    Invoke-Adb shell pm grant $package android.permission.POST_NOTIFICATIONS | Out-Null
    Invoke-Adb shell appops set $package SCHEDULE_EXACT_ALARM allow | Out-Null
    if ($api -ge 34) { Invoke-Adb shell appops set $package USE_FULL_SCREEN_INTENT allow | Out-Null }
    Run-Case 'recoverAbandonedReliabilityFixtures' | Out-Host
    Run-Case 'blockedChannelDoesNotReportSuccessfulDeliveryOrRing' | Out-Host
    Run-Case 'simultaneousAlarmsKeepBothReminderControls' | Out-Host
    Run-Case 'fullScreenDeliverySurvivesForcedDoze' | Out-Host
    try {
        Invoke-Adb shell pm revoke $package android.permission.POST_NOTIFICATIONS | Out-Null
        Run-Case 'deniedNotificationsAreReportedWithoutRinging' | Out-Host
    } finally { Invoke-Adb shell pm grant $package android.permission.POST_NOTIFICATIONS | Out-Null }
    try {
        Invoke-Adb shell appops set $package SCHEDULE_EXACT_ALARM deny | Out-Null
        Run-Case 'deniedExactAlarmUsesExplicitInexactFallback' | Out-Host
    } finally { Invoke-Adb shell appops set $package SCHEDULE_EXACT_ALARM allow | Out-Null }
    if ($api -ge 34) {
        try {
            Invoke-Adb shell appops set $package USE_FULL_SCREEN_INTENT deny | Out-Null
            Run-Case 'deniedFullScreenAccessKeepsNotificationAndSoundWithoutOpeningActivity' | Out-Host
        } finally { Invoke-Adb shell appops set $package USE_FULL_SCREEN_INTENT allow | Out-Null }
    }
    if ($IncludePin) {
        $pinSet = $false
        try {
            $pinResult = (Invoke-Adb shell locksettings set-pin 2468) -join ''
            if ($pinResult -notmatch "Pin set to") { throw 'Cannot set QA PIN; no existing credential will be overwritten.' }
            $pinSet = $true
            Run-Case 'fullScreenAlarmIsVisibleOverSecureKeyguard' | Out-Host
        } finally {
            if ($pinSet) { Invoke-Adb shell locksettings clear --old 2468 | Out-Null }
            Invoke-Adb shell input keyevent 224 | Out-Null
            Invoke-Adb shell wm dismiss-keyguard | Out-Null
        }
    }
    if ($IncludeReboot) {
        $prepared = Run-Case 'prepareFixtureForRealReboot'
        if ($prepared -contains $true) {
            Invoke-Adb shell am start -n "$package/.MainActivity" | Out-Null
            $oldBootId = (Invoke-Adb shell cat /proc/sys/kernel/random/boot_id) -join ''
            Invoke-Adb reboot | Out-Null
            Wait-ForBoot $oldBootId
            Run-Case 'verifyFixtureAfterRealReboot' | Out-Host
        }
    }
    if ($IncludeRealTiming) {
        Run-Case 'ringingStopsAfterRealFiveMinuteTimeout' | Out-Host
        Run-Case 'snoozeIsDeliveredAfterRealTenMinutes' | Out-Host
    }
} finally {
    Run-Case 'recoverAbandonedReliabilityFixtures' | Out-Host
    Invoke-Adb shell cmd deviceidle unforce | Out-Null
    Invoke-Adb shell dumpsys battery reset | Out-Null
    Invoke-Adb shell appops set $package SCHEDULE_EXACT_ALARM $originalExact | Out-Null
    if ($api -ge 34) { Invoke-Adb shell appops set $package USE_FULL_SCREEN_INTENT $originalFullScreen | Out-Null }
    if ($originalPost) { Invoke-Adb shell pm grant $package android.permission.POST_NOTIFICATIONS | Out-Null }
    else { Invoke-Adb shell pm revoke $package android.permission.POST_NOTIFICATIONS | Out-Null }
    Invoke-Adb shell am start -n "$package/.MainActivity" | Out-Null
    $results | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $OutputDirectory 'results.json') -Encoding utf8
}
if (@($results | Where-Object {$_.status -ne 'passed'}).Count -gt 0) { exit 1 }
