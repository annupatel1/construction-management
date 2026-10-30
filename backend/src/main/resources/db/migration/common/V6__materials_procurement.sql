-- Materials and procurement: catalog, requests, suppliers, purchase orders, receipts, stock ledger.

CREATE TABLE material_item (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    category VARCHAR(32) NOT NULL,
    unit VARCHAR(32) NOT NULL,
    minimum_stock DECIMAL(14,3) NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_material_item_project_name_unit UNIQUE (project_id, name, unit),
    CONSTRAINT fk_material_item_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX idx_material_item_project_active ON material_item (project_id, active, name);

CREATE TABLE supplier (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(500),
    notes VARCHAR(1000),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_supplier_project FOREIGN KEY (project_id) REFERENCES project (id)
);

CREATE INDEX idx_supplier_project_active ON supplier (project_id, active, name);

CREATE TABLE material_request (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    material_id INT NOT NULL,
    requested_by_id INT NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    needed_by DATE,
    notes VARCHAR(2000),
    status VARCHAR(32) NOT NULL DEFAULT 'REQUESTED',
    reviewed_by_id INT,
    reviewed_at DATETIME(6),
    review_note VARCHAR(1000),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_material_request_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_material_request_material FOREIGN KEY (material_id) REFERENCES material_item (id),
    CONSTRAINT fk_material_request_requested_by FOREIGN KEY (requested_by_id) REFERENCES users (id),
    CONSTRAINT fk_material_request_reviewed_by FOREIGN KEY (reviewed_by_id) REFERENCES users (id)
);

CREATE INDEX idx_material_request_project_status ON material_request (project_id, status, created_at);

CREATE TABLE purchase_order (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    material_id INT NOT NULL,
    supplier_id INT NOT NULL,
    material_request_id INT,
    quantity DECIMAL(14,3) NOT NULL,
    unit_price DECIMAL(14,2) NOT NULL,
    total_amount DECIMAL(14,2) NOT NULL,
    received_quantity DECIMAL(14,3) NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL DEFAULT 'ORDERED',
    ordered_date DATE NOT NULL,
    expected_date DATE,
    notes VARCHAR(2000),
    created_by_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_purchase_order_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_purchase_order_material FOREIGN KEY (material_id) REFERENCES material_item (id),
    CONSTRAINT fk_purchase_order_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (id),
    CONSTRAINT fk_purchase_order_request FOREIGN KEY (material_request_id) REFERENCES material_request (id),
    CONSTRAINT fk_purchase_order_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
);

CREATE INDEX idx_purchase_order_project_status ON purchase_order (project_id, status, ordered_date);
CREATE INDEX idx_purchase_order_request ON purchase_order (material_request_id);

CREATE TABLE goods_receipt (
    id INT NOT NULL AUTO_INCREMENT,
    purchase_order_id INT NOT NULL,
    received_quantity DECIMAL(14,3) NOT NULL,
    received_date DATE NOT NULL,
    unit_price DECIMAL(14,2) NOT NULL,
    amount DECIMAL(14,2) NOT NULL,
    invoice_number VARCHAR(100),
    notes VARCHAR(1000),
    received_by_id INT NOT NULL,
    expense_id INT,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_goods_receipt_expense UNIQUE (expense_id),
    CONSTRAINT fk_goods_receipt_order FOREIGN KEY (purchase_order_id) REFERENCES purchase_order (id),
    CONSTRAINT fk_goods_receipt_received_by FOREIGN KEY (received_by_id) REFERENCES users (id),
    CONSTRAINT fk_goods_receipt_expense FOREIGN KEY (expense_id) REFERENCES expense (id)
);

CREATE INDEX idx_goods_receipt_order ON goods_receipt (purchase_order_id, received_date);

CREATE TABLE stock_movement (
    id INT NOT NULL AUTO_INCREMENT,
    project_id INT NOT NULL,
    material_id INT NOT NULL,
    movement_type VARCHAR(32) NOT NULL,
    quantity DECIMAL(14,3) NOT NULL,
    unit_cost DECIMAL(14,2),
    movement_date DATE NOT NULL,
    reference_type VARCHAR(32),
    reference_id INT,
    notes VARCHAR(1000),
    created_by_id INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_stock_movement_project FOREIGN KEY (project_id) REFERENCES project (id),
    CONSTRAINT fk_stock_movement_material FOREIGN KEY (material_id) REFERENCES material_item (id),
    CONSTRAINT fk_stock_movement_created_by FOREIGN KEY (created_by_id) REFERENCES users (id)
);

CREATE INDEX idx_stock_movement_project_material ON stock_movement (project_id, material_id, movement_date);
CREATE INDEX idx_stock_movement_project_date ON stock_movement (project_id, movement_date);
