package de.st_ddt.crazyplugin.events;

import java.util.Collection;
import java.util.TreeSet;

import org.bukkit.event.HandlerList;

import de.st_ddt.crazyplugin.CrazyLightPluginInterface;

public class CrazyPlayerIPsConnectedToNameEvent extends CrazyEvent<CrazyLightPluginInterface>
{

	private static final HandlerList handlers = new HandlerList();
	protected final String name;
	protected final TreeSet<String> ips = new TreeSet<String>();

	public CrazyPlayerIPsConnectedToNameEvent(final CrazyLightPluginInterface plugin, final String name)
	{
		super(plugin);
		this.name = name;
	}

	public String getSearchedName()
	{
		return name;
	}

	public void add(final String ip)
	{
		ips.add(ip);
	}

	public void addAll(final Collection<String> ips)
	{
		ips.addAll(ips);
	}

	public TreeSet<String> getIPs()
	{
		return ips;
	}

	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}

	public static HandlerList getHandlerList()
	{
		return handlers;
	}
}
