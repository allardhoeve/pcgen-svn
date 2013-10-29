/*
 * PCStringDirective.java
 * Copyright 2013 (C) James Dempsey <jdempsey@users.sourceforge.net>
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
 * Created on 23/10/2013
 *
 * $Id: $
 */
package pcgen.io.freemarker;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import pcgen.core.PlayerCharacter;
import pcgen.io.ExportHandler;
import pcgen.util.Logging;
import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Implements a custom Freemarker macro to allow exporting of a string value  
 * from the character. It evaluates a PCGen export tokenfor the current character  
 * and returns the value as a number. e.g. <@pcstring tag="PLAYERNAME"/>
 * 
 * 
 * @author James Dempsey <jdempsey@users.sourceforge.net>
 * @version $Revision: $
 */
public class PCStringDirective implements TemplateDirectiveModel
{
	@SuppressWarnings("rawtypes")
	@Override
	public void execute(Environment env, Map params, TemplateModel[] loopVars,
		TemplateDirectiveBody body) throws TemplateException, IOException
	{
		// Check if no parameters were given:
		if (params.size() != 1 || params.get("tag") == null)
		{
			throw new TemplateModelException(
				"This directive requires a single tag parameter.");
		}
		if (loopVars.length != 0)
		{
			throw new TemplateModelException(
				"This directive doesn't allow loop variables.");
		}
		if (body != null)
		{
			throw new TemplateModelException(
				"This directive cannot take a body.");
		}

		TemplateModel model = env.getVariable("pc");
		PlayerCharacter pc =
				(PlayerCharacter) ((AdapterTemplateModel) model)
					.getAdaptedObject(PlayerCharacter.class);
		TemplateModel modelEh = env.getVariable("exportHandler");
		ExportHandler eh =
				(ExportHandler) ((AdapterTemplateModel) modelEh)
					.getAdaptedObject(ExportHandler.class);
		
		String tag = params.get("tag").toString();
		String value = getExportVariable(tag, pc, eh);
		
		env.getOut().append(value);
	}


	/**
	 * Convert the supplied export token into an string value 
	 * @param exportToken The export token to be processed.
	 * @param pc The character being exported.
	 * @param modelEh The ExportHandler managing the output.
	 * @return The value fot he export token for the character.
	 */
	public String getExportVariable(String exportToken, PlayerCharacter pc, ExportHandler modelEh)
	{
		final StringWriter sWriter = new StringWriter();
		final BufferedWriter aWriter = new BufferedWriter(sWriter);
		modelEh.replaceTokenSkipMath(pc, exportToken, aWriter);
		sWriter.flush();

		try
		{
			aWriter.flush();
		}
		catch (IOException e)
		{
			Logging.errorPrint("Couldn't flush the StringWriter used in " +
					"PCStringDirective.getExportVariable.", e);
		}

		final String bString = sWriter.toString();

		String result;
		try
		{
			// Float values
			result = String.valueOf(Float.parseFloat(bString));
		}
		catch (NumberFormatException e)
		{
			// String values
			result = bString;
		}
		return result;
	}

}
