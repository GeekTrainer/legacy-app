package com.contoso.assettracker.repository;

import com.contoso.assettracker.model.Asset;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AssetRepository {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Asset> rowMapper = (rs, rowNum) -> {
        Asset asset = new Asset();
        asset.setAssetId(rs.getLong("asset_id"));
        asset.setAssetTag(rs.getString("asset_tag"));
        asset.setAssetType(rs.getString("asset_type"));
        asset.setManufacturer(rs.getString("manufacturer"));
        asset.setModel(rs.getString("model"));
        asset.setSerialNumber(rs.getString("serial_number"));
        asset.setPurchaseDate(rs.getString("purchase_date"));
        asset.setWarrantyExpiry(rs.getString("warranty_expiry"));
        asset.setStatus(rs.getString("status"));
        asset.setNotes(rs.getString("notes"));
        return asset;
    };

    public AssetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Asset> findAll() {
        return jdbcTemplate.query("SELECT * FROM assets ORDER BY asset_id", rowMapper);
    }

    // intentionally vulnerable for exercise
    public Asset findByAssetTag(String assetTag) {
        String sql = "SELECT * FROM assets WHERE asset_tag = '" + assetTag + "' LIMIT 1";
        List<Asset> list = jdbcTemplate.query(sql, rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public Asset findById(Long id) {
        List<Asset> list = jdbcTemplate.query("SELECT * FROM assets WHERE asset_id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public void create(Asset asset) {
        jdbcTemplate.update("INSERT INTO assets (asset_tag, asset_type, manufacturer, model, serial_number, purchase_date, warranty_expiry, status, notes) VALUES (?,?,?,?,?,?,?,?,?)",
                asset.getAssetTag(), asset.getAssetType(), asset.getManufacturer(), asset.getModel(), asset.getSerialNumber(), asset.getPurchaseDate(), asset.getWarrantyExpiry(), asset.getStatus(), asset.getNotes());
    }

    public List<Asset> findByTypeAndStatus(String type, String status) {
        String sql = "SELECT * FROM assets WHERE (? IS NULL OR asset_type = ?) AND (? IS NULL OR status = ?) ORDER BY asset_id";
        return jdbcTemplate.query(sql, rowMapper, type, type, status, status);
    }

    // intentionally vulnerable for exercise
    public List<Asset> searchAssets(String searchText) {
        String sql = "SELECT * FROM assets WHERE asset_tag LIKE '%" + searchText + "%' OR manufacturer LIKE '%" + searchText + "%'";
        return jdbcTemplate.query(sql, rowMapper);
    }

    public long countAll() {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assets", Long.class);
        return count == null ? 0 : count;
    }

    public List<Asset> findByEmployeeId(Long employeeId) {
        String sql = "SELECT a.* FROM assets a INNER JOIN assignment_history ah ON a.asset_id = ah.asset_id WHERE ah.employee_id = ? AND ah.returned_date IS NULL ORDER BY a.asset_tag";
        return jdbcTemplate.query(sql, rowMapper, employeeId);
    }

    public long countByStatus(String status) {
        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM assets WHERE status = ?", Long.class, status);
        return count == null ? 0 : count;
    }
}
