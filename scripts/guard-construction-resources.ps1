# Skip resources which name construction items disabled before registry binding.
# Only native conditions change; recipe, advancement and loot payloads stay intact.
[CmdletBinding()]
param([string]$ProjectRoot = (Split-Path $PSScriptRoot -Parent))
$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $ProjectRoot).Path
$data = Join-Path $root 'src/main/resources/data'
$utf8 = [Text.UTF8Encoding]::new($false)
$optional = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
foreach ($file in Get-ChildItem -LiteralPath "$data/mineralogy/loot_table/blocks" -Filter '*.json') {
    $loot = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
    foreach ($id in $loot.pools.entries.name) {
        if ($id -match '^mineralogy:(.+)(_smooth|_brick|_slab|_stairs|_wall|_furnace|_relief_.+)$') {
            [void]$optional.Add($id)
        }
    }
}
if ($optional.Count -ne 864) { throw "Expected 864 optional construction items, found $($optional.Count)" }
function Find-OptionalItems($node, $found) {
    if ($node -is [string]) {
        if ($optional.Contains($node)) { [void]$found.Add($node) }
    } elseif ($node -is [Collections.IDictionary]) {
        foreach ($key in $node.Keys) {
            if ($key -ne 'neoforge:conditions') { Find-OptionalItems $node[$key] $found }
        }
    } elseif ($node -is [Collections.IEnumerable]) {
        foreach ($child in $node) { Find-OptionalItems $child $found }
    }
}
function Flatten-And($condition) {
    if ($null -eq $condition) { return }
    if ($condition.type -eq 'neoforge:and') {
        foreach ($child in $condition.items) { Flatten-And $child }
    } else { $condition }
}
function Guard-Resource($file, $node, $members) {
    if (-not $members.Count) { return $false }
    $existing = @($node['neoforge:conditions'] | ForEach-Object { Flatten-And $_ })
    $other = @($existing | Where-Object { $_.type -ne 'neoforge:item_exists' })
    foreach ($condition in $existing | Where-Object { $_.type -eq 'neoforge:item_exists' }) {
        if (-not $optional.Contains($condition.item)) {
            throw "Review unexpected registry condition in $($file.FullName)"
        }
        [void]$members.Add($condition.item)
    }
    $guards = @($members | Sort-Object | ForEach-Object {
        [ordered]@{ type = 'neoforge:item_exists'; item = $_ }
    }) + $other
    $result = [ordered]@{ 'neoforge:conditions' = $guards }
    foreach ($key in $node.Keys) {
        if ($key -ne 'neoforge:conditions') { $result[$key] = $node[$key] }
    }
    [IO.File]::WriteAllText($file.FullName, (($result | ConvertTo-Json -Depth 30) -replace "`r`n", "`n") + "`n", $utf8)
    return $true
}
$recipes = 0; $advancements = 0
$contracts = [Collections.Generic.List[object]]::new()
foreach ($namespace in @('mineralogy', 'minecraft')) {
    foreach ($file in Get-ChildItem -LiteralPath "$data/$namespace/recipe" -Filter '*.json') {
        $recipe = Get-Content -Raw -LiteralPath $file.FullName | ConvertFrom-Json -AsHashtable
        $members = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
        Find-OptionalItems $recipe $members
        $advFile = @()
        if ($namespace -eq 'mineralogy') {
            $path = "$data/mineralogy/advancement/recipes/$($file.Name)"
            if (Test-Path -LiteralPath $path) { $advFile = @(Get-Item -LiteralPath $path) }
        } elseif ($members.Count) {
            $path = "$data/minecraft/advancement/recipes/building_blocks/$($file.Name)"
            if (Test-Path -LiteralPath $path) { $advFile = @(Get-Item -LiteralPath $path) }
        }
        foreach ($advance in $advFile) {
            $advancement = Get-Content -Raw -LiteralPath $advance.FullName | ConvertFrom-Json -AsHashtable
            Find-OptionalItems $advancement $members
            if (Guard-Resource $advance $advancement $members) { $advancements++ }
        }
        if (Guard-Resource $file $recipe $members) {
            $recipes++
            $contracts.Add([ordered]@{ recipe = "${namespace}:$($file.BaseName)";
                advancement = $(if ($advFile.Count) { if ($namespace -eq 'mineralogy') { "mineralogy:recipes/$($file.BaseName)" } else { "minecraft:recipes/building_blocks/$($file.BaseName)" } } else { $null });
                items = @($members | Sort-Object) })
        }
    }
}
& "$PSScriptRoot/guard-construction-loot.ps1" -ProjectRoot $root
[IO.File]::WriteAllText("$root/src/test/resources/construction-resource-contract.json",
    (([ordered]@{ items = @($optional | Sort-Object); resources = @($contracts.ToArray()) } | ConvertTo-Json -Depth 20) -replace "`r`n", "`n") + "`n", $utf8)
Write-Output "Registered-item guards: $recipes recipes, $advancements advancements; enabled payloads unchanged"
