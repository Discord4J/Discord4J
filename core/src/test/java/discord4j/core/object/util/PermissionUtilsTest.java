package discord4j.core.object.util;

import discord4j.core.object.PermissionOverwrite;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static discord4j.core.object.util.Permission.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PermissionUtilsTest {

    private PermissionSet everyone;
    private Flux<PermissionSet> rolePerms1;
    private Flux<PermissionSet> rolePerms2;
    private PermissionSet basePerms1;
    private PermissionSet basePerms2;

    private Snowflake userId;
    private Snowflake badUserId;
    private Snowflake roleId;
    private Snowflake badRoleId;

    private Set<Snowflake> roleIds;

    private PermissionOverwrite role;
    private PermissionOverwrite badRole;
    private PermissionOverwrite user;
    private PermissionOverwrite badUser;
    private Set<PermissionOverwrite> overwrites;

    private PermissionSet overwritten1;
    private PermissionSet overwritten2;

    @Before
    public void initialize() {
        // Everyone has CII and MC
        everyone = PermissionSet.of(CREATE_INSTANT_INVITE, MANAGE_CHANNELS);

        // User1 has PS and BM from guild-wide role permissions
        rolePerms1 = Flux.fromArray(new PermissionSet[]{
            PermissionSet.of(PRIORITY_SPEAKER),
            PermissionSet.of(BAN_MEMBERS)
        });
        basePerms1 = PermissionSet.of(CREATE_INSTANT_INVITE, MANAGE_CHANNELS, PRIORITY_SPEAKER, BAN_MEMBERS);

        // User2 has PS and Admin from guild-wide role permissions
        rolePerms2 = Flux.fromArray(new PermissionSet[]{
            PermissionSet.of(PRIORITY_SPEAKER),
            PermissionSet.of(ADMINISTRATOR)
        });
        basePerms2 = PermissionSet.of(CREATE_INSTANT_INVITE, MANAGE_CHANNELS, PRIORITY_SPEAKER, ADMINISTRATOR);


        // Our story begins with a bit of snow...flakes.
        // Once upon a time, in a guild quite close to here, a user existed.
        // The user of our story, `user`, had only one role, `role`.
        // But other users existed in this guild! (`badUser`)
        // And other roles existed in this guild! (`badRole`)
        // (But they're irrelevant fill-ins so basically they're bad)
        userId = Snowflake.of(10);
        roleId = Snowflake.of(20);
        badUserId = Snowflake.of(15);
        badRoleId = Snowflake.of(25);

        // A set of the role IDs `user` has in our fictional guild!
        roleIds = new HashSet<>();
        roleIds.add(roleId);

        // role: mock the role overwrite object for `role`
        role = mock(PermissionOverwrite.class);
        when(role.getRoleId()).thenReturn(Optional.of(roleId));
        when(role.getUserId()).thenReturn(Optional.empty());
        when(role.getDenied()).thenReturn(PermissionSet.of(CREATE_INSTANT_INVITE));
        when(role.getAllowed()).thenReturn(PermissionSet.of(PRIORITY_SPEAKER, MANAGE_CHANNELS));

        // badRole: mock the role overwrite object for `badRole`
        badRole = mock(PermissionOverwrite.class);
        when(badRole.getRoleId()).thenReturn(Optional.of(badRoleId));
        when(badRole.getUserId()).thenReturn(Optional.empty());
        // getAllowed/Denied shouldn't be called on this object

        // user: mock the user overwrite object for `user`
        user = mock(PermissionOverwrite.class);
        when(user.getRoleId()).thenReturn(Optional.empty());
        when(user.getUserId()).thenReturn(Optional.of(userId));
        when(user.getDenied()).thenReturn(PermissionSet.of(ADMINISTRATOR, MANAGE_CHANNELS));
        when(user.getAllowed()).thenReturn(PermissionSet.none());

        // badUser: mock the user overwrite object for `badUser`
        badUser = mock(PermissionOverwrite.class);
        when(badUser.getRoleId()).thenReturn(Optional.empty());
        when(badUser.getUserId()).thenReturn(Optional.of(badUserId));
        // getAllowed/Denied shouldn't be called on this object

        // The set of ALL overwrites in this fair guild
        overwrites = new HashSet<>();
        overwrites.add(role);
        overwrites.add(badRole);
        overwrites.add(user);
        overwrites.add(badUser);

        // CII,MC,PS,BM -CII +PS,MC -Admin,MC  =>  PS,BM
        overwritten1 = PermissionSet.of(PRIORITY_SPEAKER, BAN_MEMBERS);
        // CII,MC,PS,Admin -CII +PS,MC -Admin,MC  =>  PS (see below note)
        overwritten2 = PermissionSet.of(PRIORITY_SPEAKER);

        // Overall, role and user => -CII +PS,MC -Admin,MC => -CII,Admin,MC +PS
        // NOTE: Though admin is removed, if a user *already had admin*, then they shouldn't *lose* admin, because
        //       guild-wide admin grants immunity to ALL overwrites and gives ALL permissions
    }

    @Test
    public void testApply() {
        // Starting permissions- instant invite and manage channels
        PermissionSet ps = PermissionSet.of(CREATE_INSTANT_INVITE, MANAGE_CHANNELS);

        // Add priority speaker, remove create instant invite
        PermissionOverwrite po = mock(PermissionOverwrite.class);
        when(po.getAllowed()).thenReturn(PermissionSet.of(PRIORITY_SPEAKER));
        when(po.getDenied()).thenReturn(PermissionSet.of(CREATE_INSTANT_INVITE));

        // TEST CALL
        PermissionSet result = PermissionUtils.applyOverwrite(ps, po);

        // Should be left with just manage channel and priority speaker
        assertEquals(PermissionSet.of(MANAGE_CHANNELS, PRIORITY_SPEAKER), result);
    }

    @Test
    public void testCalculateBase() {

        // TEST CALL:
        PermissionSet result1 = PermissionUtils.calculateBasePerms(
            everyone,
            rolePerms1
        ).block();

        // TEST CALL
        PermissionSet result2 = PermissionUtils.calculateBasePerms(
            everyone,
            rolePerms2
        ).block();

        assertEquals(basePerms1, result1);
        assertEquals(basePerms2, result2);
    }

    @Test
    public void testCalculateOverwrites() {

        // TEST CALL
        PermissionSet result1 = PermissionUtils.calculateOverwrites(
            basePerms1, overwrites, roleIds, userId
        ).block();

        // TEST CALL
        PermissionSet result2 = PermissionUtils.calculateOverwrites(
            basePerms2, overwrites, roleIds, userId
        ).block();

        assertEquals(overwritten1,result1);
        assertEquals(overwritten2,result2);
    }

    @Test
    public void testEffectivePerms() {

        // TEST CALL: should return overwritten1
        PermissionSet nonOwnerResult1 = PermissionUtils.effectivePermissions(
            userId,badUserId,everyone,rolePerms1,overwrites,roleIds
        ).block();

        // TEST CALL: should return PermissionSet.all() because of Admin at guild-level
        PermissionSet nonOwnerResult2 = PermissionUtils.effectivePermissions(
            userId,badUserId,everyone,rolePerms2,overwrites,roleIds
        ).block();

        // TEST CALL: should return PermissionSet.all() because of owner status
        PermissionSet ownerResult = PermissionUtils.effectivePermissions(
            userId,userId,everyone,rolePerms1,overwrites,roleIds
        ).block();

        assertEquals(overwritten1,nonOwnerResult1);
        assertEquals(PermissionSet.all(),nonOwnerResult2);
        assertEquals(PermissionSet.all(),ownerResult);
    }

}
