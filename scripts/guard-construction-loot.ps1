# Disabled construction must not leave a required, unregistered loot item.
# NeoForge's native conditional codec yields an empty table when the item is absent.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference='Stop'
$root=(Resolve-Path -LiteralPath $ProjectRoot).Path
$utf8=[Text.UTF8Encoding]::new($false)
$guarded=0
foreach($file in Get-ChildItem -LiteralPath "$root/src/main/resources/data/mineralogy/loot_table/blocks" -Filter '*.json'){
    $loot=Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    $items=@($loot.pools.entries | Where-Object {$_.type -eq 'minecraft:item'} | ForEach-Object {$_.name} | Sort-Object -Unique)
    $optional=@($items | Where-Object {$_ -match '^mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$'})
    if(-not $optional.Count){continue}
    if($optional.Count -ne 1 -or $items.Count -ne 1){throw "Review multi-item construction loot: $($file.Name)"}
    if($loot.Contains('forge:condition')){throw 'Forge condition in the NeoForge lineage'}
    $condition=[ordered]@{type='neoforge:registered'; registry='minecraft:item'; value=$optional[0]}
    if($loot.Contains('neoforge:conditions')){
        $existing=@($loot['neoforge:conditions'])
        if($existing.Count -ne 1 -or $existing[0].type -ne $condition.type -or
            $existing[0].registry -ne $condition.registry -or $existing[0].value -ne $condition.value){
            throw "Review existing loot conditions: $($file.Name)"
        }
        $loot.Remove('neoforge:conditions')
    }
    $result=[ordered]@{'neoforge:conditions'=@($condition)}
    foreach($key in $loot.Keys){$result[$key]=$loot[$key]}
    [IO.File]::WriteAllText($file.FullName,(($result | ConvertTo-Json -Depth 20) -replace "`r`n","`n")+"`n",$utf8)
    $guarded++
}
Write-Output "Guarded $guarded construction tables with NeoForge registered-item conditions; enabled payloads unchanged"
