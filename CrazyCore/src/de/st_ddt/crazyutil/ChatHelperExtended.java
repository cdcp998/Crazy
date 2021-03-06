package de.st_ddt.crazyutil;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.command.CommandSender;

import de.st_ddt.crazyplugin.data.ParameterData;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandNoSuchException;
import de.st_ddt.crazyplugin.exceptions.CrazyCommandUsageException;
import de.st_ddt.crazyplugin.exceptions.CrazyException;
import de.st_ddt.crazyutil.FilterInterface.FilterInstanceInterface;
import de.st_ddt.crazyutil.paramitrisable.BooleanParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.ColoredStringParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.IntegerParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.Paramitrisable;
import de.st_ddt.crazyutil.paramitrisable.SortStringParamitrisable;
import de.st_ddt.crazyutil.paramitrisable.StringParamitrisable;

public class ChatHelperExtended extends ChatHelper
{

	public final static Pattern PATTERN_OPENER = Pattern.compile("\\{");
	public final static Pattern PATTERN_CLOSER = Pattern.compile("\\}");
	public final static Pattern PATTERN_STRING = Pattern.compile("\"");

	protected ChatHelperExtended()
	{
	}

	public static <S extends ParameterData, T extends S> void processFullListCommand(final CommandSender sender, final String[] args, final String chatHeader, ListFormat format, final Collection<FilterInstanceInterface<S>> filters, final Map<String, ? extends Comparator<S>> sorters, final Comparator<S> defaultSort, final ListOptionsModder<T> modder, final List<T> datas) throws CrazyException
	{
		if (format == null)
			format = ListFormat.DEFAULTFORMAT;
		processFullListCommand(sender, args, chatHeader, format.headFormat(sender), format.listFormat(sender), format.entryFormat(sender), filters, sorters, defaultSort, modder, datas);
	}

	public static <S extends ParameterData, T extends S> void processFullListCommand(final CommandSender sender, final String[] args, final String chatHeader, final String headFormat, final String listFormat, final String entryFormat, final Collection<FilterInstanceInterface<S>> filters, final Map<String, ? extends Comparator<S>> sorters, final Comparator<S> defaultSort, final ListOptionsModder<T> modder, final List<T> datas) throws CrazyException
	{
		final String[] pipe = processListCommand(sender, args, chatHeader, headFormat, listFormat, entryFormat, filters, sorters, defaultSort, modder, datas);
		if (pipe != null)
			CrazyPipe.pipe(sender, datas, pipe);
	}

	public static <S, T extends S> String[] processListCommand(final CommandSender sender, final String[] args, final String chatHeader, ListFormat format, final Collection<FilterInstanceInterface<S>> filters, final Map<String, ? extends Comparator<S>> sorters, final Comparator<S> defaultSort, final ListOptionsModder<T> modder, final List<T> datas) throws CrazyException
	{
		if (format == null)
			format = ListFormat.DEFAULTFORMAT;
		return processListCommand(sender, args, chatHeader, format.headFormat(sender), format.listFormat(sender), format.entryFormat(sender), filters, sorters, defaultSort, modder, datas);
	}

	/**
	 * Process a list command and show data to sender or return pipe commands.
	 * 
	 * @param sender
	 *            The Sender the list is shown to.
	 * @param args
	 *            The args used to specify the list options
	 * @param defaultChatHeader
	 *            The chatHeader which should be shown.
	 * @param defaultHeadFormat
	 *            Used in {@link CrazyPages#show(CommandSender) CrazyPages} as message head. </br> $0$ = current Page </br> $1$ = max Page </br> $2$ = chatHeader </br> $3$ = current date.
	 * @param defaultListFormat
	 *            Used to show and seperate entries. </br> $0$ = index </br> $1$ = entry </br> $2$ = chatHeader.
	 * @param defaultEntryFormat
	 *            Used to format entries (Works only with {@link ParameterData}. Used in {@link #putArgsPara(String, ParameterData) putArgsPara}.
	 * @param filters
	 *            Available filters, can be null
	 * @param sorters
	 *            Available sorters, will be selected via sort:Name. Can be null.
	 * @param defaultSort
	 *            Default sorter, can be null.
	 * @param modder
	 *            Mods parameters and list, can be null.
	 * @param datas
	 *            The datas which should be displayed. WARNING: This List will be edited/filtered!
	 * @return Returns pipeCommands if available otherwise null
	 * @throws CrazyException
	 *             Caused by readParameters
	 * @see de.st_ddt.crazyutil.CrazyPages
	 */
	public static <S, T extends S> String[] processListCommand(final CommandSender sender, final String[] args, final String defaultChatHeader, String defaultHeadFormat, String defaultListFormat, String defaultEntryFormat, final Collection<FilterInstanceInterface<S>> filters, final Map<String, ? extends Comparator<S>> sorters, final Comparator<S> defaultSort, final ListOptionsModder<T> modder, final List<T> datas) throws CrazyException
	{
		final Map<String, Paramitrisable> params = new TreeMap<String, Paramitrisable>();
		// add filters
		if (filters != null)
			params.putAll(Filter.getFilterMap(filters));
		// add additional parameters
		final SortStringParamitrisable<S> sort = new SortStringParamitrisable<S>(sorters, defaultSort);
		if (sorters != null)
			params.put("sort", sort);
		final BooleanParamitrisable reverse = new BooleanParamitrisable(false);
		params.put("reverse", reverse);
		final IntegerParamitrisable amount = new IntegerParamitrisable(10)
		{

			@Override
			public void setParameter(final String parameter) throws CrazyException
			{
				if (parameter.equals("*"))
					value = -1;
				else
					super.setParameter(parameter);
			}
		};
		params.put("a", amount);
		params.put("amount", amount);
		final IntegerParamitrisable page = new IntegerParamitrisable(1)
		{

			@Override
			public void setParameter(final String parameter) throws CrazyException
			{
				if (parameter.equals("*"))
					amount.setParameter(parameter);
				else
					super.setParameter(parameter);
			}
		};
		params.put("p", page);
		params.put("page", page);
		params.put("", page);
		// add style params
		final StringParamitrisable chatHeader = new ColoredStringParamitrisable(defaultChatHeader);
		params.put("chatheader", chatHeader);
		if (defaultHeadFormat == null)
			defaultHeadFormat = ListFormat.DEFAULTFORMAT.headFormat(sender);
		final StringParamitrisable headFormat = new ColoredStringParamitrisable(defaultHeadFormat);
		params.put("headformat", headFormat);
		if (defaultListFormat == null)
			defaultListFormat = ListFormat.DEFAULTFORMAT.listFormat(sender);
		final StringParamitrisable listFormat = new ColoredStringParamitrisable(defaultListFormat);
		params.put("listformat", listFormat);
		if (defaultEntryFormat == null)
			defaultEntryFormat = ListFormat.DEFAULTFORMAT.entryFormat(sender);
		final StringParamitrisable entryFormat = new ColoredStringParamitrisable(defaultEntryFormat);
		params.put("entryformat", entryFormat);
		// work parameters
		if (modder != null)
			modder.modListPreOptions(params, datas);
		String[] pipe = readParameters(args, params);
		if (modder != null)
			pipe = modder.modListPostOptions(datas, pipe);
		// Filter
		if (filters != null)
			for (final FilterInstanceInterface<S> filter : filters)
				filter.filter(datas);
		// Sort
		if (sort.getSorter() != null)
			Collections.sort(datas, sort.getSorter());
		if (reverse.getValue())
			Collections.reverse(datas);
		// Output
		if (pipe == null)
			sendList(sender, chatHeader.getValue(), headFormat.getValue(), listFormat.getValue(), entryFormat.getValue(), amount.getValue(), page.getValue(), datas);
		else if (pipe[0].equals("page"))
			pipe = insertArray(pipe, 1, new String[] { "page:" + page.getValue(), "amount:" + amount.getValue(), "headFormat:\"" + headFormat.getValue() + "\"", "listFormat:\"" + listFormat.getValue() + "\"", "entryFormat:\"" + entryFormat.getValue() + "\"", "chatHeader:\"" + chatHeader.getValue() + "\"" });
		return pipe;
	}

	public static void sendList(final CommandSender sender, final String chatHeader, ListFormat format, final int amount, final int page, final List<?> datas)
	{
		if (format == null)
			format = ListFormat.DEFAULTFORMAT;
		sendList(sender, chatHeader, format.headFormat(sender), format.listFormat(sender), format.entryFormat(sender), amount, page, datas);
	}

	public static void sendList(final CommandSender sender, String chatHeader, final String headFormat, final String listFormat, final String entryFormat, int amount, int page, final List<?> datas)
	{
		if (ChatHelper.isShowingChatHeadersEnabled() == false)
			chatHeader = "";
		final int lastIndex = datas.size();
		if (page == Integer.MIN_VALUE)
			amount = lastIndex;
		else if (amount == 0)
			amount = 10;
		else if (amount < 0)
			amount = Math.max(lastIndex, 1);
		page = Math.max(1, page);
		CrazyPages.storePages(sender, chatHeader, headFormat, listFormat, entryFormat, amount, page, datas).show(sender);
	}

	public static <S> String[] readParameters(final String[] args, final Map<String, ? extends Paramitrisable> params) throws CrazyException
	{
		final int length = args.length;
		for (int i = 0; i < length; i++)
		{
			String header = null;
			String value = args[i];
			final String[] split = value.split(":", 2);
			if (split.length == 2)
			{
				header = split[0];
				value = split[1];
				if (value.startsWith("\""))
				{
					while (!value.endsWith("\""))
					{
						i++;
						if (i == length)
							throw new CrazyCommandUsageException(ChatHelper.listingString(" ", args) + "\"");
						value += " " + args[i];
					}
					final int len = value.length();
					value = value.substring(1, len - 1);
				}
				else if (value.startsWith("{"))
				{
					int depth = 1;
					int start = 1;
					Matcher matcher = PATTERN_OPENER.matcher(value);
					while (matcher.find(start))
					{
						start = matcher.end();
						depth++;
					}
					matcher = PATTERN_CLOSER.matcher(value);
					while (matcher.find(start))
					{
						start = matcher.end();
						depth--;
					}
					while (depth != 0)
					{
						i++;
						if (i == length)
							throw new CrazyCommandUsageException(ChatHelper.listingString(" ", args) + "}");
						value += " " + args[i];
						start = 0;
						matcher = PATTERN_OPENER.matcher(args[i]);
						while (matcher.find(start))
						{
							start = matcher.end();
							depth++;
						}
						matcher = PATTERN_CLOSER.matcher(args[i]);
						while (matcher.find(start))
						{
							start = matcher.end();
							depth--;
						}
					}
					final int len = value.length();
					value = value.substring(1, len - 1);
				}
			}
			else if (value.equals(">"))
				return shiftArray(args, i + 1);
			else
				header = "";
			final Paramitrisable param = params.get(header.toLowerCase());
			if (param == null)
				if (header.length() == 0)
					throw new CrazyCommandNoSuchException("Parameter", value, params.keySet());
				else
					throw new CrazyCommandNoSuchException("Parameter", header, params.keySet());
			try
			{
				param.setParameter(value);
			}
			catch (final CrazyCommandException e)
			{
				e.shiftCommandIndex(i);
				throw e;
			}
		}
		return null;
	}

	public static <S> S[] cutArray(final S[] args, final int anz)
	{
		if (anz < 0)
			return Arrays.copyOf(args, 0);
		else
			return Arrays.copyOfRange(args, 0, anz);
	}

	public static <S> S[] shiftArray(final S[] args, final int anz)
	{
		if (anz >= args.length)
			return Arrays.copyOf(args, 0);
		else
			return Arrays.copyOfRange(args, anz, args.length);
	}

	public static <S> S[] appendArray(final S[] array1, final S... array2)
	{
		final int length = array1.length + array2.length;
		final S[] res = Arrays.copyOf(array1, length);
		for (int i = array1.length; i < length; i++)
			res[i] = array2[i - array1.length];
		return res;
	}

	public static <S> S[] insertArray(final S[] array1, final int pos, final S... array2)
	{
		final int length = array1.length + array2.length;
		final S[] res = Arrays.copyOf(array1, length);
		for (int i = pos; i < array2.length + pos; i++)
			res[i] = array2[i - pos];
		for (int i = array2.length + pos; i < length; i++)
			res[i] = array1[i - array2.length];
		return res;
	}
}
