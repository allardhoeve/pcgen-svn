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

import java.util.Map;

import pcgen.cdom.base.CDOMObject;
import pcgen.core.PObject;
import pcgen.core.Source;
import pcgen.core.SourceEntry;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.GlobalLstToken;
import pcgen.persistence.lst.SourceLoader;
import pcgen.persistence.lst.SourceLstToken;

/**
 * @author djones4
 *
 */
public class SourcewebLst implements GlobalLstToken, SourceLstToken
{

	public String getTokenName()
	{
		return "SOURCEWEB";
	}

	public boolean parse(PObject obj, String value, int anInt)
	{
		obj.getSourceEntry().getSourceBook().setWebsite(value);
		return true;
	}

	public boolean parse(Map<String, String> sourceMap, String value)
	{
		sourceMap.putAll(SourceLoader.parseSource("SOURCEWEB:" + value));
		return true;
	}

	public boolean parse(LoadContext context, CDOMObject obj, String value)
	{
		obj.getSourceEntry().getSourceBook().setWebsite(value);
		return true;
	}

	public String unparse(LoadContext context, CDOMObject obj)
	{
		SourceEntry sourceEntry = obj.getSourceEntry();
		if (sourceEntry == null)
		{
			return null;
		}
		Source sourceBook = sourceEntry.getSourceBook();
		if (sourceBook == null)
		{
			return null;
		}
		String website = sourceBook.getWebsite();
		if (website == null)
		{
			return null;
		}
		return new StringBuilder().append(getTokenName()).append(':').append(
			website).toString();
	}
}
