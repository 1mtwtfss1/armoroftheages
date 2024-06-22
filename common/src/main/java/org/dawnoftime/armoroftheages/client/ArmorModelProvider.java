package org.dawnoftime.armoroftheages.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import org.dawnoftime.armoroftheages.client.models.ArmorModel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

import static org.dawnoftime.armoroftheages.Constants.MOD_ID;

// Client side
public class ArmorModelProvider {
    private static final ResourceLocation PLAYER_RESOURCE_LOCATION = new ResourceLocation("minecraft:player");

    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier){
        return new ArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier);
    }

    public static ArmorModelProvider create(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier){
        return new MixedArmorModelProvider(armorName, slot, modelSupplier, layerDefinitionSupplier, slimLayerDefinitionSupplier);
    }

    private final Supplier<LayerDefinition> layerDefinitionSupplier;
    protected final ArmorModelSupplier modelSupplier;
    private ArmorModel<?> armorModel;
    private final ModelLayerLocation modelLayerLocation;
    private final ResourceLocation resourceLocations;

    private ArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier){
        this.layerDefinitionSupplier = layerDefinitionSupplier;
        this.modelSupplier = modelSupplier;
        this.modelLayerLocation = new ModelLayerLocation(PLAYER_RESOURCE_LOCATION, armorName + "_" + slot.name().toLowerCase());
        this.resourceLocations = new ResourceLocation(MOD_ID, "textures/models/armor/" + armorName + ".png");
    }

    @NotNull
    public ResourceLocation getTexture(Entity entity) {
        return this.resourceLocations;
    }

    @NotNull
    public ModelLayerLocation getLayerLocation() {
        return this.modelLayerLocation;
    }

    public LayerDefinition createLayer(){
        return this.layerDefinitionSupplier.get();
    }

    public static boolean isSlim(Entity entity){
        return entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName());
    }

    public ArmorModel<?> getArmorModel(Entity entity) {
        if(this.armorModel == null){
            this.armorModel = this.modelSupplier.create(Minecraft.getInstance().getEntityModels().bakeLayer(this.modelLayerLocation), false);
        }
        return this.armorModel;
    }

    public static class MixedArmorModelProvider extends ArmorModelProvider{
        private final Supplier<LayerDefinition> slimLayerDefinitionSupplier;
        private final ModelLayerLocation slimModelLayerLocation;
        private final ResourceLocation slimResourceLocations;
        private ArmorModel<?> slimArmorModel;

        private MixedArmorModelProvider(String armorName, EquipmentSlot slot, ArmorModelSupplier modelSupplier, Supplier<LayerDefinition> layerDefinitionSupplier, Supplier<LayerDefinition> slimLayerDefinitionSupplier){
            super(armorName, slot, modelSupplier, layerDefinitionSupplier);
            this.slimLayerDefinitionSupplier = slimLayerDefinitionSupplier;
            this.slimModelLayerLocation = new ModelLayerLocation(PLAYER_RESOURCE_LOCATION, armorName + "_" + slot.name().toLowerCase() + "_slim");
            this.slimResourceLocations = new ResourceLocation(MOD_ID, "textures/models/armor/" + armorName + "_slim.png");
        }

        @NotNull
        public ModelLayerLocation getSlimLayerLocation() {
            return this.slimModelLayerLocation;
        }

        public LayerDefinition createSlimLayer(){
            return this.slimLayerDefinitionSupplier.get();
        }

        @Override
        public @NotNull ResourceLocation getTexture(Entity entity) {
            return isSlim(entity) ? this.slimResourceLocations : super.getTexture(entity);
        }

        @Override
        public ArmorModel<?> getArmorModel(Entity entity) {
            if(ArmorModelProvider.isSlim(entity)){
                if(this.slimArmorModel == null){
                    this.slimArmorModel = this.modelSupplier.create(Minecraft.getInstance().getEntityModels().bakeLayer(this.slimModelLayerLocation), true);
                }
                return this.slimArmorModel;
            }else{
                return super.getArmorModel(entity);
            }
        }
    }
}