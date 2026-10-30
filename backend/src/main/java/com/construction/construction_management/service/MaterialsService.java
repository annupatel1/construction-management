package com.construction.construction_management.service;

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
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MaterialsService {

    StockLedgerResponse stockLedger(int projectId, Pageable pageable);

    MaterialItemResponse createMaterial(int projectId, MaterialItemRequest request);

    MaterialItemResponse updateMaterial(int projectId, int materialId, MaterialItemRequest request);

    List<SupplierResponse> listSuppliers(int projectId, Pageable pageable);

    SupplierResponse createSupplier(int projectId, SupplierRequest request);

    SupplierResponse updateSupplier(int projectId, int supplierId, SupplierRequest request);

    List<MaterialRequestResponse> listRequests(int projectId, Pageable pageable);

    MaterialRequestResponse createRequest(int projectId, MaterialRequestRequest request);

    MaterialRequestResponse approveRequest(int projectId, int requestId,
                                          MaterialRequestReviewRequest request);

    MaterialRequestResponse rejectRequest(int projectId, int requestId,
                                         MaterialRequestReviewRequest request);

    MaterialRequestResponse cancelRequest(int projectId, int requestId);

    List<PurchaseOrderResponse> listOrders(int projectId, Pageable pageable);

    PurchaseOrderResponse createOrder(int projectId, PurchaseOrderRequest request);

    PurchaseOrderResponse receiveOrder(int projectId, int orderId, GoodsReceiptRequest request);

    PurchaseOrderResponse cancelOrder(int projectId, int orderId);

    StockMovementResponse recordMovement(int projectId, StockMovementRequest request);
}
