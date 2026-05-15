package com.tfg.tcgserver.controllers;

import com.tfg.tcgserver.api.dto.OpenPackRequest;
import com.tfg.tcgserver.models.shop.OpenPackResult;
import com.tfg.tcgserver.models.shop.PackProduct;
import com.tfg.tcgserver.service.shop.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/shop")
public class ShopController extends AuthenticatedController {

    private final ShopService shopService;

    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @GetMapping("/packs")
    public CompletableFuture<List<PackProduct>> getPacks() {
        return shopService.getPacks();
    }

    @PostMapping("/packs/{packId}/open")
    public CompletableFuture<OpenPackResult> openPack(
            @PathVariable("packId") String packId,
            @Valid @RequestBody OpenPackRequest request,
            HttpServletRequest servletRequest
    ) {
        requireSameUser(request.uid(), servletRequest);
        return shopService.openPack(request.uid(), packId);
    }
}
