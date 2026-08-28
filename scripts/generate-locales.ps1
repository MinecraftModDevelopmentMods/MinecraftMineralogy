[CmdletBinding()]
param(
    [string] $SourceLocaleRoot = ''
)

$SourceLocaleRoot = if ([string]::IsNullOrWhiteSpace($SourceLocaleRoot)) {
    Join-Path $PSScriptRoot '..\..\..\MinecraftMineralogy 112\MinecraftMineralogy\src\main\resources\assets\mineralogy\lang'
} else { $SourceLocaleRoot }
$projectRoot = Split-Path -Parent $PSScriptRoot
$targetRoot = Join-Path $projectRoot 'src\main\resources\assets\mineralogy\lang'
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$expectedLocales = @(
    'de_AU', 'de_DE', 'en_CA', 'en_EN', 'en_GB', 'en_PT', 'en_US',
    'es_ES', 'es_MX', 'fr_CA', 'fr_FR', 'ja_JP', 'ko_KR', 'pt_BR',
    'pt_PT', 'ru_RU', 'zh_CN'
)

function Convert-Key([string] $key) {
    if ($key -match '^tile\.mineralogy\.(.+)\.name$') {
        return "block.mineralogy.$($Matches[1])"
    }
    if ($key -match '^item\.mineralogy\.(.+)\.name$') {
        return "item.mineralogy.$($Matches[1])"
    }
    return $key
}

New-Item -ItemType Directory -Force -Path $targetRoot | Out-Null
foreach ($locale in $expectedLocales) {
    $source = Join-Path $SourceLocaleRoot "$locale.lang"
    if (-not (Test-Path -LiteralPath $source)) {
        throw "Missing reviewed Mineralogy 1.12 locale $source"
    }
    $translations = [ordered]@{}
    foreach ($line in [IO.File]::ReadAllLines($source, [Text.Encoding]::UTF8)) {
        if ([string]::IsNullOrWhiteSpace($line) -or $line.StartsWith('#')) { continue }
        $equals = $line.IndexOf('=')
        if ($equals -lt 1) { throw "Malformed locale line in ${source}: $line" }
        $key = Convert-Key $line.Substring(0, $equals)
        $value = $line.Substring($equals + 1)
        if ($translations.Contains($key)) { throw "Duplicate mapped key $key in $source" }
        $translations[$key] = $value
    }
    if ($translations.Count -ne 938) {
        throw "Expected 938 mapped keys in $source, found $($translations.Count)"
    }
    $destination = Join-Path $targetRoot "$($locale.ToLowerInvariant()).json"
    $json = ($translations | ConvertTo-Json -Depth 4) -replace "`r?`n", "`n"
    [IO.File]::WriteAllText($destination, $json + "`n", $utf8NoBom)
}

$actual = @(Get-ChildItem -LiteralPath $targetRoot -Filter '*.json' | Sort-Object Name)
if ($actual.Count -ne 17) {
    throw "Expected exactly 17 generated locale files, found $($actual.Count)"
}
Write-Output 'Generated 17 reviewed Mineralogy 1.17.1 JSON locales with 938 ordered keys each.'
