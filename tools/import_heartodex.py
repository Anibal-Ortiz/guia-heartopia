#!/usr/bin/env python3
"""Build the Heartodex catalog JSON from the index-page snapshot.

Heartodex terms (https://www.heartodex.com/en/terms/, effective 2026-02-02)
ask not to mass-scrape. This script does not walk item pages. It turns the
category index snapshot taken on 2026-11-06 (fish, birds, insects, wildlife)
into app/src/main/assets/catalog/heartodex.json.

The index repeats "1 Star … 5 Stars" as page chrome, not as each creature's
rarity, so that column is not stored. Price, shadow, bait and required level
are not on those indexes.

Run from the repo root:
    python3 tools/import_heartodex.py
"""

from __future__ import annotations

import json
from pathlib import Path

EXTRACTED = "2026-11-06"
ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "app" / "src" / "main" / "assets" / "catalog" / "heartodex.json"

ALL_S = ["Day", "Dawn", "Dusk", "Night"]
ALL_W = ["Rainbow", "Sunny", "Rainy"]


def row(name, location, schedule=None, weather=None, event=None):
    return {
        "name": name,
        "location": [part.strip() for part in location.split(",") if part.strip()],
        "schedule": schedule if schedule is not None else list(ALL_S),
        "weather": weather if weather is not None else list(ALL_W),
        "event": event,
    }


def slug(category, name):
    text = name.lower()
    out = []
    for ch in text:
        if ch.isascii() and ch.isalnum():
            out.append(ch)
        elif ch in " -_/":
            out.append("-")
    slug_text = "".join(out)
    while "--" in slug_text:
        slug_text = slug_text.replace("--", "-")
    return f"{category}-{slug_text.strip('-')}"


FISH = [
    row("Blue Sea Angel", "Old Sea"),
    row("Green Sea Angel", "Spectral Shoal Event, Old Sea"),
    row("Golden Cuttlefish", "Old Sea"),
    row("Purple Sea Angel", "Spectral Shoal Event, Old Sea"),
    row("Pharaoh Cuttlefish", "Old Sea"),
    row("Spineless Cuttlefish", "Old Sea"),
    row("Schneider", "Suburban Lake"),
    row("Common Bleak", "Lake"),
    row("Barbel", "Shallow River"),
    row("Common Chub", "Lake"),
    row("Oriental Shrimp", "River"),
    row("Common Prawn", "East Sea"),
    row("Crucian Carp", "Suburban Lake"),
    row("Common Whitefish", "Onsen Mountain Lake"),
    row("Sea Stickleback", "Old Sea"),
    row("Scad", "Whale Sea"),
    row("Skipjack Tuna", "Ocean"),
    row("Spined Loach", "Giantwood River"),
    row("Sea Bass", "Ocean"),
    row("European Perch", "River"),
    row("Streber", "Rosy River"),
    row("Minnow", "Tranquil River"),
    row("Beltfish", "Zephyr Sea"),
    row("Striped Red Mullet", "[EVENT] Sea Fishing"),
    row("Sardine", "Ocean"),
    row("Tench", "Forest Lake"),
    row("Seahorse", "Whale Sea", ["Day", "Dawn", "Night"]),
    row("European Smelt", "Meadow Lake"),
    row("False Scad", "Zephyr Sea"),
    row("Largemouth Bass", "Forest Lake", None, ["Rainbow", "Sunny"]),
    row("Stone Loach", "Suburban Lake"),
    row("Anglerfish", "[EVENT] Sea Fishing"),
    row("Common Octopus", "[EVENT] Sea Fishing"),
    row("Atlantic Pygmy Octopus", "Zephyr Sea"),
    row("Turbot", "[EVENT] Sea Fishing"),
    row("Hermit Crab", "East Sea"),
    row("European Crayfish", "Forest Lake", ["Dawn", "Dusk", "Night"]),
    row("Zander", "Giantwood River", None, ["Rainbow", "Sunny"]),
    row("Ruffe", "Onsen Mountain Lake", ["Day", "Dusk"]),
    row("Mud Sunfish", "Forest Lake", ["Day", "Dawn", "Dusk"]),
    row("Edible Frog", "Lake"),
    row("Atlantic Salmon", "Whale Sea", ["Day", "Dusk", "Night"]),
    row("Tilapia", "River"),
    row("River Crab", "Suburban Lake"),
    row("Common Carp", "Rosy River", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Butterfly Koi", "Meadow Lake", None, ["Rainbow", "Rainy"]),
    row("Burbot", "Tranquil River", ["Day", "Dusk"]),
    row("Mussel", "Suburban Lake", None, ["Rainbow", "Rainy"]),
    row("Goby", "East Sea", ["Day", "Dawn"]),
    row("Red-Bellied Piranha", "Giantwood River"),
    row("European Plaice", "Old Sea", ["Dusk", "Night"]),
    row("Rabbit Fish", "Ocean"),
    row("Tadpole", "Onsen Mountain Lake", None, ["Rainbow", "Rainy"]),
    row("Freshwater Blenny", "Rosy River"),
    row("Atlantic Mackerel", "Whale Sea", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("European Lobster", "Zephyr Sea", ["Dusk", "Night"]),
    row("Common Rudd", "Suburban Lake"),
    row("Trout", "Meadow Lake", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Large Pearl Mussel", "Forest Lake", None, ["Rainbow"]),
    row("Puffer Fish", "Old Sea", ["Day", "Dusk"]),
    row("Tub Gurnard", "East Sea", None, ["Rainbow"]),
    row("Chum Salmon", "Tranquil River", None, ["Rainbow"]),
    row("Nursehound", "[EVENT] Sea Fishing"),
    row("Grayling", "Suburban Lake"),
    row("European Eel", "Old Sea", ["Day", "Dawn", "Dusk"], ["Rainbow"]),
    row("Blackspot Seabream", "Zephyr Sea", ["Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Mediterranean Killifish", "Suburban Lake", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Three-Spined Stickleback", "Shallow River", None, ["Rainbow", "Rainy"]),
    row("Giant Oarfish", "[EVENT] Sea Fishing", ["Day", "Dawn"]),
    row("Mottled Sculpin", "Onsen Mountain Lake", ["Day", "Dawn", "Dusk"], ["Rainbow", "Rainy"]),
    row("Golden King Crab", "[EVENT] Sea Fishing", None, ["Rainbow"]),
    row("Blue European Crayfish", "Forest Lake", ["Dusk", "Night"]),
    row("Haddock", "East Sea", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Goldfish", "Meadow Lake", ["Day", "Dawn", "Dusk"], ["Rainbow", "Rainy"]),
    row("European Mudminnow", "Suburban Lake", ["Dawn", "Night"]),
    row("Bluefin Tuna", "Zephyr Sea", ["Day", "Dawn"], ["Rainbow"]),
    row("Northern Pike", "Suburban Lake", ["Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Moonfish", "[EVENT] Sea Fishing", ["Dusk", "Night"]),
    row("Ocean Sunfish", "East Sea", ["Dawn", "Night"]),
    row("Pumpkinseed", "Onsen Mountain Lake", ["Day", "Dawn", "Dusk"], ["Rainbow", "Sunny"]),
    row("Huchen", "Giantwood River", ["Day", "Dusk", "Night"], ["Rainbow"]),
    row("Swordfish", "Whale Sea", ["Day", "Dawn"], ["Rainbow"]),
    row("Wels Catfish", "Meadow Lake", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Bluegill", "Onsen Mountain Lake", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Shortfin Mako Shark", "[EVENT] Sea Fishing", ["Day", "Dawn"], ["Rainbow"]),
    row("Smooth Hammerhead", "Old Sea", ["Dusk", "Night"], ["Rainbow"]),
    row("Arctic Char", "Forest Lake", ["Day", "Dusk"], ["Rainbow", "Rainy"]),
    row("Asian Arowana", "Meadow Lake", ["Dawn", "Dusk", "Night"], ["Rainbow"]),
    row("Blackspot Sergeant", "Zephyr Sea", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Lionhead", "Onsen Mountain Lake", None, ["Rainbow"]),
    row("Whale Shark", "[EVENT] Sea Fishing", ["Dawn", "Dusk", "Night"]),
    row("Mahi-Mahi", "Old Sea", ["Day", "Dawn"], ["Rainbow"]),
    row("Moon Jelly", "[EVENT] Sea Fishing", ["Day", "Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Angelfish", "Suburban Lake", ["Dusk", "Night"], ["Rainbow"]),
    row("Pink Betta", "Forest Lake", ["Day", "Dusk"], ["Rainbow", "Rainy"]),
    row("Green Sea Turtle", "Whale Sea", ["Day", "Dusk"], ["Rainbow"]),
    row("White-Faced Surgeonfish", "Old Sea", ["Day", "Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Lionfish", "Whale Sea", ["Dawn", "Night"], ["Rainbow", "Rainy"]),
    row("Blue-and-Yellow Wrasse", "East Sea", ["Day", "Dawn", "Dusk"], ["Rainbow", "Sunny"]),
    row("Azure Demoiselle", "Zephyr Sea", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Japanese Flying Squid", "Whale Sea"),
    row("Sea Slug", "Neritic Shoal Event"),
    row("Mandarin Fish", "Whale Sea"),
    row("Oliver Ridley Turtle", "Whale Sea"),
    row("Flatback Turtle", "Whale Sea"),
    row("Scallop", "Whale Sea"),
    row("Director Common Octopus w/ Cinnamon Hat", "Whale Sea", event="Dreamlight Cinematics"),
    row("Director Common Octopus w/ Patterned Hat", "[EVENT] Octo Entertainment", event="Dreamlight Cinematics"),
    row("Director Pygmy Octopus w/ Cyan Hat", "Whale Sea", event="Dreamlight Cinematics"),
    row("Director Pygmy Octopus w/ Navy Hat", "Whale Sea", event="Dreamlight Cinematics"),
    row("Director Pygmy Octopus w/ Brown Hat", "Whale Sea", event="Dreamlight Cinematics"),
    row("Golden Garfish", "Garfish Event"),
    row("Pale Gold Garfish", "Forest Lake"),
    row("Brown Garfish", "Forest Lake"),
    row("Black Garfish", "Forest Lake"),
    row("Blue Brick Clownfish", "East Sea"),
    row("Colorful Brick Clownfish", "Special Brick Fish Event"),
    row("Purple Brick Clownfish", "East Sea"),
    row("Red Brick Clownfish", "East Sea"),
    row("Green Brick Clownfish", "East Sea"),
    row("Frostspore Seahorse", "[EVENT] Frostspore Fish, Old Sea", event="Winter frost season"),
    row("Frostspore King Crab", "[EVENT] Frostspore Fish, Old Sea", event="Winter frost season"),
    row("Frostspore Puffer Fish", "[EVENT] Frostspore Fish, Old Sea", event="Winter frost season"),
    row("Frostspore Ocean Sunfish", "[EVENT] Frostspore Fish, Old Sea", event="Winter frost season"),
]

BIRDS = [
    row("Long-Toothed Fruit Bat", "Forest Island", ["Dusk", "Night"]),
    row("Northern White-Faced Owl", "Forest Island", ["Day", "Dawn"]),
    row("Palau Scops Owl", "Forest Island", ["Day", "Dawn"]),
    row("Eastern Screech Owl", "Forest Island", ["Day", "Dawn"]),
    row("Pumpkin Raven", "Ruins"),
    row("Strawberry Raven", "Crater Lake"),
    row("Mandarin Raven", "Onsen"),
    row("Apple Raven", "Stone Cliff"),
    row("Truffle Raven", "Raven's Hoard Event"),
    row("Honduran White Bat", "Forest Island", ["Dusk", "Night"]),
    row("Tanzanian Woolly Bat", "Forest Island", ["Dusk", "Night"]),
    row("Mallard", "Suburban Lake"),
    row("Bearded Reedling", "Stone Cliff, Onsen, Crater Lake, Ruins"),
    row("Eurasian Bullfinch", "Suburbs"),
    row("Eurasian Wren", "Spirit-Oak Pine Forest, Forest Lake"),
    row("Pied Imperial Pigeon", "East Pier of Fishing Village, Fishing Village Square, Wharf", [], []),
    row("Greater Flamingo", "Suburban Lake"),
    row("Blue-and-yellow Macaw", "[EVENT] Nest of Hundreds"),
    row("Long-Tailed Tit", "Blanc's Head"),
    row("Wonga Pigeon", "Spirit-Oak Pine Forest, Deer Tower, Forest Lake"),
    row("Blue Peafowl", "[EVENT] Nest of Hundreds"),
    row("European Robin", "Central Square"),
    row("Eurasian Collared Dove", "Residential Area"),
    row("Pink-Necked Green Pigeon", "Windmill Flower Field"),
    row("Woodchat Shrike", "Suburbs"),
    row("Great Tit", "Crater Lake, Ruins"),
    row("Seagull", "Seaside"),
    row("Eurasian Nuthatch", "East Pier of Fishing Village, Fishing Village Square", [], []),
    row("Stock Dove", "Central Square"),
    row("Eurasian Chaffinch", "Windmill Flower Field"),
    row("Eurasian Wigeon", "River"),
    row("European Shag", "Forest Island"),
    row("Double-Barred Finch", "Lighthouse"),
    row("King Eider", "River"),
    row("Audouin's Gull", "Whale Sea"),
    row("Red-and-green Macaw", "[EVENT] Nest of Hundreds"),
    row("Silver-Throated Tit", "Forest Jump Puzzle"),
    row("Eurasian Golden Oriole", "Residential Area"),
    row("Ruddy Shelduck", "Suburban Lake"),
    row("Lady Amherst Pheasant", "Ruins"),
    row("White Wagtail", "Amethyst Beach"),
    row("Regent Bowerbird", "Spirit-Oak Pine Forest"),
    row("Przevalski's Parrotbill", "East Pier of Fishing Village, Fishing Village Square", [], []),
    row("Smew", "Forest Lake"),
    row("Wallace's Fruit Dove", "Windmill Flower Field"),
    row("Brown Noddy", "Old Sea"),
    row("Lesser Flamingo", "Forest Lake", None, ["Rainbow", "Rainy"]),
    row("Great Green Macaw", "[EVENT] Nest of Hundreds", None, ["Rainbow", "Sunny"]),
    row("African Olive Pigeon", "Onsen, Onsen Mountain Lake"),
    row("Yellow Bellied Flycatcher", "Fishing Village Square, Wharf"),
    row("Long Eared Owl", "Stone Cliff, Onsen Mountain Lake", None, ["Rainbow", "Sunny"]),
    row("Red Faced Cormorant", "Old Sea, Whale Sea"),
    row("Hawfinch", "Onsen Mountain Lake, Crater Lake"),
    row("Common Kestrel", "Deer Tower", None, ["Rainbow", "Sunny"]),
    row("Tern", "East Sea, Seaside", None, ["Rainbow"]),
    row("Lear's Macaw", "[EVENT] Nest of Hundreds", None, ["Rainbow", "Rainy"]),
    row("Azure Tit", "Windmill Flower Field, Meadow Lake"),
    row("Jambu Fruit Dove", "Suburbs"),
    row("Peregrine Falcon", "Stone Cliff, Onsen", None, ["Rainbow", "Rainy"]),
    row("Eastern Bluebird", "Suburbs", None, ["Rainbow", "Rainy"]),
    row("Pink Pigeon", "Whale Mountain, Windmill Flower Field"),
    row("Redpoll", "Spirit-Oak Pine Forest"),
    row("Imperial Shag", "East Sea, Zephyr Sea"),
    row("American Flamingo", "Amethyst Beach", None, ["Rainbow"]),
    row("White-Headed Duck", "Onsen Mountain Lake", None, ["Rainbow", "Rainy"]),
    row("Green Peafowl", "[EVENT] Nest of Hundreds"),
    row("Paradise Tanager", "Suburbs", None, ["Rainbow"]),
    row("Eagle-Owl", "Deer Tower", None, ["Rainbow", "Rainy"]),
    row("Azores Bullfinch", "Lighthouse", None, ["Rainbow"]),
    row("Amur Falcon", "Crater Lake, Ruins"),
    row("Verditer Flycatcher", "Forest Jump Puzzle", None, ["Rainbow"]),
    row("Baikal Teal", "Onsen Mountain Lake", None, ["Rainbow"]),
    row("Inca Tern", "Old Sea", ["Day", "Dawn", "Dusk"], ["Rainbow", "Rainy"]),
    row("Violet-Backed Starling", "Windmill Flower Field"),
    row("Blue-Headed Quail-Dove", "Suburbs", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("White Peafowl", "[EVENT] Nest of Hundreds"),
    row("Snowy Owl", "Spirit-Oak Pine Forest", ["Day", "Dusk"]),
    row("Red-Browed Finch", "Crater Lake", None, ["Rainbow"]),
    row("Atlantic Puffin", "Whale Sea", ["Dawn", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Blue-Footed Booby", "Zephyr Sea", None, ["Rainbow", "Rainy"]),
    row("Whooper Swan", "Suburban Lake", None, ["Rainbow", "Sunny"]),
    row("Black Swan", "Suburban Lake", None, ["Rainbow", "Rainy"]),
    row("Black Peafowl", "[EVENT] Nest of Hundreds"),
    row("Black Stork", "Meadow Lake", ["Day", "Dusk", "Night"]),
    row("Spectacled Owl", "Forest Jump Puzzle", ["Dawn", "Night"]),
    row("Wandering Albatross", "Amethyst Beach", event="Call of Whales"),
    row("Grey-mantled Albatross", "Stone Cliff", event="Call of Whales"),
    row("White Spoonbill", "Onsen Mountain Lake", event="Call of Whales"),
    row("Roseate Spoonbill", "Stray Bird Event", event="Call of Whales"),
    row("White-winged Tern", "Whale Mountain", event="Call of Whales"),
    row("Pomarine Skua", "Whale Mountain, Windmill Flower Field, Amethyst Beach", [], [], "Call of Whales"),
    row("Blue Suit Dove", "Spirit-Oak Pine Forest, [EVENT] Actor Dove", event="Call of Whales"),
    row("Gray Suit Dove", "Deer Tower, [EVENT] Actor Dove", event="Call of Whales"),
    row("Striped Suit Dove", "[EVENT] Actor Dove", event="Call of Whales"),
    row("Red Suit Dove", "Forest Island, [EVENT] Actor Dove", event="Call of Whales"),
    row("Green Suit Dove", "Forest Jump Puzzle, [EVENT] Actor Dove", event="Call of Whales"),
    row("Blue Hoopoe", "Onsen"),
    row("Colorful Hoopoe", "Hoopoe Event"),
    row("Orange Hoopoe", "Stone Cliff"),
    row("Red Hoopoe", "Ruins"),
    row("Green Hoopoe", "Crater Lake"),
    row("Blue Brick Sparrow", "Onsen"),
    row("Purple Brick Sparrow", "Ruins"),
    row("Colorful Brick Sparrow", "Evento especial de aves de bloques"),
    row("Pink Brick Sparrow", "Stone Cliff"),
    row("Green Brick Sparrow", "Crater Lake"),
    row("Winter Mallard", "[EVENT] Winter Birdwatching, Suburban Lake", event="Winter frost season"),
    row("Winter Greater Flamingo", "[EVENT] Winter Birdwatching, Suburban Lake", event="Winter frost season"),
    row("Winter Mandarin Duck", "[EVENT] Winter Birdwatching", event="Winter frost season"),
]

INSECTS = [
    row("Pumpkin Dung Beetle", "Spirit-Oak Pine Forest"),
    row("Strawberry Dung Beetle", "Deer Tower"),
    row("Mandarin Dung Beetle", "Spirit-Oak Pine Forest"),
    row("Shiitake Dung Beetle", "Forest Jump Puzzle"),
    row("Truffle Dung Beetle", "Dung Beetle Mutation Event"),
    row("Cabbage White", "East Pier of Fishing Village, Fishing Village Square", [], []),
    row("Large Red Damselfly", "Lake, River"),
    row("European Firebug", "Residential Area"),
    row("Gold Grasshopper", "Onsen Mountain Lake"),
    row("Apollo", "[EVENT] Bait the Insects"),
    row("Orange-Tip", "Central Square"),
    row("Common Brimstone", "Suburbs"),
    row("Old World Swallowtail", "Spirit-Oak Pine Forest, Deer Tower, Forest Lake"),
    row("Postman Butterfly", "[EVENT] Bait the Insects"),
    row("Sulkowsky's Morpho", "Inflatable Insect Attractor"),
    row("Bumblebee", "Windmill Flower Field"),
    row("Green Tiger Beetle", "Onsen, Onsen Mountain Lake, Crater Lake, Ruins"),
    row("Wasp Beetle", "Spirit-Oak Pine Forest, Forest Lake"),
    row("Amethyst Flower Beetle", "Residential Area", None, ["Rainbow", "Rainy"]),
    row("Four-Spotted Skimmer", "Suburban Lake", None, ["Rainbow", "Rainy"]),
    row("Seven-Spotted Ladybug", "Suburbs", None, ["Rainbow", "Rainy"]),
    row("Waroona Cuckoo Bee", "Deer Tower", ["Day", "Dawn", "Dusk"]),
    row("Fire-Colored Beetle", "Onsen, Onsen Mountain Lake, Crater Lake, Ruins"),
    row("Ant", "Fishing Village Square"),
    row("Rajah Brooke's Birdwing", "Residential Area", ["Dawn", "Dusk", "Night"]),
    row("Purple Emperor", "Whale Mountain", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Mini Moon Moth", "Suburbs", ["Day", "Dawn", "Dusk"], ["Rainbow", "Sunny"]),
    row("Katydid", "Amethyst Beach"),
    row("Pink Katydid", "[EVENT] Bait the Insects"),
    row("Cicada", "Spirit-Oak Pine Forest", ["Day", "Dusk", "Night"]),
    row("Large Banded Grasshopper", "Suburbs", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Common Whitetail", "River", None, ["Rainbow", "Rainy"]),
    row("Mediterranean Mantis", "Stone Cliff", ["Day", "Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Beautiful Leopard", "Wharf", ["Dawn", "Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Four-Spotted Ladybug", "Crater Lake", None, ["Rainbow", "Rainy"]),
    row("Splay-Footed Carpenter", "Whale Mountain", None, ["Rainbow", "Sunny"]),
    row("Giant Asian Mantis", "Ruins", None, ["Rainbow", "Sunny"]),
    row("Mourning Cloak", "Onsen", ["Day", "Dawn", "Night"], ["Rainbow", "Sunny"]),
    row("White Witch", "[EVENT] Bait the Insects", None, ["Rainbow", "Sunny"]),
    row("Peacock Butterfly", "Windmill Flower Field"),
    row("Asian Lady Beetle", "Deer Tower, Forest Lake", ["Day", "Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Beautiful Demoiselle", "Forest Lake", None, ["Rainbow", "Rainy"]),
    row("Blue Shieldbug", "Lighthouse", ["Day", "Dawn"]),
    row("Silver Jewel Scarab", "Stone Cliff", ["Day", "Dawn", "Night"], ["Rainbow", "Sunny"]),
    row("Stag Beetle", "Forest Jump Puzzle", ["Day", "Dawn", "Dusk"], ["Rainbow", "Rainy"]),
    row("Green Birdwing", "Suburbs", ["Dusk", "Night"]),
    row("Golden Jewel Scarab", "Forest Jump Puzzle", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Elegant Flower Beetle", "Whale Mountain", ["Day", "Dawn"], ["Rainbow", "Rainy"]),
    row("Minotaur Beetle", "Ruins", ["Day", "Dawn", "Night"]),
    row("Crimson Marsh Glider", "Onsen Mountain Lake", ["Day", "Dusk", "Night"], ["Rainbow"]),
    row("Blue Morpho", "Spirit-Oak Pine Forest", ["Day", "Dawn", "Dusk"], ["Rainbow", "Sunny"]),
    row("Picasso Bug", "Amethyst Beach", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Horned Beetle", "East Pier of Fishing Village", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Spanish Moon Moth", "Spirit-Oak Pine Forest", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Comet Moth", "Suburbs", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Cerulean Carpenter", "Fishing Village Square", ["Dusk", "Night"], ["Rainbow"]),
    row("Rainbow Stag Beetle", "[EVENT] Bait the Insects, Ruins"),
    row("Golden Stag Beetle", "Spirit-Oak Pine Forest", ["Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Conehead Mantis", "Ruins", ["Day", "Dusk"], ["Rainbow", "Rainy"]),
    row("Mother-Of-Pearl", "Windmill Flower Field", ["Dawn", "Night"], ["Rainbow", "Rainy"]),
    row("Hercules Beetle", "Onsen Mountain Lake, Crater Lake", ["Day", "Dusk"], ["Rainbow", "Sunny"]),
    row("Sunset Morpho", "Deer Tower", ["Day", "Dawn"], ["Rainbow"]),
    row("Morpho Helena", "Amethyst Beach", ["Day", "Dawn"], ["Rainbow"]),
    row("Bagworm Moth", "Deer Tower", ["Dusk", "Night"], ["Rainbow", "Sunny"]),
    row("Atlas Beetle", "Spirit-Oak Pine Forest", ["Day", "Dusk", "Night"], ["Rainbow"]),
    row("Orchid Mantis", "Ruins", ["Day", "Dawn"], ["Rainbow", "Rainy"]),
    row("Greater Asian Lady Beetle", "Whale Mountain", ["Day", "Dusk"], ["Rainbow"]),
    row("Goliath Beetle", "Deer Tower", ["Dusk", "Night"], ["Rainbow"]),
    row("Red Milkweed Locust", "Onsen Mountain Lake", ["Day", "Dawn"], ["Rainbow", "Rainy"]),
    row("Flamer Skimmer", "Meadow Lake", ["Day", "Dawn"], ["Rainbow"]),
    row("Purple Spotted Swallowtail", "Windmill Flower Field", ["Dawn", "Dusk", "Night"], ["Rainbow", "Rainy"]),
    row("Scarce Morpho", "Crater Lake", ["Day", "Dusk"], ["Rainbow"]),
    row("Green Metalwing", "Suburban Lake", ["Day", "Dusk"], ["Rainbow", "Rainy"]),
    row("Green Snail", "Forest Island", None, ["Rainbow", "Rainy"]),
    row("Glasswing Butterfly", "Whale Mountain", ["Day", "Dusk"], ["Rainbow"]),
    row("White-banded Erebus Moth", "Spirit-Oak Pine Forest"),
    row("Blue Milkweed Beetle", "Forest Island"),
    row("Agrias Butterfly", "Forest Jump Puzzle"),
    row("Morpho Luna", "Lost Bug Event"),
    row("Green Flower Chafer", "Deer Tower"),
    row("Golden Script Supervisor Bumblebee", "[EVENT] Script Supervisor Bee"),
    row("Purple Script Supervisor Bumblebee", "Windmill Flower Field"),
    row("Red Script Supervisor Bumblebee", "Whale Mountain"),
    row("Pink Script Supervisor Bumblebee", "Windmill Flower Field"),
    row("Green Script Supervisor Bumblebee", "Amethyst Beach"),
    row("Brown Rhinoceros Beetle", "Ruins", event="Echo of Ancients"),
    row("Iridescent Rhinoceros Beetle", "Iridescent Beetle Event", event="Echo of Ancients"),
    row("Grey-horned Firefly", "Windmill Flower Field", event="Echo of Ancients"),
    row("Purple-horned Firefly", "Whale Mountain", event="Echo of Ancients"),
    row("Pink-horned Firefly", "Amethyst Beach", event="Echo of Ancients"),
    row("Yellow Brick Large Red Damselfly", "Spirit-Oak Pine Forest"),
    row("Blue Brick Large Red Damselfly", "Deer Tower"),
    row("Purple Brick Large Red Damselfly", "Forest Island"),
    row("Colorful Brick Large Red Damselfly", "Special Brick Insect Event"),
    row("Pink Brick Large Red Damselfly", "Forest Jump Puzzle"),
    row("Frostspore Queen Alexandra's Birdwing", "[EVENT] Frostspore Butterfly, Deer Tower", event="Winter frost season"),
    row("Frostspore Purple Spotted Swallowtail", "Spirit-Oak Pine Forest, [EVENT] Frostspore Butterfly", [], None, "Winter frost season"),
    row("Frostspore Mother-of-Pearl", "Spirit-Oak Pine Forest, [EVENT] Frostspore Butterfly, Forest Jump Puzzle", [], [], "Winter frost season"),
    row("Frostspore Sulkowsky's Morpho", "Spirit-Oak Pine Forest, [EVENT] Frostspore Butterfly, Deer Tower, Forest Lake", [], [], "Winter frost season"),
]

WILDLIFE = [
    row("Alpaca", "Amethyst Beach", [], ["Sunny"]),
    row("Capybara", "Crater Lake, Ruins", [], ["Rainbow", "Rainy"]),
    row("Sika Deer", "Forest Lake", [], ["Sunny"]),
    row("Bunny", "Suburbs", [], ["Sunny"]),
    row("Ferret", "Rosy River", [], ["Rainbow"]),
    row("Sea Otter", "Fishing Village Square", [], ["Rainy"]),
    row("Panda", "Forest Jump Puzzle", [], ["Rainy"]),
    row("Fox", "Windmill Flower Field, Amethyst Beach, Meadow Lake", [], ["Rainbow"]),
    row("Dolphin", "Whalefall", [], ["Sunny"], "Call of Whales"),
    row("Maltese", "Forest Island", [], ["Sunny"], "Call of Whales"),
]

PAGES = {
    "Peces": ("https://www.heartodex.com/en/fish/", FISH),
    "Pájaros": ("https://www.heartodex.com/en/birds/", BIRDS),
    "Insectos": ("https://www.heartodex.com/en/insects/", INSECTS),
    "Fauna": ("https://www.heartodex.com/en/wild-animals/", WILDLIFE),
}


def build():
    entries = []
    seen = set()
    for category, (url, rows) in PAGES.items():
        key = {"Peces": "fish", "Pájaros": "bird", "Insectos": "insect", "Fauna": "wildlife"}[category]
        for item in rows:
            entry_id = slug(key, item["name"])
            if entry_id in seen:
                entry_id = f"{entry_id}-2"
            seen.add(entry_id)
            entries.append(
                {
                    "id": entry_id,
                    "category": category,
                    "name": item["name"],
                    "nameEs": None,
                    "location": item["location"],
                    "schedule": item["schedule"],
                    "weather": item["weather"],
                    "event": item["event"],
                    "source": "heartodex",
                    "sourceUrl": url,
                    "extractedOn": EXTRACTED,
                }
            )
    return {
        "source": "https://www.heartodex.com/",
        "extractedOn": EXTRACTED,
        "terms": "https://www.heartodex.com/en/terms/",
        "note": (
            "Extraído de las páginas índice, no de cada ficha. "
            "Heartodex pide no hacer scraping masivo. "
            "El listado repite 1–5 estrellas como filtro de la página; no es la rareza de cada criatura. "
            "Precio, sombra, cebo y nivel no aparecen en esos índices."
        ),
        "entries": entries,
    }


def main():
    payload = build()
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    counts = {}
    for entry in payload["entries"]:
        counts[entry["category"]] = counts.get(entry["category"], 0) + 1
    print(OUT)
    for name, count in counts.items():
        print(f"{name}: {count}")
    print(f"total: {len(payload['entries'])}")


if __name__ == "__main__":
    main()
