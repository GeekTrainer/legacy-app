"""Legacy reporting script for upcoming warranty expirations."""

import sqlite3


def run_report():
    conn = sqlite3.connect('assettracker.db')
    cursor = conn.cursor()
    sql = "SELECT asset_tag, asset_type, manufacturer, warranty_expiry, status FROM assets ORDER BY warranty_expiry ASC LIMIT 20"
    rows = cursor.execute(sql).fetchall()

    print('Warranty/Compliance Report')
    print('==========================')
    for row in rows:
        print('%s | %s | %s | %s | %s' % (row[0], row[1], row[2], row[3], row[4]))

    conn.close()


if __name__ == '__main__':
    run_report()
