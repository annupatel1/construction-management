import test from 'node:test'
import assert from 'node:assert/strict'
import { formatDate, formatINRShort } from './format.js'

test('formatINRShort uses lakhs and crores for large INR values', () => {
  assert.equal(formatINRShort(1250000), '₹12.5 L')
  assert.equal(formatINRShort(12500000), '₹1.25 Cr')
  assert.equal(formatINRShort(-125000), '-₹1.25 L')
})

test('formatDate handles empty and ISO dates', () => {
  assert.equal(formatDate(null), '-')
  assert.equal(formatDate('2026-11-12'), '12 Nov 2026')
})
