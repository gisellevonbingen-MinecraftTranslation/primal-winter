/*
 * Part of the Primal Winter by AlcatrazEscapee
 * Work under Copyright. See the project LICENSE.md for details.
 */

package com.alcatrazescapee.primalwinter.mixin.world;

import java.util.function.Supplier;

import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ISpawnWorldInfo;

import com.alcatrazescapee.primalwinter.util.Helpers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World
{
    private ServerWorldMixin(ISpawnWorldInfo worldInfo, RegistryKey<World> worldKey, DimensionType dimensionType, Supplier<IProfiler> profiler, boolean b1, boolean b2, long long1)
    {
        super(worldInfo, worldKey, dimensionType, profiler, b1, b2, long1);
    }

    /**
     * Place additional snow layers
     *
     * @reason MinecraftForge#7235
     */
    @Inject(method = "tickEnvironment", at = @At(value = "RETURN"))
    public void tickChunk(Chunk chunk, int randomTickSpeed, CallbackInfo ci)
    {
        if (rand.nextInt(16) == 0)
        {
            int blockX = chunk.getPos().getXStart();
            int blockZ = chunk.getPos().getZStart();
            BlockPos pos = getHeight(Heightmap.Type.MOTION_BLOCKING, getBlockRandomPos(blockX, 0, blockZ, 15));
            Helpers.placeExtraSnowOnTickChunk((ServerWorld) (Object) this, pos);
        }
    }
}
