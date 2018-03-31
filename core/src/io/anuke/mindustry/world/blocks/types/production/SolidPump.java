package io.anuke.mindustry.world.blocks.types.production;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.content.Liquids;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.graphics.Fx;
import io.anuke.mindustry.resource.Liquid;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Mathf;

/**Pump that makes liquid from solids and takes in power. Only works on solid floor blocks.*/
public class SolidPump extends Pump {
    protected Liquid result = Liquids.water;
    /**Power use per liquid unit.*/
    protected float powerUse = 0.1f;
    protected Effect updateEffect = Fx.none;
    protected float updateEffectChance = 0.02f;
    protected float rotateSpeed = 1f;

    protected final Array<Tile> drawTiles = new Array<>();

    public SolidPump(String name){
        super(name);
        hasPower = true;
        liquidRegion = name + "-liquid";
    }

    @Override
    public void draw(Tile tile) {
        SolidPumpEntity entity = tile.entity();

        Draw.rect(name, tile.drawx(), tile.drawy());
        Draw.color(tile.entity.liquid.liquid.color);
        Draw.alpha(tile.entity.liquid.amount / liquidCapacity);
        Draw.rect(liquidRegion, tile.drawx(), tile.drawy());
        Draw.color();
        Draw.rect(name + "-rotator", tile.drawx(), tile.drawy(), entity.pumpTime * rotateSpeed);
        Draw.rect(name + "-top", tile.drawx(), tile.drawy());
    }

    @Override
    public TextureRegion[] getIcon() {
        return new TextureRegion[]{Draw.region(name), Draw.region(name + "-rotator"), Draw.region(name + "-top")};
    }

    @Override
    public void update(Tile tile){
        SolidPumpEntity entity = tile.entity();

        float used = Math.min(powerUse * Timers.delta(), powerCapacity);

        float fraction = 0f;

        if(isMultiblock()){
            for(Tile other : tile.getLinkedTiles(tempTiles)){
                if(isValid(other)){
                    fraction += 1f/ size;
                }
            }
        }else{
            if(isValid(tile)) fraction = 1f;
        }

        if(tile.entity.power.amount >= used && tile.entity.liquid.amount < liquidCapacity - 0.001f){
            float maxPump = Math.min(liquidCapacity - tile.entity.liquid.amount, pumpAmount * Timers.delta() * fraction);
            tile.entity.liquid.liquid = result;
            tile.entity.liquid.amount += maxPump;
            tile.entity.power.amount -= used;
            entity.warmup = Mathf.lerpDelta(entity.warmup, 1f, 0.02f);
            if(Mathf.chance(Timers.delta() * updateEffectChance))
                Effects.effect(updateEffect, entity.x + Mathf.range(size*2f), entity.y + Mathf.range(size*2f));
        }else{
            entity.warmup = Mathf.lerpDelta(entity.warmup, 0f, 0.02f);
        }

        entity.pumpTime += entity.warmup * Timers.delta();

        tryDumpLiquid(tile);
    }

    @Override
    public boolean isLayer(Tile tile) {
        if(isMultiblock()){
            for(Tile other : tile.getLinkedTiles(drawTiles)){
                if(isValid(other)){
                    return false;
                }
            }
            return true;
        }else{
            return !isValid(tile);
        }
    }

    protected boolean isValid(Tile tile){
        return !tile.floor().liquid;
    }

    @Override
    public TileEntity getEntity() {
        return new SolidPumpEntity();
    }

    public static class SolidPumpEntity extends TileEntity{
        public float warmup;
        public float pumpTime;
    }
}