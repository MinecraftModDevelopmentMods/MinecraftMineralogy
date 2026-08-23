[CmdletBinding()]
param()

$projectRoot = Split-Path -Parent $PSScriptRoot
$recipeRoot = Join-Path $projectRoot 'src\main\resources\assets\mineralogy\recipes'
$advancementRoot = Join-Path $projectRoot 'src\main\resources\assets\mineralogy\advancements\recipes'
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$generatedNames = New-Object 'System.Collections.Generic.HashSet[string]'
$materialRecipeIds = New-Object 'System.Collections.Generic.HashSet[string]'

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

foreach ($family in $families) {
    [void] $materialRecipeIds.Add("mineralogy:${family}_brick")
    [void] $materialRecipeIds.Add("mineralogy:${family}_smooth")
    [void] $materialRecipeIds.Add("mineralogy:${family}_smooth_brick")
}

function ItemId([string] $path) {
    return "mineralogy:$path"
}

function ItemCondition([string] $item) {
    return [ordered]@{ type = 'minecraft:item_exists'; item = $item }
}

function ConfigCondition([string] $flag) {
    return [ordered]@{ type = 'mineralogy:config'; flag = $flag }
}

function ItemIngredient([string] $item, [object] $data = $null) {
    $ingredient = [ordered]@{ item = $item }
    if ($null -ne $data) {
        $ingredient['data'] = [int]$data
    }
    return $ingredient
}

function OreIngredient([string] $ore) {
    return [ordered]@{ type = 'forge:ore_dict'; ore = $ore }
}

function RecipeResult([string] $item, [int] $count) {
    return [ordered]@{ item = $item; count = $count }
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

function Write-ShapedRecipe(
    [string] $name,
    [string] $type,
    [string[]] $pattern,
    [System.Collections.IDictionary] $key,
    [string] $result,
    [int] $count,
    [object[]] $conditions
) {
    Write-Recipe $name ([ordered]@{
        conditions = $conditions
        type = $type
        pattern = $pattern
        key = $key
        result = RecipeResult $result $count
    })
}

function Write-ShapelessRecipe(
    [string] $name,
    [string] $type,
    [object[]] $ingredients,
    [string] $result,
    [int] $count,
    [object[]] $conditions
) {
    Write-Recipe $name ([ordered]@{
        conditions = $conditions
        type = $type
        ingredients = $ingredients
        result = RecipeResult $result $count
    })
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
        ([ordered]@{ x = OreIngredient $rawOre }) (ItemId "${family}_stairs") 4 `
        (ConditionsFor (ItemId "${family}_stairs"))
    Write-ShapedRecipe "${family}_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ItemIngredient $raw }) (ItemId "${family}_slab") 6 `
        (ConditionsFor (ItemId "${family}_slab"))
    Write-ShapedRecipe "${family}_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_furnace") 1 (ConditionsFor (ItemId "${family}_furnace"))
    Write-ShapedRecipe "${family}_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = OreIngredient $rawOre }) (ItemId "${family}_wall") 6 `
        (ConditionsFor (ItemId "${family}_wall"))

    Write-ShapedRecipe "${family}_brick" 'forge:ore_shaped' @('xx', 'xx') `
        ([ordered]@{ x = OreIngredient $rawOre }) $brick 4 (ConditionsFor $brick)
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
        (OreIngredient $rawOre), (ItemIngredient 'minecraft:sand' 0)
    ) $smooth 1 (ConditionsFor $smooth)
    Write-ShapedRecipe "${family}_smooth_stairs" 'forge:ore_shaped' @('x  ', 'xx ', 'xxx') `
        ([ordered]@{ x = OreIngredient $smoothOre }) (ItemId "${family}_smooth_stairs") 4 `
        (ConditionsFor (ItemId "${family}_smooth_stairs"))
    Write-ShapedRecipe "${family}_smooth_slab" 'minecraft:crafting_shaped' @('xxx') `
        ([ordered]@{ x = ItemIngredient $smooth }) (ItemId "${family}_smooth_slab") 6 `
        (ConditionsFor (ItemId "${family}_smooth_slab"))
    Write-ShapedRecipe "${family}_smooth_furnace" 'forge:ore_shaped' @('xxx', 'xyx', 'xxx') `
        ([ordered]@{ x = OreIngredient (FamilyOreName 'slab' $family 'Smooth'); y = ItemIngredient 'minecraft:furnace' }) `
        (ItemId "${family}_smooth_furnace") 1 (ConditionsFor (ItemId "${family}_smooth_furnace"))
    Write-ShapedRecipe "${family}_smooth_wall" 'forge:ore_shaped' @('xxx', 'xxx') `
        ([ordered]@{ x = OreIngredient $smoothOre }) (ItemId "${family}_smooth_wall") 6 `
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
    $material = $family.Substring(0, 1).ToUpperInvariant() + $family.Substring(1)
    $smoothOre = "stone${material}Smooth"
    $blankOre = "reliefBlank${material}"
    $leftOre = "reliefLeft${material}"
    $blank = ItemId "${family}_relief_blank"

    Write-ShapedRecipe "${family}_relief_blank" 'forge:ore_shaped' @('xxx', 'xxx', 'xxx') `
        ([ordered]@{ x = OreIngredient $smoothOre }) $blank 16 (ConditionsFor $blank)
    Write-ShapedRecipe "${family}_relief_cross" 'forge:ore_shaped' @('x x', '   ', 'x x') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_cross") 4 `
        (ConditionsFor (ItemId "${family}_relief_cross"))
    Write-ShapedRecipe "${family}_relief_hammer" 'forge:ore_shaped' @('zxz', 'zyz', 'zzz') `
        ([ordered]@{ x = OreIngredient $smoothOre; y = ItemIngredient 'minecraft:stick'; z = OreIngredient $blankOre }) `
        (ItemId "${family}_relief_hammer") 7 (ConditionsFor (ItemId "${family}_relief_hammer"))
    Write-ShapedRecipe "${family}_relief_horizontal" 'forge:ore_shaped' @('xxx') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_horizontal") 3 `
        (ConditionsFor (ItemId "${family}_relief_horizontal"))
    Write-ShapedRecipe "${family}_relief_left" 'forge:ore_shaped' @('x  ', ' x ', '  x') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_left") 3 `
        (ConditionsFor (ItemId "${family}_relief_left"))
    Write-ShapedRecipe "${family}_relief_plus" 'forge:ore_shaped' @(' x ', 'xxx', ' x ') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_plus") 5 `
        (ConditionsFor (ItemId "${family}_relief_plus"))
    Write-ShapedRecipe "${family}_relief_right" 'forge:ore_shaped' @('  x', ' x ', 'x  ') `
        ([ordered]@{ x = OreIngredient $leftOre }) (ItemId "${family}_relief_right") 3 `
        (ConditionsFor (ItemId "${family}_relief_right"))
    Write-ShapedRecipe "${family}_relief_i" 'forge:ore_shaped' @('xxx', ' x ', 'xxx') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_i") 7 `
        (ConditionsFor (ItemId "${family}_relief_i"))
    Write-ShapedRecipe "${family}_relief_vertical" 'forge:ore_shaped' @('x', 'x', 'x') `
        ([ordered]@{ x = OreIngredient $blankOre }) (ItemId "${family}_relief_vertical") 3 `
        (ConditionsFor (ItemId "${family}_relief_vertical"))

    foreach ($tool in @('axe', 'hoe', 'pickaxe', 'sword')) {
        $ingredients = @()
        for ($index = 0; $index -lt 8; $index++) {
            $ingredients += ,(OreIngredient $blankOre)
        }
        $ingredients += ,(ItemIngredient "minecraft:stone_$tool" 0)
        $result = ItemId "${family}_relief_${tool}"
        Write-ShapelessRecipe "${family}_relief_${tool}" 'forge:ore_shapeless' `
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
        }
    }

    Write-ShapelessRecipe "${family}_brick_block_polishing" 'forge:ore_shapeless' `
        @((ItemIngredient $forms.brick.block), (OreIngredient 'sand')) $forms.polished_brick.block 1 `
        @((ItemCondition $forms.brick.block), (ItemCondition $forms.polished_brick.block))
}

function Write-GlobalRecipes() {
    $drywallCondition = ConfigCondition 'ENABLE_DRYWALLS'
    for ($metadata = 0; $metadata -lt $colors.Count; $metadata++) {
        $result = ItemId "drywall_$($colors[$metadata])"
        Write-ShapelessRecipe "drywall_$($colors[$metadata])" 'forge:ore_shapeless' @(
            (OreIngredient 'drywallWhite'), (ItemIngredient 'minecraft:dye' $metadata)
        ) $result 1 (ConditionsFor $result @($drywallCondition))
    }

    $dustCondition = ConfigCondition 'ENABLE_MINERAL_DUSTS'
    $gunpowderTail = @((OreIngredient 'dustNitrate'), (OreIngredient 'dustSulfur'))
    Write-ShapelessRecipe 'gunpowder_from_sugar' 'forge:ore_shapeless' `
        (@((ItemIngredient 'minecraft:sugar')) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)
    Write-ShapelessRecipe 'gunpowder_from_charcoal' 'forge:ore_shapeless' `
        (@((ItemIngredient 'minecraft:coal' 1)) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)
    Write-ShapelessRecipe 'gunpowder_from_carbon_dust' 'forge:ore_shapeless' `
        (@((OreIngredient 'dustCarbon')) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)
    Write-ShapelessRecipe 'gunpowder_from_coal_dust' 'forge:ore_shapeless' `
        (@((OreIngredient 'dustCoal')) + $gunpowderTail) 'minecraft:gunpowder' 4 @($dustCondition)

    Write-ShapelessRecipe 'mineralfertilizer' 'forge:ore_shapeless' @(
        (OreIngredient 'dustNitrate'), (OreIngredient 'dustPhosphorous')
    ) (ItemId 'mineral_fertilizer') 1 `
        (ConditionsFor (ItemId 'mineral_fertilizer') @((ConfigCondition 'ENABLE_MINERAL_FERTILIZER')))

    Write-ShapelessRecipe 'cobblestone' 'minecraft:crafting_shapeless' @(
        (ItemIngredient 'minecraft:stone' 0), (ItemIngredient 'minecraft:stone' 0),
        (ItemIngredient 'minecraft:gravel'), (ItemIngredient 'minecraft:gravel')
    ) 'minecraft:cobblestone' 4 @()

    foreach ($storage in @(
        [ordered]@{ name = 'gypsum'; dust = 'dustGypsum'; blockOre = 'blockGypsum'; dustItem = 'gypsum_dust'; blockItem = 'gypsum' },
        [ordered]@{ name = 'chalk'; dust = 'dustChalk'; blockOre = 'blockChalk'; dustItem = 'chalk_dust'; blockItem = 'chalk' },
        [ordered]@{ name = 'rock_salt'; dust = 'dustRock_salt'; blockOre = 'blockRocksalt'; dustItem = 'rock_salt_dust'; blockItem = 'rock_salt' }
    )) {
        $block = ItemId $storage.blockItem
        Write-ShapedRecipe $storage.name 'forge:ore_shaped' @('xx', 'xx') `
            ([ordered]@{ x = OreIngredient $storage.dust }) $block 1 (ConditionsFor $block)
        Write-ShapelessRecipe "$($storage.name)_dust" 'forge:ore_shapeless' @(
            (OreIngredient $storage.blockOre)
        ) (ItemId $storage.dustItem) 4 (ConditionsFor (ItemId $storage.dustItem))
    }

    Write-ShapedRecipe 'drywall' 'forge:ore_shaped' @('pgp', 'pgp', 'pgp') `
        ([ordered]@{ p = OreIngredient 'paper'; g = OreIngredient 'dustGypsum' }) `
        (ItemId 'drywall_white') 3 `
        (ConditionsFor (ItemId 'drywall_white') @($drywallCondition))

    $lampCondition = ConfigCondition 'ENABLE_ROCK_SALT_LAMPS'
    Write-ShapelessRecipe 'rocksaltlamp' 'minecraft:crafting_shapeless' @(
        (ItemIngredient (ItemId 'rock_salt')), (ItemIngredient 'minecraft:torch'),
        (ItemIngredient 'minecraft:iron_ingot')
    ) (ItemId 'rocksaltlamp') 1 `
        (ConditionsFor (ItemId 'rocksaltlamp') @($lampCondition))
    Write-ShapedRecipe 'rocksaltstreetlamp' 'forge:ore_shaped' @('x', 'y', 'y') `
        ([ordered]@{ x = OreIngredient 'lampRocksalt'; y = ItemIngredient 'minecraft:iron_ingot' }) `
        (ItemId 'rocksaltstreetlamp') 1 `
        (ConditionsFor (ItemId 'rocksaltstreetlamp') @($lampCondition))

    foreach ($mineral in @('sulfur', 'phosphorous', 'nitrate')) {
        $material = $mineral.Substring(0, 1).ToUpperInvariant() + $mineral.Substring(1)
        $block = ItemId "${mineral}_block"
        $dust = ItemId "${mineral}_dust"
        Write-ShapedRecipe "${mineral}_block" 'minecraft:crafting_shaped' @('xxx', 'xxx', 'xxx') `
            ([ordered]@{ x = ItemIngredient $dust }) $block 1 `
            (ConditionsFor $block @($dustCondition))
        Write-ShapelessRecipe "${mineral}_dust" 'forge:ore_shapeless' @(
            (OreIngredient "block$material")
        ) $dust 9 (ConditionsFor $dust @($dustCondition))
    }
}

function Write-Factories() {
    $factories = [ordered]@{
        conditions = [ordered]@{
            config = 'zone.moddev.mc.mineralogy.recipe.ConfigConditionFactory'
        }
    }
    Write-Json (Join-Path $recipeRoot '_factories.json') $factories
    Write-Json (Join-Path (Split-Path -Parent $advancementRoot) '_factories.json') $factories
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
        $sourceRecipeId = $null
        if ($null -ne $advancement.criteria.has_rock) {
            $sourceItem = [string]$advancement.criteria.has_rock.conditions.items[0].item
            if ($materialRecipeIds.Contains($sourceItem)) {
                $sourceName = $sourceItem.Substring('mineralogy:'.Length)
                $sourceFile = Join-Path $recipeRoot "$sourceName.json"
                if (-not (Test-Path -LiteralPath $sourceFile)) {
                    throw "Advancement $($file.Name) requires missing material recipe $sourceItem"
                }
                $sourceRecipeId = $sourceItem
            }
        }

        $criteria = [ordered]@{}
        foreach ($criterion in $advancement.criteria.PSObject.Properties) {
            if ($criterion.Name -ne 'has_material_recipe') {
                $criteria[$criterion.Name] = $criterion.Value
            }
        }
        if ($null -ne $sourceRecipeId) {
            $criteria['has_material_recipe'] = [ordered]@{
                trigger = 'minecraft:recipe_unlocked'
                conditions = [ordered]@{ recipe = $sourceRecipeId }
            }
        }

        $requirements = @()
        foreach ($group in $advancement.requirements) {
            $updatedGroup = @($group | Where-Object { $_ -ne 'has_material_recipe' })
            if ($null -ne $sourceRecipeId) {
                $updatedGroup += 'has_material_recipe'
            }
            $requirements += ,$updatedGroup
        }

        $ordered = [ordered]@{ conditions = @($recipe.conditions) }
        foreach ($property in $advancement.PSObject.Properties) {
            if ($property.Name -eq 'criteria') {
                $ordered[$property.Name] = $criteria
            }
            elseif ($property.Name -eq 'requirements') {
                $ordered[$property.Name] = $requirements
            }
            elseif ($property.Name -ne 'conditions') {
                $ordered[$property.Name] = $property.Value
            }
        }
        Write-Json $file.FullName $ordered
    }
    return $files.Count
}

New-Item -ItemType Directory -Force -Path $recipeRoot | Out-Null

foreach ($family in $families) {
    Write-BaseFamilyRecipes $family
    Write-ReliefRecipes $family
    Write-ConstructionRecipes $family
}
Write-GlobalRecipes
Write-Factories

$expectedRecipeCount = ($families.Count * 50) + 37
if ($generatedNames.Count -ne $expectedRecipeCount) {
    throw "Expected $expectedRecipeCount generated recipes, produced $($generatedNames.Count)"
}

$recipeFiles = Get-ChildItem -LiteralPath $recipeRoot -Filter '*.json' |
    Where-Object { -not $_.Name.StartsWith('_') }
if ($recipeFiles.Count -ne $expectedRecipeCount) {
    throw "Expected $expectedRecipeCount recipe files, found $($recipeFiles.Count)"
}

$advancementCount = Synchronize-AdvancementConditions
Write-Output "Generated $expectedRecipeCount Mineralogy recipe JSON files and conditioned $advancementCount recipe advancements."
