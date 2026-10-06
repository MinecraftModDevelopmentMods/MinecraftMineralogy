# Keep loot references loadable when a construction registration switch is off.
# The entry, pools and drop conditions are left intact.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$utf8 = [Text.UTF8Encoding]::new($false)
$guarded = 0
foreach ($file in Get-ChildItem -LiteralPath "$root/src/main/resources/data/mineralogy/loot_table/blocks" -Filter '*.json') {
    $loot = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    $items = @($loot.pools.entries | Where-Object { $_.type -eq 'minecraft:item' } | ForEach-Object { $_.name } | Sort-Object -Unique)
    $optional = @($items | Where-Object { $_ -match '^mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$' })
    if ($optional.Count -eq 0) { continue }
    if ($optional.Count -ne 1 -or $items.Count -ne 1) { throw "Review multi-item construction loot: $($file.Name)" }
    $condition = [ordered]@{ type = 'forge:item_exists'; item = $optional[0] }
    if ($loot.Contains('forge:condition')) { $loot.Remove('forge:condition') }
    # Forge 52 checks loading conditions on pools, not on the whole loot table.
    foreach ($pool in $loot.pools) { $pool['forge:condition'] = $condition }
    $guardedLoot = [ordered]@{}
    foreach ($key in $loot.Keys) { $guardedLoot[$key] = $loot[$key] }
    [IO.File]::WriteAllText($file.FullName, (($guardedLoot | ConvertTo-Json -Depth 20) -replace "`r`n", "`n") + "`n", $utf8)
    $guarded++
}
Write-Output "Guarded $guarded construction loot tables without changing drop entries"
