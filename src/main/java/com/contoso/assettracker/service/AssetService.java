package com.contoso.assettracker.service;

import com.contoso.assettracker.model.Asset;
import com.contoso.assettracker.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AssetService {
    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    public List<Asset> findAll() { return assetRepository.findAll(); }
    public Asset findById(Long id) { return assetRepository.findById(id); }
    public void create(Asset asset) { assetRepository.create(asset); }

    public List<Asset> filterAssets(String type, String status) {
        if ((type == null || type.isBlank()) && (status == null || status.isBlank())) {
            return assetRepository.findAll();
        }
        String typeValue = type == null || type.isBlank() ? null : type;
        String statusValue = status == null || status.isBlank() ? null : status;
        return assetRepository.findByTypeAndStatus(typeValue, statusValue);
    }

    public long countAll() { return assetRepository.countAll(); }
    public long countByStatus(String status) { return assetRepository.countByStatus(status); }
    public List<Asset> findByEmployeeId(Long employeeId) { return assetRepository.findByEmployeeId(employeeId); }
}
