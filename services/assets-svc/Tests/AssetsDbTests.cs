using Contoso.Assets.Data;
using Dapper;
using Microsoft.Data.Sqlite;
using System.Data;
using Xunit;

namespace Contoso.Assets.Tests;

public class AssetsDbTests
{
    [Fact]
    public void Initialize_creates_assets_table_and_seed_data()
    {
        var dbPath = Path.Combine(Path.GetTempPath(), $"assets-{Guid.NewGuid():N}.db");
        var db = new AssetsDb(dbPath);

        try
        {
            db.Initialize();

            using var conn = db.Open();
            var count = conn.ExecuteScalar<int>("SELECT COUNT(*) FROM assets;");
            var assignedCount = conn.ExecuteScalar<int>("SELECT COUNT(*) FROM assets WHERE status = 'assigned';");
            var retiredCount = conn.ExecuteScalar<int>("SELECT COUNT(*) FROM assets WHERE status = 'retired';");
            var lostCount = conn.ExecuteScalar<int>("SELECT COUNT(*) FROM assets WHERE status = 'lost';");
            var assetOne = conn.QuerySingle<string>("SELECT asset_tag FROM assets WHERE id = 1;");
            var activeAssignmentTargets = conn.ExecuteScalar<int>("""
                SELECT COUNT(*)
                FROM assets
                WHERE id IN (1, 2, 5, 6, 8, 9, 12, 14, 16, 17, 20, 21);
                """);

            Assert.Equal(21, count);
            Assert.Equal(12, assignedCount);
            Assert.Equal(1, retiredCount);
            Assert.Equal(1, lostCount);
            Assert.Equal("CON-LPT-001", assetOne);
            Assert.Equal(12, activeAssignmentTargets);
        }
        finally
        {
            if (File.Exists(dbPath))
            {
                File.Delete(dbPath);
            }
        }
    }

    [Fact]
    public void Initialize_is_idempotent()
    {
        var dbPath = Path.Combine(Path.GetTempPath(), $"assets-{Guid.NewGuid():N}.db");
        var db = new AssetsDb(dbPath);

        try
        {
            db.Initialize();
            db.Initialize();

            using var conn = db.Open();
            var count = conn.ExecuteScalar<int>("SELECT COUNT(*) FROM assets;");

            Assert.Equal(21, count);
        }
        finally
        {
            if (File.Exists(dbPath))
            {
                File.Delete(dbPath);
            }
        }
    }

    [Fact]
    public void Open_escapes_connection_string_special_characters_in_database_path()
    {
        var directory = Path.Combine(Path.GetTempPath(), $"assets-{Guid.NewGuid():N}");
        var dbPath = Path.Combine(directory, "assets;with-special-chars.db");
        var db = new AssetsDb(dbPath);

        try
        {
            using var conn = db.Open();

            Assert.Equal(ConnectionState.Open, conn.State);
            Assert.True(File.Exists(dbPath));
        }
        finally
        {
            if (Directory.Exists(directory))
            {
                Directory.Delete(directory, recursive: true);
            }
        }
    }
}
