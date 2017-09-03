package lain.mods.skins;

import lain.mods.skins.api.ISkin;
import lain.mods.skins.api.ISkinProviderService;
import lain.mods.skins.api.SkinProviderAPI;
import lain.mods.skins.providers.CrafatarCachedCapeProvider;
import lain.mods.skins.providers.CrafatarCachedSkinProvider;
import lain.mods.skins.providers.CustomCachedCapeProvider;
import lain.mods.skins.providers.CustomCachedSkinProvider;
import lain.mods.skins.providers.MojangCachedCapeProvider;
import lain.mods.skins.providers.MojangCachedSkinProvider;
import lain.mods.skins.providers.UserManagedCapeProvider;
import lain.mods.skins.providers.UserManagedSkinProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.mojang.authlib.GameProfile;

@Mod(modid = "offlineskins", useMetadata = true, acceptedMinecraftVersions = "[1.11],[1.11.2]")
public class OfflineSkins
{

    @SideOnly(Side.CLIENT)
    public static ResourceLocation bindTexture(GameProfile profile, ResourceLocation result)
    {
        if (SkinData.isDefaultSkin(result) && profile != null)
        {
            ISkin skin = skinService.getSkin(profile);
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationCape(AbstractClientPlayer player, ResourceLocation result)
    {
        if (result == null && capeService != null)
        {
            ISkin cape = capeService.getSkin(player.getGameProfile());
            if (cape != null && cape.isSkinReady())
                return cape.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static ResourceLocation getLocationSkin(AbstractClientPlayer player, ResourceLocation result)
    {
        if (SkinPass)
            return result;

        if (usingDefaultSkin(player) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player.getGameProfile());
            if (skin != null && skin.isSkinReady())
                return skin.getSkinLocation();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static String getSkinType(AbstractClientPlayer player, String result)
    {
        if (usingDefaultSkin(player) && skinService != null)
        {
            ISkin skin = skinService.getSkin(player.getGameProfile());
            if (skin != null && skin.isSkinReady())
                return skin.getSkinType();
        }
        return result;
    }

    @SideOnly(Side.CLIENT)
    public static boolean usingDefaultSkin(AbstractClientPlayer player)
    {
        try
        {
            SkinPass = true;
            return SkinData.isDefaultSkin(player.getLocationSkin());
        }
        finally
        {
            SkinPass = false;
        }
    }

    private static boolean SkinPass = false;

    @SideOnly(Side.CLIENT)
    public static ISkinProviderService skinService;
    @SideOnly(Side.CLIENT)
    public static ISkinProviderService capeService;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void handleClientTicks(TickEvent.ClientTickEvent event)
    {
        if (skinService == null && capeService == null)
            return;

        if (event.phase == TickEvent.Phase.START)
        {
            World world = Minecraft.getMinecraft().world;
            if (world != null && world.playerEntities != null && !world.playerEntities.isEmpty())
            {
                for (Object obj : world.playerEntities)
                {
                    // This should keep skins/capes loaded.
                    if (obj instanceof AbstractClientPlayer)
                    {
                        if (skinService != null)
                            skinService.getSkin(((AbstractClientPlayer) obj).getGameProfile());
                        if (capeService != null)
                            capeService.getSkin(((AbstractClientPlayer) obj).getGameProfile());
                    }
                }
            }
        }
    }

    @Mod.EventHandler
    public void init(FMLPreInitializationEvent event)
    {
        if (event.getSide().isClient())
        {
            Configuration config = new Configuration(event.getSuggestedConfigurationFile());
            boolean useCustomProvider = config.get(Configuration.CATEGORY_CLIENT, "useCustomProvider", true).getBoolean(true);
            String customProvider = config.get(Configuration.CATEGORY_CLIENT, "CustomProvider", "https://crafatar.com", "[default: https://crafatar.com]").getString();
            if (config.hasChanged())
                config.save();

            skinService = SkinProviderAPI.createService();
            capeService = SkinProviderAPI.createService();
            if (useCustomProvider)
                skinService.register(new CustomCachedSkinProvider(customProvider));
            if (useCustomProvider)
                capeService.register(new CustomCachedCapeProvider(customProvider));
            MinecraftForge.EVENT_BUS.register(this);
        }
    }

}
