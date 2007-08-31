package pcgen.cdom.helper;

import java.util.Set;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.CDOMSingleRef;
import pcgen.core.PlayerCharacter;

public class ChooseChoiceSet<T extends CDOMObject> implements
		PrimitiveChoiceSet<T>
{

	private final CDOMSingleRef<T> reference;

	public ChooseChoiceSet(CDOMSingleRef<T> ref)
	{
		if (ref == null)
		{
			throw new IllegalArgumentException("Reference cannot be null");
		}
		reference = ref;
	}

	public Class<T> getChoiceClass()
	{
		return reference.getReferenceClass();
	}

	public Set<T> getSet(PlayerCharacter pc)
	{
		T obj = reference.resolvesTo();
		// TODO Auto-generated method stub
		return null;
	}

	public String getLSTformat()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
