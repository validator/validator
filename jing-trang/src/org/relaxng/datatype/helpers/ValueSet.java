package org.relaxng.datatype.helpers;

import java.util.AbstractSet;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import org.relaxng.datatype.Datatype;

/**
 * A {@link Set} implementation for value objects of datatypes.
 * 
 * <p>
 * Since the value objects of datatypes do not implement the hashCode method
 * and the equals method correctly, normal collection classes like Hashtable or
 * HashSet cannot be used.
 * 
 * <p>
 * This class implements a set who behaves consistently with the equality notion of
 * the specified datatype.
 * 
 * <p>
 * To achieve that, this class wraps value objects by the wrapper, and stores it
 * to a ordinary set. The wrapper implements the hashCode method and the equals method
 * in the expected way.
 * 
 * @author <a href="mailto:kohsuke.kawaguchi@eng.sun.com">Kohsuke KAWAGUCHI</a>
 */
public final class ValueSet extends AbstractSet {
	
	private final Datatype dt;
	private final Set storage;

	/**
	 * creates a set.
	 * 
	 * @param dt
	 *		This datatype object is used for comparison of value objects.
	 */
	ValueSet( Datatype dt ) {
		this( dt, new HashSet() );
	}
	
	/**
	 * creates a set with user-specified back-end storage.
	 * 
	 * @param dt
	 *		This datatype object is used for comparison of value objects.
	 * @param storage
	 *		a normal set implementation. This object is used as the actual
	 *		storage for this set.
	 */
	ValueSet( Datatype dt, Set storage ) {
		this.dt = dt;
		this.storage = storage;
	}
	
	static final class Item {
		Item( Datatype dt, Object o ) { this.o=o; this.dt=dt; }
		
		/** actual payload. */
		public final Object o;
		
		public final Datatype dt;
		
		public int hashCode() {
			return dt.valueHashCode(o);
		}
		public boolean equals( Object rhs ) {
			if(!(rhs instanceof Item))	return false;
			return dt.sameValue( this.o, ((Item)rhs).o );
		}
	}
	
	public Iterator iterator() {
		final Iterator itr = storage.iterator();
		return new Iterator(){
			public void remove() { itr.remove(); }
			public boolean hasNext() { return itr.hasNext(); }
			public Object next() {
				return ((Item)itr.next()).o;
			}
		};
	}
	
	public int size() {
		return storage.size();
	}
	
	public boolean add( Object o ) {
		return storage.add( new Item(dt,o) );
	}
	
	public boolean contains( Object o ) {
		return storage.contains( new Item(dt,o) );
	}
	
	public boolean remove( Object o ) {
		return storage.remove( new Item(dt,o) );
	}
}
