package com.cursorhide;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.nio.IntBuffer;

@Mod(modid = "cursorhide", name = "CursorHide", version = "1.0", clientSideOnly = true, acceptedMinecraftVersions = "[1.8.9]")
public class CursorHide {

    private static KeyBinding toggleKey;
    private static boolean hidden = false;
    private static boolean keyWasDown = false;
    private static GuiScreen lastScreen = null;
    private static Cursor emptyCursor = null;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        toggleKey = new KeyBinding("Esconder cursor", Keyboard.KEY_H, "CursorHide");
        ClientRegistry.registerKeyBinding(toggleKey);
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();

        // alterna uma vez por aperto da tecla, dentro do jogo e nos menus
        boolean down = isBindDown(toggleKey.getKeyCode());
        if (down && !keyWasDown && !(mc.currentScreen instanceof GuiChat)) {
            toggle(mc);
        }
        keyWasDown = down;

        // reaplica o ponteiro quando a tela muda, porque o Minecraft pode resetá-lo
        if (mc.currentScreen != lastScreen) {
            lastScreen = mc.currentScreen;
            applyCursor();
        }
    }

    private static boolean isBindDown(int code) {
        if (code == Keyboard.KEY_NONE) return false;
        if (code < 0) return Mouse.isButtonDown(code + 100); // botão do mouse
        return Keyboard.isKeyDown(code);
    }

    private static void toggle(Minecraft mc) {
        hidden = !hidden;
        applyCursor();
        if (mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new ChatComponentText(
                    "§7[CursorHide] " + (hidden ? "§aPonteiro escondido" : "§cPonteiro visível")));
        }
    }

    private static void applyCursor() {
        try {
            if (!Mouse.isCreated()) return;
            if (hidden) {
                if (emptyCursor == null) {
                    int size = Math.max(1, Cursor.getMinCursorSize());
                    IntBuffer buf = BufferUtils.createIntBuffer(size * size); // zeros = totalmente transparente
                    emptyCursor = new Cursor(size, size, 0, 0, 1, buf, null);
                }
                Mouse.setNativeCursor(emptyCursor);
            } else {
                Mouse.setNativeCursor(null);
            }
        } catch (Throwable ignored) {
            // se o ambiente não suportar cursor nativo, o ponteiro simplesmente continua visível
        }
    }
}
