/*
 * Copyright (c) 2007 Tom Parker <thpr@users.sourceforge.net>
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
package plugin.pretokens.test;

import org.junit.Before;
import org.junit.Test;

import pcgen.cdom.enumeration.AssociationKey;
import pcgen.cdom.enumeration.ListKey;
import pcgen.cdom.enumeration.SpellDescriptor;
import pcgen.cdom.graph.PCGraphGrantsEdge;
import pcgen.core.Language;
import pcgen.core.PObject;
import pcgen.core.prereq.Prerequisite;
import pcgen.core.prereq.PrerequisiteException;
import pcgen.core.prereq.PrerequisiteOperator;
import pcgen.core.prereq.PrerequisiteTest;
import pcgen.core.spell.Spell;

public class PreSpellDescriptorTesterTest extends
		AbstractCDOMPreTestTestCase<Spell>
{

	PreSpellDescriptorTester tester = new PreSpellDescriptorTester();
	private SpellDescriptor mindAffecting;
	private SpellDescriptor mind;
	private SpellDescriptor fear;

	@Before
	@Override
	public void setUp()
	{
		super.setUp();
		mindAffecting = SpellDescriptor.getConstant("Mind-Affecting");
		mind = SpellDescriptor.getConstant("Mind");
		fear = SpellDescriptor.getConstant("Fear");
	}

	@Override
	public Class<Spell> getCDOMClass()
	{
		return Spell.class;
	}

	@Override
	public Class<? extends PObject> getFalseClass()
	{
		return Language.class;
	}

	public String getKind()
	{
		return "SPELL.DESCRIPTOR";
	}

	public PrerequisiteTest getTest()
	{
		return tester;
	}

	public Prerequisite getSimplePrereq()
	{
		Prerequisite p;
		p = new Prerequisite();
		p.setKind(getKind());
		p.setKey("Fear");
		p.setOperand("1");
		p.setSubKey("1");
		p.setOperator(PrerequisiteOperator.GTEQ);
		return p;
	}

	public Prerequisite getLevelTwoPrereq()
	{
		Prerequisite p;
		p = new Prerequisite();
		p.setKind(getKind());
		p.setKey("Fear");
		p.setOperand("1");
		p.setSubKey("2");
		p.setOperator(PrerequisiteOperator.GTEQ);
		return p;
	}

	public Prerequisite getStartPrereq()
	{
		Prerequisite p;
		p = new Prerequisite();
		p.setKind(getKind());
		p.setKey("Mind-Affecting");
		p.setOperand("1");
		p.setSubKey("1");
		p.setOperator(PrerequisiteOperator.GTEQ);
		return p;
	}

	@Test
	public void testSimple() throws PrerequisiteException
	{
		Prerequisite prereq = getSimplePrereq();
		// PC Should start without
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		PObject spell = getObject("Wild Mage");
		PCGraphGrantsEdge edge = grantObject(spell);
		edge.setAssociation(AssociationKey.SPELL_LEVEL, Integer.valueOf(1));
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mindAffecting);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, fear);
		assertEquals(1, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mind);
		assertEquals(1, getTest().passesCDOM(prereq, pc));
	}

	@Test
	public void testLevelTwo() throws PrerequisiteException
	{
		Prerequisite prereq = getLevelTwoPrereq();
		// PC Should start without
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		PObject spell = getObject("Wild Mage");
		PCGraphGrantsEdge edge = grantObject(spell);
		edge.setAssociation(AssociationKey.SPELL_LEVEL, Integer.valueOf(1));
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mindAffecting);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, fear);
		// Insufficient Level
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mind);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		edge.setAssociation(AssociationKey.SPELL_LEVEL, Integer.valueOf(2));
		assertEquals(1, getTest().passesCDOM(prereq, pc));
		edge.setAssociation(AssociationKey.SPELL_LEVEL, Integer.valueOf(3));
		assertEquals(1, getTest().passesCDOM(prereq, pc));
	}

	@Test
	public void testFalseObject() throws PrerequisiteException
	{
		Prerequisite prereq = getSimplePrereq();
		// PC Should start without
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		PObject lang = grantFalseObject("Winged Mage");
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		lang.addToListFor(ListKey.SPELL_DESCRIPTOR, mindAffecting);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		lang.addToListFor(ListKey.SPELL_DESCRIPTOR, fear);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		lang.addToListFor(ListKey.SPELL_DESCRIPTOR, mind);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
	}

	@Test
	public void testFalseStart() throws PrerequisiteException
	{
		Prerequisite prereq = getStartPrereq();
		// PC Should start without
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		PObject spell = getObject("Wild Mage");
		PCGraphGrantsEdge edge = grantObject(spell);
		edge.setAssociation(AssociationKey.SPELL_LEVEL, Integer.valueOf(3));
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, fear);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mind);
		assertEquals(0, getTest().passesCDOM(prereq, pc));
		spell.addToListFor(ListKey.SPELL_DESCRIPTOR, mindAffecting);
		assertEquals(1, getTest().passesCDOM(prereq, pc));
	}

}
