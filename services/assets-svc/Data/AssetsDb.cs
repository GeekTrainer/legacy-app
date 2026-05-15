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

        var connection = new SqliteConnection($"Data Source={_dbPath}");
        connection.Open();
        return connection;
    }

    public void Initialize()
    {
        using var connection = Open();
        using var command = connection.CreateCommand();
        command.CommandText = """
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

            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
            SELECT 'AT-LAP-1001', 'Laptop', 'Lenovo', 'ThinkPad X1 Carbon', 'PF4A1001', '2023-03-15', '2026-03-15', 'assigned', 'Assigned to engineering.'
            WHERE NOT EXISTS (SELECT 1 FROM assets WHERE asset_tag = 'AT-LAP-1001');

            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
            SELECT 'AT-LAP-1002', 'Laptop', 'Dell', 'Latitude 7440', 'DL74401002', '2023-05-20', '2026-05-20', 'available', 'Ready for onboarding.'
            WHERE NOT EXISTS (SELECT 1 FROM assets WHERE asset_tag = 'AT-LAP-1002');

            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
            SELECT 'AT-MON-2001', 'Monitor', 'LG', 'UltraFine 27', 'LG27UF2001', '2022-09-01', '2025-09-01', 'assigned', 'Docking station desk setup.'
            WHERE NOT EXISTS (SELECT 1 FROM assets WHERE asset_tag = 'AT-MON-2001');

            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
            SELECT 'AT-PHN-3001', 'Phone', 'Apple', 'iPhone 14', 'APL143001', '2022-11-10', '2024-11-10', 'retired', 'Retired after refresh cycle.'
            WHERE NOT EXISTS (SELECT 1 FROM assets WHERE asset_tag = 'AT-PHN-3001');

            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes)
            SELECT 'AT-KBD-4001', 'Keyboard', 'Logitech', 'MX Keys', 'LOGMX4001', '2024-01-12', '2026-01-12', 'lost', 'Missing from inventory audit.'
            WHERE NOT EXISTS (SELECT 1 FROM assets WHERE asset_tag = 'AT-KBD-4001');
            """;
        command.ExecuteNonQuery();
    }
}
