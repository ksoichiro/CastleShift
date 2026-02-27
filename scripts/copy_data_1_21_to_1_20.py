#!/usr/bin/env python3
"""
Data File Copier: 1.21.1 → 1.20.1

Copies JSON data files from 1.21.1 to 1.20.1, renaming top-level directories
where Minecraft 1.21 changed from plural to singular naming.

Key renames:
- loot_table → loot_tables
- structure → structures
- advancement → advancements
- recipe → recipes
- predicate → predicates
- item_modifier → item_modifiers

Directories not in the mapping (worldgen, tags, etc.) are copied as-is.
.nbt files are skipped (handled by convert_nbt_1_21_to_1_20.py).

Usage:
    python3 copy_data_1_21_to_1_20.py <input_dir> <output_dir>
    python3 copy_data_1_21_to_1_20.py --dry-run <input_dir> <output_dir>
"""

import sys
import argparse
import shutil
from pathlib import Path

# 1.21 (singular) → 1.20 (plural)
DIR_RENAME_MAP = {
    "loot_table": "loot_tables",
    "structure": "structures",
    "advancement": "advancements",
    "recipe": "recipes",
    "predicate": "predicates",
    "item_modifier": "item_modifiers",
}

SKIP_EXTENSIONS = {".nbt"}
SKIP_FILENAMES = {".DS_Store"}


def rename_top_level_dir(rel_path: Path) -> Path:
    """Rename the first path component if it matches the rename mapping."""
    parts = rel_path.parts
    if not parts:
        return rel_path
    top = parts[0]
    if top in DIR_RENAME_MAP:
        return Path(DIR_RENAME_MAP[top], *parts[1:])
    return rel_path


def copy_data_files(input_dir: str, output_dir: str, dry_run: bool = False) -> int:
    """Copy data files from input_dir to output_dir with directory renaming.

    Returns the number of files copied.
    """
    input_path = Path(input_dir)
    output_path = Path(output_dir)

    if not input_path.exists():
        print(f"Error: Input directory does not exist: {input_dir}", file=sys.stderr)
        return 0

    # Collect all files, excluding .nbt
    all_files = sorted(
        f for f in input_path.rglob("*")
        if f.is_file() and f.suffix not in SKIP_EXTENSIONS and f.name not in SKIP_FILENAMES
    )

    if not all_files:
        print(f"Warning: No copyable files found in {input_dir}")
        return 0

    print(f"Copying {len(all_files)} data file(s)...")
    print(f"  Input:  {input_dir}")
    print(f"  Output: {output_dir}")
    if dry_run:
        print("  (dry-run mode)")
    print()

    copied = 0
    for src_file in all_files:
        rel = src_file.relative_to(input_path)
        dest_rel = rename_top_level_dir(rel)
        dest_file = output_path / dest_rel

        renamed = ""
        if rel != dest_rel:
            renamed = f"  (renamed: {rel.parts[0]} -> {dest_rel.parts[0]})"

        print(f"  {dest_rel}{renamed}")

        if not dry_run:
            dest_file.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(str(src_file), str(dest_file))
        copied += 1

    print()
    print(f"{'Would copy' if dry_run else 'Copied'} {copied} file(s)")
    return copied


def main():
    parser = argparse.ArgumentParser(
        description="Copy Minecraft data files from 1.21.1 to 1.20.1 with directory renaming"
    )
    parser.add_argument(
        "--dry-run", action="store_true",
        help="Show what would be copied without actually copying"
    )
    parser.add_argument("input", help="Input data directory (1.21.1)")
    parser.add_argument("output", help="Output data directory (1.20.1)")

    args = parser.parse_args()

    copied = copy_data_files(args.input, args.output, dry_run=args.dry_run)
    sys.exit(0 if copied > 0 else 1)


if __name__ == "__main__":
    main()
