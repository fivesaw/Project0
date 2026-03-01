package com.legitclient.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.Vec3;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RenderCache {

    private static final int CACHE_VALIDITY_MS = 50;
    private static final float POSITION_THRESHOLD = 0.1f;

    private final Map<Integer, CachedEntityData> entityCache;
    private final Map<String, Object> genericCache;

    private long lastCacheTime;
    private Vec3 lastPlayerPos;
    private float lastPlayerYaw;
    private float lastPlayerPitch;
    private boolean cacheValid;

    public RenderCache() {
        this.entityCache = new ConcurrentHashMap<Integer, CachedEntityData>();
        this.genericCache = new ConcurrentHashMap<String, Object>();
        this.lastCacheTime = 0L;
        this.lastPlayerPos = new Vec3(0.0D, 0.0D, 0.0D);
        this.lastPlayerYaw = 0f;
        this.lastPlayerPitch = 0f;
        this.cacheValid = false;
    }

    public void onRenderTick() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) {
            invalidate();
            return;
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCacheTime > CACHE_VALIDITY_MS) {
            cacheValid = false;
        }

        Vec3 currentPos = mc.thePlayer.getPositionVector();
        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        if (lastPlayerPos != null) {
            double dx = currentPos.xCoord - lastPlayerPos.xCoord;
            double dy = currentPos.yCoord - lastPlayerPos.yCoord;
            double dz = currentPos.zCoord - lastPlayerPos.zCoord;

            if (dx * dx + dy * dy + dz * dz > POSITION_THRESHOLD * POSITION_THRESHOLD) {
                cacheValid = false;
            }
        }

        if (Math.abs(currentYaw - lastPlayerYaw) > 5f || Math.abs(currentPitch - lastPlayerPitch) > 5f) {
            invalidateEntityRotations();
        }

        lastPlayerPos = currentPos;
        lastPlayerYaw = currentYaw;
        lastPlayerPitch = currentPitch;
        lastCacheTime = currentTime;
    }

    public CachedEntityData getEntityData(Entity entity) {
        int entityId = entity.getEntityId();
        CachedEntityData data = entityCache.get(entityId);

        if (data == null || !data.isValid()) {
            data = new CachedEntityData(entity);
            entityCache.put(entityId, data);
        }

        return data;
    }

    public void putGeneric(String key, Object value) {
        genericCache.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T getGeneric(String key, Class<T> type) {
        Object value = genericCache.get(key);
        if (value != null && type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public void invalidate() {
        cacheValid = false;
        entityCache.clear();
        genericCache.clear();
    }

    public void fullInvalidate() {
        invalidate();
        lastPlayerPos = null;
        lastPlayerYaw = 0f;
        lastPlayerPitch = 0f;
    }

    private void invalidateEntityRotations() {
        for (CachedEntityData data : entityCache.values()) {
            data.invalidateRenderPos();
        }
    }

    public void removeEntity(int entityId) {
        entityCache.remove(entityId);
    }

    public boolean isCacheValid() {
        return cacheValid;
    }

    public void setCacheValid(boolean valid) {
        this.cacheValid = valid;
    }

    public static class CachedEntityData {
        private final int entityId;
        private Vec3 position;
        private Vec3 renderOffset;
        private double lastUpdateTick;
        private boolean renderPosValid;

        public CachedEntityData(Entity entity) {
            this.entityId = entity.getEntityId();
            this.position = entity.getPositionVector();
            this.lastUpdateTick = entity.ticksExisted;
            this.renderPosValid = false;
        }

        public Vec3 getPosition() {
            return position;
        }

        public void updatePosition(Vec3 newPos) {
            this.position = newPos;
        }

        public Vec3 getRenderOffset() {
            return renderOffset;
        }

        public void setRenderOffset(Vec3 offset) {
            this.renderOffset = offset;
            this.renderPosValid = true;
        }

        public boolean isValid() {
            return true;
        }

        public boolean isRenderPosValid() {
            return renderPosValid;
        }

        public void invalidateRenderPos() {
            this.renderPosValid = false;
        }
    }
}
