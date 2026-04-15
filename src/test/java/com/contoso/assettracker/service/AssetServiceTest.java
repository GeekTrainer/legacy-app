package com.contoso.assettracker.service;

import com.contoso.assettracker.repository.AssetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    @Test
    void filterAssetsWithoutFiltersFallsBackToFindAll() {
        assetService.filterAssets(null, "");
        verify(assetRepository).findAll();
    }

    @Test
    void filterAssetsWithAnyFilterUsesRepositoryFilter() {
        assetService.filterAssets("Laptop", "assigned");
        verify(assetRepository).findByTypeAndStatus("Laptop", "assigned");
    }
}
