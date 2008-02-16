package plugin.primitive.weaponprof;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import pcgen.cdom.base.CDOMSimpleSingleRef;
import pcgen.cdom.enumeration.ListKey;
import pcgen.character.CharacterDataStore;
import pcgen.core.Deity;
import pcgen.core.WeaponProf;
import pcgen.persistence.LoadContext;
import pcgen.persistence.lst.PrimitiveToken;

public class DeityWeaponToken implements PrimitiveToken<WeaponProf>
{

	public boolean initialize(LoadContext context, String value, String args)
	{
		if (args != null)
		{
			return false;
		}
		if (value != null)
		{
			return false;
			// throw new IllegalArgumentException(
			// "Deity Weapon Primitive does not allow a value");
		}
		return true;
	}

	public String getTokenName()
	{
		return "DEITY";
	}

	public Class<WeaponProf> getReferenceClass()
	{
		return WeaponProf.class;
	}

	public Set<WeaponProf> getSet(CharacterDataStore pc)
	{
		List<Deity> deities = pc.getActiveGraph().getGrantedNodeList(
				Deity.class);
		Set<WeaponProf> wpSet = new HashSet<WeaponProf>();
		if (deities == null)
		{
			return wpSet;
		}
		for (Deity deity : deities)
		{
			List<CDOMSimpleSingleRef<WeaponProf>> weapons = deity
					.getListFor(ListKey.DEITYWEAPON);
			if (weapons != null)
			{
				for (CDOMSimpleSingleRef<WeaponProf> wpRef : weapons)
				{
					wpSet.add(wpRef.resolvesTo());
				}
			}
		}
		return wpSet;
	}

	public String getLSTformat()
	{
		return null;
	}

	public boolean allow(CharacterDataStore pc, WeaponProf obj)
	{
		List<Deity> deities = pc.getActiveGraph().getGrantedNodeList(
				Deity.class);
		if (deities == null)
		{
			return false;
		}
		for (Deity deity : deities)
		{
			List<CDOMSimpleSingleRef<WeaponProf>> weapons = deity
					.getListFor(ListKey.DEITYWEAPON);
			if (weapons != null)
			{
				for (CDOMSimpleSingleRef<WeaponProf> wpRef : weapons)
				{
					if (wpRef.contains(obj))
					{
						return true;
					}
				}
			}
		}
		return false;
	}

}
