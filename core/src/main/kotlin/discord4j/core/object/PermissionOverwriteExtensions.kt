package discord4j.core.`object`

import discord4j.core.`object`.PermissionOverwrite
import discord4j.core.`object`.util.Snowflake
import discord4j.core.grab

/**
 * Grabs the role ID associated with this overwrite.
 *
 * @return The nullable [Snowflake] of the role.
 */
fun PermissionOverwrite.roleId(): Snowflake? = roleId.grab()

/**
 * Grabs the member ID associated with this overwrite.
 *
 * @return The nullable [Snowflake] of the role.
 */
fun PermissionOverwrite.memberId(): Snowflake? = memberId.grab()
                
