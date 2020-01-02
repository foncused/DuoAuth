package me.foncused.duoauth.bungee;

import me.foncused.duoauth.bungee.event.Event;
import net.md_5.bungee.api.plugin.Plugin;

public class DuoAuth extends Plugin {

	@Override
	public void onEnable() {
		this.getProxy().registerChannel("duoauth:filter");
		this.registerEvents();
	}

	private void registerEvents() {
		this.getProxy().getPluginManager().registerListener(this, new Event(this));
	}

}
