Summary: Multi-format schema converter based on RELAX NG.
Name: trang
Version: 
Release: 1
URL: http://www.thaiopensource.com/relaxng/
Source: %{name}-%{version}.tar.gz
License: BSD
Group: Applications/Text
BuildRoot: %{_tmppath}/%{name}-root

BuildRequires: gcc-java >= 3.2-7

%description
Trang converts between different schema languages for XML.  It
supports the following languages: RELAX NG (both XML and compact
syntax), XML 1.0 DTDs, W3C XML Schema.  A schema written in any of the
supported schema languages can be converted into any of the other
supported schema languages, except that W3C XML Schema is supported
for output only, not for input.

Trang can also infer a schema from one or more example XML documents.

Trang is constructed around an RELAX NG object model designed to
support schema conversion.  For each schema language supported for
input, there is an input module that can convert from the schema
language into this internal object model.  Similarly, for each schema
language supported for output, there is an output module that can
convert from the internal object model in the schema language.

Trang aims to produce human-understandable schemas; it tries for a
translation that preserves all aspects of the input schema that may be
significant to a human reader, including the definitions, the way the
schema is divided into files, annotations and comments.

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
%{_bindir}/trang
%{_mandir}/man1/trang.1*

%doc trang-manual.html

%changelog
* Sat Feb 22 2003 James Clark <jjc@jclark.com>
- Initial build.


