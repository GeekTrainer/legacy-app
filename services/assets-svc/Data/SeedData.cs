using Dapper;
using Microsoft.Data.Sqlite;

namespace Contoso.Assets.Data;

internal static class SeedData
{
    public static void Apply(SqliteConnection conn)
    {
        // Intentionally messy: inconsistent manufacturer casing ("Dell" vs "DELL" vs "Dell Inc."),
        // some missing serial numbers, mix of statuses. Mirrors real legacy data.
        var rows = new (string tag, string type, string manuf, string model, string? serial, string? purchase, string? warranty, string status, string? notes)[]
        {
            ("CON-LPT-001", "Laptop",         "Fabrikam",      "FB Latitude 7430",  "FB-7430-001", "2022-03-14", "2025-03-14", "assigned",  null),
            ("CON-LPT-002", "Laptop",         "FABRIKAM",      "FB Latitude 7430",  "FB-7430-002", "2022-03-14", "2025-03-14", "assigned",  null),
            ("CON-LPT-003", "Laptop",         "Fabrikam Inc.", "FB Latitude 5440",  "FB-5440-003", "2023-01-10", "2026-01-10", "available", null),
            ("CON-LPT-004", "Laptop",         "Wingtip",       "WT ThinkBook X1",   null,          "2021-08-22", "2024-08-22", "retired",   "Battery swelling"),
            ("CON-LPT-005", "Laptop",         "Litware",       "LW StudioBook 14",  "LW-SB-005",   "2023-06-01", "2026-06-01", "assigned",  null),
            ("CON-LPT-006", "Laptop",         "litware",       "LW StudioAir 13",   null,          "2023-02-18", "2026-02-18", "assigned",  null),
            ("CON-LPT-007", "Laptop",         "Proseware",     "PW EliteBook 840",  "PW-EB-007",   "2020-11-04", "2023-11-04", "lost",      "Lost on travel"),
            ("CON-MON-001", "Monitor",        "Fabrikam",      "FB U2723QE",        "FB-U2723-001","2022-05-12", "2025-05-12", "assigned",  null),
            ("CON-MON-002", "Monitor",        "Fabrikam",      "FB U2723QE",        "FB-U2723-002","2022-05-12", "2025-05-12", "assigned",  null),
            ("CON-MON-003", "Monitor",        "Adatum",        "AT 27UK850",        "AT-27-003",   "2021-09-09", "2024-09-09", "available", null),
            ("CON-MON-004", "Monitor",        "Trey",          "TR S27R650",        null,          "2020-04-22", "2023-04-22", "retired",   null),
            ("CON-KBD-001", "Keyboard",       "Tailspin",      "TS MX Keys",        "TS-MX-001",   "2023-01-05", "2025-01-05", "assigned",  null),
            ("CON-KBD-002", "Keyboard",       "Tailspin",      "TS MX Keys",        null,          "2023-01-05", "2025-01-05", "available", null),
            ("CON-KBD-003", "Keyboard",       "Northwind",     "NW K2",             "NW-K2-003",   "2022-11-30", "2024-11-30", "assigned",  null),
            ("CON-PHN-001", "Phone",          "Relecloud",     "RC IP Phone 8845",  "RC-8845-001", "2019-07-14", "2022-07-14", "available", null),
            ("CON-PHN-002", "Phone",          "Relecloud",     "RC IP Phone 8845",  "RC-8845-002", "2019-07-14", "2022-07-14", "assigned",  null),
            ("CON-BDG-001", "Keycard",        "Lucerne",       "LC iCLASS SE",      null,          "2023-04-01", null,         "assigned",  null),
            ("CON-BDG-002", "Keycard",        "Lucerne",       "LC iCLASS SE",      null,          "2023-04-01", null,         "assigned",  null),
            ("CON-BDG-003", "Keycard",        "Lucerne",       "LC iCLASS SE",      null,          "2023-04-01", null,         "available", null),
            ("CON-DCK-001", "Docking Station","Fabrikam",      "FB WD19TBS",        "FB-WD19-001", "2022-08-15", "2025-08-15", "assigned",  null),
            ("CON-DCK-002", "Docking Station","Fabrikam",      "FB WD19TBS",        "FB-WD19-002", "2022-08-15", "2025-08-15", "assigned",  null),
            ("CON-DCK-003", "Docking Station","Fabrikam",      "FB WD19TBS",        null,          "2022-08-15", "2025-08-15", "available", null),
        };

        const string sql = """
            INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number,
                                purchase_date, warranty_expiry, status, notes)
            VALUES (@tag, @type, @manuf, @model, @serial, @purchase, @warranty, @status, @notes);
        """;

        foreach (var r in rows)
        {
            conn.Execute(sql, new {
                tag = r.tag, type = r.type, manuf = r.manuf, model = r.model,
                serial = r.serial, purchase = r.purchase, warranty = r.warranty,
                status = r.status, notes = r.notes
            });
        }
    }
}
