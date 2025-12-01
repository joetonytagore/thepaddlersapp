package com.thepaddlers.common

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Instant

class TestEntity : BaseEntity()

class BaseEntityTest {
    @Test
    fun `softDelete sets deletedAt`() {
        val entity = TestEntity()
        assertNull(entity.deletedAt)
        entity.softDelete()
        assertNotNull(entity.deletedAt)
        assertTrue(entity.deletedAt!! <= Instant.now())
    }
}

