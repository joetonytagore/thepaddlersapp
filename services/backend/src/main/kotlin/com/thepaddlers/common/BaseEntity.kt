package com.thepaddlers.common

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import java.time.Instant
import org.hibernate.annotations.Filter
import org.hibernate.annotations.FilterDef
import org.hibernate.annotations.ParamDef

@MappedSuperclass
@FilterDef(name = "deletedFilter", parameters = [ParamDef(name = "isDeleted", type = "boolean")])
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
abstract class BaseEntity {
    @Column(name = "deleted_at")
    var deletedAt: Instant? = null

    fun softDelete() {
        deletedAt = Instant.now()
    }
}
