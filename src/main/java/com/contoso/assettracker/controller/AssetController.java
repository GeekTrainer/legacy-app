package com.contoso.assettracker.controller;

import com.contoso.assettracker.model.Asset;
import com.contoso.assettracker.service.AssetService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AssetController {
    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping("/assets")
    public String listAssets(@RequestParam(required = false) String type,
                             @RequestParam(required = false) String status,
                             Model model) {
        model.addAttribute("assets", assetService.filterAssets(type, status));
        model.addAttribute("selectedType", type);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("asset", new Asset());
        return "assets";
    }

    @GetMapping("/assets/detail")
    public String assetDetail(@RequestParam Long id, Model model) {
        model.addAttribute("asset", assetService.findById(id));
        return "asset-detail";
    }

    @PostMapping("/assets")
    public String createAsset(@ModelAttribute Asset asset, Model model) {
        assetService.create(asset);
        model.addAttribute("message", "Asset created.");
        return "redirect:/assets";
    }
}
