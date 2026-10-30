package com.construction.construction_management.controller;

import com.construction.construction_management.dto.GoodsReceiptRequest;
import com.construction.construction_management.dto.MaterialItemRequest;
import com.construction.construction_management.dto.MaterialItemResponse;
import com.construction.construction_management.dto.MaterialRequestRequest;
import com.construction.construction_management.dto.MaterialRequestResponse;
import com.construction.construction_management.dto.MaterialRequestReviewRequest;
import com.construction.construction_management.dto.PurchaseOrderRequest;
import com.construction.construction_management.dto.PurchaseOrderResponse;
import com.construction.construction_management.dto.StockLedgerResponse;
import com.construction.construction_management.dto.StockMovementRequest;
import com.construction.construction_management.dto.StockMovementResponse;
import com.construction.construction_management.dto.SupplierRequest;
import com.construction.construction_management.dto.SupplierResponse;
import com.construction.construction_management.service.MaterialsService;
import com.construction.construction_management.util.Pages;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects/{projectId}")
public class MaterialsController {

    private final MaterialsService materialsService;

    public MaterialsController(MaterialsService materialsService) {
        this.materialsService = materialsService;
    }

    @GetMapping("/stock-ledger")
    public StockLedgerResponse stockLedger(@PathVariable int projectId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "100") int size) {
        return materialsService.stockLedger(projectId, Pages.of(page, size));
    }

    @PostMapping("/materials")
    public MaterialItemResponse createMaterial(@PathVariable int projectId,
                                               @Valid @RequestBody MaterialItemRequest request) {
        return materialsService.createMaterial(projectId, request);
    }

    @PutMapping("/materials/{materialId}")
    public MaterialItemResponse updateMaterial(@PathVariable int projectId,
                                               @PathVariable int materialId,
                                               @Valid @RequestBody MaterialItemRequest request) {
        return materialsService.updateMaterial(projectId, materialId, request);
    }

    @GetMapping("/suppliers")
    public List<SupplierResponse> listSuppliers(@PathVariable int projectId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "100") int size) {
        return materialsService.listSuppliers(projectId, Pages.of(page, size));
    }

    @PostMapping("/suppliers")
    public SupplierResponse createSupplier(@PathVariable int projectId,
                                           @Valid @RequestBody SupplierRequest request) {
        return materialsService.createSupplier(projectId, request);
    }

    @PutMapping("/suppliers/{supplierId}")
    public SupplierResponse updateSupplier(@PathVariable int projectId,
                                           @PathVariable int supplierId,
                                           @Valid @RequestBody SupplierRequest request) {
        return materialsService.updateSupplier(projectId, supplierId, request);
    }

    @GetMapping("/material-requests")
    public List<MaterialRequestResponse> listRequests(@PathVariable int projectId,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "100") int size) {
        return materialsService.listRequests(projectId, Pages.of(page, size));
    }

    @PostMapping("/material-requests")
    public MaterialRequestResponse createRequest(@PathVariable int projectId,
                                                 @Valid @RequestBody MaterialRequestRequest request) {
        return materialsService.createRequest(projectId, request);
    }

    @PostMapping("/material-requests/{requestId}/approve")
    public MaterialRequestResponse approveRequest(@PathVariable int projectId,
                                                  @PathVariable int requestId,
                                                  @Valid @RequestBody(required = false)
                                                  MaterialRequestReviewRequest request) {
        return materialsService.approveRequest(projectId, requestId, request);
    }

    @PostMapping("/material-requests/{requestId}/reject")
    public MaterialRequestResponse rejectRequest(@PathVariable int projectId,
                                                 @PathVariable int requestId,
                                                 @Valid @RequestBody(required = false)
                                                 MaterialRequestReviewRequest request) {
        return materialsService.rejectRequest(projectId, requestId, request);
    }

    @PostMapping("/material-requests/{requestId}/cancel")
    public MaterialRequestResponse cancelRequest(@PathVariable int projectId,
                                                 @PathVariable int requestId) {
        return materialsService.cancelRequest(projectId, requestId);
    }

    @GetMapping("/purchase-orders")
    public List<PurchaseOrderResponse> listOrders(@PathVariable int projectId,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "100") int size) {
        return materialsService.listOrders(projectId, Pages.of(page, size));
    }

    @PostMapping("/purchase-orders")
    public PurchaseOrderResponse createOrder(@PathVariable int projectId,
                                             @Valid @RequestBody PurchaseOrderRequest request) {
        return materialsService.createOrder(projectId, request);
    }

    @PostMapping("/purchase-orders/{orderId}/receive")
    public PurchaseOrderResponse receiveOrder(@PathVariable int projectId,
                                              @PathVariable int orderId,
                                              @Valid @RequestBody GoodsReceiptRequest request) {
        return materialsService.receiveOrder(projectId, orderId, request);
    }

    @PostMapping("/purchase-orders/{orderId}/cancel")
    public PurchaseOrderResponse cancelOrder(@PathVariable int projectId,
                                             @PathVariable int orderId) {
        return materialsService.cancelOrder(projectId, orderId);
    }

    @PostMapping("/stock-movements")
    public StockMovementResponse recordMovement(@PathVariable int projectId,
                                                @Valid @RequestBody StockMovementRequest request) {
        return materialsService.recordMovement(projectId, request);
    }
}
