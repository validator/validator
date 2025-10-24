package org.javacc.utils;

import java.util.Map;

/**
 *
 *
 * @author Chris Ainsley
 *
 */
public class OptionInfo implements Comparable<OptionInfo>{
	  String _name;
	  OptionType _type;
	  Object _default;

	public OptionInfo(String name, OptionType type, Object default1) {
		_name = name;
		_type = type;
		_default = default1;
	}

	public String getName() {
		return _name;
	}

	public OptionType getType() {
		return _type;
	}

	public Object getDefault() {
		return _default;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_default == null) ? 0 : _default.hashCode());
		result = prime * result + ((_name == null) ? 0 : _name.hashCode());
		result = prime * result + ((_type == null) ? 0 : _type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OptionInfo other = (OptionInfo) obj;
		if (_default == null) {
			if (other._default != null)
				return false;
		} else if (!_default.equals(other._default))
			return false;
		if (_name == null) {
			if (other._name != null)
				return false;
		} else if (!_name.equals(other._name))
			return false;
		if (_type != other._type)
			return false;
		return true;
	}

	@Override
	public int compareTo(OptionInfo o) {
		return this._name.compareTo(o._name);
	}


}
