# Disabled construction must not leave a required, unregistered loot item.
# Use existing construction flags and their nested registration prerequisites,
# without depending on an item registry during loot-condition decoding.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference='Stop'
$root=(Resolve-Path -LiteralPath $ProjectRoot).Path
$utf8=[Text.UTF8Encoding]::new($false)
$guarded=0
function Construction-Flags([string]$id) {
    if ($id -match '_relief_') { return @('GENERATE_SMOOTH', 'GENERATE_RELIEFS') }
    $prefix = if ($id -match '_smooth_brick(?:_|$)') { 'SMOOTHBRICK' }
              elseif ($id -match '_smooth(?:_|$)') { 'SMOOTH' }
              elseif ($id -match '_brick(?:_|$)') { 'BRICK' } else { 'ROCK' }
    [string[]]$flags = @(switch ($prefix) {
        'SMOOTHBRICK' { 'GENERATE_SMOOTH'; 'GENERATE_SMOOTHBRICK' }
        'SMOOTH' { 'GENERATE_SMOOTH' }
        'BRICK' { 'GENERATE_BRICK' }
    })
    if ($id.EndsWith('_furnace')) { $flags += @("GENERATE_${prefix}SLAB", "GENERATE_${prefix}FURNACE") }
    elseif ($id.EndsWith('_slab')) { $flags += "GENERATE_${prefix}SLAB" }
    elseif ($id.EndsWith('_stairs')) { $flags += "GENERATE_${prefix}STAIRS" }
    elseif ($id.EndsWith('_wall')) { $flags += "GENERATE_${prefix}WALL" }
    if (-not $flags) { throw "Unmapped construction item $id" }
    return @($flags)
}
foreach($file in Get-ChildItem -LiteralPath "$root/src/main/resources/data/mineralogy/loot_tables/blocks" -Filter '*.json'){
    $loot=Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    $items=@($loot.pools.entries | Where-Object {$_.type -eq 'minecraft:item'} | ForEach-Object {$_.name} | Sort-Object -Unique)
    $optional=@($items | Where-Object {$_ -match '^mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$'})
    if(-not $optional.Count){continue}
    if($optional.Count -ne 1 -or $items.Count -ne 1){throw "Review multi-item construction loot: $($file.Name)"}
    if($loot.Contains('forge:condition')){throw 'Forge condition in the NeoForge lineage'}
    $conditions=@(Construction-Flags $optional[0] | ForEach-Object {
        [ordered]@{type='mineralogy:config'; flag=$_}
    })
    if($loot.Contains('neoforge:conditions')){
        $existing=@($loot['neoforge:conditions'])
        $registered=$existing.Count -eq 1 -and $existing[0].type -eq 'neoforge:registered' -and
            $existing[0].registry -eq 'minecraft:item' -and $existing[0].value -eq $optional[0]
        $matches=[System.Text.Json.Nodes.JsonNode]::DeepEquals(
            [System.Text.Json.Nodes.JsonNode]::Parse((ConvertTo-Json -InputObject @($existing) -Depth 10)),
            [System.Text.Json.Nodes.JsonNode]::Parse((ConvertTo-Json -InputObject @($conditions) -Depth 10)))
        if(-not $registered -and -not $matches){
            throw "Review existing loot conditions: $($file.Name)"
        }
        $loot.Remove('neoforge:conditions')
    }
    $result=[ordered]@{'neoforge:conditions'=$conditions}
    foreach($key in $loot.Keys){$result[$key]=$loot[$key]}
    [IO.File]::WriteAllText($file.FullName,(($result | ConvertTo-Json -Depth 20) -replace "`r`n","`n")+"`n",$utf8)
    $guarded++
}
Write-Output "Guarded $guarded construction tables with their existing registration flags; enabled payloads unchanged"
