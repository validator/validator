Summary: RELAX NG Validator
Name: jing
Version:
Release: 1
URL: http://www.thaiopensource.com/relaxng/
Source: %{name}-%{version}.tar.gz
License: BSD
Group: Applications/Text
BuildRoot: %{_tmppath}/%{name}-root

%description
A validator for RELAX NG.

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
%{_bindir}/jing


%changelog
* Sat Feb 22 2003 James Clark <jjc@jclark.com>
- Initial build.


