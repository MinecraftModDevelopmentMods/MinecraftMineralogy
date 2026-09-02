[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$recipeRoot = Join-Path $projectRoot 'src\main\resources\data\mineralogy\recipes'
$advancementRoot = Join-Path $projectRoot 'src\main\resources\data\mineralogy\advancements\recipes'
$minecraftRecipeRoot = Join-Path $projectRoot 'src\main\resources\data\minecraft\recipes'
$minecraftAdvancementRoot = Join-Path $projectRoot 'src\main\resources\data\minecraft\advancements\recipes'
$itemTagRoot = Join-Path $projectRoot 'src\main\resources\data\mineralogy\tags\items'
$blockTagRoot = Join-Path $projectRoot 'src\main\resources\data\mineralogy\tags\blocks'
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$generatedNames = New-Object 'System.Collections.Generic.HashSet[string]'
$unlockSources = @{}

$families = @(
    'andesite', 'basalt', 'diorite', 'granite', 'rhyolite', 'pegmatite',
    'diabase', 'gabbro', 'peridotite', 'basaltic_glass', 'scoria', 'tuff',
    'shale', 'conglomerate', 'dolomite', 'limestone', 'siltstone', 'marble',
    'slate', 'schist', 'gneiss', 'phyllite', 'amphibolite', 'hornfels',
    'quartzite', 'novaculite', 'rock_salt'
)

$colors = @(
    'black', 'red', 'green', 'brown', 'blue', 'purple', 'cyan', 'silver',
    'gray', 'pink', 'lime', 'yellow', 'light_blue', 'magenta', 'orange', 'white'
)

# Minecraft owns these full-block identities in 1.20.6. Mineralogy keeps its
# legacy blocks registered, but recipes may accept either identity where doing
# so cannot compete with a native recipe.
$nativeFullBlocks = @{
    andesite = @{ raw = 'minecraft:andesite'; smooth = 'minecraft:polished_andesite' }
    basalt = @{ raw = 'minecraft:basalt'; smooth = 'minecraft:polished_basalt' }
    diorite = @{ raw = 'minecraft:diorite'; smooth = 'minecraft:polished_diorite' }
    granite = @{ raw = 'minecraft:granite'; smooth = 'minecraft:polished_granite' }
    tuff = @{ raw = 'minecraft:tuff' }
}

$nativeConstructionForms = @{
    andesite = @{ raw = @('stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
    diorite = @{ raw = @('stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
    granite = @{ raw = @('stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
}

$nativeExactRecipeInputs = @{
    # Raw-to-smooth remains exact because the native polished override uses
    # the same block-plus-sand route. Native construction forms remain native.
    andesite = @{ raw = @('smooth', 'stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
    basalt = @{ raw = @('smooth') }
    diorite = @{ raw = @('smooth', 'stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
    granite = @{ raw = @('smooth', 'stairs', 'slab', 'wall'); smooth = @('stairs', 'slab') }
}

function ItemId([string] $path) {
    return "mineralogy:$path"
}

function ItemCondition([string] $item) {
    return [ordered]@{ type = 'forge:item_exists'; item = $item }
}

function ConfigCondition([string] $flag) {
    return [ordered]@{ type = 'mineralogy:config'; flag = $flag }
}

function NotCondition([System.Collections.IDictionary] $condition) {
    return [ordered]@{ type = 'forge:not'; value = $condition }
}

function CombinedCondition([object[]] $conditions) {
    $present = @($conditions | Where-Object { $null -ne $_ })
    if ($present.Count -eq 0) { return $null }
    if ($present.Count -eq 1) { return $present[0] }
    return [ordered]@{ type = 'forge:and'; values = $present }
}

function ItemTagNotEmptyCondition([string] $tag) {
    return [ordered]@{
        type = 'forge:not'
        value = [ordered]@{ type = 'forge:tag_empty'; tag = $tag }
    }
}

function ItemIngredient([string] $item, [object] $data = $null) {
    if ($null -ne $data) {
        if ($item -eq 'minecraft:sand') {
            $item = if ([int]$data -eq 1) { 'minecraft:red_sand' } else { 'minecraft:sand' }
        }
        elseif ($item -eq 'minecraft:coal' -and [int]$data -eq 1) {
            $item = 'minecraft:charcoal'
        }
        elseif ($item -eq 'minecraft:dye') {
            $dyes = @('ink_sac', 'red_dye', 'green_dye', 'cocoa_beans', 'lapis_lazuli',
                'purple_dye', 'cyan_dye', 'light_gray_dye', 'gray_dye', 'pink_dye',
                'lime_dye', 'yellow_dye', 'light_blue_dye', 'magenta_dye',
                'orange_dye', 'bone_meal')
            $item = "minecraft:$($dyes[[int]$data])"
        }
    }
    $ingredient = [ordered]@{ item = $item }
    return $ingredient
}

function AdvancementPredicate([object] $ingredient) {
    if ($ingredient -is [System.Collections.IDictionary]) {
        if ($ingredient.Contains('item')) {
            return [ordered]@{ items = [string]$ingredient['item'] }
        }
        if ($ingredient.Contains('tag')) {
            return [ordered]@{ items = "#$([string]$ingredient['tag'])" }
        }
        if ($ingredient.Contains('items')) {
            return [ordered]@{ items = $ingredient['items'] }
        }
    }
    if ($null -ne $ingredient.PSObject.Properties['item']) {
        return [ordered]@{ items = [string]$ingredient.item }
    }
    if ($null -ne $ingredient.PSObject.Properties['tag']) {
        return [ordered]@{ items = "#$([string]$ingredient.tag)" }
    }
    if ($null -ne $ingredient.PSObject.Properties['items']) {
        return [ordered]@{ items = $ingredient.items }
    }
    throw "Cannot convert recipe ingredient into a Minecraft 1.20.6 advancement predicate"
}

function OreIngredient([string] $ore) {
    if ($ore -eq 'sand') { return [ordered]@{ tag = 'forge:sand' } }
    if ($ore -eq 'paper') { return [ordered]@{ tag = 'forge:paper' } }
    if ($ore -eq 'drywallWhite') { return [ordered]@{ tag = 'mineralogy:drywall/white' } }
    if ($ore -eq 'lampRocksalt') { return [ordered]@{ tag = 'forge:lamps/rock_salt' } }
    if ($ore -match '^dust(.+)$') {
        $name = $Matches[1] -replace 'Rock_salt', 'rock_salt'
        return [ordered]@{ tag = "forge:dusts/$($name.ToLowerInvariant())" }
    }
    if ($ore -match '^block(.+)$') {
        $name = $Matches[1] -replace 'Rocksalt', 'rock_salt'
        return [ordered]@{ tag = "forge:storage_blocks/$($name.ToLowerInvariant())" }
    }
    if ($ore -match '^stone(.+?)(SmoothBrick|Smooth|Brick)?$') {
        $family = Convert-CamelToSnake $Matches[1]
        $finish = if ($Matches[2]) { '/' + (Convert-CamelToSnake $Matches[2]) } else { '' }
        return [ordered]@{ tag = "mineralogy:stones/$family$finish" }
    }
    if ($ore -match '^slab(.+?)(SmoothBrick|Smooth|Brick)?$') {
        $family = Convert-CamelToSnake $Matches[1]
        $finish = if ($Matches[2]) { '/' + (Convert-CamelToSnake $Matches[2]) } else { '' }
        return [ordered]@{ tag = "mineralogy:slabs/$family$finish" }
    }
    throw "No Minecraft 1.20.6 tag mapping for legacy OreDictionary key $ore"
}

function Convert-CamelToSnake([string] $value) {
    return (($value -creplace '([a-z0-9])([A-Z])', '$1_$2').ToLowerInvariant())
}

function RecipeResult([string] $item, [int] $count) {
    return [ordered]@{ id = $item; count = $count }
}

function VanillaResult([string] $item, [int] $count = 1) {
    $result = [ordered]@{ id = $item }
    if ($count -ne 1) { $result.count = $count }
    return $result
}

function CraftingCategoryForResult([string] $result) {
    $path = ($result -split ':', 2)[1]
    if ($result -in @(
            'minecraft:gunpowder',
            'mineralogy:fertilizer',
            'mineralogy:crude_oil_bucket',
            'mineralogy:sulfur',
            'mineralogy:phosphorous',
            'mineralogy:nitrate'
        ) -or $path.EndsWith('_furnace')) {
        return 'misc'
    }
    return 'building'
}

function VanillaCraftingCategoryForResult([string] $result) {
    if ($result -in @(
            'minecraft:stone_axe',
            'minecraft:stone_hoe',
            'minecraft:stone_pickaxe',
            'minecraft:stone_shovel',
            'minecraft:stone_sword'
        )) {
        return 'equipment'
    }
    if ($result -in @(
            'minecraft:dispenser',
            'minecraft:dropper',
            'minecraft:lever',
            'minecraft:observer',
            'minecraft:piston'
        )) {
        return 'redstone'
    }
    if ($result -in @('minecraft:brewing_stand', 'minecraft:furnace')) {
        return 'misc'
    }
    if ($result.EndsWith('_armor_trim_smithing_template')) {
        return 'misc'
    }
    return 'building'
}

function VanillaShapedRecipe(
    [string[]] $pattern,
    [System.Collections.IDictionary] $key,
    [string] $result,
    [int] $count = 1
) {
    return [ordered]@{
        type = 'minecraft:crafting_shaped'
        category = VanillaCraftingCategoryForResult $result
        pattern = $pattern
        key = $key
        result = VanillaResult $result $count
        show_notification = $true
    }
}

function VanillaShapelessRecipe(
    [object[]] $ingredients,
    [string] $result,
    [int] $count = 1,
    [string] $group = ''
) {
    $recipe = [ordered]@{
        type = 'minecraft:crafting_shapeless'
        category = VanillaCraftingCategoryForResult $result
        ingredients = $ingredients
        result = VanillaResult $result $count
    }
    if (-not [string]::IsNullOrWhiteSpace($group)) {
        $recipe.Insert(1, 'group', $group)
    }
    return $recipe
}

function VanillaStonecuttingRecipe(
    [object] $ingredient,
    [string] $result,
    [int] $count
) {
    return [ordered]@{
        type = 'minecraft:stonecutting'
        ingredient = $ingredient
        result = VanillaResult $result $count
    }
}

function Write-ConditionalMinecraftRecipe(
    [string] $name,
    [System.Collections.IDictionary] $condition,
    [System.Collections.IDictionary] $enabledRecipe,
    [System.Collections.IDictionary] $fallbackRecipe
) {
    Write-Json (Join-Path $minecraftRecipeRoot "$name.json") ([ordered]@{
        type = 'forge:conditional'
        recipes = @(
            [ordered]@{ 'forge:condition' = $condition; recipe = $enabledRecipe },
            [ordered]@{ 'forge:condition' = (NotCondition $condition); recipe = $fallbackRecipe }
        )
    })
}

function ConditionsFor([string] $result, [object[]] $extra = @()) {
    return @($extra) + @((ItemCondition $result))
}

function Write-Json([string] $path, [System.Collections.IDictionary] $value) {
    $json = ($value | ConvertTo-Json -Depth 20) -replace "`r?`n", "`n"
    [System.IO.File]::WriteAllText($path, $json + "`n", $utf8NoBom)
}

function Write-Recipe([string] $name, [System.Collections.IDictionary] $recipe) {
    if (-not $generatedNames.Add($name)) {
        throw "Duplicate generated recipe name: $name"
    }
    Write-Json (Join-Path $recipeRoot "$name.json") $recipe
}

function Register-UnlockSource(
    [string] $name,
    [object] $sourceIngredient
) {
    if ($unlockSources.ContainsKey($name)) {
        throw "Duplicate recipe-book unlock source: $name"
    }
    $unlockSources[$name] = if ($sourceIngredient -is [string]) {
        ItemIngredient ([string]$sourceIngredient)
    }
    else {
        $sourceIngredient
    }
}

function Get-SandUnlockMode([string] $recipeName) {
    if ($recipeName -match '^(.+)_smooth$') {
        return 'normal'
    }
    if ($recipeName -match '^(.+)_(raw|brick)_(stairs|slab|wall)_polishing$' -or
            $recipeName -match '^(.+)_brick_block_polishing$') {
        return 'ore_dictionary'
    }
    return ''
}

function Add-SandUnlockCriteria(
    [System.Collections.IDictionary] $criteria,
    [string] $sandMode
) {
    if ([string]::IsNullOrWhiteSpace($sandMode)) {
        return
    }

    $criteria['has_sand'] = [ordered]@{
        trigger = 'minecraft:inventory_changed'
        conditions = [ordered]@{
            items = @([ordered]@{ items = @('minecraft:sand') })
        }
    }
    if ($sandMode -eq 'ore_dictionary') {
        $criteria['has_red_sand'] = [ordered]@{
            trigger = 'minecraft:inventory_changed'
            conditions = [ordered]@{
                items = @([ordered]@{ items = @('minecraft:red_sand') })
            }
        }
    }
}

function Get-UnlockRequirements([string] $sandMode) {
    $requirements = @()
    $requirements += ,@('has_the_recipe', 'has_rock')
    if ($sandMode -eq 'ore_dictionary') {
        $requirements += ,@('has_the_recipe', 'has_sand', 'has_red_sand')
    }
    elseif ($sandMode -eq 'normal') {
        $requirements += ,@('has_the_recipe', 'has_sand')
    }
    return ,$requirements
}

function Resolve-UnlockIngredient([object] $ingredient) {
    if ($null -ne $ingredient.item) {
        return [ordered]@{ item = [string]$ingredient.item }
    }
    if ($null -ne $ingredient.tag) {
        return [ordered]@{ tag = [string]$ingredient.tag }
    }
    return $null
}

function Infer-UnlockSource([string] $recipeName, [object] $recipe) {
    if ($unlockSources.ContainsKey($recipeName)) { return $unlockSources[$recipeName] }
    if ($null -ne $recipe.ingredients -and $recipe.ingredients.Count -gt 0) {
        return Resolve-UnlockIngredient $recipe.ingredients[0]
    }
    if ($null -ne $recipe.ingredient) { return Resolve-UnlockIngredient $recipe.ingredient }
    if ($null -ne $recipe.key) {
        $first = $recipe.key.PSObject.Properties | Select-Object -First 1
        if ($null -ne $first) { return Resolve-UnlockIngredient $first.Value }
    }
    return $null
}

function Matrix-Contains(
    [System.Collections.IDictionary] $matrix,
    [string] $family,
    [string] $finish,
    [string] $form
) {
    return $matrix.ContainsKey($family) -and
        $matrix[$family].ContainsKey($finish) -and
        $matrix[$family][$finish] -contains $form
}

function ConstructionFormIngredient(
    [string] $family,
    [string] $finish,
    [string] $form,
    [string] $mineralogyItem,
    [string] $familyOreName
) {
    if (Matrix-Contains $nativeExactRecipeInputs $family $finish $form) {
        return ItemIngredient $mineralogyItem
    }
    # Slabs historically use exact inputs. Widen only a native family whose
    # matching vanilla block has no target-native slab recipe (basalt here).
    if ($form -eq 'slab' -and -not $nativeFullBlocks.ContainsKey($family)) {
        return ItemIngredient $mineralogyItem
    }
    return OreIngredient $familyOreName
}

function NativeTagAliases(
    [string] $kind,
    [string] $family,
    [string] $finish
) {
    if (-not $nativeFullBlocks.ContainsKey($family)) {
        return @()
    }
    $nativeFinish = if ([string]::IsNullOrWhiteSpace($finish)) { 'raw' } else { $finish }
    if ($nativeFinish -notin @('raw', 'smooth')) {
        return @()
    }
    if (-not $nativeFullBlocks[$family].ContainsKey($nativeFinish)) {
        return @()
    }
    if ($kind -eq 'stones') {
        if ($family -eq 'basalt' -and $nativeFinish -eq 'smooth') {
            return @($nativeFullBlocks[$family][$nativeFinish], 'minecraft:smooth_basalt')
        }
        return @($nativeFullBlocks[$family][$nativeFinish])
    }
    if ($kind -eq 'slabs' -and
            (Matrix-Contains $nativeConstructionForms $family $nativeFinish 'slab')) {
        $prefix = if ($nativeFinish -eq 'smooth') { 'polished_' } else { '' }
        return @("minecraft:${prefix}${family}_slab")
    }
    return @()
}

function Write-ShapedRecipe(
    [string] $name,
    [string] $type,
    [string[]] $pattern,
    [System.Collections.IDictionary] $key,
    [string] $result,
    [int] $count,
    [object[]] $conditions
) {
    if ($type.StartsWith('forge:ore_')) { $type = 'minecraft:crafting_shaped' }
    $recipe = [ordered]@{
        type = $type
        category = CraftingCategoryForResult $result
        pattern = $pattern
        key = $key
        result = RecipeResult $result $count
        show_notification = $true
    }
    $condition = CombinedCondition $conditions
    if ($null -ne $condition) { $recipe.Insert(0, 'forge:condition', $condition) }
    Write-Recipe $name $recipe
}

function Write-ShapelessRecipe(
    [string] $name,
    [string] $type,
    [object[]] $ingredients,
    [string] $result,
    [int] $count,
    [object[]] $conditions
) {
    if ($type.StartsWith('forge:ore_')) { $type = 'minecraft:crafting_shapeless' }
    $recipe = [ordered]@{
        type = $type
        category = CraftingCategoryForResult $result
        ingredients = $ingredients
        result = RecipeResult $result $count
    }
    $condition = CombinedCondition $conditions
    if ($null -ne $condition) { $recipe.Insert(0, 'forge:condition', $condition) }
    Write-Recipe $name $recipe
}

function FamilyOreName([string] $prefix, [string] $family, [string] $suffix = '') {
    $material = $family.Substring(0, 1).ToUpperInvariant() + $family.Substring(1)
    return "$prefix$material$suffix"
}

function Write-BaseFamilyRecipes([string] $family) {
    $raw = ItemId $family
    $rawOre = FamilyOreName 'stone' $family
    $brick = ItemId "${family}_brick"
    $brickOre = FamilyOreName 'stone' $family 'Brick'
    $smooth = ItemId "${family}_smooth"
    $smoothOre = FamilyOreName 'stone' $family 'Smooth'
    $smoothBrick = ItemId "${family}_smooth_brick"
    $smoothBrickOre = FamilyOreName 'stone' $family 'SmoothBrick'

    Write-ShapelessRecipe "${family}_cobblestone" 'forge:ore_shapeless' @(
        (OreIngredient $rawOre), (OreIngredient $rawOre),
        (ItemIngredient 'minecraft:gravel'), (ItemIngredient 'minecraft:gravel')
    ) 'minecraft:cobblestone' 4 @()

    Write-ShapedRecipe "${family}_stairs" 'forge:ore_shaped' @('x  ', 'xx ', 'xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'raw' 'stairs' $raw $rawOre }) `
        (ItemId "${family}_stairs") 4 `
        (ConditionsFor (ItemId "${family}_stairs"))
    Write-ShapedRecipe "${family}_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'raw' 'slab' $raw $rawOre }) `
        (ItemId "${family}_slab") 6 `
        (ConditionsFor (ItemId "${family}_slab"))
    Write-ShapedRecipe "${family}_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_furnace") 1 (ConditionsFor (ItemId "${family}_furnace"))
    Write-ShapedRecipe "${family}_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'raw' 'wall' $raw $rawOre }) `
        (ItemId "${family}_wall") 6 `
        (ConditionsFor (ItemId "${family}_wall"))

    Write-ShapedRecipe "${family}_brick" 'forge:ore_shaped' @('xx', 'xx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'raw' 'brick' $raw $rawOre }) `
        $brick 4 (ConditionsFor $brick)
    Write-ShapedRecipe "${family}_brick_stairs" 'forge:ore_shaped' @('x  ', 'xx ', 'xxx') `
        ([ordered]@{ x = OreIngredient $brickOre }) (ItemId "${family}_brick_stairs") 4 `
        (ConditionsFor (ItemId "${family}_brick_stairs"))
    Write-ShapedRecipe "${family}_brick_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ItemIngredient $brick }) (ItemId "${family}_brick_slab") 6 `
        (ConditionsFor (ItemId "${family}_brick_slab"))
    Write-ShapedRecipe "${family}_brick_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family 'Brick'); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_brick_furnace") 1 (ConditionsFor (ItemId "${family}_brick_furnace"))
    Write-ShapedRecipe "${family}_brick_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = OreIngredient $brickOre }) (ItemId "${family}_brick_wall") 6 `
        (ConditionsFor (ItemId "${family}_brick_wall"))

    Write-ShapelessRecipe "${family}_smooth" 'forge:ore_shapeless' @(
        (ConstructionFormIngredient $family 'raw' 'smooth' $raw $rawOre),
        (ItemIngredient 'minecraft:sand' 0)
    ) $smooth 1 (ConditionsFor $smooth)
    Write-ShapedRecipe "${family}_smooth_stairs" 'forge:ore_shaped' @('x  ', 'xx ', 'xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'smooth' 'stairs' $smooth $smoothOre }) `
        (ItemId "${family}_smooth_stairs") 4 `
        (ConditionsFor (ItemId "${family}_smooth_stairs"))
    Write-ShapedRecipe "${family}_smooth_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'smooth' 'slab' $smooth $smoothOre }) `
        (ItemId "${family}_smooth_slab") 6 `
        (ConditionsFor (ItemId "${family}_smooth_slab"))
    Write-ShapedRecipe "${family}_smooth_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family 'Smooth'); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_smooth_furnace") 1 (ConditionsFor (ItemId "${family}_smooth_furnace"))
    Write-ShapedRecipe "${family}_smooth_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = ConstructionFormIngredient $family 'smooth' 'wall' $smooth $smoothOre }) `
        (ItemId "${family}_smooth_wall") 6 `
        (ConditionsFor (ItemId "${family}_smooth_wall"))

    Write-ShapedRecipe "${family}_smooth_brick" 'forge:ore_shaped' @('xx', 'xx') `
        ([ordered]@{ x = OreIngredient $smoothOre }) $smoothBrick 4 (ConditionsFor $smoothBrick)
    Write-ShapedRecipe "${family}_smooth_brick_stairs" 'forge:ore_shaped' @('x  ', 'xx ', 'xxx') `
        ([ordered]@{ x = OreIngredient $smoothBrickOre }) (ItemId "${family}_smooth_brick_stairs") 4 `
        (ConditionsFor (ItemId "${family}_smooth_brick_stairs"))
    Write-ShapedRecipe "${family}_smooth_brick_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ItemIngredient $smoothBrick }) (ItemId "${family}_smooth_brick_slab") 6 `
        (ConditionsFor (ItemId "${family}_smooth_brick_slab"))
    Write-ShapedRecipe "${family}_smooth_brick_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family 'SmoothBrick'); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_smooth_brick_furnace") 1 (ConditionsFor (ItemId "${family}_smooth_brick_furnace"))
    Write-ShapedRecipe "${family}_smooth_brick_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = OreIngredient $smoothBrickOre }) (ItemId "${family}_smooth_brick_wall") 6 `
        (ConditionsFor (ItemId "${family}_smooth_brick_wall"))
}

function Write-ReliefRecipes([string] $family) {
    $smooth = ItemId "${family}_smooth"
    $smoothOre = FamilyOreName 'stone' $family 'Smooth'
    $smoothIngredient = if ($nativeFullBlocks.ContainsKey($family)) {
        OreIngredient $smoothOre
    }
    else {
        ItemIngredient $smooth
    }
    $blank = ItemId "${family}_relief_blank"
    $left = ItemId "${family}_relief_left"

    Write-ShapedRecipe "${family}_relief_blank" 'minecraft:crafting_shaped' @('xxx', 'xxx', 'xxx') `
        ([ordered]@{ x = $smoothIngredient }) $blank 16 (ConditionsFor $blank)
    Write-ShapedRecipe "${family}_relief_cross" 'minecraft:crafting_shaped' @('x x', '   ', 'x x') `
        ([ordered]@{ x = ItemIngredient $blank }) (ItemId "${family}_relief_cross") 4 `
        (ConditionsFor (ItemId "${family}_relief_cross"))
    Write-ShapedRecipe "${family}_relief_hammer" 'minecraft:crafting_shaped' @('zxz', 'zyz', 'zzz') `
        ([ordered]@{ x = ItemIngredient $smooth; y = ItemIngredient 'minecraft:stick'; z = ItemIngredient $blank }) `
        (ItemId "${family}_relief_hammer") 7 (ConditionsFor (ItemId "${family}_relief_hammer"))
    Write-ShapedRecipe "${family}_relief_horizontal" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ItemIngredient $blank }) (ItemId "${family}_relief_horizontal") 3 `
        (ConditionsFor (ItemId "${family}_relief_horizontal"))
    Write-ShapedRecipe "${family}_relief_left" 'minecraft:crafting_shaped' @('x  ', ' x ', '  x') `
        ([ordered]@{ x = ItemIngredient $blank }) $left 3 `
        (ConditionsFor (ItemId "${family}_relief_left"))
    Write-ShapedRecipe "${family}_relief_plus" 'minecraft:crafting_shaped' @(' x ', 'xxx', ' x ') `
        ([ordered]@{ x = ItemIngredient $blank }) (ItemId "${family}_relief_plus") 5 `
        (ConditionsFor (ItemId "${family}_relief_plus"))
    Write-ShapelessRecipe "${family}_relief_right" 'minecraft:crafting_shapeless' `
        @((ItemIngredient $left), (ItemIngredient $left)) (ItemId "${family}_relief_right") 2 `
        (ConditionsFor (ItemId "${family}_relief_right"))
    Write-ShapedRecipe "${family}_relief_i" 'minecraft:crafting_shaped' @('xxx', ' x ', 'xxx') `
        ([ordered]@{ x = ItemIngredient $blank }) (ItemId "${family}_relief_i") 7 `
        (ConditionsFor (ItemId "${family}_relief_i"))
    Write-ShapedRecipe "${family}_relief_vertical" 'minecraft:crafting_shaped' @('x', 'x', 'x') `
        ([ordered]@{ x = ItemIngredient $blank }) (ItemId "${family}_relief_vertical") 3 `
        (ConditionsFor (ItemId "${family}_relief_vertical"))

    foreach ($tool in @('axe', 'hoe', 'pickaxe', 'sword')) {
        $ingredients = @()
        for ($index = 0; $index -lt 8; $index++) {
            $ingredients += ,(ItemIngredient $blank)
        }
        $ingredients += ,(ItemIngredient "minecraft:stone_$tool" 0)
        $result = ItemId "${family}_relief_${tool}"
        Write-ShapelessRecipe "${family}_relief_${tool}" 'minecraft:crafting_shapeless' `
            $ingredients $result 8 (ConditionsFor $result)
    }
}

function Write-ConstructionRecipes([string] $family) {
    $forms = [ordered]@{
        raw = [ordered]@{ block = ItemId $family; stairs = ItemId "${family}_stairs"; slab = ItemId "${family}_slab"; wall = ItemId "${family}_wall" }
        brick = [ordered]@{ block = ItemId "${family}_brick"; stairs = ItemId "${family}_brick_stairs"; slab = ItemId "${family}_brick_slab"; wall = ItemId "${family}_brick_wall" }
        polished = [ordered]@{ block = ItemId "${family}_smooth"; stairs = ItemId "${family}_smooth_stairs"; slab = ItemId "${family}_smooth_slab"; wall = ItemId "${family}_smooth_wall" }
        polished_brick = [ordered]@{ block = ItemId "${family}_smooth_brick"; stairs = ItemId "${family}_smooth_brick_stairs"; slab = ItemId "${family}_smooth_brick_slab"; wall = ItemId "${family}_smooth_brick_wall" }
    }

    foreach ($finish in $forms.Keys) {
        $form = $forms[$finish]
        Write-ShapelessRecipe "${family}_${finish}_slab_recombination" 'minecraft:crafting_shapeless' `
            @((ItemIngredient $form.slab), (ItemIngredient $form.slab)) $form.block 1 `
            @((ItemCondition $form.slab), (ItemCondition $form.block))
        Register-UnlockSource "${family}_${finish}_slab_recombination" $form.slab
    }

    foreach ($conversion in @(
        [ordered]@{ name = 'raw'; source = $forms.raw; target = $forms.brick },
        [ordered]@{ name = 'polished'; source = $forms.polished; target = $forms.polished_brick }
    )) {
        foreach ($shape in @('stairs', 'slab', 'wall')) {
            $plural = if ($shape -eq 'slab') { 'slabs' } elseif ($shape -eq 'wall') { 'walls' } else { 'stairs' }
            Write-ShapedRecipe "${family}_$($conversion.name)_${plural}_to_brick" 'minecraft:crafting_shaped' `
                @('xx', 'xx') ([ordered]@{ x = ItemIngredient $conversion.source[$shape] }) `
                $conversion.target[$shape] 4 `
                @((ItemCondition $conversion.source[$shape]), (ItemCondition $conversion.target[$shape]))
            Register-UnlockSource "${family}_$($conversion.name)_${plural}_to_brick" `
                $conversion.source[$shape]
        }
    }

    foreach ($polishing in @(
        [ordered]@{ name = 'raw'; source = $forms.raw; target = $forms.polished },
        [ordered]@{ name = 'brick'; source = $forms.brick; target = $forms.polished_brick }
    )) {
        foreach ($shape in @('stairs', 'slab', 'wall')) {
            Write-ShapelessRecipe "${family}_$($polishing.name)_${shape}_polishing" 'forge:ore_shapeless' `
                @((ItemIngredient $polishing.source[$shape]), (OreIngredient 'sand')) `
                $polishing.target[$shape] 1 `
                @((ItemCondition $polishing.source[$shape]), (ItemCondition $polishing.target[$shape]))
            Register-UnlockSource "${family}_$($polishing.name)_${shape}_polishing" `
                $polishing.source[$shape]
        }
    }

    Write-ShapelessRecipe "${family}_brick_block_polishing" 'forge:ore_shapeless' `
        @((ItemIngredient $forms.brick.block), (OreIngredient 'sand')) $forms.polished_brick.block 1 `
        @((ItemCondition $forms.brick.block), (ItemCondition $forms.polished_brick.block))
    Register-UnlockSource "${family}_brick_block_polishing" $forms.brick.block
}

function Write-GlobalRecipes() {
    $drywallCondition = ConfigCondition 'ENABLE_DRYWALLS'
    for ($metadata = 0; $metadata -lt $colors.Count; $metadata++) {
        $result = ItemId "drywall_$($colors[$metadata])"
        Write-ShapelessRecipe "drywall_$($colors[$metadata])" 'forge:ore_shapeless' @(
            (OreIngredient 'drywallWhite'), (ItemIngredient 'minecraft:dye' $metadata)
        ) $result 1 (ConditionsFor $result @($drywallCondition))
        Register-UnlockSource "drywall_$($colors[$metadata])" (ItemId 'drywall_white')
    }

    $dustCondition = ConfigCondition 'ENABLE_MINERAL_DUSTS'
    $gunpowderTail = @((OreIngredient 'dustNitrate'), (OreIngredient 'dustSulfur'))
    Write-ShapelessRecipe 'gunpowder_from_sugar' 'forge:ore_shapeless' `
        (@((ItemIngredient 'minecraft:sugar')) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)
    Register-UnlockSource 'gunpowder_from_sugar' (ItemId 'nitrate_dust')
    Write-ShapelessRecipe 'gunpowder_from_charcoal' 'forge:ore_shapeless' `
        (@((ItemIngredient 'minecraft:coal' 1)) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)
    Register-UnlockSource 'gunpowder_from_charcoal' (ItemId 'nitrate_dust')
    Write-ShapelessRecipe 'gunpowder_from_carbon_dust' 'forge:ore_shapeless' `
        (@((OreIngredient 'dustCarbon')) + $gunpowderTail) 'minecraft:gunpowder' 4 `
        @($dustCondition, (ItemTagNotEmptyCondition 'forge:dusts/carbon'))
    Register-UnlockSource 'gunpowder_from_carbon_dust' (ItemId 'nitrate_dust')
    Write-ShapelessRecipe 'gunpowder_from_coal_dust' 'forge:ore_shapeless' `
        (@((OreIngredient 'dustCoal')) + $gunpowderTail) 'minecraft:gunpowder' 4 `
        @($dustCondition, (ItemTagNotEmptyCondition 'forge:dusts/coal'))
    Register-UnlockSource 'gunpowder_from_coal_dust' (ItemId 'nitrate_dust')

    Write-ShapelessRecipe 'mineralfertilizer' 'forge:ore_shapeless' @(
        (OreIngredient 'dustNitrate'), (OreIngredient 'dustPhosphorous')
    ) (ItemId 'mineral_fertilizer') 1 `
        (ConditionsFor (ItemId 'mineral_fertilizer') @((ConfigCondition 'ENABLE_MINERAL_FERTILIZER')))
    Register-UnlockSource 'mineralfertilizer' (ItemId 'nitrate_dust')

    Write-ShapelessRecipe 'cobblestone' 'minecraft:crafting_shapeless' @(
        (ItemIngredient 'minecraft:stone' 0), (ItemIngredient 'minecraft:stone' 0),
        (ItemIngredient 'minecraft:gravel'), (ItemIngredient 'minecraft:gravel')
    ) 'minecraft:cobblestone' 4 @()
    Register-UnlockSource 'cobblestone' 'minecraft:stone'

    foreach ($storage in @(
        [ordered]@{ name = 'gypsum'; dust = 'dustGypsum'; blockOre = 'blockGypsum'; dustItem = 'gypsum_dust'; blockItem = 'gypsum' },
        [ordered]@{ name = 'chalk'; dust = 'dustChalk'; blockOre = 'blockChalk'; dustItem = 'chalk_dust'; blockItem = 'chalk' },
        [ordered]@{ name = 'rock_salt'; dust = 'dustRock_salt'; blockOre = 'blockRocksalt'; dustItem = 'rock_salt_dust'; blockItem = 'rock_salt' }
    )) {
        $block = ItemId $storage.blockItem
        Write-ShapedRecipe $storage.name 'forge:ore_shaped' @('xx', 'xx') `
            ([ordered]@{ x = OreIngredient $storage.dust }) $block 1 (ConditionsFor $block)
        Register-UnlockSource $storage.name (ItemId $storage.dustItem)
        Write-ShapelessRecipe "$($storage.name)_dust" 'forge:ore_shapeless' @(
            (OreIngredient $storage.blockOre)
        ) (ItemId $storage.dustItem) 4 (ConditionsFor (ItemId $storage.dustItem))
        Register-UnlockSource "$($storage.name)_dust" $block
    }

    Write-ShapedRecipe 'drywall' 'forge:ore_shaped' @('pgp', 'pgp', 'pgp') `
        ([ordered]@{ p = OreIngredient 'paper'; g = OreIngredient 'dustGypsum' }) `
        (ItemId 'drywall_white') 3 `
        (ConditionsFor (ItemId 'drywall_white') @($drywallCondition))
    Register-UnlockSource 'drywall' (ItemId 'gypsum_dust')

    $lampCondition = ConfigCondition 'ENABLE_ROCK_SALT_LAMPS'
    Write-ShapelessRecipe 'rocksaltlamp' 'minecraft:crafting_shapeless' @(
        (ItemIngredient (ItemId 'rock_salt')), (ItemIngredient 'minecraft:torch'),
        (ItemIngredient 'minecraft:iron_ingot')
    ) (ItemId 'rocksaltlamp') 1 `
        (ConditionsFor (ItemId 'rocksaltlamp') @($lampCondition))
    Register-UnlockSource 'rocksaltlamp' (ItemId 'rock_salt')
    Write-ShapedRecipe 'rocksaltstreetlamp' 'forge:ore_shaped' @('x', 'y', 'y') `
        ([ordered]@{ x = OreIngredient 'lampRocksalt'; y = ItemIngredient 'minecraft:iron_ingot' }) `
        (ItemId 'rocksaltstreetlamp') 1 `
        (ConditionsFor (ItemId 'rocksaltstreetlamp') @($lampCondition))
    Register-UnlockSource 'rocksaltstreetlamp' (ItemId 'rocksaltlamp')

    foreach ($mineral in @('sulfur', 'phosphorous', 'nitrate')) {
        $material = $mineral.Substring(0, 1).ToUpperInvariant() + $mineral.Substring(1)
        $block = ItemId "${mineral}_block"
        $dust = ItemId "${mineral}_dust"
        Write-ShapedRecipe "${mineral}_block" 'minecraft:crafting_shaped' @('xxx', 'xxx', 'xxx') `
            ([ordered]@{ x = ItemIngredient $dust }) $block 1 `
            (ConditionsFor $block @($dustCondition))
        Register-UnlockSource "${mineral}_block" $dust
        Write-ShapelessRecipe "${mineral}_dust" 'forge:ore_shapeless' @(
            (OreIngredient "block$material")
        ) $dust 9 (ConditionsFor $dust @($dustCondition))
        Register-UnlockSource "${mineral}_dust" $block
    }
}

function Ensure-MissingRecipeAdvancements() {
    $recipeFiles = Get-ChildItem -LiteralPath $recipeRoot -Filter '*.json' |
        Where-Object { -not $_.Name.StartsWith('_') }
    $created = 0
    foreach ($recipeFile in $recipeFiles) {
        $recipeName = $recipeFile.BaseName
        $advancementFile = Join-Path $advancementRoot "$recipeName.json"
        if (Test-Path -LiteralPath $advancementFile) {
            continue
        }
        $recipe = Get-Content -LiteralPath $recipeFile.FullName -Raw | ConvertFrom-Json
        $sourceIngredient = Infer-UnlockSource $recipeName $recipe
        if ($null -eq $sourceIngredient) {
            throw "Generated recipe $recipeName has no direct recipe-book unlock source"
        }
        $criteria = [ordered]@{
            has_the_recipe = [ordered]@{
                trigger = 'minecraft:recipe_unlocked'
                conditions = [ordered]@{ recipe = "mineralogy:$recipeName" }
            }
            has_rock = [ordered]@{
                trigger = 'minecraft:inventory_changed'
                conditions = [ordered]@{
                    items = @((AdvancementPredicate $sourceIngredient))
                }
            }
        }
        $sandMode = Get-SandUnlockMode $recipeName
        Add-SandUnlockCriteria $criteria $sandMode
        $requirements = Get-UnlockRequirements $sandMode

        $generatedAdvancement = [ordered]@{}
        if ($null -ne $recipe.'forge:condition') {
            $generatedAdvancement['forge:condition'] = $recipe.'forge:condition'
        }
        $generatedAdvancement.rewards = [ordered]@{ recipes = @("mineralogy:$recipeName") }
        $generatedAdvancement.criteria = $criteria
        $generatedAdvancement.requirements = $requirements
        $generatedAdvancement.sends_telemetry_event = $false
        Write-Json $advancementFile $generatedAdvancement
        $created++
    }
    return $created
}

function Synchronize-AdvancementConditions() {
    $files = Get-ChildItem -LiteralPath $advancementRoot -Filter '*.json'
    foreach ($file in $files) {
        $advancement = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
        $recipeId = [string]$advancement.rewards.recipes[0]
        if (-not $recipeId.StartsWith('mineralogy:')) {
            throw "Unexpected recipe reward in $($file.FullName): $recipeId"
        }
        $recipeName = $recipeId.Substring('mineralogy:'.Length)
        $recipeFile = Join-Path $recipeRoot "$recipeName.json"
        if (-not (Test-Path -LiteralPath $recipeFile)) {
            throw "Advancement $($file.Name) references missing generated recipe $recipeId"
        }
        $recipe = Get-Content -LiteralPath $recipeFile -Raw | ConvertFrom-Json
        if ($recipeName -match '^(.+)_relief_(.+)$') {
            $family = $Matches[1]
            $relief = $Matches[2]
            if ($relief -eq 'blank') {
                $sourceIngredient = if ($nativeFullBlocks.ContainsKey($family)) {
                    OreIngredient (FamilyOreName 'stone' $family 'Smooth')
                }
                else {
                    ItemIngredient "mineralogy:${family}_smooth"
                }
            }
            elseif ($relief -eq 'right') {
                $sourceIngredient = ItemIngredient "mineralogy:${family}_relief_left"
            }
            else {
                $sourceIngredient = ItemIngredient "mineralogy:${family}_relief_blank"
            }
            $advancement.criteria.has_rock.conditions.items = @((AdvancementPredicate $sourceIngredient))
        }
        if ($null -eq $advancement.criteria.has_the_recipe -or $null -eq $advancement.criteria.has_rock) {
            throw "Advancement $($file.Name) must have self-recipe and direct-input criteria"
        }
        $criteria = [ordered]@{}
        foreach ($criterion in $advancement.criteria.PSObject.Properties) {
            if ($criterion.Name -notin @('has_material_recipe', 'has_sand', 'has_red_sand', 'has_furnace')) {
                $criteria[$criterion.Name] = $criterion.Value
            }
        }
        $sandMode = Get-SandUnlockMode $recipeName
        Add-SandUnlockCriteria $criteria $sandMode
        $requirements = Get-UnlockRequirements $sandMode

        $ordered = [ordered]@{}
        if ($null -ne $recipe.'forge:condition') {
            $ordered['forge:condition'] = $recipe.'forge:condition'
        }
        foreach ($property in $advancement.PSObject.Properties) {
            if ($property.Name -eq 'criteria') {
                $ordered[$property.Name] = $criteria
            }
            elseif ($property.Name -eq 'requirements') {
                $ordered[$property.Name] = $requirements
            }
            elseif ($property.Name -ne 'forge:condition') {
                $ordered[$property.Name] = $property.Value
            }
        }
        $ordered.sends_telemetry_event = $false
        Write-Json $file.FullName $ordered
    }
    return $files.Count
}

function Prepare-TargetDirectories() {
    New-Item -ItemType Directory -Force -Path $recipeRoot | Out-Null
    New-Item -ItemType Directory -Force -Path $advancementRoot | Out-Null
    New-Item -ItemType Directory -Force -Path $minecraftRecipeRoot | Out-Null
    New-Item -ItemType Directory -Force -Path $minecraftAdvancementRoot | Out-Null

    foreach ($file in Get-ChildItem -LiteralPath $recipeRoot -Filter '*.json') {
        $recipe = Get-Content -LiteralPath $file.FullName -Raw | ConvertFrom-Json
        if ($recipe.type -eq 'minecraft:smelting') {
            if ($null -eq $recipe.ingredient.item) {
                throw "Smelting recipe $($file.Name) has no direct item ingredient"
            }
            Register-UnlockSource $file.BaseName ([string]$recipe.ingredient.item)
            $orderedRecipe = [ordered]@{
                type = [string]$recipe.type
                category = 'blocks'
            }
            foreach ($property in $recipe.PSObject.Properties) {
                if ($property.Name -notin @('type', 'category')) {
                    if ($property.Name -eq 'result' -and $property.Value -is [string]) {
                        $orderedRecipe[$property.Name] = [ordered]@{ id = [string]$property.Value }
                    }
                    else {
                        $orderedRecipe[$property.Name] = $property.Value
                    }
                }
            }
            Write-Json $file.FullName $orderedRecipe
        }
        else {
            Remove-Item -LiteralPath $file.FullName
        }
    }
    foreach ($file in Get-ChildItem -LiteralPath $advancementRoot -Filter '*.json') {
        Remove-Item -LiteralPath $file.FullName
    }
    foreach ($file in Get-ChildItem -LiteralPath $minecraftRecipeRoot -Filter '*.json') {
        Remove-Item -LiteralPath $file.FullName
    }
    foreach ($file in Get-ChildItem -LiteralPath $minecraftAdvancementRoot -Filter '*.json' -Recurse) {
        Remove-Item -LiteralPath $file.FullName
    }
}

function Write-TargetTags() {
    foreach ($family in $families) {
        foreach ($finish in @('', 'brick', 'smooth', 'smooth_brick')) {
            $suffix = if ($finish) { "_$finish" } else { '' }
            $pathSuffix = if ($finish) { "/$finish" } else { '' }
            foreach ($kind in @('stones', 'slabs')) {
                $item = if ($kind -eq 'slabs') {
                    "mineralogy:${family}${suffix}_slab"
                }
                else {
                    "mineralogy:${family}${suffix}"
                }
                $destination = Join-Path $itemTagRoot "$kind\$family$pathSuffix.json"
                New-Item -ItemType Directory -Force -Path (Split-Path -Parent $destination) | Out-Null
                $values = @($item)
                $values += @(NativeTagAliases $kind $family $finish)
                Write-Json $destination ([ordered]@{ replace = $false; values = $values })
            }
        }

        $blockDestination = Join-Path $blockTagRoot "stones\$family.json"
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $blockDestination) | Out-Null
        $blockValues = @("mineralogy:$family")
        $blockValues += @(NativeTagAliases 'stones' $family '')
        Write-Json $blockDestination ([ordered]@{ replace = $false; values = $blockValues })
    }
}

function Write-CobblestoneRecipeTags() {
    $familyTags = @($families | ForEach-Object { "#mineralogy:stones/$_" })
    Write-Json (Join-Path $itemTagRoot 'cobblestone_equivalents.json') ([ordered]@{
        replace = $false
        values = @('#forge:cobblestone') + $familyTags
    })
    Write-Json (Join-Path $itemTagRoot 'stone_crafting_materials.json') ([ordered]@{
        replace = $false
        values = @('#minecraft:stone_crafting_materials') + $familyTags
    })
    Write-Json (Join-Path $itemTagRoot 'stone_tool_materials.json') ([ordered]@{
        replace = $false
        values = @('#minecraft:stone_tool_materials') + $familyTags
    })
}

function VanillaRecipeAdvancement(
    [string] $recipeName,
    [string] $criterionName,
    [System.Collections.IDictionary] $criterionIngredient
) {
    return [ordered]@{
        parent = 'minecraft:recipes/root'
        rewards = [ordered]@{ recipes = @("minecraft:$recipeName") }
        criteria = [ordered]@{
            $criterionName = [ordered]@{
                trigger = 'minecraft:inventory_changed'
                conditions = [ordered]@{ items = @((AdvancementPredicate $criterionIngredient)) }
            }
            has_the_recipe = [ordered]@{
                trigger = 'minecraft:recipe_unlocked'
                conditions = [ordered]@{ recipe = "minecraft:$recipeName" }
            }
        }
        requirements = ,@($criterionName, 'has_the_recipe')
        sends_telemetry_event = $false
    }
}

function Write-ConditionalMinecraftAdvancement(
    [string] $category,
    [string] $recipeName,
    [System.Collections.IDictionary] $condition,
    [string] $criterionName,
    [System.Collections.IDictionary] $enabledIngredient,
    [System.Collections.IDictionary] $fallbackIngredient
) {
    $directory = Join-Path $minecraftAdvancementRoot $category
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    Write-Json (Join-Path $directory "$recipeName.json") ([ordered]@{
        advancements = @(
            (Add-ConditionToAdvancement $condition `
                (VanillaRecipeAdvancement $recipeName $criterionName $enabledIngredient)),
            (Add-ConditionToAdvancement (NotCondition $condition) `
                (VanillaRecipeAdvancement $recipeName $criterionName $fallbackIngredient))
        )
    })
}

function Add-ConditionToAdvancement(
    [System.Collections.IDictionary] $condition,
    [System.Collections.IDictionary] $advancement
) {
    $result = [ordered]@{ 'forge:condition' = $condition }
    foreach ($entry in $advancement.GetEnumerator()) {
        $result[$entry.Key] = $entry.Value
    }
    return $result
}

function Write-CobblestoneRecipeOverrides() {
    $condition = ConfigCondition 'COBBLESTONE_EQUIVILENT'
    $enabledCobblestone = [ordered]@{ tag = 'mineralogy:cobblestone_equivalents' }
    $fallbackCobblestone = [ordered]@{ tag = 'forge:cobblestone' }
    $enabledCrafting = [ordered]@{ tag = 'mineralogy:stone_crafting_materials' }
    $fallbackCrafting = [ordered]@{ tag = 'minecraft:stone_crafting_materials' }
    $enabledTools = [ordered]@{ tag = 'mineralogy:stone_tool_materials' }
    $fallbackTools = [ordered]@{ tag = 'minecraft:stone_tool_materials' }

    Write-ConditionalMinecraftRecipe 'furnace' $condition `
        (VanillaShapedRecipe @('###', '# #', '###') ([ordered]@{ '#' = $enabledCrafting }) 'minecraft:furnace') `
        (VanillaShapedRecipe @('###', '# #', '###') ([ordered]@{ '#' = $fallbackCrafting }) 'minecraft:furnace')
    Write-ConditionalMinecraftRecipe 'brewing_stand' $condition `
        (VanillaShapedRecipe @(' B ', '###') ([ordered]@{
            B = ItemIngredient 'minecraft:blaze_rod'; '#' = $enabledCrafting
        }) 'minecraft:brewing_stand') `
        (VanillaShapedRecipe @(' B ', '###') ([ordered]@{
            B = ItemIngredient 'minecraft:blaze_rod'; '#' = $fallbackCrafting
        }) 'minecraft:brewing_stand')
    Write-ConditionalMinecraftRecipe 'lever' $condition `
        (VanillaShapedRecipe @('X', '#') ([ordered]@{
            '#' = $enabledCobblestone; X = ItemIngredient 'minecraft:stick'
        }) 'minecraft:lever') `
        (VanillaShapedRecipe @('X', '#') ([ordered]@{
            '#' = $fallbackCobblestone; X = ItemIngredient 'minecraft:stick'
        }) 'minecraft:lever')
    Write-ConditionalMinecraftRecipe 'piston' $condition `
        (VanillaShapedRecipe @('TTT', '#X#', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $enabledCobblestone
            T = [ordered]@{ tag = 'minecraft:planks' }; X = ItemIngredient 'minecraft:iron_ingot'
        }) 'minecraft:piston') `
        (VanillaShapedRecipe @('TTT', '#X#', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $fallbackCobblestone
            T = [ordered]@{ tag = 'minecraft:planks' }; X = ItemIngredient 'minecraft:iron_ingot'
        }) 'minecraft:piston')
    Write-ConditionalMinecraftRecipe 'dispenser' $condition `
        (VanillaShapedRecipe @('###', '#X#', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $enabledCobblestone
            X = ItemIngredient 'minecraft:bow'
        }) 'minecraft:dispenser') `
        (VanillaShapedRecipe @('###', '#X#', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $fallbackCobblestone
            X = ItemIngredient 'minecraft:bow'
        }) 'minecraft:dispenser')
    Write-ConditionalMinecraftRecipe 'dropper' $condition `
        (VanillaShapedRecipe @('###', '# #', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $enabledCobblestone
        }) 'minecraft:dropper') `
        (VanillaShapedRecipe @('###', '# #', '#R#') ([ordered]@{
            R = ItemIngredient 'minecraft:redstone'; '#' = $fallbackCobblestone
        }) 'minecraft:dropper')
    Write-ConditionalMinecraftRecipe 'observer' $condition `
        (VanillaShapedRecipe @('###', 'RRQ', '###') ([ordered]@{
            Q = ItemIngredient 'minecraft:quartz'; R = ItemIngredient 'minecraft:redstone'
            '#' = $enabledCobblestone
        }) 'minecraft:observer') `
        (VanillaShapedRecipe @('###', 'RRQ', '###') ([ordered]@{
            Q = ItemIngredient 'minecraft:quartz'; R = ItemIngredient 'minecraft:redstone'
            '#' = $fallbackCobblestone
        }) 'minecraft:observer')
    Write-ConditionalMinecraftRecipe 'mossy_cobblestone_from_vine' $condition `
        (VanillaShapelessRecipe @($enabledCobblestone, (ItemIngredient 'minecraft:vine')) 'minecraft:mossy_cobblestone' 1 'mossy_cobblestone') `
        (VanillaShapelessRecipe @($fallbackCobblestone, (ItemIngredient 'minecraft:vine')) 'minecraft:mossy_cobblestone' 1 'mossy_cobblestone')
    Write-ConditionalMinecraftRecipe 'mossy_cobblestone_from_moss_block' $condition `
        (VanillaShapelessRecipe @($enabledCobblestone, (ItemIngredient 'minecraft:moss_block')) 'minecraft:mossy_cobblestone' 1 'mossy_cobblestone') `
        (VanillaShapelessRecipe @($fallbackCobblestone, (ItemIngredient 'minecraft:moss_block')) 'minecraft:mossy_cobblestone' 1 'mossy_cobblestone')
    Write-ConditionalMinecraftRecipe 'andesite' $condition `
        (VanillaShapelessRecipe @((ItemIngredient 'minecraft:diorite'), $enabledCobblestone) 'minecraft:andesite' 2) `
        (VanillaShapelessRecipe @((ItemIngredient 'minecraft:diorite'), $fallbackCobblestone) 'minecraft:andesite' 2)
    Write-ConditionalMinecraftRecipe 'diorite' $condition `
        (VanillaShapedRecipe @('CQ', 'QC') ([ordered]@{
            Q = ItemIngredient 'minecraft:quartz'; C = $enabledCobblestone
        }) 'minecraft:diorite' 2) `
        (VanillaShapedRecipe @('CQ', 'QC') ([ordered]@{
            Q = ItemIngredient 'minecraft:quartz'; C = $fallbackCobblestone
        }) 'minecraft:diorite' 2)

    $toolRecipes = [ordered]@{
        stone_axe = @('XX', 'X#', ' #')
        stone_hoe = @('XX', ' #', ' #')
        stone_pickaxe = @('XXX', ' # ', ' # ')
        stone_shovel = @('X', '#', '#')
        stone_sword = @('X', 'X', '#')
    }
    foreach ($recipeName in $toolRecipes.Keys) {
        Write-ConditionalMinecraftRecipe $recipeName $condition `
            (VanillaShapedRecipe $toolRecipes[$recipeName] ([ordered]@{
                '#' = ItemIngredient 'minecraft:stick'; X = $enabledTools
            }) "minecraft:$recipeName") `
            (VanillaShapedRecipe $toolRecipes[$recipeName] ([ordered]@{
                '#' = ItemIngredient 'minecraft:stick'; X = $fallbackTools
            }) "minecraft:$recipeName")
    }

    foreach ($template in @('coast', 'sentry', 'vex')) {
        $recipeName = "${template}_armor_trim_smithing_template"
        $templateItem = "minecraft:$recipeName"
        $enabledRecipe = VanillaShapedRecipe @('#S#', '#C#', '###') ([ordered]@{
            '#' = ItemIngredient 'minecraft:diamond'
            C = $enabledCobblestone
            S = ItemIngredient $templateItem
        }) $templateItem 2
        $fallbackRecipe = VanillaShapedRecipe @('#S#', '#C#', '###') ([ordered]@{
            '#' = ItemIngredient 'minecraft:diamond'
            C = ItemIngredient 'minecraft:cobblestone'
            S = ItemIngredient $templateItem
        }) $templateItem 2
        Write-ConditionalMinecraftRecipe $recipeName $condition $enabledRecipe $fallbackRecipe
    }

    $advancements = @(
        @('decorations', 'furnace', 'has_cobblestone', $enabledCrafting, $fallbackCrafting),
        @('brewing', 'brewing_stand', 'has_blaze_rod', (ItemIngredient 'minecraft:blaze_rod'), (ItemIngredient 'minecraft:blaze_rod')),
        @('redstone', 'lever', 'has_cobblestone', $enabledCobblestone, $fallbackCobblestone),
        @('redstone', 'piston', 'has_redstone', (ItemIngredient 'minecraft:redstone'), (ItemIngredient 'minecraft:redstone')),
        @('redstone', 'dispenser', 'has_bow', (ItemIngredient 'minecraft:bow'), (ItemIngredient 'minecraft:bow')),
        @('redstone', 'dropper', 'has_redstone', (ItemIngredient 'minecraft:redstone'), (ItemIngredient 'minecraft:redstone')),
        @('redstone', 'observer', 'has_quartz', (ItemIngredient 'minecraft:quartz'), (ItemIngredient 'minecraft:quartz')),
        @('building_blocks', 'mossy_cobblestone_from_vine', 'has_vine', (ItemIngredient 'minecraft:vine'), (ItemIngredient 'minecraft:vine')),
        @('building_blocks', 'mossy_cobblestone_from_moss_block', 'has_moss_block', (ItemIngredient 'minecraft:moss_block'), (ItemIngredient 'minecraft:moss_block')),
        @('building_blocks', 'andesite', 'has_stone', (ItemIngredient 'minecraft:diorite'), (ItemIngredient 'minecraft:diorite')),
        @('building_blocks', 'diorite', 'has_quartz', (ItemIngredient 'minecraft:quartz'), (ItemIngredient 'minecraft:quartz')),
        @('tools', 'stone_axe', 'has_cobblestone', $enabledTools, $fallbackTools),
        @('tools', 'stone_hoe', 'has_cobblestone', $enabledTools, $fallbackTools),
        @('tools', 'stone_pickaxe', 'has_cobblestone', $enabledTools, $fallbackTools),
        @('tools', 'stone_shovel', 'has_cobblestone', $enabledTools, $fallbackTools),
        @('combat', 'stone_sword', 'has_cobblestone', $enabledTools, $fallbackTools)
    )
    foreach ($entry in $advancements) {
        Write-ConditionalMinecraftAdvancement $entry[0] $entry[1] $condition $entry[2] $entry[3] $entry[4]
    }
}

function Write-NativeSlabOverrides() {
    foreach ($family in @('andesite', 'diorite', 'granite')) {
        $raw = $nativeFullBlocks[$family].raw
        $smooth = $nativeFullBlocks[$family].smooth
        foreach ($finish in @(
                @{ recipe = "${family}_slab"; source = $raw; mineralogy = "mineralogy:${family}_slab"; vanilla = "minecraft:${family}_slab" },
                @{ recipe = "polished_${family}_slab"; source = $smooth; mineralogy = "mineralogy:${family}_smooth_slab"; vanilla = "minecraft:polished_${family}_slab" }
            )) {
            $condition = ItemCondition $finish.mineralogy
            Write-ConditionalMinecraftRecipe $finish.recipe $condition `
                (VanillaShapedRecipe @('###') ([ordered]@{ '#' = ItemIngredient $finish.source }) $finish.mineralogy 6) `
                (VanillaShapedRecipe @('###') ([ordered]@{ '#' = ItemIngredient $finish.source }) $finish.vanilla 6)
        }

        foreach ($route in @(
                @{ recipe = "${family}_slab_from_${family}_stonecutting"; source = $raw; mineralogy = "mineralogy:${family}_slab"; vanilla = "minecraft:${family}_slab" },
                @{ recipe = "polished_${family}_slab_from_${family}_stonecutting"; source = $raw; mineralogy = "mineralogy:${family}_smooth_slab"; vanilla = "minecraft:polished_${family}_slab" },
                @{ recipe = "polished_${family}_slab_from_polished_${family}_stonecutting"; source = $smooth; mineralogy = "mineralogy:${family}_smooth_slab"; vanilla = "minecraft:polished_${family}_slab" }
            )) {
            $condition = ItemCondition $route.mineralogy
            Write-ConditionalMinecraftRecipe $route.recipe $condition `
                (VanillaStonecuttingRecipe (ItemIngredient $route.source) $route.mineralogy 2) `
                (VanillaStonecuttingRecipe (ItemIngredient $route.source) $route.vanilla 2)
        }
    }
}

function Write-NativeSlabConversions() {
    foreach ($family in @('andesite', 'diorite', 'granite')) {
        foreach ($finish in @(
                @{ name = 'raw'; mineralogy = "mineralogy:${family}_slab"; vanilla = "minecraft:${family}_slab" },
                @{ name = 'smooth'; mineralogy = "mineralogy:${family}_smooth_slab"; vanilla = "minecraft:polished_${family}_slab" }
            )) {
            $prefix = if ($finish.name -eq 'raw') { "${family}_slab" } else { "${family}_smooth_slab" }
            $conditions = @((ItemCondition $finish.mineralogy), (ItemCondition $finish.vanilla))

            Write-ShapelessRecipe "${prefix}_to_vanilla" 'minecraft:crafting_shapeless' `
                @((ItemIngredient $finish.mineralogy)) $finish.vanilla 1 $conditions
            Register-UnlockSource "${prefix}_to_vanilla" $finish.mineralogy

            Write-ShapelessRecipe "${prefix}_from_vanilla" 'minecraft:crafting_shapeless' `
                @((ItemIngredient $finish.vanilla)) $finish.mineralogy 1 $conditions
            Register-UnlockSource "${prefix}_from_vanilla" $finish.vanilla
        }
    }
}

function Write-NativePolishedOverrides() {
    New-Item -ItemType Directory -Force -Path $minecraftRecipeRoot | Out-Null
    $minecraftBuildingAdvancementRoot = Join-Path $minecraftAdvancementRoot 'building_blocks'
    New-Item -ItemType Directory -Force -Path $minecraftBuildingAdvancementRoot | Out-Null
    foreach ($family in @($nativeFullBlocks.Keys | Where-Object { $nativeFullBlocks[$_].ContainsKey('smooth') })) {
        $raw = $nativeFullBlocks[$family].raw
        $smooth = $nativeFullBlocks[$family].smooth
        $recipeName = "polished_$family"
        Write-Json (Join-Path $minecraftRecipeRoot "$recipeName.json") ([ordered]@{
            type = 'minecraft:crafting_shapeless'
            category = 'building'
            ingredients = @((ItemIngredient $raw), (ItemIngredient 'minecraft:sand'))
            result = RecipeResult $smooth 1
        })
        Write-Json (Join-Path $minecraftBuildingAdvancementRoot "$recipeName.json") ([ordered]@{
            parent = 'minecraft:recipes/root'
            rewards = [ordered]@{ recipes = @("minecraft:$recipeName") }
            criteria = [ordered]@{
                has_rock = [ordered]@{
                    trigger = 'minecraft:inventory_changed'
                    conditions = [ordered]@{ items = @((AdvancementPredicate (ItemIngredient $raw))) }
                }
                has_sand = [ordered]@{
                    trigger = 'minecraft:inventory_changed'
                    conditions = [ordered]@{ items = @((AdvancementPredicate (ItemIngredient 'minecraft:sand'))) }
                }
                has_the_recipe = [ordered]@{
                    trigger = 'minecraft:recipe_unlocked'
                    conditions = [ordered]@{ recipe = "minecraft:$recipeName" }
                }
            }
            requirements = @(
                @('has_the_recipe', 'has_rock'),
                @('has_the_recipe', 'has_sand')
            )
            sends_telemetry_event = $false
        })
    }
}

Prepare-TargetDirectories
Write-TargetTags
Write-CobblestoneRecipeTags
Write-CobblestoneRecipeOverrides
Write-NativeSlabOverrides
Write-NativePolishedOverrides
New-Item -ItemType Directory -Force -Path $recipeRoot | Out-Null

foreach ($family in $families) {
    Write-BaseFamilyRecipes $family
    Write-ReliefRecipes $family
    Write-ConstructionRecipes $family
}
Write-NativeSlabConversions
Write-GlobalRecipes

$expectedRecipeCount = ($families.Count * 50) + 49
if ($generatedNames.Count -ne $expectedRecipeCount) {
    throw "Expected $expectedRecipeCount generated recipes, produced $($generatedNames.Count)"
}

$expectedTargetRecipeCount = $expectedRecipeCount + 28
$recipeFiles = Get-ChildItem -LiteralPath $recipeRoot -Filter '*.json' |
    Where-Object { -not $_.Name.StartsWith('_') }
if ($recipeFiles.Count -ne $expectedTargetRecipeCount) {
    throw "Expected $expectedTargetRecipeCount target recipe files, found $($recipeFiles.Count)"
}

$createdAdvancements = Ensure-MissingRecipeAdvancements
$advancementCount = Synchronize-AdvancementConditions
if ($advancementCount -ne $expectedTargetRecipeCount) {
    throw "Expected $expectedTargetRecipeCount recipe advancements, found $advancementCount"
}
Write-Output "Generated $expectedRecipeCount crafting recipe JSON files, retained 28 target-native smelting recipes, created $createdAdvancements missing recipe advancements, and conditioned $advancementCount Minecraft 1.20.6 recipe advancements."
