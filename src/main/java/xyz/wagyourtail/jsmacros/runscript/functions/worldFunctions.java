package xyz.wagyourtail.jsmacros.runscript.functions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import xyz.wagyourtail.jsmacros.jsMacros;
import xyz.wagyourtail.jsmacros.compat.interfaces.IBossBarHud;
import xyz.wagyourtail.jsmacros.reflector.BlockDataHelper;
import xyz.wagyourtail.jsmacros.reflector.BlockPosHelper;
import xyz.wagyourtail.jsmacros.reflector.BossBarHelper;
import xyz.wagyourtail.jsmacros.reflector.EntityHelper;
import xyz.wagyourtail.jsmacros.reflector.PlayerEntityHelper;
import xyz.wagyourtail.jsmacros.reflector.PlayerListEntryHelper;

public class worldFunctions extends Functions {
    public worldFunctions(String libName) {
        super(libName);
    }
    
    public worldFunctions(String libName, List<String> excludeLanguages) {
        super(libName, excludeLanguages);
    }
    
    public List<PlayerEntityHelper> getLoadedPlayers() {
        List<PlayerEntityHelper> players = new ArrayList<>();
        for (AbstractClientPlayerEntity p : ImmutableList.copyOf(mc.world.getPlayers())) {
            players.add(new PlayerEntityHelper(p));
        }
        return players;
    }
    
    public List<PlayerListEntryHelper> getPlayers() {
        List<PlayerListEntryHelper> players = new ArrayList<>();
        for (PlayerListEntry p : ImmutableList.copyOf(mc.getNetworkHandler().getPlayerList())) {
            players.add(new PlayerListEntryHelper(p));
        }
        return players;
    }
    
    public BlockDataHelper getBlock(int x, int y, int z) {
        BlockState b = mc.world.getBlockState(new BlockPos(x,y,z));
        BlockEntity t = mc.world.getBlockEntity(new BlockPos(x,y,z));
        if (b.getBlock().equals(Blocks.VOID_AIR)) return null;
        return new BlockDataHelper(b, t, new BlockPos(x,y,z));
        
    }
    
    public List<EntityHelper> getEntities() {
        List<EntityHelper> entities = new ArrayList<>();
        for (Entity e : ImmutableList.copyOf(mc.world.getEntities())) {
            if (e.getType() == EntityType.PLAYER) {
                entities.add(new PlayerEntityHelper((PlayerEntity)e));
            } else {
                entities.add(new EntityHelper(e));
            }
        }
        return entities;
    }
    
    public String getDimension() {
        return mc.world.getRegistryKey().getValue().toString();
    }
    
    public String getBiome() {
        return mc.world.getRegistryManager().get(Registry.BIOME_KEY).getId(mc.world.getBiome(mc.player.getBlockPos())).toString();
    }
    
    public long getTime() {
        return mc.world.getTime();
    }
    
    public long getTimeOfDay() {
        return mc.world.getTimeOfDay();
    }
    
    public BlockPosHelper getRespawnPos() {
        if (mc.world.getDimension().isNatural()) return new BlockPosHelper( mc.world.getSpawnPos());
        return null;
    }
    
    public int getDifficulty() {
        return mc.world.getDifficulty().getId();
    }
    
    public int moonPhase() {
        return mc.world.getMoonPhase();
    }
    
    public int getSkyLight(int x, int y, int z) {
        return mc.world.getLightLevel(LightType.SKY, new BlockPos(x, y, z));
    }
    
    public int getBlockLight(int x, int y, int z) {
        return mc.world.getLightLevel(LightType.BLOCK, new BlockPos(x, y, z));
    }
    
    public Clip playSoundFile(String file, double volume) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(new File(jsMacros.config.macroFolder, file)));
        FloatControl gainControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
        float min = gainControl.getMinimum();
        float range = gainControl.getMaximum() - min;
        float gain = (float) ((range * volume) + min);
        gainControl.setValue(gain);
        clip.addLineListener(new LineListener() {
            @Override
            public void update(LineEvent event) {
                if(event.getType().equals(LineEvent.Type.STOP)) {
                    clip.close();
                }
            }
        });
        clip.start();
        return clip;
    }
    
    public void playSound(String id) {
        playSound(id, 1F);
    }
    
    public void playSound(String id, float volume) {
        playSound(id, volume, 0.25F);
    }
    
    public void playSound(String id, float volume, float pitch) {
        mc.getSoundManager().play(PositionedSoundInstance.master(Registry.SOUND_EVENT.get(new Identifier(id)), pitch, volume));
    }
    
    public void playSound(String id, float volume, float pitch, double x, double y, double z) {
        mc.world.playSound(x, y, z, Registry.SOUND_EVENT.get(new Identifier(id)), SoundCategory.MASTER, volume, pitch, true);
    }
    
    public Map<String, BossBarHelper> getBossBars() {
        Map<UUID, ClientBossBar> bars = ImmutableMap.copyOf(((IBossBarHud) mc.inGameHud.getBossBarHud()).getBossBars());
        Map<String, BossBarHelper> out = new HashMap<>();
        for (Map.Entry<UUID, ClientBossBar> e : ImmutableList.copyOf(bars.entrySet())) {
            out.put(e.getKey().toString(), new BossBarHelper(e.getValue()));
        }
        return out;
    }
    
    public boolean isChunkLoaded(int chunkX, int chunkZ) {
        if (mc.world == null) return false;
        return mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ);
    }
    
    public String getCurrentServerAddress() {
        ClientPlayNetworkHandler h = mc.getNetworkHandler();
        if (h == null) return null;
        ClientConnection c = h.getConnection();
        if (c == null) return null;
        return c.getAddress().toString();
    }
    
    public String getBiomeAt(int x, int z) {
        return mc.world.getRegistryManager().get(Registry.BIOME_KEY).getId(mc.world.getBiome(new BlockPos(x, 10, z))).toString();
    }
    
    
}
