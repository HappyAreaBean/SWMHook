package cc.happyareabean.swmhook.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@AllArgsConstructor
public class SWMWorldLoadedEvent extends Event {

	private static final HandlerList HANDLERS = new HandlerList();

 	@Getter private String templateName;
 	@Getter private String worldName;
 	@Getter private Boolean success;

	@Override
	public HandlerList getHandlers() {
		return HANDLERS;
	}

	public static HandlerList getHandlerList() {
		return HANDLERS;
	}
}
