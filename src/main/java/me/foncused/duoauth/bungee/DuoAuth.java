package me.foncused.duoauth.bungee;

import me.foncused.duoauth.bungee.event.Event;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class DuoAuth extends Plugin {

	private ProxyServer proxy;

	@Override
	public void onEnable() {
		this.proxy = this.getProxy();
		this.proxy.registerChannel("duoauth:filter");
		this.registerEvents();
	}

	private void registerEvents() {
		this.proxy.getPluginManager().registerListener(this, new Event(this));
	}

}
