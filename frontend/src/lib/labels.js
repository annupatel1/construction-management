// English labels for backend enum values. They go through t() so they translate too.
export const LABELS = {
  role: { HOUSE_OWNER: 'House owner', CONTRACTOR: 'Contractor', LABOUR: 'Labour', ADMIN: 'Admin' },
  contractType: { LABOUR_ONLY: 'Labour only', LABOUR_MATERIAL: 'Labour + Material' },
  projectStatus: { ONGOING: 'Ongoing', COMPLETED: 'Completed', ON_HOLD: 'On hold' },
  assignmentStatus: { ASSIGNED: 'Working', COMPLETED: 'Completed', REMOVED: 'Removed' },
  requirementStatus: { OPEN: 'Open', CLOSED: 'Closed' },
  attendance: { FULL_DAY: 'Full day', HALF_DAY: 'Half day', OVERTIME: 'Overtime', ABSENT: 'Absent' },
  paymentMode: { CASH: 'Cash', UPI: 'UPI', BANK_TRANSFER: 'Bank transfer' },
  paymentType: { WAGE: 'Wage', ADVANCE: 'Advance' },
  category: {
    CEMENT: 'Cement', BRICK: 'Brick', AGGREGATE: 'Aggregate', STEEL: 'Steel', SAND: 'Sand', OTHER: 'Other',
  },
}

export const options = (group) => Object.keys(LABELS[group])

export const STATUS_TONE = {
  ONGOING: 'brand', COMPLETED: 'success', ON_HOLD: 'warning',
  ASSIGNED: 'brand', REMOVED: 'neutral',
  OPEN: 'success', CLOSED: 'neutral',
  FULL_DAY: 'success', HALF_DAY: 'warning', OVERTIME: 'brand', ABSENT: 'danger',
}
