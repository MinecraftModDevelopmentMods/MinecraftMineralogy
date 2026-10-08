# Isolated tag generator. Recipe and provider data are never regenerated here.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$projectPath = (Resolve-Path -LiteralPath $ProjectRoot).Path
$properties = Get-Content -LiteralPath (Join-Path $projectPath 'gradle.properties')
$mc = ($properties | Where-Object { $_ -match '^minecraft_version=' }) -replace '^minecraft_version=', ''
$singular = $mc -notin @('1.16.5', '1.17.1', '1.18.2', '1.19.4', '1.20.1', '1.20.6')
$cubes = $mc -in @('26.2', '26.3')
$bats = $mc -in @('1.21.11', '26.1.2', '26.2', '26.3')
$blockDir = if ($singular) { 'block' } else { 'blocks' }
$itemDir = if ($singular) { 'item' } else { 'items' }
$provider = Get-Content -Raw -LiteralPath (Join-Path $projectPath 'src/main/resources/data/mineralogy/orespawn/provider.json') | ConvertFrom-Json
$outputs = @($provider.rocks.PSObject.Properties | ForEach-Object { $_.Value.block } | Sort-Object -Unique)
$aliases = @($provider.profile_defaults.worldgen_aliases.PSObject.Properties | ForEach-Object { $_.Name })
$terrain = @(@($outputs) + @($aliases) | Sort-Object -Unique)
if ($outputs.Count -ne 32 -or $terrain.Count -ne (32 + $aliases.Count)) { throw 'Unexpected provider terrain contract' }
$families = @('andesite','basalt','diorite','granite','rhyolite','pegmatite','diabase','gabbro','peridotite','basaltic_glass','scoria','tuff','shale','conglomerate','dolomite','limestone','siltstone','marble','slate','schist','gneiss','phyllite','amphibolite','hornfels','quartzite','novaculite','rock_salt')
$utf8 = [System.Text.UTF8Encoding]::new($false)
$contracts = [ordered]@{}
function Write-Tag([string]$RelativePath, [object[]]$Values) {
    $destination = Join-Path $projectPath ('src/main/resources/data/' + $RelativePath)
    [System.IO.Directory]::CreateDirectory((Split-Path $destination -Parent)) | Out-Null
    $json = [ordered]@{ replace = $false; values = @($Values) } | ConvertTo-Json -Depth 8
    [System.IO.File]::WriteAllText($destination, ($json -replace "`r`n", "`n") + "`n", $utf8)
    $contracts[$RelativePath] = @($Values)
}
function Optional([string]$Id) { [ordered]@{ id = $Id; required = $false } }
Write-Tag "mineralogy/tags/$blockDir/terrain_rocks.json" $terrain
Write-Tag "mineralogy/tags/$itemDir/terrain_rocks.json" $terrain
$naturalTags = @()
if ($mc -ne '1.16.5') { $naturalTags += @('dripstone_replaceable_blocks','moss_replaceable','azalea_root_replaceable') }
if ($mc -notin @('1.16.5','1.17.1','1.18.2')) { $naturalTags += 'sculk_replaceable' }
if ($mc -notin @('1.16.5','1.17.1')) { $naturalTags += 'goats_spawnable_on' }
if ($mc -in @('26.1.2','26.2','26.3')) { $naturalTags += 'forest_rock_can_place_on' }
foreach ($tag in $naturalTags) {
    Write-Tag "minecraft/tags/$blockDir/$tag.json" @('#mineralogy:terrain_rocks')
}
if ($mc -eq '1.21.11' -and [bool]($properties -match '^(neoforge_version=|loader_name=neoforge$)')) {
    # NeoForge 21.11's native blob feature still calls Feature.isStone directly.
    Write-Tag "mineralogy/tags/$blockDir/forest_rock_substrates.json" @('#mineralogy:terrain_rocks')
}
if ($bats) {
    Write-Tag "minecraft/tags/$blockDir/bats_spawnable_on.json" @('#mineralogy:terrain_rocks')
}
if ($cubes) {
    $stones = @($families | ForEach-Object { "mineralogy:$_" }) + @('mineralogy:chalk','mineralogy:chert','mineralogy:gypsum','mineralogy:pumice')
    Write-Tag "mineralogy/tags/$itemDir/sulfur_cube_stones.json" $stones
    Write-Tag "minecraft/tags/$itemDir/sulfur_cube_archetype/slow_bouncy.json" @('#mineralogy:sulfur_cube_stones')
}
if ($mc -notin @('1.16.5','1.17.1','1.18.2')) {
    $hard = @($families | Where-Object { $_ -notin @('rock_salt','scoria','siltstone') } | ForEach-Object { "mineralogy:$_" }) + @('mineralogy:chert')
    $hard += @($terrain | Where-Object { $_ -in @('minecraft:andesite','minecraft:basalt','minecraft:diorite','minecraft:granite','minecraft:tuff') })
    Write-Tag "mineralogy/tags/$blockDir/horn_breaking_rocks.json" @($hard | Sort-Object -Unique)
    Write-Tag "minecraft/tags/$blockDir/snaps_goat_horn.json" @('#mineralogy:horn_breaking_rocks')
}
$rawStones = @($terrain | Where-Object { $_ -ne 'minecraft:sandstone' })
foreach ($kind in @($blockDir,$itemDir)) {
    Write-Tag "mineralogy/tags/$kind/raw_stones.json" $rawStones
    foreach ($shape in @('slab','stairs','wall')) {
        $forms = foreach ($family in $families) { foreach ($finish in @('','_smooth','_brick','_smooth_brick')) { Optional "mineralogy:$family${finish}_$shape" } }
        $aggregate = if ($shape -eq 'slab') { 'slabs' } elseif ($shape -eq 'wall') { 'walls' } else { 'stairs' }
        Write-Tag "minecraft/tags/$kind/$aggregate.json" $forms
    }
    $isNeoForge = [bool]($properties -match '^(neoforge_version=|loader_name=neoforge$)')
    $modernCommon = $mc -in @('1.21.1','1.21.11','26.1.2','26.2','26.3') -or $isNeoForge
    if ($modernCommon) {
        Write-Tag "c/tags/$kind/stones.json" @('#mineralogy:raw_stones')
        $furnaces = foreach ($family in $families) { foreach ($finish in @('','_smooth','_brick','_smooth_brick')) {
            Optional "mineralogy:$family${finish}_furnace"
            if ($kind -eq $blockDir) { Optional "mineralogy:lit_$family${finish}_furnace" }
        } }
        Write-Tag "c/tags/$kind/player_workstations/furnaces.json" $furnaces
        $colors = @('white','orange','magenta','light_blue','yellow','lime','pink','gray','light_gray','cyan','purple','blue','brown','green','red','black')
        foreach ($color in $colors) {
            $legacyColor = if ($color -eq 'light_gray') { 'silver' } else { $color }
            Write-Tag "c/tags/$kind/dyed/$color.json" @("mineralogy:drywall_$legacyColor")
        }
        Write-Tag "c/tags/$kind/dyed.json" @($colors | ForEach-Object { "#c:dyed/$_" })
    }
    # Older Forge stone consumers remain supported where the loader defines them.
    if ($mc -notin @('26.1.2','26.2','26.3') -and -not $isNeoForge) {
        Write-Tag "forge/tags/$kind/stone.json" @('#mineralogy:raw_stones')
    }
    $commonNamespace = if (Test-Path -LiteralPath (Join-Path $projectPath "src/main/resources/data/c/tags/$kind/ores")) { 'c' } else { 'forge' }
    Write-Tag "$commonNamespace/tags/$kind/ores.json" @('nitrate','phosphorous','sulfur' | ForEach-Object { "#$commonNamespace`:ores/$_" })
    Write-Tag "$commonNamespace/tags/$kind/storage_blocks.json" @('chalk','gypsum','nitrate','phosphorous','rock_salt','sulfur' | ForEach-Object { "#$commonNamespace`:storage_blocks/$_" })
    if ($mc -notin @('1.16.5','1.17.1')) {
        $normalNamespace = if ($modernCommon) { 'c' } else { 'forge' }
        $normalPath = if ($modernCommon) { 'cobblestones/normal' } else { 'cobblestone/normal' }
        Write-Tag "$normalNamespace/tags/$kind/$normalPath.json" @('mineralogy:chert','mineralogy:pumice')
    }
}
if ($mc -eq '26.3') {
    $lit = foreach ($family in $families) { foreach ($finish in @('','_smooth','_brick','_smooth_brick')) { Optional "mineralogy:lit_$family${finish}_furnace" } }
    Write-Tag "minecraft/tags/$blockDir/cats_can_sit_on.json" $lit
}
if ($mc -ne '1.16.5') {
    Write-Tag "mineralogy/tags/$blockDir/dripstone_base_stones.json" @('#minecraft:base_stone_overworld','#mineralogy:terrain_rocks')
}
# The native straight-wall control loses its post under our non-colliding lamp.
Write-Tag "minecraft/tags/$blockDir/wall_post_override.json" @('mineralogy:rocksaltlamp')
# Registration switches can remove any construction form. Keep every existing
# family and compatibility tag loadable, without changing its member identities.
foreach ($file in Get-ChildItem -LiteralPath (Join-Path $projectPath 'src/main/resources/data') -Recurse -Filter '*.json' | Where-Object { $_.FullName -match '[\\/]tags[\\/]' }) {
    $tag = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json
    $changed = $false
    $values = foreach ($value in $tag.values) {
        if ($value -is [string] -and $value -match '^mineralogy:(lit_)?(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$') {
            $changed = $true; Optional $value
        } else { $value }
    }
    if ($tag.PSObject.Properties.Name -contains 'optional') {
        $changed = $true
        foreach ($value in $tag.optional) { $values += Optional $value }
        $tag.PSObject.Properties.Remove('optional')
    }
    if ($changed) {
        $tag.values = @($values)
        [IO.File]::WriteAllText($file.FullName, (($tag | ConvertTo-Json -Depth 8) -replace "`r`n", "`n") + "`n", $utf8)
    }
}
$contractPath = Join-Path $projectPath 'src/test/resources/tag-compatibility-contract.json'
[IO.Directory]::CreateDirectory((Split-Path $contractPath -Parent)) | Out-Null
[IO.File]::WriteAllText($contractPath, (([ordered]@{ minecraft = $mc; tags = $contracts } | ConvertTo-Json -Depth 12) -replace "`r`n", "`n") + "`n", $utf8)
Write-Output "Generated terrain compatibility for Minecraft $mc ($($terrain.Count) terrain identities; sulfur cubes: $cubes)"
