import json

with open('tools/heartodex_ids.json', 'r', encoding='utf-8') as f:
    ids_data = json.load(f)

with open('app/src/main/assets/catalog/heartodex.json', 'r', encoding='utf-8') as f:
    catalog_data = json.load(f)

img_map = {item['id']: item.get('image', '') for item in ids_data}
matched = 0
for entry in catalog_data.get('entries', []):
    eid = entry.get('id')
    if eid in img_map and img_map[eid]:
        rel = img_map[eid]
        entry['imageUrl'] = f'https://www.heartodex.com{rel}' if rel.startswith('/') else rel
        matched += 1
    else:
        entry['imageUrl'] = ''

print(f"Matched {matched} of {len(catalog_data.get('entries', []))} entries.")

with open('app/src/main/assets/catalog/heartodex.json', 'w', encoding='utf-8') as f:
    json.dump(catalog_data, f, ensure_ascii=False, indent=2)
