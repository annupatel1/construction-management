package com.construction.construction_management.service.impl;

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
import com.construction.construction_management.entity.Expense;
import com.construction.construction_management.entity.GoodsReceipt;
import com.construction.construction_management.entity.MaterialItem;
import com.construction.construction_management.entity.MaterialRequest;
import com.construction.construction_management.entity.MaterialRequestStatus;
import com.construction.construction_management.entity.NotificationType;
import com.construction.construction_management.entity.Project;
import com.construction.construction_management.entity.PurchaseOrder;
import com.construction.construction_management.entity.PurchaseOrderStatus;
import com.construction.construction_management.entity.RoleName;
import com.construction.construction_management.entity.StockMovement;
import com.construction.construction_management.entity.StockMovementType;
import com.construction.construction_management.entity.Supplier;
import com.construction.construction_management.entity.User;
import com.construction.construction_management.exception.BadRequestException;
import com.construction.construction_management.exception.ResourceNotFoundException;
import com.construction.construction_management.repository.ExpenseRepository;
import com.construction.construction_management.repository.MaterialItemRepository;
import com.construction.construction_management.repository.MaterialRequestRepository;
import com.construction.construction_management.repository.PurchaseOrderRepository;
import com.construction.construction_management.repository.StockMovementRepository;
import com.construction.construction_management.repository.SupplierRepository;
import com.construction.construction_management.security.CurrentUser;
import com.construction.construction_management.service.AccessGuard;
import com.construction.construction_management.service.AuditService;
import com.construction.construction_management.service.MaterialsService;
import com.construction.construction_management.service.NotificationService;
import com.construction.construction_management.util.Money;
import com.construction.construction_management.util.Pages;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaterialsServiceImpl implements MaterialsService {

    private static final List<StockMovementType> INBOUND =
            List.of(StockMovementType.RECEIPT, StockMovementType.RETURN);

    private final MaterialItemRepository materialRepository;
    private final SupplierRepository supplierRepository;
    private final MaterialRequestRepository requestRepository;
    private final PurchaseOrderRepository orderRepository;
    private final StockMovementRepository movementRepository;
    private final ExpenseRepository expenseRepository;
    private final CurrentUser currentUser;
    private final AccessGuard guard;
    private final AuditService auditService;
    private final NotificationService notificationService;

    public MaterialsServiceImpl(MaterialItemRepository materialRepository,
                                SupplierRepository supplierRepository,
                                MaterialRequestRepository requestRepository,
                                PurchaseOrderRepository orderRepository,
                                StockMovementRepository movementRepository,
                                ExpenseRepository expenseRepository,
                                CurrentUser currentUser,
                                AccessGuard guard,
                                AuditService auditService,
                                NotificationService notificationService) {
        this.materialRepository = materialRepository;
        this.supplierRepository = supplierRepository;
        this.requestRepository = requestRepository;
        this.orderRepository = orderRepository;
        this.movementRepository = movementRepository;
        this.expenseRepository = expenseRepository;
        this.currentUser = currentUser;
        this.guard = guard;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional(readOnly = true)
    public StockLedgerResponse stockLedger(int projectId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        boolean procurement = canViewProcurement();
        List<MaterialItem> items = (procurement
                ? materialRepository.findByProjectIdOrderByNameAscIdAsc(projectId,
                        PageRequest.of(0, Pages.MAX_SIZE))
                : materialRepository.findByProjectIdAndActiveTrueOrderByNameAscIdAsc(projectId,
                        PageRequest.of(0, Pages.MAX_SIZE)))
                .getContent();
        List<MaterialItemResponse> materials = items.stream()
                .map(item -> MaterialItemResponse.from(item, balance(projectId, item.getId())))
                .toList();
        List<StockMovementResponse> movements = movementRepository
                .findByProjectIdOrderByMovementDateDescIdDesc(projectId, pageable)
                .map(movement -> StockMovementResponse.from(movement, procurement))
                .getContent();
        long lowStockCount = materials.stream()
                .filter(material -> material.isActive() && material.isLowStock())
                .count();
        return new StockLedgerResponse(materials, movements, lowStockCount);
    }

    @Override
    @Transactional
    public MaterialItemResponse createMaterial(int projectId, MaterialItemRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        String name = request.getName().trim();
        String unit = request.getUnit().trim();
        if (materialRepository.existsByProjectIdAndNameIgnoreCaseAndUnitIgnoreCase(
                projectId, name, unit)) {
            throw new BadRequestException("A material with this name and unit already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        MaterialItem material = new MaterialItem();
        material.setProject(project);
        material.setName(name);
        material.setCategory(request.getCategory());
        material.setUnit(unit);
        material.setMinimumStock(Money.quantity(Money.nvl(request.getMinimumStock())));
        material.setActive(request.getActive() == null || request.getActive());
        material.setCreatedAt(now);
        material.setUpdatedAt(now);
        MaterialItem saved = materialRepository.save(material);
        auditService.record("MATERIAL_CREATED", "MaterialItem", saved.getId(),
                "projectId=" + projectId + "; name=" + saved.getName() + "; unit=" + saved.getUnit());
        return MaterialItemResponse.from(saved, Money.quantity(BigDecimal.ZERO));
    }

    @Override
    @Transactional
    public MaterialItemResponse updateMaterial(int projectId, int materialId,
                                               MaterialItemRequest request) {
        guard.projectForSiteManagement(projectId);
        MaterialItem material = loadMaterial(projectId, materialId);
        String name = request.getName().trim();
        String unit = request.getUnit().trim();
        if (materialRepository.existsByProjectIdAndNameIgnoreCaseAndUnitIgnoreCaseAndIdNot(
                projectId, name, unit, materialId)) {
            throw new BadRequestException("A material with this name and unit already exists");
        }
        if (!unit.equalsIgnoreCase(material.getUnit())
                && movementRepository.existsByMaterialId(materialId)) {
            throw new BadRequestException("Unit cannot change after stock movements are recorded");
        }
        material.setName(name);
        material.setCategory(request.getCategory());
        material.setUnit(unit);
        if (request.getMinimumStock() != null) {
            material.setMinimumStock(Money.quantity(request.getMinimumStock()));
        }
        if (request.getActive() != null) {
            material.setActive(request.getActive());
        }
        material.setUpdatedAt(LocalDateTime.now());
        MaterialItem saved = materialRepository.save(material);
        auditService.record("MATERIAL_UPDATED", "MaterialItem", saved.getId(),
                "projectId=" + projectId + "; name=" + saved.getName()
                        + "; active=" + saved.isActive());
        return MaterialItemResponse.from(saved, balance(projectId, materialId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SupplierResponse> listSuppliers(int projectId, Pageable pageable) {
        guard.projectForOwnerOrContractor(projectId);
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id")));
        return supplierRepository.findByProjectIdOrderByNameAscIdAsc(projectId, page)
                .map(SupplierResponse::from)
                .getContent();
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(int projectId, SupplierRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        LocalDateTime now = LocalDateTime.now();
        Supplier supplier = new Supplier();
        supplier.setProject(project);
        applySupplier(supplier, request);
        supplier.setActive(request.getActive() == null || request.getActive());
        supplier.setCreatedAt(now);
        supplier.setUpdatedAt(now);
        Supplier saved = supplierRepository.save(supplier);
        auditService.record("SUPPLIER_CREATED", "Supplier", saved.getId(),
                "projectId=" + projectId + "; name=" + saved.getName());
        return SupplierResponse.from(saved);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(int projectId, int supplierId, SupplierRequest request) {
        guard.projectForSiteManagement(projectId);
        Supplier supplier = loadSupplier(projectId, supplierId);
        applySupplier(supplier, request);
        if (request.getActive() != null) {
            supplier.setActive(request.getActive());
        }
        supplier.setUpdatedAt(LocalDateTime.now());
        Supplier saved = supplierRepository.save(supplier);
        auditService.record("SUPPLIER_UPDATED", "Supplier", saved.getId(),
                "projectId=" + projectId + "; active=" + saved.isActive());
        return SupplierResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialRequestResponse> listRequests(int projectId, Pageable pageable) {
        guard.projectForSiteRead(projectId);
        return requestRepository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId, pageable)
                .map(MaterialRequestResponse::from)
                .getContent();
    }

    @Override
    @Transactional
    public MaterialRequestResponse createRequest(int projectId, MaterialRequestRequest request) {
        Project project = guard.projectForSiteRead(projectId);
        MaterialItem material = loadMaterial(projectId, request.getMaterialId());
        if (!material.isActive()) {
            throw new BadRequestException("This material is no longer active");
        }
        if (request.getNeededBy() != null && request.getNeededBy().isBefore(LocalDate.now())) {
            throw new BadRequestException("Needed-by date cannot be in the past");
        }
        BigDecimal quantity = Money.quantity(request.getQuantity());
        if (quantity.signum() <= 0) {
            throw new BadRequestException("Quantity must be at least 0.001");
        }
        LocalDateTime now = LocalDateTime.now();
        MaterialRequest materialRequest = new MaterialRequest();
        materialRequest.setProject(project);
        materialRequest.setMaterial(material);
        materialRequest.setRequestedBy(currentUser.get());
        materialRequest.setQuantity(quantity);
        materialRequest.setNeededBy(request.getNeededBy());
        materialRequest.setNotes(clean(request.getNotes()));
        materialRequest.setStatus(MaterialRequestStatus.REQUESTED);
        materialRequest.setCreatedAt(now);
        materialRequest.setUpdatedAt(now);
        MaterialRequest saved = requestRepository.save(materialRequest);
        auditService.record("MATERIAL_REQUESTED", "MaterialRequest", saved.getId(),
                "projectId=" + projectId + "; material=" + material.getName()
                        + "; quantity=" + saved.getQuantity());
        notifyManagers(project, NotificationType.MATERIAL_REQUESTED, "Material requested",
                currentUser.get().getName() + " requested " + saved.getQuantity().toPlainString()
                        + " " + material.getUnit() + " of " + material.getName(),
                "/projects/" + projectId);
        return MaterialRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public MaterialRequestResponse approveRequest(int projectId, int requestId,
                                                  MaterialRequestReviewRequest request) {
        guard.projectForSiteManagement(projectId);
        MaterialRequest materialRequest = lockRequest(projectId, requestId);
        requireRequested(materialRequest);
        materialRequest.setStatus(MaterialRequestStatus.APPROVED);
        review(materialRequest, request == null ? null : request.getReviewNote());
        MaterialRequest saved = requestRepository.save(materialRequest);
        auditService.record("MATERIAL_REQUEST_APPROVED", "MaterialRequest", saved.getId(),
                "projectId=" + projectId);
        notifyRequestReviewed(saved);
        return MaterialRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public MaterialRequestResponse rejectRequest(int projectId, int requestId,
                                                 MaterialRequestReviewRequest request) {
        guard.projectForSiteManagement(projectId);
        MaterialRequest materialRequest = lockRequest(projectId, requestId);
        requireRequested(materialRequest);
        materialRequest.setStatus(MaterialRequestStatus.REJECTED);
        review(materialRequest, request == null ? null : request.getReviewNote());
        MaterialRequest saved = requestRepository.save(materialRequest);
        auditService.record("MATERIAL_REQUEST_REJECTED", "MaterialRequest", saved.getId(),
                "projectId=" + projectId);
        notifyRequestReviewed(saved);
        return MaterialRequestResponse.from(saved);
    }

    @Override
    @Transactional
    public MaterialRequestResponse cancelRequest(int projectId, int requestId) {
        guard.projectForSiteRead(projectId);
        MaterialRequest materialRequest = lockRequest(projectId, requestId);
        if (materialRequest.getStatus() != MaterialRequestStatus.REQUESTED
                && materialRequest.getStatus() != MaterialRequestStatus.APPROVED) {
            throw new BadRequestException("Only a requested or approved material can be cancelled");
        }
        if (materialRequest.getStatus() == MaterialRequestStatus.APPROVED
                && orderRepository.existsByMaterialRequestIdAndStatusNot(
                        materialRequest.getId(), PurchaseOrderStatus.CANCELLED)) {
            throw new BadRequestException("This request already has an active purchase order");
        }
        boolean manager = isAdminOrContractor();
        boolean requester = materialRequest.getRequestedBy().getId() == currentUser.id();
        if (!manager && !requester) {
            throw new AccessDeniedException("Only the requester or the contractor can cancel this request");
        }
        materialRequest.setStatus(MaterialRequestStatus.CANCELLED);
        materialRequest.setUpdatedAt(LocalDateTime.now());
        MaterialRequest saved = requestRepository.save(materialRequest);
        auditService.record("MATERIAL_REQUEST_CANCELLED", "MaterialRequest", saved.getId(),
                "projectId=" + projectId);
        return MaterialRequestResponse.from(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PurchaseOrderResponse> listOrders(int projectId, Pageable pageable) {
        guard.projectForOwnerOrContractor(projectId);
        return orderRepository.findByProjectIdOrderByCreatedAtDescIdDesc(projectId, pageable)
                .map(PurchaseOrderResponse::from)
                .getContent();
    }

    @Override
    @Transactional
    public PurchaseOrderResponse createOrder(int projectId, PurchaseOrderRequest request) {
        Project project = guard.projectForSiteManagement(projectId);
        MaterialItem material = loadMaterial(projectId, request.getMaterialId());
        if (!material.isActive()) {
            throw new BadRequestException("This material is no longer active");
        }
        Supplier supplier = loadSupplier(projectId, request.getSupplierId());
        if (!supplier.isActive()) {
            throw new BadRequestException("This supplier is no longer active");
        }

        BigDecimal quantity = Money.quantity(request.getQuantity());
        BigDecimal unitPrice = Money.money(request.getUnitPrice());
        if (quantity.signum() <= 0 || unitPrice.signum() <= 0) {
            throw new BadRequestException("Quantity and unit price must be greater than 0");
        }
        BigDecimal totalAmount = Money.multiply(quantity, unitPrice);
        if (totalAmount.signum() <= 0) {
            throw new BadRequestException("Purchase order value is too small");
        }

        MaterialRequest materialRequest = null;
        if (request.getMaterialRequestId() != null) {
            materialRequest = lockRequest(projectId, request.getMaterialRequestId());
            if (materialRequest.getStatus() != MaterialRequestStatus.APPROVED) {
                throw new BadRequestException("Only an approved material request can be ordered");
            }
            if (orderRepository.existsByMaterialRequestIdAndStatusNot(
                    materialRequest.getId(), PurchaseOrderStatus.CANCELLED)) {
                throw new BadRequestException("A purchase order already exists for this request");
            }
            if (materialRequest.getMaterial().getId() != material.getId()) {
                throw new BadRequestException("Material does not match the approved request");
            }
            if (materialRequest.getQuantity().compareTo(quantity) != 0) {
                throw new BadRequestException("Purchase order quantity must match the approved request");
            }
        }

        LocalDate orderedDate = request.getOrderedDate() == null
                ? LocalDate.now() : request.getOrderedDate();
        if (request.getExpectedDate() != null && request.getExpectedDate().isBefore(orderedDate)) {
            throw new BadRequestException("Expected date cannot be before the order date");
        }

        LocalDateTime now = LocalDateTime.now();
        PurchaseOrder order = new PurchaseOrder();
        order.setProject(project);
        order.setMaterial(material);
        order.setSupplier(supplier);
        order.setMaterialRequest(materialRequest);
        order.setQuantity(quantity);
        order.setUnitPrice(unitPrice);
        order.setTotalAmount(totalAmount);
        order.setReceivedQuantity(Money.quantity(BigDecimal.ZERO));
        order.setStatus(PurchaseOrderStatus.ORDERED);
        order.setOrderedDate(orderedDate);
        order.setExpectedDate(request.getExpectedDate());
        order.setNotes(clean(request.getNotes()));
        order.setCreatedBy(currentUser.get());
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        PurchaseOrder saved = orderRepository.save(order);

        if (materialRequest != null) {
            materialRequest.setStatus(MaterialRequestStatus.ORDERED);
            materialRequest.setUpdatedAt(now);
            requestRepository.save(materialRequest);
        }
        auditService.record("PURCHASE_ORDER_CREATED", "PurchaseOrder", saved.getId(),
                "projectId=" + projectId + "; material=" + material.getName()
                        + "; quantity=" + saved.getQuantity()
                        + "; total=" + saved.getTotalAmount());
        return PurchaseOrderResponse.from(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse receiveOrder(int projectId, int orderId, GoodsReceiptRequest request) {
        guard.projectForSiteManagement(projectId);
        PurchaseOrder order = orderRepository.lockByIdAndProjectId(orderId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (order.getStatus() == PurchaseOrderStatus.RECEIVED
                || order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("This purchase order cannot receive more goods");
        }
        BigDecimal quantity = Money.quantity(request.getReceivedQuantity());
        if (quantity.signum() <= 0) {
            throw new BadRequestException("Received quantity must be at least 0.001");
        }
        BigDecimal newReceived = Money.quantity(Money.nvl(order.getReceivedQuantity()).add(quantity));
        if (newReceived.compareTo(order.getQuantity()) > 0) {
            throw new BadRequestException("Received quantity exceeds the ordered quantity");
        }
        if (request.getReceivedDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Received date cannot be in the future");
        }

        LocalDateTime now = LocalDateTime.now();
        BigDecimal amount = Money.multiply(quantity, order.getUnitPrice());
        if (amount.signum() <= 0) {
            throw new BadRequestException("Receipt value is too small");
        }
        Expense expense = new Expense();
        expense.setProject(order.getProject());
        expense.setCategory(order.getMaterial().getCategory());
        expense.setAmount(amount);
        expense.setQuantity(quantity);
        expense.setUnit(order.getMaterial().getUnit());
        expense.setPurchaseDate(request.getReceivedDate());
        Expense savedExpense = expenseRepository.save(expense);

        GoodsReceipt receipt = new GoodsReceipt();
        receipt.setPurchaseOrder(order);
        receipt.setReceivedQuantity(quantity);
        receipt.setReceivedDate(request.getReceivedDate());
        receipt.setUnitPrice(order.getUnitPrice());
        receipt.setAmount(amount);
        receipt.setInvoiceNumber(clean(request.getInvoiceNumber()));
        receipt.setNotes(clean(request.getNotes()));
        receipt.setReceivedBy(currentUser.get());
        receipt.setExpenseId(savedExpense.getId());
        receipt.setCreatedAt(now);
        order.getReceipts().add(receipt);

        order.setReceivedQuantity(newReceived);
        order.setStatus(newReceived.compareTo(order.getQuantity()) == 0
                ? PurchaseOrderStatus.RECEIVED : PurchaseOrderStatus.PARTIALLY_RECEIVED);
        order.setUpdatedAt(now);
        PurchaseOrder saved = orderRepository.save(order);

        StockMovement movement = new StockMovement();
        movement.setProject(order.getProject());
        movement.setMaterial(order.getMaterial());
        movement.setMovementType(StockMovementType.RECEIPT);
        movement.setQuantity(quantity);
        movement.setUnitCost(order.getUnitPrice());
        movement.setMovementDate(request.getReceivedDate());
        movement.setReferenceType("GOODS_RECEIPT");
        movement.setReferenceId(receipt.getId());
        movement.setNotes("Purchase order #" + order.getId());
        movement.setCreatedBy(currentUser.get());
        movement.setCreatedAt(now);
        movementRepository.save(movement);

        if (saved.getStatus() == PurchaseOrderStatus.RECEIVED
                && saved.getMaterialRequest() != null) {
            MaterialRequest materialRequest = saved.getMaterialRequest();
            materialRequest.setStatus(MaterialRequestStatus.FULFILLED);
            materialRequest.setUpdatedAt(now);
            requestRepository.save(materialRequest);
        }

        auditService.record("GOODS_RECEIVED", "GoodsReceipt", receipt.getId(),
                "projectId=" + projectId + "; orderId=" + orderId
                        + "; quantity=" + quantity + "; amount=" + amount
                        + "; expenseId=" + savedExpense.getId());
        notifyGoodsReceived(saved, quantity);
        return PurchaseOrderResponse.from(saved);
    }

    @Override
    @Transactional
    public PurchaseOrderResponse cancelOrder(int projectId, int orderId) {
        guard.projectForSiteManagement(projectId);
        PurchaseOrder order = orderRepository.lockByIdAndProjectId(orderId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found"));
        if (order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BadRequestException("This purchase order is already cancelled");
        }
        if (order.getReceivedQuantity() != null
                && order.getReceivedQuantity().compareTo(BigDecimal.ZERO) > 0) {
            throw new BadRequestException("A purchase order with receipts cannot be cancelled");
        }
        order.setStatus(PurchaseOrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        PurchaseOrder saved = orderRepository.save(order);
        if (saved.getMaterialRequest() != null
                && saved.getMaterialRequest().getStatus() == MaterialRequestStatus.ORDERED) {
            MaterialRequest materialRequest = saved.getMaterialRequest();
            materialRequest.setStatus(MaterialRequestStatus.APPROVED);
            materialRequest.setUpdatedAt(LocalDateTime.now());
            requestRepository.save(materialRequest);
        }
        auditService.record("PURCHASE_ORDER_CANCELLED", "PurchaseOrder", saved.getId(),
                "projectId=" + projectId);
        return PurchaseOrderResponse.from(saved);
    }

    @Override
    @Transactional
    public StockMovementResponse recordMovement(int projectId, StockMovementRequest request) {
        guard.projectForSiteRead(projectId);
        if (!canRecordStock()) {
            throw new AccessDeniedException("Only site staff can record stock movements");
        }
        StockMovementType type = request.getMovementType();
        if (type == StockMovementType.RECEIPT) {
            throw new BadRequestException("Record purchases through a goods receipt");
        }
        MaterialItem material = materialRepository.lockByIdAndProjectId(request.getMaterialId(), projectId)
                .orElseThrow(() -> new BadRequestException("Material does not belong to this project"));
        if (!material.isActive()) {
            throw new BadRequestException("This material is no longer active");
        }
        BigDecimal quantity = Money.quantity(request.getQuantity());
        if (quantity.signum() <= 0) {
            throw new BadRequestException("Quantity must be at least 0.001");
        }
        BigDecimal currentBalance = balance(projectId, material.getId());
        if ((type == StockMovementType.ISSUE || type == StockMovementType.WASTAGE)
                && currentBalance.compareTo(quantity) < 0) {
            throw new BadRequestException("Not enough stock available for this movement");
        }
        LocalDate movementDate = request.getMovementDate() == null
                ? LocalDate.now() : request.getMovementDate();
        if (movementDate.isAfter(LocalDate.now())) {
            throw new BadRequestException("Movement date cannot be in the future");
        }

        LocalDateTime now = LocalDateTime.now();
        StockMovement movement = new StockMovement();
        movement.setProject(material.getProject());
        movement.setMaterial(material);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setMovementDate(movementDate);
        movement.setReferenceType("MANUAL");
        movement.setNotes(clean(request.getNotes()));
        movement.setCreatedBy(currentUser.get());
        movement.setCreatedAt(now);
        StockMovement saved = movementRepository.save(movement);
        auditService.record("STOCK_MOVEMENT_RECORDED", "StockMovement", saved.getId(),
                "projectId=" + projectId + "; material=" + material.getName()
                        + "; type=" + type + "; quantity=" + quantity);
        return StockMovementResponse.from(saved, canViewProcurement());
    }

    private void notifyManagers(Project project, NotificationType type, String title,
                                String body, String link) {
        notificationService.send(project.getHouseOwner(), project, type, title, body, link);
        if (project.getContractor() != null
                && (project.getHouseOwner() == null
                || project.getContractor().getId() != project.getHouseOwner().getId())) {
            notificationService.send(project.getContractor(), project, type, title, body, link);
        }
    }

    private void notifyRequestReviewed(MaterialRequest request) {
        String status = request.getStatus() == MaterialRequestStatus.APPROVED ? "approved" : "rejected";
        String body = "Your request for " + request.getQuantity().toPlainString() + " "
                + request.getMaterial().getUnit() + " of " + request.getMaterial().getName()
                + " was " + status;
        if (request.getReviewNote() != null) {
            body += ": " + request.getReviewNote();
        }
        notificationService.send(request.getRequestedBy(), request.getProject(),
                NotificationType.MATERIAL_REQUEST_REVIEWED,
                "Material request " + status,
                body,
                "/projects/" + request.getProject().getId());
    }

    private void notifyGoodsReceived(PurchaseOrder order, BigDecimal quantity) {
        Project project = order.getProject();
        String body = quantity.toPlainString() + " " + order.getMaterial().getUnit() + " of "
                + order.getMaterial().getName() + " received for " + project.getTitle();
        User owner = project.getHouseOwner();
        User contractor = project.getContractor();
        notificationService.send(owner, project, NotificationType.GOODS_RECEIVED,
                "Goods received", body, "/projects/" + project.getId());
        if (contractor != null && (owner == null || contractor.getId() != owner.getId())) {
            notificationService.send(contractor, project, NotificationType.GOODS_RECEIVED,
                    "Goods received", body, "/projects/" + project.getId());
        }
        MaterialRequest request = order.getMaterialRequest();
        if (request != null) {
            User requester = request.getRequestedBy();
            boolean alreadyNotified = (owner != null && requester.getId() == owner.getId())
                    || (contractor != null && requester.getId() == contractor.getId());
            if (!alreadyNotified) {
                notificationService.send(requester, project, NotificationType.GOODS_RECEIVED,
                        "Goods received", body, "/projects/" + project.getId());
            }
        }
    }

    private BigDecimal balance(int projectId, int materialId) {
        BigDecimal value = movementRepository.balance(projectId, materialId, INBOUND);
        return Money.quantity(value == null ? BigDecimal.ZERO : value);
    }

    private MaterialItem loadMaterial(int projectId, int materialId) {
        return materialRepository.findByIdAndProjectId(materialId, projectId)
                .orElseThrow(() -> new BadRequestException("Material does not belong to this project"));
    }

    private Supplier loadSupplier(int projectId, int supplierId) {
        return supplierRepository.findByIdAndProjectId(supplierId, projectId)
                .orElseThrow(() -> new BadRequestException("Supplier does not belong to this project"));
    }

    private MaterialRequest lockRequest(int projectId, int requestId) {
        return requestRepository.lockByIdAndProjectId(requestId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Material request not found"));
    }

    private void requireRequested(MaterialRequest request) {
        if (request.getStatus() != MaterialRequestStatus.REQUESTED) {
            throw new BadRequestException("Only a requested material can be reviewed");
        }
    }

    private void review(MaterialRequest materialRequest, String reviewNote) {
        materialRequest.setReviewedBy(currentUser.get());
        materialRequest.setReviewedAt(LocalDateTime.now());
        materialRequest.setReviewNote(clean(reviewNote));
        materialRequest.setUpdatedAt(LocalDateTime.now());
    }

    private void applySupplier(Supplier supplier, SupplierRequest request) {
        supplier.setName(request.getName().trim());
        supplier.setPhone(clean(request.getPhone()));
        supplier.setAddress(clean(request.getAddress()));
        supplier.setNotes(clean(request.getNotes()));
    }

    private boolean isAdminOrContractor() {
        RoleName role = currentUser.role();
        return role == RoleName.ADMIN || role == RoleName.CONTRACTOR;
    }

    private boolean canRecordStock() {
        RoleName role = currentUser.role();
        return role == RoleName.ADMIN || role == RoleName.CONTRACTOR || role == RoleName.ENGINEER;
    }

    private boolean canViewProcurement() {
        RoleName role = currentUser.role();
        return role == RoleName.ADMIN || role == RoleName.CONTRACTOR || role == RoleName.HOUSE_OWNER;
    }

    private static String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
