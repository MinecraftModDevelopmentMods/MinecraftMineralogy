[CmdletBinding()]
param(
    [switch] $Check
)

$projectRoot = Split-Path -Parent $PSScriptRoot
$legacyModelRoot = Join-Path $projectRoot 'src\main\resources\assets\mineralogy\models\item'
$definitionRoot = Join-Path $projectRoot 'src\main\resources\assets\mineralogy\items'
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

$expected = [ordered]@{}
foreach ($model in Get-ChildItem -LiteralPath $legacyModelRoot -Filter '*.json' | Sort-Object Name) {
    $id = [IO.Path]::GetFileNameWithoutExtension($model.Name)
    if ($id.EndsWith('_double_slab') -or ($id.StartsWith('lit_') -and $id.EndsWith('_furnace'))) {
        continue
    }
    $expected[$model.Name] = @"
{
  "model": {
    "type": "minecraft:model",
    "model": "mineralogy:item/$id"
  }
}
"@ -replace "`r?`n", "`n"
}

if ($expected.Count -ne 928) {
    throw "Expected 928 registered-item model definitions, found $($expected.Count)"
}

if ($Check) {
    if (-not (Test-Path -LiteralPath $definitionRoot)) {
        throw "Missing Minecraft 26.2 item-definition directory $definitionRoot"
    }
    $actual = @(Get-ChildItem -LiteralPath $definitionRoot -Filter '*.json')
    if ($actual.Count -ne $expected.Count) {
        throw "Expected $($expected.Count) item definitions, found $($actual.Count)"
    }
    foreach ($entry in $expected.GetEnumerator()) {
        $path = Join-Path $definitionRoot $entry.Key
        if (-not (Test-Path -LiteralPath $path)) {
            throw "Missing item definition $($entry.Key)"
        }
        if ([IO.File]::ReadAllText($path, [Text.Encoding]::UTF8) -cne $entry.Value) {
            throw "Item definition differs from its generated contract: $($entry.Key)"
        }
    }
    Write-Output 'Verified 928 Minecraft 26.2 Mineralogy item definitions.'
    return
}

New-Item -ItemType Directory -Force -Path $definitionRoot | Out-Null
foreach ($existing in Get-ChildItem -LiteralPath $definitionRoot -Filter '*.json' -ErrorAction SilentlyContinue) {
    if (-not $expected.Contains($existing.Name)) {
        Remove-Item -LiteralPath $existing.FullName
    }
}
foreach ($entry in $expected.GetEnumerator()) {
    [IO.File]::WriteAllText((Join-Path $definitionRoot $entry.Key), $entry.Value, $utf8NoBom)
}

Write-Output 'Generated 928 Minecraft 26.2 Mineralogy item definitions.'
