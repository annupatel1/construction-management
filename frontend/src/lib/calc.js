// Wages and project money are worked out here from the lists the API returns.
// The backend is the source of truth for stored money; these helpers only format
// and add up values, rounding each rupee total to two decimals so binary floating
// point noise (0.1 + 0.2) never reaches the screen.
const OVERTIME_DIVISOR = 8 // one overtime hour = daily rate / 8

/** Round a number to paise. */
export const round2 = (n) => Math.round((Number(n) || 0) * 100) / 100

/** Days counted for one attendance record */
export function dayValue(status) {
  if (status === 'FULL_DAY' || status === 'OVERTIME') return 1
  if (status === 'HALF_DAY') return 0.5
  return 0
}

export const sum = (list, pick) => round2(list.reduce((t, x) => t + (Number(pick(x)) || 0), 0))

/** Wage sheet for one labourer on one site */
export function wageSummary(assignment, attendance = [], payments = []) {
  const rate = Number(assignment.dailyRate) || 0
  const days = sum(attendance, (a) => dayValue(a.status))
  const overtimeHours = sum(attendance, (a) => (a.status === 'OVERTIME' ? a.overtimeHours : 0))
  const earned = round2(days * rate + overtimeHours * (rate / OVERTIME_DIVISOR))
  // payments carry a type: ADVANCE is money given early, everything else is a wage payment
  const advancePayments = sum(payments.filter((p) => p.type === 'ADVANCE'), (p) => p.amount)
  const paid = sum(payments.filter((p) => p.type !== 'ADVANCE'), (p) => p.amount)
  const advance = round2((Number(assignment.advanceTaken) || 0) + advancePayments)
  return { days, overtimeHours, earned, advance, paid, balance: round2(earned - advance - paid) }
}

/** Money picture of a project. wages = [{earned, advance, paid}] per labourer (contractor view). */
export function projectFinance(project, payments = [], expenses = [], wages = []) {
  const estimated = round2(project.totalEstimatedCost)
  const received = sum(payments, (p) => p.amount)
  const materials = sum(expenses, (e) => e.amount)
  const wagesEarned = sum(wages, (w) => w.earned)
  const wagesPaid = sum(wages, (w) => w.advance + w.paid)
  // With a "Labour only" contract the owner supplies material, so it is not the contractor's cost.
  const materialCost = project.contractType === 'LABOUR_MATERIAL' ? materials : 0
  return {
    estimated,
    received,
    due: round2(estimated - received),
    materials,
    wagesEarned,
    wagesPaid,
    profit: round2(received - materialCost - wagesEarned),
  }
}
