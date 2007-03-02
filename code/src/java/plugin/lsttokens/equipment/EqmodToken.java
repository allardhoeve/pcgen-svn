/*
 * Copyright 2006-2007 (C) Tom Parker <thpr@users.sourceforge.net>
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
 * Current Ver: $Revision$
 * Last Editor: $Author$
 * Last Edited: $Date$
 */
package plugin.lsttokens.equipment;

import java.util.Set;
import java.util.StringTokenizer;

import pcgen.cdom.base.CDOMSimpleSingleRef;
import pcgen.cdom.base.Constants;
import pcgen.cdom.enumeration.AssociationKey;
import pcgen.cdom.graph.PCGraphGrantsEdge;
import pcgen.cdom.graph.PCGraphEdge;
import pcgen.cdom.inst.EquipmentHead;
import pcgen.core.Equipment;
import pcgen.core.EquipmentModifier;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.EquipmentLstToken;

/**
 * Deals with EQMOD token
 */
public class EqmodToken implements EquipmentLstToken
{
	private static final Class<EquipmentModifier> EQUIPMENT_MODIFIER_CLASS =
			EquipmentModifier.class;

	public String getTokenName()
	{
		return "EQMOD";
	}

	public boolean parse(Equipment eq, String value)
	{
		eq.addEqModifiers(value, true);
		return true;
	}

	public boolean parse(LoadContext context, Equipment eq, String value)
	{
		return parseEqMod(context, getEquipmentHead(context, eq, 1), value);
	}

	protected boolean parseEqMod(LoadContext context, EquipmentHead primHead,
		String value)
	{
		StringTokenizer dotTok = new StringTokenizer(value, Constants.DOT);

		while (dotTok.hasMoreTokens())
		{
			String aEqModName = dotTok.nextToken();

			if (aEqModName.equalsIgnoreCase(Constants.LST_NONE))
			{
				continue;
			}
			StringTokenizer pipeTok = new StringTokenizer(aEqModName, "|");

			// The type of EqMod, eg: ABILITYPLUS
			final String eqModKey = pipeTok.nextToken();

			/*
			 * TODO Need to handle these special cases???
			 */
			// if (eqModKey.equals(EQMOD_WEIGHT)) {
			// if (pipeTok.hasMoreTokens()) {
			// setWeightMod(pipeTok.nextToken().replace(',', '.'));
			// }
			// return;
			// }
			//
			// if (eqModKey.equals(EQMOD_DAMAGE)) {
			// if (pipeTok.hasMoreTokens()) {
			// setDamageMod(pipeTok.nextToken());
			// }
			// return;
			// }
			CDOMSimpleSingleRef<EquipmentModifier> eqMod =
					context.ref.getCDOMReference(EQUIPMENT_MODIFIER_CLASS,
						eqModKey);

			PCGraphGrantsEdge edge =
					context.graph.linkObjectIntoGraph(getTokenName(), primHead,
						eqMod);

			while (pipeTok.hasMoreTokens())
			{
				String assocTok = pipeTok.nextToken();
				if (assocTok.indexOf(']') == -1)
				{
					edge.setAssociation(AssociationKey.ONLY, assocTok);
				}
				else
				{
					if (!setAssoc(edge, assocTok))
					{
						return false;
					}
				}
			}
		}
		return true;
	}

	private boolean setAssoc(PCGraphGrantsEdge edge, String assocTok)
	{
		StringTokenizer bracketTok = new StringTokenizer(assocTok, "]");
		while (bracketTok.hasMoreTokens())
		{
			String assoc = bracketTok.nextToken();
			if (assoc.length() == 0 && !bracketTok.hasMoreTokens())
			{
				// Last one should be empty
				break;
			}
			int openBracketLoc = assoc.indexOf('[');
			if (openBracketLoc == -1)
			{
				return false;
			}
			if (openBracketLoc != assoc.lastIndexOf('['))
			{
				return false;
			}
			String assocKey = assoc.substring(0, openBracketLoc);
			String assocVal = assoc.substring(openBracketLoc + 1);
			edge.setAssociation(AssociationKey
				.getKeyFor(String.class, assocKey), assocVal);
		}
		return true;
	}

	protected EquipmentHead getEquipmentHead(LoadContext context, Equipment eq,
		int index)
	{
		EquipmentHead head = getEquipmentHeadReference(context, eq, index);
		if (head == null)
		{
			// Isn't there already, so create new
			head = new EquipmentHead(this, index);
			context.graph.linkObjectIntoGraph(Constants.VT_EQ_HEAD, eq, head);
		}
		return head;
	}

	private EquipmentHead getEquipmentHeadReference(LoadContext context,
		Equipment eq, int index)
	{
		Set<PCGraphEdge> edges =
				context.graph.getChildLinksFromToken(Constants.VT_EQ_HEAD, eq,
					EquipmentHead.class);
		for (PCGraphEdge edge : edges)
		{
			EquipmentHead head =
					(EquipmentHead) edge.getSinkNodes().iterator().next();
			if (head.getHeadIndex() == index)
			{
				return head;
			}
		}
		return null;
	}

	public String unparse(LoadContext context, Equipment eq)
	{
		EquipmentHead head = getEquipmentHeadReference(context, eq, 1);
		if (head == null)
		{
			return null;
		}
		Set<PCGraphEdge> edgeList =
				context.graph.getChildLinksFromToken(getTokenName(), head,
					EquipmentModifier.class);
		if (edgeList == null || edgeList.isEmpty())
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		boolean needDot = false;
		for (PCGraphEdge edge : edgeList)
		{
			EquipmentModifier eqMod =
					(EquipmentModifier) edge.getSinkNodes().get(0);
			if (needDot)
			{
				sb.append('.');
			}
			sb.append(eqMod.getKeyName());
			if (edge.hasAssociations())
			{
				sb.append(Constants.PIPE);
				for (AssociationKey ak : edge.getAssociationKeys())
				{
					String st = (String) edge.getAssociation(ak);
					sb.append(ak).append('[').append(st).append(']');
				}
			}
			needDot = true;
		}
		return sb.toString();
	}
}
