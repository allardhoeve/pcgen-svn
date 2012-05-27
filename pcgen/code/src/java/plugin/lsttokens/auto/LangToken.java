/*
 * Copyright 2007 (C) Thomas Parker <thpr@users.sourceforge.net>
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
package plugin.lsttokens.auto;

import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.CDOMReference;
import pcgen.cdom.base.ChooseResultActor;
import pcgen.cdom.base.Constants;
import pcgen.cdom.content.ConditionalChoiceActor;
import pcgen.cdom.enumeration.ListKey;
import pcgen.core.Globals;
import pcgen.core.Language;
import pcgen.core.PlayerCharacter;
import pcgen.core.QualifiedObject;
import pcgen.core.prereq.Prerequisite;
import pcgen.persistence.PersistenceLayerException;
import pcgen.persistence.lst.output.prereq.PrerequisiteWriter;
import pcgen.persistence.lst.prereq.PreParserFactory;
import pcgen.rules.context.Changes;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.TokenUtilities;
import pcgen.rules.persistence.token.AbstractNonEmptyToken;
import pcgen.rules.persistence.token.CDOMSecondaryToken;
import pcgen.rules.persistence.token.ParseResult;
import pcgen.util.Logging;

public class LangToken extends AbstractNonEmptyToken<CDOMObject> implements CDOMSecondaryToken<CDOMObject>,
		ChooseResultActor {

	private static final Class<Language> LANGUAGE_CLASS = Language.class;

	@Override
	public String getParentToken()
	{
		return "AUTO";
	}

	@Override
	public String getTokenName()
	{
		return "LANG";
	}

	private String getFullName()
	{
		return getParentToken() + ":" + getTokenName();
	}

	@Override
	protected ParseResult parseNonEmptyToken(LoadContext context, CDOMObject obj, String value)
	{
		String lang = value;

		ParseResult pr = checkSeparatorsAndNonEmpty('|', lang);
		if (!pr.passed())
		{
			return pr;
		}

		boolean foundAny = false;
		boolean foundOther = false;

		StringTokenizer tok = new StringTokenizer(lang, Constants.PIPE);

		boolean isPre = false;
		Prerequisite prereq = null; // Do not initialize, null is significant!

		while (tok.hasMoreTokens())
		{
			String token = tok.nextToken();
			if (PreParserFactory.isPreReqString(token))
			{
				if (isPre)
				{
					String errorText = "Invalid " + getTokenName() + ": " + value
							+ "  PRExxx must be at the END of the Token";
					Logging.errorPrint(errorText);
					return new ParseResult.Fail(errorText);
				}
				prereq = getPrerequisite(token);
				if (prereq == null)
				{
					return new ParseResult.Fail("Error generating Prerequisite " + prereq + " in " + getFullName());
				}
				int preStart = value.indexOf(token) - 1;
				lang = value.substring(0, preStart);
				isPre = true;
			}
		}

		tok = new StringTokenizer(lang, Constants.PIPE);
		while (tok.hasMoreTokens())
		{
			String token = tok.nextToken();
			if ("%LIST".equals(token))
			{
				ChooseResultActor cra;
				if (prereq == null)
				{
					cra = this;
				} else
				{
					ConditionalChoiceActor cca = new ConditionalChoiceActor(this);
					cca.addPrerequisite(prereq);
					cra = cca;
				}
				foundOther = true;
				context.obj.addToList(obj, ListKey.CHOOSE_ACTOR, cra);
			} else if (Constants.LST_ALL.equals(token))
			{
				foundAny = true;
				context.getObjectContext().addToList(
						obj,
						ListKey.AUTO_LANGUAGE,
						new QualifiedObject<CDOMReference<Language>>(context.ref.getCDOMAllReference(LANGUAGE_CLASS),
								prereq));
			} else
			{
				foundOther = true;
				CDOMReference<Language> ref = TokenUtilities.getTypeOrPrimitive(context, LANGUAGE_CLASS, token);
				if (ref == null)
				{
					return new ParseResult.Fail("  Error was encountered while parsing " + getTokenName());
				}
				context.getObjectContext().addToList(obj, ListKey.AUTO_LANGUAGE,
						new QualifiedObject<CDOMReference<Language>>(ref, prereq));
			}
		}

		if (foundAny && foundOther)
		{
			return new ParseResult.Fail("Non-sensical " + getFullName() + ": Contains ANY and a specific reference: "
					+ value);
		}

		return ParseResult.SUCCESS;
	}

	@Override
	public String[] unparse(LoadContext context, CDOMObject obj)
	{
		PrerequisiteWriter prereqWriter = new PrerequisiteWriter();
		Changes<QualifiedObject<CDOMReference<Language>>> changes = context.obj.getListChanges(obj,
				ListKey.AUTO_LANGUAGE);
		Changes<ChooseResultActor> listChanges = context.getObjectContext().getListChanges(obj, ListKey.CHOOSE_ACTOR);
		Collection<QualifiedObject<CDOMReference<Language>>> added = changes.getAdded();
		StringBuilder sb = new StringBuilder();
		Collection<ChooseResultActor> listAdded = listChanges.getAdded();
		boolean foundAny = false;
		boolean foundOther = false;
		if (listAdded != null && !listAdded.isEmpty())
		{
			for (ChooseResultActor cra : listAdded)
			{
				if (cra.getSource().equals(getTokenName()))
				{
					try
					{
						sb.append(cra.getLstFormat());
						foundOther = true;
					} catch (PersistenceLayerException e)
					{
						context.addWriteMessage("Error writing Prerequisite: " + e);
						return null;
					}
				}
			}
		}
		if (added != null)
		{
			boolean needPipe = sb.length() > 0;
			for (QualifiedObject<CDOMReference<Language>> spp : added)
			{
				CDOMReference<Language> lang = spp.getRawObject();
				List<Prerequisite> prereqs = spp.getPrerequisiteList();
				String ab = lang.getLSTformat(false);
				boolean isUnconditionalAll = Constants.LST_ALL.equals(ab);
				foundAny |= isUnconditionalAll;
				foundOther |= !isUnconditionalAll;
				if (needPipe)
				{
					sb.append('|');
				}
				needPipe = true;
				if (prereqs != null && !prereqs.isEmpty())
				{
					if (prereqs.size() > 1)
					{
						context.addWriteMessage("Error: " + obj.getClass().getSimpleName()
								+ " had more than one Prerequisite for " + getFullName());
						return null;
					}
					Prerequisite p = prereqs.get(0);
					StringWriter swriter = new StringWriter();
					try
					{
						prereqWriter.write(swriter, p);
					} catch (PersistenceLayerException e)
					{
						context.addWriteMessage("Error writing Prerequisite: " + e);
						return null;
					}
					ab = ab + '|' + swriter.toString();
				}
				sb.append(ab);
			}
		}
		if (foundAny && foundOther)
		{
			context.addWriteMessage("Non-sensical " + getFullName() + ": Contains ANY and a specific reference: " + sb);
			return null;
		}
		if (sb.length() == 0)
		{
			// okay
			return null;
		}
		return new String[] { sb.toString() };
	}

	@Override
	public Class<CDOMObject> getTokenClass()
	{
		return CDOMObject.class;
	}

	@Override
	public void apply(PlayerCharacter pc, CDOMObject obj, String o)
	{
		Language l = Globals.getContext().ref.silentlyGetConstructedCDOMObject(LANGUAGE_CLASS, o);
		if (l != null)
		{
			pc.addAutoLanguage(l, obj);
		}
	}

	@Override
	public void remove(PlayerCharacter pc, CDOMObject obj, String o)
	{
		Language l = Globals.getContext().ref.silentlyGetConstructedCDOMObject(LANGUAGE_CLASS, o);
		if (l != null)
		{
			pc.removeAutoLanguage(l, obj);
		}
	}

	@Override
	public String getSource()
	{
		return getTokenName();
	}

	@Override
	public String getLstFormat()
	{
		return "%LIST";
	}
}
