Summary: A RELAX NG Validator.
Name: jing
Version:
Release: 1
URL: http://www.thaiopensource.com/relaxng/
Source: %{name}-%{version}.tar.gz
License: BSD
Group: Applications/Text
BuildRoot: %{_tmppath}/%{name}-root

BuildRequires: gcc-java >= 3.2-7

%description
Jing is an implementation of RELAX NG, a schema language for
XML. RELAX NG has been standardized by OASIS and is in the final
stages of standardization by ISO as ISO/IEC 19757-2.  Jing validates
an XML document against a RELAX NG schema.  Jing supports both the
original XML syntax for RELAX NG schemas, and the more recent non-XML
compact syntax.

%prep
%setup -q

%build
%configure GCJFLAGS="${GCJFLAGS:-%optflags}"
make

%install
rm -rf $RPM_BUILD_ROOT
%makeinstall

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%{_bindir}/jing
%{_mandir}/man1/jing.1*

%changelog
* Sat Feb 22 2003 James Clark <jjc@jclark.com>
- Initial build.


