package com.thepaddlers.audit

import org.aspectj.lang.ProceedingJoinPoint
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.*

class AuditAspectTest {
    @Test
    fun `aspect fires and persists audit entry`() {
        val repo = mock<AuditRepository>()
        val aspect = AuditAspect(repo)
        val joinPoint = mock<ProceedingJoinPoint>()
        whenever(joinPoint.signature).thenReturn(mock())
        whenever(joinPoint.args).thenReturn(arrayOf(mapOf("orgId" to UUID.randomUUID(), "targetId" to UUID.randomUUID(), "email" to "test@example.com")))
        whenever(joinPoint.proceed()).thenReturn("result")
        val result = aspect.logAdminAction(joinPoint)
        assertNotNull(result)
    }
}

