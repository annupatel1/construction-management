import test from 'node:test'
import assert from 'node:assert/strict'
import { dayValue, projectFinance, round2, sum, wageSummary } from './calc.js'

test('round2 removes floating point noise and accepts strings', () => {
  assert.equal(round2(0.1 + 0.2), 0.3)
  assert.equal(round2('12.345'), 12.35)
  assert.equal(round2(null), 0)
  assert.equal(round2(undefined), 0)
})

test('dayValue counts full, half and overtime days', () => {
  assert.equal(dayValue('FULL_DAY'), 1)
  assert.equal(dayValue('OVERTIME'), 1)
  assert.equal(dayValue('HALF_DAY'), 0.5)
  assert.equal(dayValue('ABSENT'), 0)
  assert.equal(dayValue(undefined), 0)
})

test('sum rounds the total to paise', () => {
  assert.equal(sum([{ amount: 0.1 }, { amount: 0.2 }], (row) => row.amount), 0.3)
  assert.equal(sum([], (row) => row.amount), 0)
})

test('wageSummary calculates days, overtime, advance, paid and balance', () => {
  const assignment = { dailyRate: 500, advanceTaken: 1000 }
  const attendance = [
    { status: 'FULL_DAY' },
    { status: 'HALF_DAY' },
    { status: 'OVERTIME', overtimeHours: 2 },
    { status: 'ABSENT' },
  ]
  const payments = [
    { type: 'ADVANCE', amount: 500 },
    { type: 'WAGE', amount: 1000 },
  ]

  assert.deepEqual(wageSummary(assignment, attendance, payments), {
    days: 2.5,
    overtimeHours: 2,
    earned: 1375,
    advance: 1500,
    paid: 1000,
    balance: -1125,
  })
})

test('wageSummary handles an assignment with no attendance or payments', () => {
  assert.deepEqual(wageSummary({ dailyRate: 800, advanceTaken: 200 }), {
    days: 0,
    overtimeHours: 0,
    earned: 0,
    advance: 200,
    paid: 0,
    balance: -200,
  })
})

test('projectFinance includes material cost for labour-and-material contracts', () => {
  const project = { totalEstimatedCost: 100000, contractType: 'LABOUR_MATERIAL' }
  const payments = [{ amount: 60000 }]
  const expenses = [{ amount: 20000 }]
  const wages = [{ earned: 15000, advance: 5000, paid: 5000 }]

  assert.deepEqual(projectFinance(project, payments, expenses, wages), {
    estimated: 100000,
    received: 60000,
    due: 40000,
    materials: 20000,
    wagesEarned: 15000,
    wagesPaid: 10000,
    profit: 25000,
  })
})

test('projectFinance excludes material cost for labour-only contracts', () => {
  const project = { totalEstimatedCost: 100000, contractType: 'LABOUR_ONLY' }
  const payments = [{ amount: 60000 }]
  const expenses = [{ amount: 20000 }]
  const wages = [{ earned: 15000, advance: 5000, paid: 5000 }]

  const result = projectFinance(project, payments, expenses, wages)
  assert.equal(result.materials, 20000)
  assert.equal(result.profit, 45000)
})
