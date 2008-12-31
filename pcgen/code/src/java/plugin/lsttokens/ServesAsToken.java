/*
 * Copyright 2008 (C) Thomas Parker <thpr@users.sourceforge.net>
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
 */
package plugin.lsttokens;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import pcgen.base.lang.StringUtil;
import pcgen.base.util.TreeMapToList;
import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.CategorizedCDOMObject;
import pcgen.cdom.base.Constants;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.reference.CDOMSingleRef;
import pcgen.cdom.reference.CategorizedCDOMReference;
import pcgen.cdom.reference.ReferenceManufacturer;
import pcgen.core.Ability;
import pcgen.core.AbilityCategory;
import pcgen.core.PCClass;
import pcgen.core.PObject;
import pcgen.core.Race;
import pcgen.core.SettingsHandler;
import pcgen.core.Skill;
import pcgen.persistence.PersistenceLayerException;
import pcgen.rules.context.Changes;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractToken;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.util.Logging;
import pcgen.util.StringPClassUtil;

/**
 * Deals with the SERVESAS token for Abilities
 */
public class ServesAsToken extends AbstractToken implements
		CDOMPrimaryToken<CDOMObject>
{

	@Override
	public String getTokenName()
	{
		return "SERVESAS";
	}

	public List<Class<? extends PObject>> getLegalTypes()
	{
		return Arrays.asList(

		PCClass.class, Ability.class, Skill.class, Race.class
		// Ability.class, Deity.class, Domain.class,Equipment.class,
				// Race.class, Skill.class,Spell.class, PCTemplate.class,
				// WeaponProf.class
				);
	}

	public boolean parse(LoadContext context, CDOMObject obj, String value)
			throws PersistenceLayerException
	{
		if (!getLegalTypes().contains(obj.getClass()))
		{
			Logging.log(Logging.LST_ERROR, "Cannot use SERVESAS on a " + obj.getClass());
			Logging.log(Logging.LST_ERROR, "   bad use found in "
					+ obj.getClass().getSimpleName() + " " + obj.getKeyName());
			return false;
		}
		if (isEmpty(value) || hasIllegalSeparator('|', value))
		{
			return false;
		}
		StringTokenizer st = new StringTokenizer(value, Constants.PIPE);
		String key = st.nextToken();
		int equalLoc = key.indexOf('=');
		Class<? extends PObject> servingClass;
		ReferenceManufacturer<? extends PObject, ? extends CDOMSingleRef<?>> mfg;
		if (equalLoc == -1)
		{
			if ("ABILITY".equals(key))
			{
				Logging.log(Logging.LST_ERROR, "Invalid use of ABILITY in SERVESAS "
						+ "(requires ABILITY=<category>): " + key);
				return false;
			}
			servingClass = StringPClassUtil.getClassFor(key);
			if (servingClass == null)
			{
				Logging.log(Logging.LST_ERROR, getTokenName()
						+ " expecting a POBJECT Type, found: " + key);
				return false;
			}
			if (!servingClass.equals(obj.getClass()))
			{
				Logging.log(Logging.LST_ERROR, getTokenName()
						+ " expecting a POBJECT Type valid for "
						+ obj.getClass().getSimpleName() + ", found: " + key);
				return false;
			}
			mfg = context.ref.getManufacturer(servingClass);
		}
		else
		{
			if (!"ABILITY".equals(key.substring(0, equalLoc)))
			{
				Logging.log(Logging.LST_ERROR, "Invalid use of = in SERVESAS "
						+ "(only valid for ABILITY): " + key);
				return false;
			}
			String category = key.substring(equalLoc + 1);
			key = key.substring(0, equalLoc);
			AbilityCategory cat = SettingsHandler.getGame().getAbilityCategory(
					category);
			if (cat == null)
			{
				Logging.log(Logging.LST_ERROR,
						"Could not find AbilityCategory " + category + " in "
								+ getTokenName());
				return false;
			}
			mfg = context.ref.getManufacturer(Ability.class, cat);
		}
		if (!st.hasMoreTokens())
		{
			Logging.log(Logging.LST_ERROR, getTokenName()
					+ " must include at least one target object");
			return false;
		}

		ListKey<CDOMReference> listkey = ListKey.getKeyFor(CDOMReference.class,
				"SERVES_AS_" + key);
		while (st.hasMoreTokens())
		{
			CDOMSingleRef<?> ref = mfg.getReference(st.nextToken());
			context.obj.addToList(obj, listkey, ref);
		}

		return true;
	}

	public String[] unparse(LoadContext context, CDOMObject obj)
	{
		String key = StringPClassUtil.getStringFor(obj.getClass());
		ListKey<CDOMReference> listkey = ListKey.getKeyFor(CDOMReference.class,
				"SERVES_AS_" + key);
		Changes<CDOMReference> changes = context.obj.getListChanges(obj,
				listkey);
		Collection<CDOMReference> removedItems = changes.getRemoved();
		if (removedItems != null && !removedItems.isEmpty()
				|| changes.includesGlobalClear())
		{
			context.addWriteMessage(getTokenName()
							+ " does not support .CLEAR");
			return null;
		}
		Collection<CDOMReference> added = changes.getAdded();
		if (added == null || added.isEmpty())
		{
			// Zero indicates no Token (and no global clear, so nothing to do)
			return null;
		}
		TreeMapToList<String, String> map = new TreeMapToList<String, String>();
		for (CDOMReference ref : added)
		{
			String mapKey = key;
			if (CategorizedCDOMObject.class.isAssignableFrom(obj.getClass()))
			{
				CategorizedCDOMReference<Ability> catref = (CategorizedCDOMReference<Ability>) ref;
				mapKey = key + "=" + catref.getCDOMCategory().toString();
			}
			map.addToListFor(mapKey, ref.getLSTformat());
		}
		List<String> returnList = new ArrayList<String>();
		for (String mapKey : map.getKeySet())
		{
			Set<String> set = new TreeSet<String>(map.getListFor(mapKey));
			returnList.add(mapKey + '|'
					+ StringUtil.joinToStringBuffer(set, "|"));
		}
		return returnList.toArray(new String[returnList.size()]);
	}

	public Class<CDOMObject> getTokenClass()
	{
		return CDOMObject.class;
	}
}
