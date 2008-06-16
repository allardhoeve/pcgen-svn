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
package pcgen.cdom.base;

import java.util.Collection;

/**
 * A CDOMReference stores references to Objects. Often these are CDOMObjects,
 * but that is not strictly required.
 * 
 * The intent is for a CDOMReference to be created in order to identify that a
 * reference was made to an object. The CDOMReference can later be resolved to
 * identify the exact Objects to which the CDOMReference refers.
 * 
 * CDOMReference does not limit the quantity of object to which a single
 * CDOMReference can refer (it may be more than one).
 * 
 * @param <T>
 *            The class of object this CDOMReference refers to.
 */
public abstract class CDOMReference<T extends PrereqObject>
// implements PrimitiveChoiceFilter<T>
{

	/**
	 * The name of this CDOMReference. This is the identifying information about
	 * the CDOMReference, and may (or may not) be used to identify the objects
	 * to which this CDOMReference resolves (will depend on the implementation)
	 */
	private final String name;

	/**
	 * The class of object this CDOMReference refers to.
	 */
	private final Class<T> clazz;

	/**
	 * Constructs a new CDOMReference to the given Class of object, with the
	 * given name.
	 * 
	 * @param cl
	 *            The class of object this CDOMReference refers to.
	 * @param nm
	 *            The name of this CDOMReference.
	 */
	public CDOMReference(Class<T> cl, String nm)
	{
		clazz = cl;
		name = nm;
	}

	/**
	 * Returns the name of this CDOMReference. Note that this name is suitable
	 * for display, but it does not represent information that should be stored
	 * in a persistent state (it is not sufficient information to reconstruct
	 * this CDOMReference)
	 * 
	 * @return The name of this CDOMReference.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * The class of object this CDOMReference refers to.
	 * 
	 * @return The class of object this CDOMReference refers to.
	 */
	public Class<T> getReferenceClass()
	{
		return clazz;
	}

	/**
	 * Adds an object to be included in the Collection of objects to which this
	 * CDOMReference refers.
	 * 
	 * Note that specific implementations may limit the number of times this
	 * method may be called, and may throw an IllegalStateException if that
	 * limit is exceeded.
	 * 
	 * @param obj
	 *            an object to be included in the Collection of objects to which
	 *            this CDOMReference refers.
	 */
	public abstract void addResolution(T obj);

	/**
	 * Returns true if the given Object is included in the Collection of Objects
	 * to which this CDOMReference refers.
	 * 
	 * @param obj
	 *            The object to be tested to see if it is referred to by this
	 *            CDOMReference.
	 * @return true if the given Object is included in the Collection of Objects
	 *         to which this CDOMReference refers; false otherwise.
	 */
	public abstract boolean contains(T obj);

	/**
	 * Returns a representation of this CDOMReference, suitable for storing in
	 * an LST file.
	 * 
	 * Note that this will ALWAYS return a comma-delimted list of objects if
	 * more than one object is present in the CDOMReference.
	 */
	public abstract String getLSTformat();

	/**
	 * Returns the count of the number of objects included in the Collection of
	 * Objects to which this CDOMReference refers.
	 * 
	 * Note that the behavior of this class is undefined if the CDOMReference
	 * has not yet been resolved.
	 * 
	 * @return the count of the number of objects included in the Collection of
	 *         Objects to which this CDOMReference refers.
	 */
	public abstract int getObjectCount();

	/**
	 * Returns a Collection containing the Objects to which this CDOMReference
	 * refers.
	 * 
	 * It is intended that classes which extend CDOMReference will make this
	 * method reference-semantic, meaning that ownership of the Collection
	 * returned by this method will be transferred to the calling object.
	 * Modification of the returned Collection should not result in modifying
	 * the CDOMReference, and modifying the CDOMReference after the Collection
	 * is returned should not modify the Collection.
	 * 
	 * Note that the behavior of this class is undefined if the CDOMReference
	 * has not yet been resolved. (It may return null or an empty Collection;
	 * that is implementation dependent)
	 * 
	 * @return A Collection containing the Objects to which this CDOMReference
	 *         refers.
	 */
	public abstract Collection<T> getContainedObjects();

	/**
	 * Returns a String representation of this CDOMReference, primarily for
	 * purposes of debugging. It is strongly advised that no dependency on this
	 * method be created, as the return value may be changed without warning.
	 */
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " " + clazz.getSimpleName() + " "
				+ name;
	}

}
