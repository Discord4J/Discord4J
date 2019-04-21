package discord4j.core.`object`.audit


import discord4j.core.`object`.audit.AuditLogChange
import discord4j.core.grab


fun <T> AuditLogChange<T>.oldValue(): T? = oldValue.grab()
fun <T> AuditLogChange<T>.currentValue(): T? = currentValue.grab()
