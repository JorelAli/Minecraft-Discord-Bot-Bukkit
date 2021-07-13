package minedisco.minecraft;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import org.bukkit.advancement.Advancement;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import minedisco.discord.DiscordBot;
import net.minecraft.advancements.AdvancementDisplay;
import net.minecraft.network.chat.IChatBaseComponent;

public class PlayerAdvancementListener implements Listener {

	private DiscordBot bot;
	private MethodHandle cachedMethodHandle = null;

	public PlayerAdvancementListener(DiscordBot bot) {
		this.bot = bot;
	}

	private String getAdvancementTitle(Advancement advancement) {
		if(cachedMethodHandle == null) {
			try {
				MethodType methodType = MethodType.methodType(net.minecraft.advancements.Advancement.class);
				cachedMethodHandle = MethodHandles.lookup().findVirtual(advancement.getClass(), "getHandle", methodType);
			} catch (ReflectiveOperationException e) {
				System.out.println("Failed to get MethodHandle for " + advancement.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		
		try {
			net.minecraft.advancements.Advancement nmsAdvancement = (net.minecraft.advancements.Advancement) cachedMethodHandle.invoke(advancement);
			AdvancementDisplay displayInfo = nmsAdvancement.c(); // DisplayInfo getDisplay()
			IChatBaseComponent title = displayInfo.a(); // Component getTitle()
			return title.getString();
		} catch (Throwable e) {
			e.printStackTrace();
			return "";
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void advancementDone(PlayerAdvancementDoneEvent event) {
		bot.sendMessageToChannel(event.getPlayer().getName() + " has made the advancement ["
				+ getAdvancementTitle(event.getAdvancement()) + "]");
	}

}
