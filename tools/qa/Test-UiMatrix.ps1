param(
    [Parameter(Mandatory = $true)]
    [string]$Serial,
    [switch]$ExtendedOnly,
    [string]$Adb = "$env:LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe",
    [string]$OutputDirectory = "D:/Documents/Projects/Android/NurseCompose2/.codex-qa/ui-matrix"
)
$ErrorActionPreference = 'Stop'
$script:AdbExecutable = $Adb
$script:QaSerial = $Serial
function Invoke-QaAdb {
    & $script:AdbExecutable -s $script:QaSerial @args
}
# Every command is bound to one explicitly selected disposable QA emulator.
$avdName = (& $script:AdbExecutable -s $Serial emu avd name) -join ' '
if ($avdName -notmatch '^Codex_') {
    throw "UI matrix requires a Codex_ QA AVD; selected: $Serial ($avdName)"
}
$Adb = 'Invoke-QaAdb'
$package = 'com.nullpointer.nourseCompose'
$originalLocale = (& $Adb shell cmd locale get-app-locales $package) -join ''
$originalNight = (& $Adb shell cmd uimode night) -join ''
$originalFontScale = (& $Adb shell settings get system font_scale) -join ''
$results = [System.Collections.Generic.List[object]]::new()
New-Item -ItemType Directory -Force -Path $OutputDirectory | Out-Null

function Get-Ui {
    for ($attempt = 0; $attempt -lt 3; $attempt++) {
        & $Adb shell uiautomator dump /sdcard/nurse-ui-qa.xml | Out-Null
        if ($LASTEXITCODE -eq 0) { break }
        Start-Sleep -Seconds 1
    }
    if ($LASTEXITCODE -ne 0) { throw 'UI dump failed after three attempts' }
    [xml]$xml = (& $Adb shell cat /sdcard/nurse-ui-qa.xml) -join ''
    return $xml
}
function Tap-Node($node) {
    if ($null -eq $node) { throw 'Requested UI node missing' }
    $b = [regex]::Matches($node.bounds, '\d+')
    $x = [int](([int]$b[0].Value + [int]$b[2].Value) / 2)
    $y = [int](([int]$b[1].Value + [int]$b[3].Value) / 2)
    & $Adb shell input tap $x $y
}
function Tap-Label([string]$label) {
    $ui = Get-Ui
    $matches = @($ui.SelectNodes('//node') | Where-Object { $_.text -eq $label -or $_.'content-desc' -eq $label })
    if ($matches.Count -eq 0) { throw "Missing label: $label" }
    Tap-Node $matches[-1]
}
function Tap-ScrolledLabel([string]$label, [switch]$Up) {
    for ($attempt = 0; $attempt -lt 5; $attempt++) {
        $ui = Get-Ui
        $nodes = @($ui.SelectNodes('//node') | Where-Object { $_.text -eq $label -or $_.'content-desc' -eq $label })
        if ($nodes.Count -gt 0) { Tap-Node $nodes[-1]; return }
        if ($Up) { & $Adb shell input swipe 540 650 540 1550 450 }
        else { & $Adb shell input swipe 540 1550 540 650 450 }
    }
    throw "Missing scrollable label: $label"
}
function Reset-App {
    & $Adb shell am force-stop $package
    & $Adb shell am start -n "$package/.MainActivity" | Out-Null
    Start-Sleep -Seconds 2
    $ui = Get-Ui
    $skip = @($ui.SelectNodes('//node') | Where-Object { $_.text -eq $strings['intro_skip'] })
    if ($skip.Count -gt 0) {
        Tap-Node $skip[-1]
        Start-Sleep -Seconds 1
    }
}
function Tap-Prefix([string]$label) {
    $ui = Get-Ui
    Tap-Node (@($ui.SelectNodes('//node') | Where-Object { $_.text.StartsWith($label + ':') })[0])
}
function Assert-Label([string]$label) {
    $ui = Get-Ui
    if (@($ui.SelectNodes('//node') | Where-Object { $_.text -eq $label -or $_.'content-desc' -eq $label }).Count -eq 0) {
        throw "Assertion failed: $label"
    }
    Write-Output "PASS: $label"
}
function Open-Editor {
    Reset-App
    Tap-Label $strings['title_medications']
    Tap-Label $strings['action_add_medication']
}
function Capture([string]$name) {
    # Allow navigation/FAB animations to settle before hierarchy and screenshot.
    Start-Sleep -Milliseconds 500
    $ui = Get-Ui
    $path = Join-Path $OutputDirectory "$script:combo-$name"
    $ui.OuterXml | Set-Content -LiteralPath "$path.xml" -Encoding utf8
    & $Adb shell screencap -p /sdcard/nurse-ui-qa.png
    & $Adb pull /sdcard/nurse-ui-qa.png "$path.png" 2>&1 | Out-Null
    $nodes = @($ui.SelectNodes('//node') | Where-Object { $_.text -ne '' -or $_.'content-desc' -ne '' } | ForEach-Object {
        [pscustomobject]@{text=$_.text; description=$_.'content-desc'; bounds=$_.bounds; clickable=$_.clickable}
    })
    $results.Add([pscustomobject]@{combination=$script:combo; screen=$name; screenshot="$path.png"; nodes=$nodes})
    Write-Output "$script:combo/$name captured"
}

try {
    foreach ($language in @('en', 'es')) {
        [xml]$res = Get-Content -Raw -Encoding utf8 ("app/src/main/res/" + $(if ($language -eq 'es') {'values-es'} else {'values'}) + '/strings.xml')
        $strings = @{}
        $res.resources.string | ForEach-Object { $strings[$_.name] = $_.InnerText }
        & $Adb shell cmd locale set-app-locales $package --locales $language
        foreach ($night in @('no', 'yes')) {
            $script:combo = "$language-" + $(if ($night -eq 'yes') {'dark'} else {'light'})
            & $Adb shell cmd uimode night $night | Out-Null
            if ($ExtendedOnly) {
                & $Adb shell settings put system font_scale 1.0
                Open-Editor
                Tap-Label $strings['action_save']
                Assert-Label $strings['error_medication_name']
                Capture 'missing-name'
                Tap-Prefix $strings['label_start_time']
                Capture 'start-date-dialog'
                Tap-Label $strings['intro_next']
                Capture 'start-time-dialog'
                Tap-Label $strings['button_cancel_title']
                Tap-Label $strings['action_select_photo']
                Capture 'photo-options'
                & $Adb shell input keyevent 4
                $ui = Get-Ui
                Tap-Node (@($ui.SelectNodes('//node') | Where-Object { $_.class -eq 'android.widget.EditText' })[0])
                Capture 'name-keyboard'
                & $Adb shell input keyevent 4
                Tap-ScrolledLabel $strings['interval_unit_minutes']
                Tap-ScrolledLabel '60'
                Capture 'interval-keyboard'
                & $Adb shell input keyevent 123
                & $Adb shell input keyevent 67 67
                & $Adb shell input text 0
                & $Adb shell input keyevent 4
                Assert-Label '0'
                # The name remains blank throughout: this submit cannot persist a record.
                Tap-Label $strings['action_save']
                Assert-Label $strings['error_medication_interval']
                Capture 'invalid-interval'
                Tap-ScrolledLabel $strings['schedule_date_range'] -Up
                Capture 'duration-range'
                Tap-Prefix $strings['label_end_date']
                Capture 'end-date-dialog'
                Tap-Label $strings['intro_next']
                Capture 'end-time-dialog'
                Tap-Label $strings['button_cancel_title']
                & $Adb shell settings put system font_scale 1.3
                Reset-App
                Capture 'large-font-home'
                Open-Editor
                Capture 'large-font-editor-top'
                & $Adb shell input swipe 540 1550 540 500 500
                Capture 'large-font-editor-bottom'
                & $Adb shell settings put system font_scale 1.0
                continue
            }
            Reset-App
            Capture 'glucose'
            foreach ($tab in @('pressure', 'medications', 'temperature', 'oxygen')) {
                Tap-Label $strings["title_$tab"]
                Capture $tab
            }
            Tap-Label $strings['title_medications']
            Tap-Label $strings['action_add_medication']
            Capture 'reminder-editor-top'
            & $Adb shell input swipe 540 1550 540 650 450
            Capture 'reminder-editor-bottom'
            Tap-Label $strings['action_back']
            Capture 'reminder-return'
            Tap-Label $strings['action_open_menu']
            Capture 'drawer'
            & $Adb shell input keyevent 4
            foreach ($item in @(
                @('title_alarm_logs','alarm-logs'),
                @('title_export_medication_pdf_option','export'),
                @('title_settings_option','settings')
            )) {
                Reset-App
                Tap-Label $strings['action_open_menu']
                Tap-Label $strings[$item[0]]
                Capture $item[1]
                if ($item[1] -eq 'settings') {
                    Tap-Label $strings['title_diagnostics']
                    Capture 'diagnostics'
                }
            }
        }
    }
} finally {
    if ($originalFontScale -eq 'null') { & $Adb shell settings delete system font_scale | Out-Null }
    else { & $Adb shell settings put system font_scale $originalFontScale }
    $results | ConvertTo-Json -Depth 6 | Set-Content -LiteralPath (Join-Path $OutputDirectory 'matrix.json') -Encoding utf8
    $localeMatch = [regex]::Match($originalLocale, '\[(.*?)\]')
    if ($localeMatch.Success -and $localeMatch.Groups[1].Value) {
        & $Adb shell cmd locale set-app-locales $package --locales $localeMatch.Groups[1].Value
    } else { & $Adb shell cmd locale set-app-locales $package }
    $nightMatch = [regex]::Match($originalNight, '(yes|no|auto|custom_schedule|custom_bedtime)')
    if ($nightMatch.Success) { & $Adb shell cmd uimode night $nightMatch.Value | Out-Null }
    Reset-App
    Write-Output "Captured $($results.Count) screens; original language/appearance restored."
}
