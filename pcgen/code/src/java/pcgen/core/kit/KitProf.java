/*
 * KitProf.java
 * Copyright 2001 (C) Greg Bingleman <byngl@hotmail.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on September 28, 2002, 11:50 PM
 *
 * $Id$
 */
package pcgen.core.kit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.core.Globals;
import pcgen.core.Kit;
import pcgen.core.PCClass;
import pcgen.core.PObject;
import pcgen.core.PlayerCharacter;
import pcgen.core.Race;
import pcgen.core.WeaponProf;
import pcgen.core.utils.CoreUtility;
import pcgen.core.utils.ListKey;

/**
 * <code>KitFeat</code>.
 *
 * @author Greg Bingleman <byngl@hotmail.com>
 * @version $Revision$
 */
public final class KitProf extends BaseKit implements Serializable, Cloneable
{
	// Only change the UID when the serialized form of the class has also changed
	private static final long serialVersionUID = 1;

	private final List profList = new ArrayList();
	private boolean racialProf = false;

	// These members store the state of an instance of this class.  They are
	// not cloned.
	private transient PObject thePObject = null;
	private transient List weaponProfs = null;

	/**
	 * Constructor
	 * @param argProfList
	 */
	public KitProf(final String argProfList)
	{
		final StringTokenizer aTok = new StringTokenizer(argProfList, "|");

		while (aTok.hasMoreTokens())
		{
			profList.add(aTok.nextToken());
		}
	}

	/**
	 * Get the proficiency list for this kit
	 * @return the proficiency list for this kit
	 */
	public List getProfList()
	{
		return profList;
	}

	/**
	 * True if it is a racial proficiency
	 * @return True if it is a racial proficiency
	 */
	public boolean isRacial()
	{
		return racialProf;
	}

	/**
	 * Set racial proficiency flag
	 * @param argRacial
	 */
	public void setRacialProf(final boolean argRacial)
	{
		racialProf = argRacial;
	}

	public String toString()
	{
		final int maxSize = profList.size();
		final StringBuffer info = new StringBuffer(maxSize * 10);

		if ((choiceCount != 1) || (maxSize != 1))
		{
			info.append(choiceCount).append(" of ");
		}

		info.append(CoreUtility.joinToStringBuffer(profList, ", "));

		return info.toString();
	}

	public boolean testApply(Kit aKit, PlayerCharacter aPC, List warnings)
	{
		thePObject = null;
		weaponProfs = null;

		ListKey weaponProfKey = ListKey.SELECTED_WEAPON_PROF_BONUS;

		List bonusList = null;
		if (isRacial())
		{
			final Race pcRace = aPC.getRace();

			if (pcRace == null)
			{
				warnings.add("PROF: PC has no race");

				return false;
			}
			if (pcRace.getSafeSizeOfListFor(weaponProfKey) != 0)
			{
				warnings.add(
					"PROF: Race has already selected bonus weapon proficiency");

				return false;
			}
			thePObject = pcRace;
			bonusList = pcRace.getWeaponProfBonus();
		}
		else
		{
			ArrayList pcClasses = aPC.getClassList();
			if (pcClasses == null || pcClasses.size() == 0)
			{
				warnings.add("PROF: No owning class found.");

				return false;
			}

			// Search for a class that has bonusWeaponProfs.
			PCClass pcClass = null;
			// TODO:  Never used!
			boolean found = false;
			for (Iterator i = pcClasses.iterator(); i.hasNext(); )
			{
				pcClass = (PCClass)i.next();
				bonusList = pcClass.getWeaponProfBonus();
				if (bonusList != null && bonusList.size() > 0)
				{
					found = true;
					break;
				}
			}
			thePObject = pcClass;
			if (pcClass.getSafeSizeOfListFor(weaponProfKey) != 0)
			{
				warnings.add(
					"PROF: Class has already selected bonus weapon proficiency");

				return false;
			}
		}
		if ((bonusList == null) || (bonusList.size() == 0))
		{
			warnings.add("PROF: No optional weapon proficiencies");

			return false;
		}

		final List aProfList = new ArrayList();

		for (Iterator e = getProfList().iterator(); e.hasNext();)
		{
			String profName = (String)e.next();
			if (!bonusList.contains(profName))
			{
				warnings.add(
					"PROF: Weapon proficiency \"" + profName +
					"\" is not in list of choices");
				continue;
			}

			final WeaponProf aProf = Globals.getWeaponProfNamed(profName);

			if (aProf != null)
			{
				aProfList.add(profName);
			}
			else
			{
				warnings.add(
					"PROF: Non-existant proficiency \"" + profName + "\"");
			}
		}

		int numberOfChoices = getChoiceCount();

		//
		// Can't choose more entries than there are...
		//
		if (numberOfChoices > aProfList.size())
		{
			numberOfChoices = aProfList.size();
		}

		if (numberOfChoices == 0)
		{
			return false;
		}

		List xs;

		if (numberOfChoices == aProfList.size())
		{
			xs = aProfList;
		}
		else
		{
			//
			// Force user to make enough selections
			//
			while (true)
			{
				xs = Globals.getChoiceFromList(
						"Choose Proficiencies",
						aProfList,
						new ArrayList(),
						numberOfChoices);

				if (xs.size() != 0)
				{
					break;
				}
			}
		}

		//
		// Add to list of things to add to the character
		//
		for (Iterator e = xs.iterator(); e.hasNext();)
		{
			final String     profName = (String) e.next();
			final WeaponProf aProf    = Globals.getWeaponProfNamed(profName);

			if (aProf != null)
			{
				if (weaponProfs == null)
				{
					weaponProfs = new ArrayList();
				}
				weaponProfs.add(aProf);
			}
		}
		return false;
	}

	public void apply(PlayerCharacter aPC)
	{
		for (Iterator i = weaponProfs.iterator(); i.hasNext(); )
		{
			WeaponProf prof = (WeaponProf)i.next();
			thePObject.addSelectedWeaponProfBonus(prof.getName());
		}
	}

	public Object clone()
	{
		KitProf aClone = (KitProf)super.clone();
		aClone.profList.addAll(profList);
		aClone.racialProf = racialProf;

		return aClone;
	}

	public String getObjectName()
	{
		return "Proficiencies";
	}
}
