/*
 * Copyright (c) 2009 Tom Parker <thpr@users.sourceforge.net>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA
 */
package pcgen.gui.converter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pcgen.cdom.enumeration.ListKey;
import pcgen.core.Ability;
import pcgen.core.ArmorProf;
import pcgen.core.Campaign;
import pcgen.core.Deity;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.EquipmentModifier;
import pcgen.core.GameMode;
import pcgen.core.Globals;
import pcgen.core.Language;
import pcgen.core.PCAlignment;
import pcgen.core.PCStat;
import pcgen.core.PCTemplate;
import pcgen.core.Race;
import pcgen.core.SettingsHandler;
import pcgen.core.ShieldProf;
import pcgen.core.SizeAdjustment;
import pcgen.core.Skill;
import pcgen.core.WeaponProf;
import pcgen.core.character.CompanionMod;
import pcgen.core.spell.Spell;
import pcgen.gui.converter.loader.BasicLoader;
import pcgen.gui.converter.loader.ClassLoader;
import pcgen.gui.converter.loader.CopyLoader;
import pcgen.gui.converter.loader.SelfCopyLoader;
import pcgen.io.PCGFile;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.AbilityCategoryLoader;
import pcgen.persistence.lst.CampaignSourceEntry;
import pcgen.persistence.lst.LstFileLoader;
import pcgen.persistence.lst.StatsAndChecksLoader;
import pcgen.rules.context.LoadContext;
import pcgen.util.Logging;
import pcgen.util.PropertyFactory;

public class BatchConverter
{

	private final AbilityCategoryLoader catLoader = new AbilityCategoryLoader();
	private final StatsAndChecksLoader statCheckLoader = new StatsAndChecksLoader();
	private final String outDir;
	private final File rootDir;
	private final ArrayList<Campaign> campaigns;
	private final LoadContext context;
	private List<Loader> loaders;
	private Set<URI> written = new HashSet<URI>();

	public BatchConverter(LoadContext lc, String outputDirectory,
			String rootDirectory, List<Campaign> list)
	{
		outDir = outputDirectory;
		rootDir = new File(rootDirectory);
		campaigns = new ArrayList<Campaign>(list);
		context = lc;
		loaders = setupLoaders(context);
	}

	public void process() throws PersistenceLayerException
	{
		// The first thing we need to do is load the
		// correct statsandchecks.lst file for this gameMode
		GameMode gamemode = SettingsHandler.getGame();
		if (gamemode == null)
		{
			// Autoload campaigns is set but there
			// is no current gameMode, so just return
			return;
		}
		File gameModeDir = new File(SettingsHandler.getPcgenSystemDir(),
				"gameModes");
		File specificGameModeDir = new File(gameModeDir, gamemode
				.getFolderName());
		File statsAndChecks = new File(specificGameModeDir,
				"statsandchecks.lst");
		statCheckLoader.loadLstFile(context, statsAndChecks.toURI());

		for (PCAlignment al : gamemode.getUnmodifiableAlignmentList())
		{
			context.ref.registerAbbreviation(al, al.getKeyName());
		}
		for (PCStat st : gamemode.getUnmodifiableStatList())
		{
			context.ref.registerAbbreviation(st, st.getAbb());
		}
		for (SizeAdjustment sz : gamemode.getUnmodifiableSizeAdjustmentList())
		{
			context.ref.registerAbbreviation(sz, sz.getAbbreviation());
		}

		ArrayList<Campaign> totalCampaigns = new ArrayList<Campaign>(campaigns);
		for (Campaign campaign : campaigns)
		{
			// Add all sub-files to the main campaign, regardless of exclusions
			for (CampaignSourceEntry fName : campaign
					.getSafeListFor(ListKey.FILE_PCC))
			{
				URI uri = fName.getURI();
				if (PCGFile.isPCGenCampaignFile(uri))
				{
					Campaign c = Globals.getCampaignByURI(uri, false);
					totalCampaigns.add(c);
				}
			}
		}
		startCampaign(totalCampaigns);
	}

	private void startCampaign(List<Campaign> list)
	{
		for (Campaign campaign : list)
		{
			// load ability categories first as they used to only be at the game
			// mode
			try
			{
				catLoader.loadLstFiles(context, campaign
						.getSafeListFor(ListKey.FILE_ABILITY_CATEGORY));
			}
			catch (PersistenceLayerException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			int procs = Math.max(1, Runtime.getRuntime().availableProcessors());
			for (int i = 0; i <= procs; i++)
			{
				startItem(campaign);
			}
		}
	}

	private void startItem(final Campaign campaign)
	{
		for (final Loader loader : loaders)
		{
			List<CampaignSourceEntry> files = loader.getFiles(campaign);
			for (final CampaignSourceEntry cse : files)
			{
				final URI uri = cse.getURI();
				if (written.contains(uri))
				{
					continue;
				}
				written.add(uri);
				File in = new File(uri.getPath().substring(1));
				File base = findSubRoot(rootDir, in);
				String relative = in.toString().substring(
						base.toString().length() + 1);
				File outFile = new File(rootDir, File.separator + outDir
						+ File.separator + relative);
				if (outFile.exists())
				{
					System.err.println("Won't overwrite: " + outFile);
				}
				ensureParents(outFile.getParentFile());
				try
				{
					String result = load(uri, loader);
					if (result != null)
					{
						FileWriter fis = new FileWriter(outFile);
						fis.write(result);
						fis.close();
					}
				}
				catch (PersistenceLayerException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				catch (IOException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private String load(URI uri, Loader loader) throws InterruptedException,
			PersistenceLayerException
	{
		StringBuilder dataBuffer;
		context.setSourceURI(uri);
		context.setExtractURI(uri);
		try
		{
			dataBuffer = LstFileLoader.readFromURI(uri);
		}
		catch (PersistenceLayerException ple)
		{
			String message = PropertyFactory.getFormattedString(
					"Errors.LstFileLoader.LoadError", //$NON-NLS-1$
					uri, ple.getMessage());
			Logging.errorPrint(message);
			return null;
		}

		StringBuilder resultBuffer = new StringBuilder(dataBuffer.length());
		final String aString = dataBuffer.toString();

		String[] fileLines = aString.replaceAll("\r\n", "\r").split(
				LstFileLoader.LINE_SEPARATOR_REGEXP);
		for (int line = 0; line < fileLines.length; line++)
		{
			String lineString = fileLines[line];
			if ((lineString.length() == 0)
					|| (lineString.charAt(0) == LstFileLoader.LINE_COMMENT_CHAR)
					|| lineString.startsWith("SOURCE"))
			{
				resultBuffer.append(lineString);
			}
			else
			{
				loader.process(resultBuffer, line, lineString);
			}
			resultBuffer.append("\n");
		}
		return resultBuffer.toString();
	}

	private List<Loader> setupLoaders(LoadContext context)
	{
		List<Loader> loaders = new ArrayList<Loader>();
		loaders.add(new BasicLoader<WeaponProf>(context, WeaponProf.class,
				ListKey.FILE_WEAPON_PROF));
		loaders.add(new BasicLoader<ArmorProf>(context, ArmorProf.class,
				ListKey.FILE_ARMOR_PROF));
		loaders.add(new BasicLoader<ShieldProf>(context, ShieldProf.class,
				ListKey.FILE_SHIELD_PROF));
		loaders.add(new BasicLoader<Skill>(context, Skill.class,
				ListKey.FILE_SKILL));
		loaders.add(new BasicLoader<Language>(context, Language.class,
				ListKey.FILE_LANGUAGE));
		loaders.add(new BasicLoader<Ability>(context, Ability.class,
				ListKey.FILE_FEAT));
		loaders.add(new BasicLoader<Ability>(context, Ability.class,
				ListKey.FILE_ABILITY));
		loaders.add(new BasicLoader<Race>(context, Race.class,
				ListKey.FILE_RACE));
		loaders.add(new BasicLoader<Domain>(context, Domain.class,
				ListKey.FILE_DOMAIN));
		loaders.add(new BasicLoader<Spell>(context, Spell.class,
				ListKey.FILE_SPELL));
		loaders.add(new BasicLoader<Deity>(context, Deity.class,
				ListKey.FILE_DEITY));
		loaders.add(new BasicLoader<PCTemplate>(context, PCTemplate.class,
				ListKey.FILE_TEMPLATE));
		loaders.add(new BasicLoader<Equipment>(context, Equipment.class,
				ListKey.FILE_EQUIP));
		loaders.add(new BasicLoader<EquipmentModifier>(context,
				EquipmentModifier.class, ListKey.FILE_EQUIP_MOD));
		loaders.add(new BasicLoader<CompanionMod>(context, CompanionMod.class,
				ListKey.FILE_COMPANION_MOD));
		loaders.add(new ClassLoader(context));
		loaders.add(new CopyLoader(ListKey.FILE_ABILITY_CATEGORY));
		loaders.add(new CopyLoader(ListKey.LICENSE_FILE));
		loaders.add(new CopyLoader(ListKey.FILE_KIT));
		loaders.add(new CopyLoader(ListKey.FILE_BIO_SET));
		loaders.add(new CopyLoader(ListKey.FILE_PCC));
		loaders.add(new SelfCopyLoader());
		return loaders;
	}

	private File findSubRoot(File root, File in)
	{
		if (in.getParentFile() == null)
		{
			return null;
		}
		if (in.getParentFile().getAbsolutePath().equals(root.getAbsolutePath()))
		{
			return in;
		}
		return findSubRoot(root, in.getParentFile());
	}

	private void ensureParents(File parentFile)
	{
		if (!parentFile.exists())
		{
			ensureParents(parentFile.getParentFile());
			parentFile.mkdir();
		}
	}

}
