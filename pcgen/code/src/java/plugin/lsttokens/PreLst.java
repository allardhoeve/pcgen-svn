/*
 * Created on Sep 2, 2005
 *
 */
package plugin.lsttokens;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.Constants;
import pcgen.core.prereq.Prerequisite;
import pcgen.rules.context.Changes;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractToken;
import pcgen.rules.persistence.token.CDOMPrimaryToken;

/**
 * @author djones4
 * 
 */
public class PreLst extends AbstractToken implements
		CDOMPrimaryToken<CDOMObject>
{
	@Override
	public String getTokenName()
	{
		return "PRE";
	}

	public boolean parse(LoadContext context, CDOMObject pcc, String value)
	{
		if (Constants.LST_DOT_CLEAR.equals(value))
		{
			context.obj.clearPrerequisiteList(pcc);
			return true;
		}
		return false;
	}

	public String[] unparse(LoadContext context, CDOMObject pcc)
	{
		Changes<Prerequisite> changes = context.obj.getPrerequisiteChanges(pcc);
		if (changes == null || !changes.includesGlobalClear())
		{
			// indicates no Token
			return null;
		}
		return new String[] { Constants.LST_DOT_CLEAR };
	}

	public Class<CDOMObject> getTokenClass()
	{
		return CDOMObject.class;
	}
}
