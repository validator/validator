package org.relaxng.datatype.helpers;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.relaxng.datatype.Datatype;

public class ValueMap implements Map {
	
	private final Map storage;
	private final Datatype dt;
	
	public ValueMap( Datatype dt ) {
		this( dt, new HashMap() );
	}
	
	public ValueMap( Datatype dt, Map storage ) {
		this.dt = dt;
		this.storage = storage;
	}
	
    public boolean containsKey(Object key) {
		return storage.containsKey(new ValueSet.Item(dt,key));
	}
	
	public boolean containsValue(Object value) {
		return storage.containsValue(value);
	}
	
	public Object get(Object key) {
		return storage.get(new ValueSet.Item(dt,key));
	}

    public Object put(Object key, Object value) {
		return storage.put(new ValueSet.Item(dt,key),value);
	}

    public Object remove(Object key) {
		return storage.remove(new ValueSet.Item(dt,key));
	}
	
	public int size() {
		return storage.size();
	}
	
	public boolean isEmpty() {
		return storage.isEmpty();
	}
	
	public void clear() {
		storage.clear();
	}
	
	public Collection values() {
		return storage.values();
	}
	
	public void putAll( Map m ) {
		Iterator i = m.entrySet().iterator();
		while (i.hasNext()) {
			Entry e = (Entry)i.next();
			put(e.getKey(), e.getValue());
		}
	}
	
	public Set keySet() {
		return new ValueSet(dt,storage.keySet());
	}
	
	public Set entrySet() {
		// very sloppy implementation
		return new AbstractSet() {
			public Iterator iterator() {
				final Iterator itr = storage.entrySet().iterator();
				return new Iterator(){
					public Object next() {
						final Entry e = (Entry)itr.next();
						return new Entry(){
							public Object getValue() {
								return e.getValue();
							}
							public Object setValue(Object o) {
								return e.setValue(o);
							}
							public Object getKey() {
								ValueSet.Item itm = (ValueSet.Item)e.getKey();
								return itm.o;
							}
						};
					}
					public void remove() {
						itr.remove();
					}
					public boolean hasNext() {
						return itr.hasNext();
					}
				};
			}
			public int size() {
				return storage.size();
			}
		};
	}
}
