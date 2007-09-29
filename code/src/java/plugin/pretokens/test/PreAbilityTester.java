/*
 * PreAbilityTester.java
 * Copyright 2007 (C) James Dempsey <jdempsey@users.sourceforge.net>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.       See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Created on January 23, 2006
 *
 * Current Ver: $Revision: 1777 $
 * Last Editor: $Author: jdempsey $
 * Last Edited: $Date: 2006-12-17 15:36:01 +1100 (Sun, 17 Dec 2006) $
 *
 */
package plugin.pretokens.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.enumeration.ObjectKey;
import pcgen.cdom.enumeration.Type;
import pcgen.cdom.graph.PCGenGraph;
import pcgen.core.Ability;
import pcgen.core.AbilityCategory;
import pcgen.core.Domain;
import pcgen.core.Equipment;
import pcgen.core.EquipmentList;
import pcgen.core.GameMode;
import pcgen.core.Globals;
import pcgen.core.PObject;
import pcgen.core.PlayerCharacter;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.core.WeaponProf;
import pcgen.core.prereq.AbstractPrerequisiteTest;
import pcgen.core.prereq.Prerequisite;
import pcgen.core.prereq.PrerequisiteException;
import pcgen.core.prereq.PrerequisiteTest;
import pcgen.core.spell.Spell;
import pcgen.util.PropertyFactory;

/**
 * <code>PreAbilityParser</code> tests whether a character passes ability
 * prereqs.
 * 
 * Last Editor: $Author: jdempsey $ Last Edited: $Date: 2006-12-17 15:36:01
 * +1100 (Sun, 17 Dec 2006) $
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: 1777 $
 */
public class PreAbilityTester extends AbstractPrerequisiteTest implements
		PrerequisiteTest
{

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcgen.core.prereq.PrerequisiteTest#passes(pcgen.core.PlayerCharacter)
	 */
	@Override
	public int passes(final Prerequisite prereq, final Equipment equipment,
		final PlayerCharacter aPC) throws PrerequisiteException
	{
		if (aPC == null)
		{
			return 0;
		}
		return passes(prereq, aPC);
	}

	@Override
	public int passes(final Prerequisite prereq, final PlayerCharacter character)
		throws PrerequisiteException
	{
		final boolean countMults = prereq.isCountMultiples();

		final int number;
		try
		{
			number = Integer.parseInt(prereq.getOperand());
		}
		catch (NumberFormatException exceptn)
		{
			throw new PrerequisiteException(PropertyFactory.getFormattedString(
				"PreAbility.error", prereq.toString())); //$NON-NLS-1$
		}

		GameMode gameMode = SettingsHandler.getGame();
		String key = prereq.getKey();
		String subKey = prereq.getSubKey();
		String categoryName = prereq.getCategoryName();
		AbilityCategory category = gameMode.getAbilityCategory(categoryName);
		final boolean keyIsAny = key.equalsIgnoreCase("ANY"); //$NON-NLS-1$
		final boolean keyIsType =
				key.startsWith("TYPE=") || key.startsWith("TYPE."); //$NON-NLS-1$ //$NON-NLS-2$
		final boolean subKeyIsType =
				subKey != null
					&& (subKey.startsWith("TYPE=") || subKey.startsWith("TYPE.")); //$NON-NLS-1$ //$NON-NLS-2$
		if (keyIsType)
		{
			key = key.substring(5);
		}
		if (subKeyIsType)
		{
			subKey = subKey.substring(5);
		}

		int runningTotal = 0;
		final List<Ability> abilityList =
				buildAbilityList(character, categoryName, category);
		if (!abilityList.isEmpty())
		{
			for (Ability ability : abilityList)
			{
				final String abilityKey = ability.getKeyName();
				if (keyIsAny
					|| (!keyIsType && abilityKey.equalsIgnoreCase(key))
					|| (keyIsType && ability.isType(key)))
				{
					// either this feat has matched on the name, or the type

					if (subKey != null)
					{
						runningTotal +=
								checkForSubKeyMatch(character, countMults, key,
									subKey, subKeyIsType, ability);
					}
					else
					{
						// Subkey == null

						runningTotal++;
						if (ability.isMultiples() && countMults)
						{
							runningTotal += (ability.getAssociatedCount() - 1);
						}
					}
				}
				else
				{
					if (subKey != null)
					{
						final String s1 = key + " (" + subKey + ")";
						final String s2 = key + "(" + subKey + ")";
						if (abilityKey.equalsIgnoreCase(s1)
							|| ability.getKeyName().equalsIgnoreCase(s2))
						{
							runningTotal++;
							if (!countMults)
							{
								break;
							}
						}
					}
				}
			}
		}

		runningTotal = prereq.getOperator().compare(runningTotal, number);
		return countedTotal(prereq, runningTotal);
	}

	/**
	 * Having matched the ability on the other criteria, check for a match
	 * against the sub-key.
	 * 
	 * @param character
	 *            The character being tested.
	 * @param countMults
	 *            Should multiple occurrences be counted?
	 * @param key
	 *            The key that needs to be matched
	 * @param subKey
	 *            The sub key that needs to be matched.
	 * @param subKeyIsType
	 *            Does the subkey refer to a type?
	 * @param aFeat
	 *            The ability being checked for a match.
	 * @return The number of matches made
	 */
	private int checkForSubKeyMatch(final PlayerCharacter character,
		final boolean countMults, String key, String subKey,
		final boolean subKeyIsType, Ability aFeat)
	{
		final String cType = subKey;
		final List availableList = new ArrayList();
		final List selectedList = new ArrayList();
		final String aChoiceString = aFeat.getChoiceString();
		int runningTotal = 0;

		aFeat.modChoices(availableList, selectedList, false, character, true);
		availableList.clear();

		if (subKeyIsType) // TYPE syntax
		{
			if (aChoiceString.startsWith("SKILL")) //$NON-NLS-1$
			{
				runningTotal =
						subKeySkill(countMults, runningTotal, cType,
							selectedList);
			}
			else if (aChoiceString.startsWith("WEAPONPROFS")) //$NON-NLS-1$
			{
				runningTotal =
						subKeyWeaponProf(countMults, runningTotal, cType,
							selectedList);
			}
			else if (aChoiceString.startsWith("DOMAIN")) //$NON-NLS-1$
			{
				runningTotal =
						subKeyDomain(countMults, runningTotal, cType,
							selectedList);
			}
			else if (aChoiceString.startsWith("SPELL")) //$NON-NLS-1$
			{
				runningTotal =
						subKeySpell(countMults, runningTotal, cType,
							selectedList);
			}
			// End. subKeyIsType
		}
		else
		{
			if (aFeat.getKeyName().equalsIgnoreCase(key)
				&& aFeat.containsAssociated(subKey))
			{
				runningTotal++;
				if (aFeat.isMultiples() && countMults)
				{
					runningTotal += (aFeat.getAssociatedCount() - 1);
				}
			}
			else
			{
				final int wildCardPos = subKey.indexOf('%');

				if (wildCardPos > -1)
				{
					for (int k = 0; k < aFeat.getAssociatedCount(); ++k)
					{

						final String fString =
								aFeat.getAssociated(k).toUpperCase();
						if (wildCardPos == 0
							|| fString.startsWith(subKey.substring(0,
								wildCardPos - 1).toUpperCase()))
						{
							runningTotal++;
							if (!countMults)
							{
								break;
							}
						}
					}
				}
			}
		}
		return runningTotal;
	}

	/**
	 * Build up a list of the character's abilities which match the category
	 * requirements.
	 * 
	 * @param character
	 *            The character to be tested.
	 * @param categoryName
	 *            The name of the required category, null if any category will
	 *            be matched.
	 * @param category
	 *            The category to be matched
	 * @return A list of categories matching.
	 */
	private List<Ability> buildAbilityList(final PlayerCharacter character,
		String categoryName, AbilityCategory category)
	{
		final List<Ability> abilityList = new ArrayList<Ability>();
		if (character != null)
		{
			Collection<AbilityCategory> allCats = SettingsHandler.getGame().getAllAbilityCategories();
			if (categoryName == null)
			{
				for (AbilityCategory aCat : allCats)
				{
					abilityList.addAll(character.getAggregateAbilityList(aCat));
				}
			}
			else
			{
				for (AbilityCategory aCat : allCats)
				{
					if (aCat.getAbilityCategory().equals(category.getKeyName()))
					{
						abilityList.addAll(character.getAggregateAbilityList(aCat));
					}
				}
			}
		}
		return abilityList;
	}

	/**
	 * @param countMults
	 * @param runningTotal
	 * @param cType
	 * @param selectedList
	 * @return int
	 */
	private int subKeySpell(final boolean countMults, int runningTotal,
		final String cType, final List selectedList)
	{
		int returnTotal = runningTotal;
		for (Object aObj : selectedList)
		{
			final Spell sp;
			String spellKey = null;
			if (aObj instanceof PObject)
			{
				spellKey = ((PObject) aObj).getKeyName();
			}
			else
			{
				spellKey = aObj.toString();
			}
			sp = Globals.getSpellKeyed(spellKey);
			if (sp == null)
			{
				continue;
			}
			if (sp.isType(cType))
			{
				returnTotal++;
				if (!countMults)
				{
					break;
				}
			}
		}
		return returnTotal;
	}

	/**
	 * @param countMults
	 * @param runningTotal
	 * @param cType
	 * @param selectedList
	 * @return int
	 */
	private int subKeyDomain(final boolean countMults, int runningTotal,
		final String cType, final List selectedList)
	{
		int returnTotal = runningTotal;
		for (Object aObj : selectedList)
		{
			final Domain dom;
			dom = Globals.getDomainKeyed(aObj.toString());
			if (dom == null)
			{
				continue;
			}
			if (dom.isType(cType))
			{
				returnTotal++;
				if (!countMults)
				{
					break;
				}
			}
		}
		return returnTotal;
	}

	/**
	 * @param countMults
	 * @param runningTotal
	 * @param cType
	 * @param selectedList
	 * @return int
	 */
	private int subKeyWeaponProf(final boolean countMults, int runningTotal,
		final String cType, final List selectedList)
	{
		int returnTotal = runningTotal;
		for (Object aObj : selectedList)
		{
			final WeaponProf wp;
			wp = Globals.getWeaponProfKeyed(aObj.toString());
			if (wp == null)
			{
				continue;
			}
			final Equipment eq;
			eq = EquipmentList.getEquipmentKeyed(wp.getKeyName());
			if (eq == null)
			{
				continue;
			}
			if (eq.isType(cType))
			{
				returnTotal++;
				if (!countMults)
				{
					break;
				}
			}
		}
		return returnTotal;
	}

	/**
	 * @param countMults
	 * @param runningTotal
	 * @param cType
	 * @param selectedList
	 * @return int
	 */
	private int subKeySkill(final boolean countMults, int runningTotal,
		final String cType, final List selectedList)
	{
		int returnTotal = runningTotal;
		for (Object aObj : selectedList)
		{
			final Skill sk;
			sk = Globals.getSkillKeyed(aObj.toString());
			if (sk == null)
			{
				continue;
			}
			if (sk.isType(cType))
			{
				returnTotal++;
				if (!countMults)
				{
					break;
				}
			}
		}
		return returnTotal;
	}

	@Override
	public String toHtmlString(final Prerequisite prereq)
	{
		String aString = prereq.getKey();
		if ((prereq.getSubKey() != null) && !prereq.getSubKey().equals(""))
		{
			aString = aString + " ( " + prereq.getSubKey() + " )";
		}

		if (aString.startsWith("TYPE=")) //$NON-NLS-1$
		{
			if (prereq.getCategoryName().length() > 0)
			{
				// {0} {1} {2}(s) of type {3}
				return PropertyFactory.getFormattedString(
					"PreAbility.type.toHtml", //$NON-NLS-1$
					new Object[]{prereq.getOperator().toDisplayString(),
						prereq.getOperand(), prereq.getCategoryName(),
						aString.substring(5)});
			}
			else
			{
				// {0} {1} ability(s) of type {2}
				return PropertyFactory.getFormattedString(
					"PreAbility.type.noCat.toHtml", //$NON-NLS-1$ 
					new Object[]{prereq.getOperator().toDisplayString(),
						prereq.getOperand(), aString.substring(5)});
			}

		}
		// {2} {3} {1} {0}
		return PropertyFactory.getFormattedString("PreAbility.toHtml", //$NON-NLS-1$
			new Object[]{prereq.getCategoryName(), aString,
				prereq.getOperator().toDisplayString(), prereq.getOperand()});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see pcgen.core.prereq.PrerequisiteTest#kindsHandled()
	 */
	public String kindHandled()
	{
		return "ABILITY"; //$NON-NLS-1$
	}

	public int passesCDOM(Prerequisite prereq, PlayerCharacter character)
		throws PrerequisiteException
	{
		boolean countMults = prereq.isCountMultiples();

		int number;
		try
		{
			number = Integer.parseInt(prereq.getOperand());
		}
		catch (NumberFormatException exceptn)
		{
			throw new PrerequisiteException(PropertyFactory.getFormattedString(
				"PreFeat.error", prereq.toString())); //$NON-NLS-1$
		}

		String key = prereq.getKey();
		String subKey = prereq.getSubKey();
		String categoryName = prereq.getCategoryName();
		pcgen.cdom.enumeration.AbilityCategory category =
				pcgen.cdom.enumeration.AbilityCategory.valueOf(categoryName);
		//TODO What if CATEGORY=null??
		final boolean keyIsAny = key.equalsIgnoreCase("ANY"); //$NON-NLS-1$
		boolean keyIsType = key.startsWith("TYPE=") || key.startsWith("TYPE."); //$NON-NLS-1$ //$NON-NLS-2$
		boolean subKeyIsType =
				subKey != null
					&& (subKey.startsWith("TYPE=") || subKey.startsWith("TYPE.")); //$NON-NLS-1$ //$NON-NLS-2$
		if (keyIsType)
		{
			key = key.substring(5);
		}
		if (subKeyIsType)
		{
			subKey = subKey.substring(5);
		}

		int runningTotal = 0;
		PCGenGraph activeGraph = character.getActiveGraph();
		List<Ability> list = activeGraph.getGrantedNodeList(Ability.class);
		ABILITY: for (Ability a : list)
		{
			if (!category.equals(a.getCDOMCategory()))
			{
				continue;
			}
			String featKey = a.getKeyName();
			if (!keyIsAny && keyIsType)
			{
				StringTokenizer tok = new StringTokenizer(key, ".");
				// Must match all listed types in order to qualify
				while (tok.hasMoreTokens())
				{
					Type requiredType = Type.getConstant(tok.nextToken());
					if (!a.containsInList(ListKey.TYPE, requiredType))
					{
						continue ABILITY;
					}
				}
			}
			else if (!keyIsAny && !featKey.equalsIgnoreCase(key))
			{
				if (!subKeyIsType && subKey != null)
				{
					String s1 = key + " (" + subKey + ")";
					String s2 = key + "(" + subKey + ")";
					if (featKey.equalsIgnoreCase(s1)
						|| featKey.equalsIgnoreCase(s2))
					{
						runningTotal++;
						if (!countMults)
						{
							break;
						}
					}
				}
				continue ABILITY;
			}
			//TODO Need an else !keyIsAny here for error checking?
			// either this feat has matched on the name, or the type
			if (subKey == null)
			{
				runningTotal += getAbilityWeight(character, countMults, a);
			}
			else if (subKeyIsType) // TYPE syntax
			{
				runningTotal +=
						getAssociatedCountOfType(character, countMults, a,
							subKey);
			}
			else if (featKey.equalsIgnoreCase(key)
				&& character.containsAssociatedKey(a, subKey))
			{
				Boolean mult = a.get(ObjectKey.MULTIPLE_ALLOWED);
				if (countMults && mult != null && mult.booleanValue())
				{
					// TODO I think this is broken - matches 5.12, tho'
					// - thpr Jun 2, 07
					runningTotal += character.getAssociatedCount(a);
				}
				else
				{
					runningTotal++;
				}
			}
			else
			{
				runningTotal +=
						getWildcardCount(character, countMults, a, subKey);
			}
		}

		runningTotal = prereq.getOperator().compare(runningTotal, number);
		return countedTotal(prereq, runningTotal);
	}

	private int getWildcardCount(PlayerCharacter character, boolean countMults,
		Ability a, String subKey)
	{
		int count = 0;

		int wildCardPos = subKey.indexOf('%');

		if (wildCardPos > -1)
		{
			if (wildCardPos == 0)
			{
				if (countMults)
				{
					count += character.getAssociatedCount(a);
				}
				else
				{
					count++;
				}
			}
			else
			{
				List<PObject> assoc = character.getAssociated(a);
				String subStart = subKey.substring(0, wildCardPos - 1);
				for (PObject po : assoc)
				{
					if (po.getKeyName().regionMatches(true, 0, subStart, 0,
						wildCardPos))
					{
						count++;
						if (!countMults)
						{
							break;
						}
					}
				}
			}
		}
		return count;
	}

	private int getAssociatedCountOfType(PlayerCharacter character,
		boolean countMults, Ability a, String subKey)
	{
		int runningTotal = 0;
		List<PObject> list = character.getAssociated(a);
		POBJECT: for (PObject po : list)
		{
			StringTokenizer tok = new StringTokenizer(subKey.substring(5), ".");
			// Must match all listed types in order to qualify
			while (tok.hasMoreTokens())
			{
				Type requiredType = Type.getConstant(tok.nextToken());
				if (!po.containsInList(ListKey.TYPE, requiredType))
				{
					continue POBJECT;
				}
			}
			runningTotal++;
			if (!countMults)
			{
				break;
			}
		}
		return runningTotal;
	}

	private int getAbilityWeight(PlayerCharacter character, boolean countMults,
		Ability a)
	{
		int increment;
		Boolean mult = a.get(ObjectKey.MULTIPLE_ALLOWED);
		if (countMults && mult != null && mult.booleanValue())
		{
			Boolean stack = a.get(ObjectKey.STACKS);
			if (stack != null && stack.booleanValue())
			{
				increment = character.getTotalWeight(a);
			}
			else
			{
				increment = character.getAssociatedCount(a);
			}
		}
		else
		{
			increment = 1;
		}
		return increment;
	}

}
