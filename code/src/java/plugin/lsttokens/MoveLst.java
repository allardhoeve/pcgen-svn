/*
 * Copyright 2006-2007 (C) Tom Parker <thpr@users.sourceforge.net>
 * Copyright 2005-2006 (C) Devon Jones
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
package plugin.lsttokens;

import java.util.Set;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.graph.PCGraphEdge;
import pcgen.core.Equipment;
import pcgen.core.Movement;
import pcgen.core.PObject;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.GlobalLstToken;

/**
 * @author djones4
 *
 */
public class MoveLst implements GlobalLstToken
{

	public String getTokenName()
	{
		return "MOVE";
	}

	public boolean parse(PObject obj, String value, int anInt)
	{
		if (obj instanceof Equipment)
		{
			return false;
		}
		Movement cm = Movement.getMovementFrom(value);
		cm.setMoveRatesFlag(0);
		obj.setMovement(cm);
		return true;
	}

	public boolean parse(LoadContext context, CDOMObject obj, String value)
	{
		Movement cm = Movement.getMovementFrom(value);
		cm.setMoveRatesFlag(0);
		context.graph.linkObjectIntoGraph(getTokenName(), obj, cm);
		return true;
	}

	public String unparse(LoadContext context, CDOMObject obj)
	{
		Set<PCGraphEdge> edgeList =
				context.graph.getChildLinksFromToken(getTokenName(), obj,
					Movement.class);
		StringBuilder sb = new StringBuilder();
		boolean needsTab = false;
		for (PCGraphEdge edge : edgeList)
		{
			if (needsTab)
			{
				sb.append('\t');
			}
			Movement m = (Movement) edge.getSinkNodes().get(0);
			sb.append(m.toLSTString());
		}
		return sb.toString();
	}
}
