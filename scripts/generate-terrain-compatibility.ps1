# Isolated resource generator: never regenerate recipes or existing family tags.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$projectPath = (Resolve-Path -LiteralPath $ProjectRoot).Path
$properties = Get-Content -LiteralPath (Join-Path $projectPath 'gradle.properties')
$mc = ($properties | Where-Object { $_ -match '^minecraft_version=' }) -replace '^minecraft_version=', ''
$singular = $mc -notin @('1.19.4', '1.20.1', '1.20.6')
$cubes = $mc -in @('26.2', '26.3')
$bats = $mc -in @('1.21.11', '26.1.2', '26.2', '26.3')
$blockDir = if ($singular) { 'block' } else { 'blocks' }
$itemDir = if ($singular) { 'item' } else { 'items' }
$provider = Get-Content -Raw -LiteralPath (Join-Path $projectPath 'src/main/resources/data/mineralogy/orespawn/provider.json') | ConvertFrom-Json
$outputs = @($provider.rocks.PSObject.Properties | ForEach-Object { $_.Value.block } | Sort-Object -Unique)
$aliases = @($provider.profile_defaults.worldgen_aliases.PSObject.Properties | ForEach-Object { $_.Name })
$terrain = @(@($outputs) + @($aliases) | Sort-Object -Unique)
if ($outputs.Count -ne 32 -or $aliases.Count -ne 5 -or $terrain.Count -ne 37) { throw 'Unexpected provider terrain contract' }
$families = @('andesite','basalt','diorite','granite','rhyolite','pegmatite','diabase','gabbro','peridotite','basaltic_glass','scoria','tuff','shale','conglomerate','dolomite','limestone','siltstone','marble','slate','schist','gneiss','phyllite','amphibolite','hornfels','quartzite','novaculite','rock_salt')
$utf8 = [System.Text.UTF8Encoding]::new($false)
function Write-Tag([string]$RelativePath, [object[]]$Values) {
    $destination = Join-Path $projectPath ('src/main/resources/data/' + $RelativePath)
    [System.IO.Directory]::CreateDirectory((Split-Path $destination -Parent)) | Out-Null
    $json = [ordered]@{ replace = $false; values = @($Values) } | ConvertTo-Json -Depth 8
    [System.IO.File]::WriteAllText($destination, ($json -replace "`r`n", "`n") + "`n", $utf8)
}
Write-Tag "mineralogy/tags/$blockDir/terrain_rocks.json" $terrain
Write-Tag "mineralogy/tags/$itemDir/terrain_rocks.json" $terrain
foreach ($tag in @('sculk_replaceable','dripstone_replaceable_blocks','moss_replaceable')) {
    Write-Tag "minecraft/tags/$blockDir/$tag.json" @('#mineralogy:terrain_rocks')
}
if ($bats) {
    Write-Tag "minecraft/tags/$blockDir/bats_spawnable_on.json" @('#mineralogy:terrain_rocks')
}
if ($cubes) {
    $stones = @($families | ForEach-Object { "mineralogy:$_" }) + @('mineralogy:chalk','mineralogy:chert','mineralogy:gypsum','mineralogy:pumice')
    foreach ($suffix in @('_smooth','_brick','_smooth_brick')) {
        foreach ($family in $families) { $stones += [ordered]@{ id = "mineralogy:$family$suffix"; required = $false } }
    }
    Write-Tag "mineralogy/tags/$itemDir/sulfur_cube_stones.json" $stones
    Write-Tag "minecraft/tags/$itemDir/sulfur_cube_archetype/slow_bouncy.json" @('#mineralogy:sulfur_cube_stones')
}
Write-Output "Generated terrain compatibility for Minecraft $mc ($($terrain.Count) terrain identities; sulfur cubes: $cubes)"
