"""Legacy bulk import utility for assets from CSV."""

import csv
import sqlite3
import sys


def import_assets(csv_path):
    conn = sqlite3.connect('assettracker.db')
    cursor = conn.cursor()
    imported = 0

    with open(csv_path, 'r') as handle:
        reader = csv.DictReader(handle)
        for row in reader:
            cursor.execute(
                "INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                (
                    row.get('asset_tag'),
                    row.get('asset_type'),
                    row.get('manufacturer'),
                    row.get('model'),
                    row.get('serial_number'),
                    row.get('purchase_date'),
                    row.get('warranty_expiry'),
                    row.get('status') or 'available',
                    row.get('notes')
                )
            )
            imported += 1

    conn.commit()
    conn.close()
    print('Imported %s rows from %s' % (imported, csv_path))


if __name__ == '__main__':
    if len(sys.argv) != 2:
        print('Usage: python scripts/import_assets.py scripts/data/sample_import.csv')
        sys.exit(1)
    import_assets(sys.argv[1])
