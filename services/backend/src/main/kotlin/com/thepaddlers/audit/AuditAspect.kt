package com.thepaddlers.audit

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AdminAction(val action: String = "")

@Aspect
@Component
class AuditAspect @Autowired constructor(
    private val auditRepository: AuditRepository
) {
    private val objectMapper = jacksonObjectMapper()
    @Around("@annotation(com.thepaddlers.audit.AdminAction)")
    @Transactional
    fun logAdminAction(joinPoint: ProceedingJoinPoint): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = signature.method
        val annotation = method.getAnnotation(AdminAction::class.java)
        val action = annotation?.action ?: method.name
        val args = joinPoint.args.map { MaskingUtil.mask(it) }
        val actorId = SecurityContextHolder.getContext().authentication?.principal as? UUID ?: UUID.randomUUID()
        val orgId = args.find { it is Map<*, *> && it.containsKey("orgId") }?.let { (it as Map<*, *>)["orgId"] as? UUID } ?: UUID.randomUUID()
        val targetId = args.find { it is Map<*, *> && it.containsKey("targetId") }?.let { (it as Map<*, *>)["targetId"] as? UUID }
        val createdAt = Instant.now()
        val details = mutableMapOf<String, Any?>()
        details["args"] = args
        var result: Any? = null
        var exception: Exception? = null
        try {
            result = joinPoint.proceed()
            details["result"] = result
        } catch (ex: Exception) {
            details["exception"] = ex.toString()
            exception = ex
        }
        val entry = AuditEntry(
            actorId = actorId,
            action = action,
            targetId = targetId,
            orgId = orgId,
            details = objectMapper.writeValueAsString(details),
            createdAt = createdAt
        )
        auditRepository.save(entry)
        if (exception != null) throw exception
        return result
    }
}

object MaskingUtil {
    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val phoneRegex = Regex("\\b\\d{10,15}\\b")
    fun mask(obj: Any?): Any? {
        return when (obj) {
            is String -> emailRegex.replace(phoneRegex.replace(obj, "[REDACTED]"), "[REDACTED]")
            is Map<*, *> -> obj.mapValues { mask(it.value) }
            is List<*> -> obj.map { mask(it) }
            else -> obj
        }
    }
}

