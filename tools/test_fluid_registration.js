const assert = require('node:assert/strict');
const test = require('node:test');
const fs = require('node:fs');
const path = require('node:path');
const root = path.join(__dirname, '../src/main/java/zone/moddev/mc/mineralogy');
const mod = fs.readFileSync(path.join(root, 'Mineralogy.java'), 'utf8');
const fluids = fs.readFileSync(path.join(root, 'fluids/MineralogyFluids.java'), 'utf8');

test('parallel construction does not write the shared fluid registry', () => {
  assert.ok(!/registerFluids\s*\(/.test(mod));
});
test('the sequential block event registers oil before creating its block', () => {
  assert.ok(/@SubscribeEvent\s+public static void registerBlock\(RegistryEvent\.Register<Block> event\)\s*\{\s*registerFluids\(\);/.test(fluids));
  assert.equal((fluids.match(/registerFluids\(\);/g) || []).length, 1);
});
test('fluid registration is private and remains idempotent', () => {
  assert.ok(/private static void registerFluids\(\)/.test(fluids));
  assert.ok(/if \(CRUDE_OIL != null\)/.test(fluids));
});
