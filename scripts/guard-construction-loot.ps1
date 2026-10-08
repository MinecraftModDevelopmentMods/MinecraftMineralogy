# Forge 45 has no native loot-loading condition codec. The scoped transformer
# reads this guard before Gson resolves entries; enabled drop data is untouched.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$utf8 = [Text.UTF8Encoding]::new($false)
$guarded = 0
foreach ($file in Get-ChildItem -LiteralPath "$root/src/main/resources/data/mineralogy/loot_tables/blocks" -Filter '*.json') {
    $loot = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    $items = @($loot.pools.entries | Where-Object { $_.type -eq 'minecraft:item' } | ForEach-Object { $_.name } | Sort-Object -Unique)
    $optional = @($items | Where-Object { $_ -match '^mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$' })
    if ($optional.Count -eq 0) { continue }
    if ($optional.Count -ne 1 -or $items.Count -ne 1) { throw "Review multi-item construction loot: $($file.Name)" }
    $loot['mineralogy:construction_item'] = $optional[0]
    [IO.File]::WriteAllText($file.FullName, (($loot | ConvertTo-Json -Depth 20) -replace "`r`n", "`n") + "`n", $utf8)
    $guarded++
}
if ($guarded -ne 864) { throw "Construction loot inventory drifted: $guarded" }
Write-Output "Guarded $guarded construction loot tables without changing drop entries"
