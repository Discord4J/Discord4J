package discord4j.core.`object`.audit

import discord4j.core.`object`.audit.AuditLogChange
import discord4j.core.`object`.audit.AuditLogEntry
import discord4j.core.`object`.audit.ChangeKey
import discord4j.core.`object`.audit.OptionKey
import discord4j.core.`object`.util.Snowflake
import discord4j.core.grab


fun AuditLogEntry.targetId(): Snowflake? = targetId.grab()
fun AuditLogEntry.reason(): String? = reason.grab()
fun <T> AuditLogEntry.awaitChange(key: ChangeKey<T>): AuditLogChange<T>? = getChange(key).grab()
fun <T> AuditLogEntry.awaitOption(key: OptionKey<T>): T? = getOption(key).grab()



