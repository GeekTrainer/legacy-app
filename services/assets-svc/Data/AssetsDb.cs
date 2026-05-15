using Microsoft.Data.Sqlite;

namespace Contoso.Assets.Data;

public sealed class AssetsDb
{
    private readonly string _dbPath;

    public AssetsDb(string dbPath)
    {
        _dbPath = dbPath;
    }

    public SqliteConnection Open()
    {
        var directory = Path.GetDirectoryName(_dbPath);
        if (!string.IsNullOrWhiteSpace(directory))
        {
            Directory.CreateDirectory(directory);
        }

        var connectionString = new SqliteConnectionStringBuilder
        {
            DataSource = _dbPath
        }.ToString();

        var connection = new SqliteConnection(connectionString);
        connection.Open();
        return connection;
    }

    public void Initialize()
    {
        using var connection = Open();
        using var command = connection.CreateCommand();
        command.CommandText = SchemaAndSeedSql;
        command.ExecuteNonQuery();
    }

    private const string SchemaAndSeedSql = """
        CREATE TABLE IF NOT EXISTS assets (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            asset_tag TEXT NOT NULL UNIQUE,
            asset_type TEXT NOT NULL,
            manufacturer TEXT NOT NULL,
            model TEXT NOT NULL,
            serial_number TEXT,
            purchase_date TEXT,
            warranty_expiry TEXT,
            status TEXT NOT NULL,
            notes TEXT
        );

        INSERT OR IGNORE INTO assets
            (id, asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
        VALUES
            (1,  'CON-LPT-001', 'Laptop',          'Dell',     'Latitude 7420',        'DL7420001',  '2022-03-15', '2025-03-15', 'assigned',  'Assigned to Maya Patel.'),
            (2,  'CON-MON-001', 'Monitor',         'LG',       'UltraFine 27',         'LG27UF001',  '2022-03-15', '2025-03-15', 'assigned',  'Dual-monitor request.'),
            (3,  'CON-LPT-002', 'Laptop',          'Lenovo',   'ThinkPad X1 Carbon',   'PF4A0002',   '2022-04-11', '2025-04-11', 'available', 'Ready for reassignment.'),
            (4,  'CON-LPT-004', 'Laptop',          'Dell',     'Latitude 7400',        'DL7400004',  '2021-09-01', '2024-09-01', 'retired',   'Battery swelling — withdrawn.'),
            (5,  'CON-LPT-005', 'Laptop',          'Apple',    'MacBook Pro 14',       'C02LPT005',  '2023-06-05', '2026-06-05', 'assigned',  'M-series upgrade.'),
            (6,  'CON-LPT-006', 'Laptop',          'Lenovo',   'ThinkPad T14',         'PF4A0006',   '2023-02-22', '2026-02-22', 'assigned',  NULL),
            (7,  'CON-LPT-007', 'Laptop',          'Dell',     'Latitude 7390',        'DL7390007',  '2020-11-10', '2023-11-10', 'lost',      'Reported lost while travelling.'),
            (8,  'CON-MON-002', 'Monitor',         'Dell',     'UltraSharp U2723QE',   'DU2723002',  '2022-05-15', '2025-05-15', 'assigned',  NULL),
            (9,  'CON-KBD-001', 'Keyboard',        'Logitech', 'MX Keys',              'LOGMX001',   '2022-05-15', '2025-05-15', 'assigned',  NULL),
            (10, 'CON-PHN-001', 'Phone',           'Apple',    'iPhone 14',            'APL14001',   '2023-01-09', '2025-01-09', 'available', NULL),
            (11, 'CON-KBD-002', 'Keyboard',        'Logitech', 'MX Keys S',            'LOGMXS002',  '2020-05-01', '2023-05-01', 'available', 'Returned by Hiroshi Tanaka.'),
            (12, 'CON-DCK-001', 'Docking Station', 'Dell',     'WD19TBS',              'WD19TBS001', '2023-01-08', '2026-01-08', 'assigned',  NULL),
            (13, 'CON-BAD-001', 'Keycard',         'HID',      'Seos Keycard',         'HID001',     '2023-01-10', NULL,         'available', NULL),
            (14, 'CON-MON-003', 'Monitor',         'Samsung',  'ViewFinity S8',        'SMS8003',    '2022-12-01', '2025-12-01', 'assigned',  NULL),
            (15, 'CON-PHN-002', 'Phone',           'Google',   'Pixel 8',              'GOOGPX002',  '2023-10-05', '2025-10-05', 'available', NULL),
            (16, 'CON-LPT-008', 'Laptop',          'HP',       'EliteBook 840',        'HPE840008',  '2023-04-10', '2026-04-10', 'assigned',  NULL),
            (17, 'CON-DCK-002', 'Docking Station', 'Dell',     'WD19TBS',              'WD19TBS002', '2023-04-10', '2026-04-10', 'assigned',  'Dock for Diego Hernandez.'),
            (18, 'CON-BAD-002', 'Keycard',         'HID',      'Seos Keycard',         'HID002',     '2023-04-12', NULL,         'available', NULL),
            (19, 'CON-MON-004', 'Monitor',         'LG',       '32UP550',              'LG32UP004',  '2024-01-12', '2027-01-12', 'available', NULL),
            (20, 'CON-LPT-009', 'Laptop',          'Lenovo',   'ThinkPad P1',          'PF4A0009',   '2022-08-20', '2025-08-20', 'assigned',  NULL),
            (21, 'CON-LPT-010', 'Laptop',          'Dell',     'Latitude 7440',        'DL7440010',  '2022-08-20', '2025-08-20', 'assigned',  NULL);
        """;
}
