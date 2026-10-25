// English labels for backend enum values. They go through t() so they translate too.
export const LABELS = {
  role: { HOUSE_OWNER: 'House owner', CONTRACTOR: 'Contractor', LABOUR: 'Labour', ENGINEER: 'Engineer', ADMIN: 'Admin' },
  contractType: { LABOUR_ONLY: 'Labour only', LABOUR_MATERIAL: 'Labour + Material' },
  projectStatus: { ONGOING: 'Ongoing', COMPLETED: 'Completed', ON_HOLD: 'On hold' },
  assignmentStatus: { ASSIGNED: 'Working', COMPLETED: 'Completed', REMOVED: 'Removed' },
  requirementStatus: { OPEN: 'Open', CLOSED: 'Closed' },
  invitationStatus: { PENDING: 'Pending response', ACCEPTED: 'Accepted', DECLINED: 'Declined', CANCELLED: 'Cancelled', EXPIRED: 'Expired' },
  engineerAvailability: { AVAILABLE: 'Available', BUSY: 'Busy', NOT_AVAILABLE: 'Not available' },
  credentialStatus: { UNVERIFIED: 'Unverified', PENDING: 'Pending review', VERIFIED: 'Verified' },
  attendance: { FULL_DAY: 'Full day', HALF_DAY: 'Half day', OVERTIME: 'Overtime', ABSENT: 'Absent' },
  paymentMode: { CASH: 'Cash', UPI: 'UPI', BANK_TRANSFER: 'Bank transfer' },
  paymentType: { WAGE: 'Wage', ADVANCE: 'Advance' },
  category: {
    CEMENT: 'Cement', BRICK: 'Brick', AGGREGATE: 'Aggregate', STEEL: 'Steel', SAND: 'Sand', OTHER: 'Other',
  },
  progressStatus: { DRAFT: 'Draft', SUBMITTED: 'Submitted', PUBLISHED: 'Published' },
  taskStatus: { TODO: 'To do', IN_PROGRESS: 'In progress', BLOCKED: 'Blocked', DONE: 'Done' },
  milestoneStatus: { PLANNED: 'Planned', IN_PROGRESS: 'In progress', COMPLETED: 'Completed', DELAYED: 'Delayed' },
  issueStatus: {
    OPEN: 'Open', ASSIGNED: 'Assigned', IN_PROGRESS: 'In progress',
    READY_FOR_REVIEW: 'Ready for review', RESOLVED: 'Resolved', REOPENED: 'Reopened',
  },
  issueCategory: { QUALITY: 'Quality', SAFETY: 'Safety', DELAY: 'Delay', OTHER: 'Other' },
  materialRequestStatus: {
    REQUESTED: 'Requested', APPROVED: 'Approved', REJECTED: 'Rejected',
    ORDERED: 'Ordered', FULFILLED: 'Fulfilled', CANCELLED: 'Cancelled',
  },
  purchaseOrderStatus: {
    ORDERED: 'Ordered', PARTIALLY_RECEIVED: 'Partially received',
    RECEIVED: 'Received', CANCELLED: 'Cancelled',
  },
  stockMovementType: { RECEIPT: 'Receipt', ISSUE: 'Issued', RETURN: 'Returned', WASTAGE: 'Wastage' },
}

export const options = (group) => Object.keys(LABELS[group])

export const STATUS_TONE = {
  ONGOING: 'brand', COMPLETED: 'success', ON_HOLD: 'warning',
  ASSIGNED: 'brand', REMOVED: 'neutral',
  OPEN: 'success', CLOSED: 'neutral',
  FULL_DAY: 'success', HALF_DAY: 'warning', OVERTIME: 'brand', ABSENT: 'danger',
  PENDING: 'warning', ACCEPTED: 'success', DECLINED: 'danger', CANCELLED: 'neutral', EXPIRED: 'neutral',
  AVAILABLE: 'success', BUSY: 'warning', NOT_AVAILABLE: 'neutral',
  UNVERIFIED: 'neutral', VERIFIED: 'success',
  DRAFT: 'neutral', SUBMITTED: 'warning', PUBLISHED: 'success',
  TODO: 'neutral', IN_PROGRESS: 'brand', BLOCKED: 'danger', DONE: 'success',
  PLANNED: 'neutral', DELAYED: 'warning',
  ASSIGNED: 'brand', READY_FOR_REVIEW: 'warning', RESOLVED: 'success', REOPENED: 'danger',
  REQUESTED: 'warning', APPROVED: 'success', REJECTED: 'danger',
  ORDERED: 'brand', PARTIALLY_RECEIVED: 'warning', RECEIVED: 'success', FULFILLED: 'success',
  ISSUE: 'brand', RETURN: 'warning', WASTAGE: 'danger', RECEIPT: 'success',
}

// Some enum values mean different things in different groups (for example, OPEN).
export const GROUP_STATUS_TONE = {
  requirementStatus: { OPEN: 'success', CLOSED: 'neutral' },
  issueStatus: {
    OPEN: 'danger', ASSIGNED: 'brand', IN_PROGRESS: 'brand',
    READY_FOR_REVIEW: 'warning', RESOLVED: 'success', REOPENED: 'danger',
  },
  progressStatus: { DRAFT: 'neutral', SUBMITTED: 'warning', PUBLISHED: 'success' },
  taskStatus: { TODO: 'neutral', IN_PROGRESS: 'brand', BLOCKED: 'danger', DONE: 'success' },
  milestoneStatus: { PLANNED: 'neutral', IN_PROGRESS: 'brand', COMPLETED: 'success', DELAYED: 'warning' },
  materialRequestStatus: {
    REQUESTED: 'warning', APPROVED: 'success', REJECTED: 'danger',
    ORDERED: 'brand', FULFILLED: 'success', CANCELLED: 'neutral',
  },
  purchaseOrderStatus: {
    ORDERED: 'brand', PARTIALLY_RECEIVED: 'warning', RECEIVED: 'success', CANCELLED: 'neutral',
  },
  stockMovementType: {
    RECEIPT: 'success', ISSUE: 'brand', RETURN: 'warning', WASTAGE: 'danger',
  },
}
