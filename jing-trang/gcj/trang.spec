Summary: Trang multi-schema converter
Name: trang
Version: 
Release: 1
URL: http://www.thaiopensource.com/relaxng/
Source: %{name}-%{version}.tar.gz
License: BSD
Group: Applications/Text
BuildRoot: %{_tmppath}/%{name}-root

%description
Trang converts schemas for XML between different schema languages.

%prep
%setup -q

%build
%configure
make

%install
rm -rf $RPM_BUILD_ROOT
%makeinstall

%clean
rm -rf $RPM_BUILD_ROOT

%files
%defattr(-,root,root)
%{_bindir}/trang


%changelog
* Sat Feb 22 2003 James Clark <jjc@jclark.com>
- Initial build.


