param(
    [string]$Adb = "$env:LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe",
    [Parameter(Mandatory=$true)][string]$OutputDirectory
)
$ErrorActionPreference = 'Stop'
$package = 'com.nullpointer.nourseCompose'
$originalLocale = (& $Adb shell cmd locale get-app-locales $package) -join ''
$originalNight = (& $Adb shell cmd uimode night) -join ''
$originalFont = (& $Adb shell settings get system font_scale) -join ''
$testClass = 'com.nullpointer.nourseCompose.medication.MedicationAlarmInstrumentedTest'
$regularCases = @('standardAlarmIsDeliveredByAlarmManager','foregroundFullScreenTakenStopsRinging','snoozeStopsRingingAndClosesAlarmScreen','fullScreenIsDeliveredWithScreenOff','backDoesNotMarkTakenAndLeavesNotificationControls')
$regularSelection = ($regularCases | ForEach-Object { "$testClass#$_" }) -join ','
$results = @()
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null
try {
    foreach ($language in @('en','es')) {
        foreach ($night in @('no','yes')) {
            $combo = "$language-" + $(if ($night -eq 'yes') {'dark'} else {'light'})
            $folder = Join-Path $OutputDirectory $combo
            New-Item -ItemType Directory -Force -Path $folder | Out-Null
            & $Adb shell cmd locale set-app-locales $package --locales $language
            & $Adb shell cmd uimode night $night | Out-Null
            & $Adb shell settings put system font_scale 1.0
            & $Adb shell am force-stop $package
            # Direct instrumentation intentionally avoids Gradle's APK uninstaller.
            $output = (& $Adb shell am instrument -w -e class $regularSelection "$package.test/androidx.test.runner.AndroidJUnitRunner" 2>&1) -join "`n"
            # Set global scale before starting instrumentation, never from the test thread:
            # changing it during an ActivityScenario caused a configuration/launch stall.
            & $Adb shell settings put system font_scale 2.0
            & $Adb shell am force-stop $package
            $output += "`n" + ((& $Adb shell am instrument -w -e class "$testClass#fullScreenActionsRemainReachableWithLargeTextAndLongName" "$package.test/androidx.test.runner.AndroidJUnitRunner" 2>&1) -join "`n")
            $output | Set-Content -LiteralPath (Join-Path $folder 'instrumentation.log') -Encoding utf8
            & $Adb pull "/sdcard/Android/data/$package/files/qa-alarm" $folder 2>&1 | Out-Null
            $failed = [regex]::Matches($output, 'Tests run:\s*(\d+),\s*Failures:\s*(\d+)')
            $passed = [regex]::Matches($output, 'OK \((\d+) tests?\)')
            $testCount = 0; $failureCount = 0
            foreach ($match in $failed) { $testCount += [int]$match.Groups[1].Value; $failureCount += [int]$match.Groups[2].Value }
            foreach ($match in $passed) { $testCount += [int]$match.Groups[1].Value }
            if ($testCount -eq 6) {
                $result = [pscustomobject]@{combination=$combo; tests=$testCount; failures=$failureCount; status=$(if($failureCount -gt 0){'failed'}else{'passed'})}
            } else {
                $result = [pscustomobject]@{combination=$combo; tests=$null; failures=$null; status='incomplete'}
            }
            $results += $result
            Write-Output "$combo : $($result.status), tests=$($result.tests), failures=$($result.failures)"
        }
    }
} finally {
    & $Adb shell am force-stop $package
    if ($originalFont -eq 'null') { & $Adb shell settings delete system font_scale | Out-Null }
    else { & $Adb shell settings put system font_scale $originalFont }
    $results | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $OutputDirectory 'results.json') -Encoding utf8
    $m = [regex]::Match($originalLocale, '\[(.*?)\]')
    if ($m.Success -and $m.Groups[1].Value) { & $Adb shell cmd locale set-app-locales $package --locales $m.Groups[1].Value }
    else { & $Adb shell cmd locale set-app-locales $package }
    $m = [regex]::Match($originalNight, '(yes|no|auto|custom_schedule|custom_bedtime)')
    if ($m.Success) { & $Adb shell cmd uimode night $m.Value | Out-Null }
    & $Adb shell am start -n "$package/.MainActivity" | Out-Null
}
if (@($results | Where-Object {$_.status -ne 'passed'}).Count -gt 0) { exit 1 }
