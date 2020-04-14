package com.mumfrey.worldeditcui;

import io.netty.buffer.Unpooled;

import java.io.File;
import java.util.List;

/*
import org.dimdev.rift.listener.MessageAdder;
import org.dimdev.rift.listener.client.ClientTickable;
import org.dimdev.rift.network.Message;
import org.dimdev.riftloader.listener.InitializationListener;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;*/

import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;

import net.minecraft.client.network.packet.CustomPayloadS2CPacket;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.client.MinecraftClient;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.mumfrey.worldeditcui.config.CUIConfiguration;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerChannel;
import com.mumfrey.worldeditcui.event.listeners.CUIListenerWorldRender;
import com.mumfrey.worldeditcui.input.KeySetting;

/**
 * Main litemod entry point
 * 
 * @author Adam Mummery-Smith
 * @author Julius Schoenhut (1337Zero)
 */

public class LiteModWorldEditCUI implements ModInitializer, PacketConsumer,  ClientTickCallback{

	public static String path = "";


	private static final String SUB_CHANNEL_WECUI = "cui";
	private static final String CHANNEL_WECUI = "worldedit";

	private WorldEditCUI controller;
	public static LiteModWorldEditCUI instance;

	private ClientWorld lastWorld;
	private PlayerEntity lastPlayer;

	private static boolean joinedServer = false;
	private boolean init = false;

	private KeySetting keyBindToggleUI;
	private KeySetting keyBindClearSel;
	private KeySetting keyBindChunkBorder;
	private KeySetting keyBindLShift;
	private KeySetting keyBindRShift;

	private boolean visible = true;
	private boolean alwaysOnTop = true;

	private CUIListenerWorldRender worldRenderListener;
	private CUIListenerChannel channelListener;


	private boolean registered = false;

	public void init(File configPath) {
		init = true;
		LiteModWorldEditCUI.instance = this;
		keyBindToggleUI = new KeySetting(290, "wecui.keys.toggle");
		keyBindClearSel = new KeySetting(291, "wecui.keys.clear");
		keyBindChunkBorder = new KeySetting(292, "wecui.keys.chunk");
		keyBindLShift = new KeySetting(340, "wecui.keys.control.lshift");
		keyBindRShift = new KeySetting(344, "wecui.keys.control.rshift");
	}

	public void upgradeSettings(String version, File configPath, File oldConfigPath) {
	}

	public void onInitCompleted() {
		path = System.getProperty("user.dir");
		path = path + System.getProperty("file.separator") + "mods" + System.getProperty("file.separator") + "worldeditcui" + System.getProperty("file.separator");
		File wecuiFolder = new File(path);
		wecuiFolder.mkdirs();
		
		MinecraftClient minecraft = MinecraftClient.getInstance();
		this.controller = new WorldEditCUI();
		this.controller.initialise(minecraft);

		this.worldRenderListener = new CUIListenerWorldRender(this.controller, minecraft);
		this.channelListener = new CUIListenerChannel(this.controller);
	}

	protected PacketByteBuf getRegistrationData() {
		// If any mods have registered channels, send the REGISTER packet
		StringBuilder channelList = new StringBuilder();

		for (String channel : getChannels()) {
			channelList.append(channel);
			channelList.append("\0");
		}

		PacketByteBuf buffer = new PacketByteBuf(Unpooled.buffer());
		buffer.writeBytes(channelList.toString().getBytes(Charsets.UTF_8));
		return buffer;
	}

	private void register() {
		if (!registered) {

			Identifier id = new Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI);
			ServerSidePacketRegistry.INSTANCE.register(id, this);
			ClientSidePacketRegistry.INSTANCE.register(new Identifier(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI), this);
			registered = true;
		}
	}


	public void receiveMessage(String message, String channel) {
		if (channel.trim().equalsIgnoreCase("minecraft")) {
			System.out.println("recieved message from " + channel + ": '" + message.trim());
			if (message.trim().equalsIgnoreCase("worldedit:cui")) {
				System.out.println("register worldedit:cui");
				register();
			} else {
				System.out.println("unknown message '" + message + "'");
			}
		}
	}

	public List<String> getChannels() {
		return ImmutableList.<String>of(LiteModWorldEditCUI.CHANNEL_WECUI + ":" + SUB_CHANNEL_WECUI);
	}

	public void onCustomPayload(String channel, PacketByteBuf data) {
		try {
			int readableBytes = data.readableBytes();
			if (readableBytes > 0) {
				byte[] payload = new byte[readableBytes];
				data.readBytes(payload);
				this.channelListener.onMessage(new String(payload, Charsets.UTF_8));
			} else {
				this.controller.getDebugger().debug("Warning, invalid (zero length) payload received from server");
			}
		} catch (Exception ex) {
		}
	}
	@Override
	public void tick(MinecraftClient client) {		
		if(client.world != null) {			
			this.onTick(client, client.getTickDelta(), true, true);
		}else {
			this.onTick(client, client.getTickDelta(), false, false);
		}
	}
	public void onTick(MinecraftClient mc, float partialTicks, boolean inGame, boolean clock) {
		if(!init) {
			this.init(null);
			init = true;
		}
		/*
		if (MinecraftClient.getInstance().player != null && MinecraftClient.getInstance().getNetworkHandler() != null
				&& !LiteModWorldEditCUI.joinedServer) {
			LiteModWorldEditCUI.joinedServer = true;
		}
		*/

		CUIConfiguration config = this.controller.getConfiguration();

		if (inGame && mc.currentScreen == null) {
			if (this.keyBindToggleUI.isPressed()) {
				if (keyBindLShift.isKeyDown() || keyBindRShift.isKeyDown()) {
					config.setAlwaysOnTop(!config.isAlwaysOnTop());
				} else {
					this.visible = !this.visible;
				}
			}

			if (this.keyBindClearSel.isPressed()) {
				if (mc.player != null) {
					mc.player.sendChatMessage("//sel");
				}

				if (config.isClearAllOnKey()) {
					this.controller.clearRegions();
				}
			}

			if (this.keyBindChunkBorder.isPressed()) {
				this.controller.toggleChunkBorders();
			}
		}
		
		if (inGame && clock && this.controller != null) {
			this.alwaysOnTop = config.isAlwaysOnTop();
			
			if (mc.world != this.lastWorld || mc.player != this.lastPlayer) {
				this.lastWorld = mc.world;
				this.lastPlayer = mc.player;

				
				this.controller.clear();
				if (mc.player != null && config.isPromiscuous()) {
					
					//Delay the we cui command
					Thread t1 =new Thread(new Runnable() {						
						@Override
						public void run() {
							try {
								Thread.sleep(config.getDelayedCommand());
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
							controller.getDebugger().debug("sending /we cui command after " + config.getDelayedCommand() + " nanoseconds");
							mc.player.sendChatMessage("/we cui"); // Tricks WE to send the current selection
						}
					});  
					t1.start();  
					
				}
			}
		}
	}

	public String getName() {
		return "WorldEditCUI - Fabric";
	}

	public String getVersion() {
		return "1.15.2";
	}

	public WorldEditCUI getController() {
		return this.controller;
	}

	@Override
	public void onInitialize() {
		LiteModWorldEditCUI.instance = this;
		MixinBootstrap.init();
		Mixins.addConfiguration("mixins.wecui.json");
		this.onInitCompleted();
		ClientTickCallback.EVENT.register(this);		
	}

	@Override
	public void accept(PacketContext context, PacketByteBuf buffer) {
		onCustomPayload("unused channel", buffer);
	}

	public void onHudRender(float tickDelta, double cameraX, double cameraY, double cameraZ) {
		if (this.visible && this.alwaysOnTop) {
			this.worldRenderListener.onRender(tickDelta,cameraX,cameraY,cameraZ);
		}
	}
}
