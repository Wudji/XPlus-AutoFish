package troy.autofish;

import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData.DataValue;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import troy.autofish.config.Config;
import troy.autofish.mixin.FishingHookAccessor;
import troy.autofish.monitor.FishMonitorState;
import troy.autofish.scheduler.ActionType;
import troy.autofish.scheduler.AutofishScheduler;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class AutofishDetectionTest {

    private Config config;
    private Minecraft client;
    private FishingHook hook;
    private Autofish autofish;
    private AutofishScheduler scheduler;

    @BeforeAll
    static void initializeRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        config = new Config();
        config.setOpenWaterDetectEnabled(false);
        FabricModAutofish mod = mock(FabricModAutofish.class);
        when(mod.getConfig()).thenReturn(config);
        scheduler = spy(new AutofishScheduler(mod));
        doNothing().when(scheduler).onFishCaught();
        when(mod.getScheduler()).thenReturn(scheduler);
        client = mock(Minecraft.class);
        client.level = mock(ClientLevel.class);
        client.player = mock(LocalPlayer.class);
        ItemStack rod = mock(ItemStack.class);
        when(rod.getItem()).thenReturn(Items.FISHING_ROD);
        when(client.player.getMainHandItem()).thenReturn(rod);
        when(client.player.getOffhandItem()).thenReturn(ItemStack.EMPTY);
        hook = mock(FishingHook.class, withSettings().extraInterfaces(FishingHookAccessor.class));
        when(hook.getId()).thenReturn(10);
        client.player.fishing = hook;
        autofish = new Autofish(mod);
        Field clientField = Autofish.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(autofish, client);
    }

    @Test
    void entityDetectionIsTheDefaultAndAnIdleBobberDoesNotReel() {
        assertEquals(Config.DetectionMode.ENTITY, config.getDetectionMode());
        autofish.tick(client);
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void aSustainedBiteQueuesOnlyOneReelAcrossClientTicks() {
        setBiting(true);
        autofish.tick(client);
        autofish.tick(client);
        assertTrue(scheduler.isRecastQueued());
        verify(scheduler, times(1)).scheduleAction(eq(ActionType.REEL_IN), anyLong(), any(Runnable.class));
    }

    @Test
    void singleplayerUsesTheSameClientBiteState() {
        when(client.isLocalServer()).thenReturn(true);
        setBiting(true);
        autofish.tick(client);
        assertTrue(scheduler.isRecastQueued());
    }

    @Test
    void anotherPlayersBobberCannotTriggerACatch() {
        FishingHook otherHook = mock(FishingHook.class, withSettings().extraInterfaces(FishingHookAccessor.class));
        when(((FishingHookAccessor) otherHook).autofish$isBiting()).thenReturn(true);
        new FishMonitorState().hookTick(autofish, client, otherHook);
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void disabledAutofishingDoesNotReelOnABite() {
        config.setAutofishEnabled(false);
        setBiting(true);
        autofish.tick(client);
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void holdingAnotherItemPreventsStateDetection() {
        when(client.player.getMainHandItem()).thenReturn(ItemStack.EMPTY);
        setBiting(true);
        autofish.tick(client);
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void hookedEntityMetadataDoesNotCountAsABite() {
        autofish.handlePacket(new ClientboundSetEntityDataPacket(10,
                List.of(new DataValue<>(8, EntityDataSerializers.INT, 21))));
        autofish.tick(client);
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void soundAndMotionEventsDoNotTriggerEntityDetection() {
        detectSound(Config.SoundDetectionSource.SERVER_PACKET);
        autofish.handlePacket(new ClientboundSetEntityMotionPacket(10, new Vec3(0, -0.4, 0)));
        assertFalse(scheduler.isRecastQueued());
    }

    @Test
    void changingToSoundDetectionStopsReadingBiteStateAndWorksInSingleplayer() {
        when(client.isLocalServer()).thenReturn(true);
        config.setDetectionMode(Config.DetectionMode.SOUND);
        autofish.setDetection();
        setBiting(true);
        autofish.tick(client);
        assertFalse(scheduler.isRecastQueued());
        verify((FishingHookAccessor) hook, never()).autofish$isBiting();
        detectSound(Config.SoundDetectionSource.SERVER_PACKET);
        assertTrue(scheduler.isRecastQueued());
    }

    @Test
    void soundDetectionAcceptsOnlyTheConfiguredSource() {
        config.setDetectionMode(Config.DetectionMode.SOUND);
        config.setSoundDetectionSource(Config.SoundDetectionSource.CLIENT_PLAYBACK);
        autofish.setDetection();
        detectSound(Config.SoundDetectionSource.SERVER_PACKET);
        assertFalse(scheduler.isRecastQueued());
        detectSound(Config.SoundDetectionSource.CLIENT_PLAYBACK);
        assertTrue(scheduler.isRecastQueued());
    }

    @Test
    void motionDetectionRetainsItsFluidAndSettlingChecks() {
        config.setDetectionMode(Config.DetectionMode.MOTION);
        autofish.setDetection();
        when(hook.level()).thenReturn(client.level);
        when(hook.getBoundingBox()).thenReturn(new AABB(0, 62, 0, 0.25, 62.25, 0.25));
        when(client.level.getFluidState(any())).thenReturn(Fluids.WATER.defaultFluidState());
        autofish.tick(client);
        autofish.timeMillis = 1000;
        autofish.handlePacket(new ClientboundSetEntityMotionPacket(10, new Vec3(0, 0.1, 0)));
        autofish.handlePacket(new ClientboundSetEntityMotionPacket(10, new Vec3(0, -0.4, 0)));
        assertFalse(scheduler.isRecastQueued());
        autofish.timeMillis = 2001;
        autofish.handlePacket(new ClientboundSetEntityMotionPacket(10, new Vec3(0, -0.4, 0)));
        assertTrue(scheduler.isRecastQueued());
    }

    private void setBiting(boolean biting) {
        when(((FishingHookAccessor) hook).autofish$isBiting()).thenReturn(biting);
    }

    private void detectSound(Config.SoundDetectionSource source) {
        autofish.handleSound(net.minecraft.resources.Identifier.fromNamespaceAndPath("minecraft", "entity.fishing_bobber.splash"),
                0, 62, 0, source);
    }
}
