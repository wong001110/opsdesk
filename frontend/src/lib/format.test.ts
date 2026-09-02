import { describe, expect, it } from 'vitest'
import { nextTicketStatus } from './format'

describe('nextTicketStatus', () => {
  it('follows the server workflow without inventing reverse transitions', () => {
    expect(nextTicketStatus('OPEN')).toBe('IN_PROGRESS')
    expect(nextTicketStatus('IN_PROGRESS')).toBe('DONE')
    expect(nextTicketStatus('DONE')).toBeNull()
  })
})
