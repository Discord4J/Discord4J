package discord4j.core.`object`.audit


import discord4j.core.grab


fun <T> AuditLogChange<T>.nullableOldValue(): T? = oldValue.grab()
fun <T> AuditLogChange<T>.nullableCurrentValue(): T? = currentValue.grab()
