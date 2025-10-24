var W3C = {
	
	start: function(){
		
		//select elements
		
		W3C.Legends = $$('legend.toggletext');
		W3C.Options = $$('div.options');
		
		W3C.TabSet = $('tabset_tabs');
		
		W3C.Tabs = W3C.TabSet.getElements('li');
		W3C.TabLinks = W3C.TabSet.getElements('a');
		
		W3C.Sections = $$('fieldset.tabset_content');
		W3C.Submits = $$('input[type=submit]');
		W3C.Forms = $$('form');
		
		W3C.URI = $('uri');
		W3C.URI.required = true;
		W3C.URI.addEventListener("change",
			function() { this.setCustomValidity('') }, false);
		W3C.URI.Msg = $$('.instructions')[0].textContent
			.replace(":", "");
		W3C.URI.addEventListener("invalid",
			function() { this.setCustomValidity(W3C.URI.Msg) });
		W3C.File = $('file');
		W3C.File.required = true;
		W3C.File.addEventListener("change",
			function() { this.setCustomValidity('') }, false);
		W3C.File.Msg = $$('.instructions')[1].textContent
			.replace(":", "");
		W3C.File.addEventListener("invalid",
			function() { this.setCustomValidity(W3C.File.Msg) });
		W3C.Textarea = $$('textarea')[0];
		W3C.Textarea.required = true;
		W3C.Textarea.addEventListener("change",
			function() { this.setCustomValidity('') });
		W3C.Textarea.Msg = $$('.instructions')[2].textContent
			.replace(":", "");
		W3C.Textarea.addEventListener("invalid",
			function() { this.setCustomValidity(W3C.Textarea.Msg) });
		W3C.Submits.each(function(submit, i){
			var value = submit.value;
			submit.setStyle('display', 'none');
			var link = new Element('a', {'class': 'submit', 'href': '#'});
			var span = new Element('span').set('text', value).inject(link);
			link.injectAfter(submit).addEvent('click', function(event){
				new Event(event).stop();
				if ('reportValidity' in document.createElement('form')) {
					if (W3C.Forms[i].reportValidity()) {
						W3C.Forms[i].submit();
					}
				} else {
					W3C.Forms[i].submit();
				}
			});
		});
		
		
		//initialize effects arrays
		
		W3C.SectionFx = [];
		W3C.OptionsFx = [];
		
		//creating the Effects for the Options
		
		W3C.Options.each(function(option, i){
			W3C.OptionsFx[i] = new Fx.Slide(option, {'wait': false, 'duration': 180});
			W3C.OptionsFx[i].addEvent('onComplete', function(){
				if (this.element.getStyle('margin-top').toInt() == 0){
					this.wrapper.setStyle('height', 'auto');
				}
				else {
				  this.element.setStyle('display','none');  // if a slideOut completed, set display:none
				}
			});
		});
		
		//creating links on legends, with event listeners
		
		W3C.Legends.each(function(legend, i){
			var html = legend.innerHTML;
			var pid = W3C.Sections[i].id.replace(/-/g, '_');
			var opt = '+with_options';
			legend.set('html', '<a href="#'+ pid + opt + '">' + html + '</a>');
			var option = W3C.Options[i];
			var link = legend.getFirst();
			link.addEvent('click', function(event){
			  option.setStyle('display', 'block'); // before any slide effect, set display:block
				var block = (option.getStyle('margin-top').toInt() == 0);
				W3C.setHash((block) ? pid : pid + opt);
				new Event(event).stop();
				W3C.refreshOptionLinks(!block, i);
			});
		});
		
		//creating event listeners on tabs
		
		W3C.Tabs.each(function(li, i){
			var link = li.getFirst();
			link.href = link.original = '#' + link.href.split('#')[1].replace(/-/g, '_');
			li.addEvent('click', function(){
				W3C.updateLocation();
				W3C.displaySection(i);
			});
		});
		
		//updating the location
		
		W3C.updateLocation();
		
		//setting the initial display of the options, based on the location
		
		W3C.refreshOptionLinks(W3C.WithOptions);
		
		//attaching the Sections effects, and display section based on the uri
		
		W3C.Sections.each(function(section, i){
			var fakeId = section.id.replace(/-/g, '_');
			W3C.SectionFx[i] = new Fx.Tween(section, {property:'opacity', link: 'cancel', duration: 220});
			section.setStyle('display', 'none');
			if (W3C.Location[0] && fakeId.contains(W3C.Location[0].replace(/-/g, '_'))){
				W3C.displaySection(i, true);
				W3C.Located = true;
			}
		});
		
		//displaying the first section if no one is located
		
		if (!W3C.Located) W3C.displaySection(0, true);
		
		if (window.ie) $$('legend').setStyle('margin-left', '-0.4em');
	},
	
	updateLocation: function(){
		W3C.Location = window.location.hash.replace('#', '').split('+');
		W3C.WithOptions = (W3C.Location[1] && W3C.Location[1].contains('with_options'));
	},
	
	refreshOptionLinks: function(options, idx){
		
		if (!options){
			if ($chk(idx)) W3C.OptionsFx[idx].slideOut();
			W3C.Legends.removeClass('toggled');
		} else {
			if ($chk(idx)) W3C.OptionsFx[idx].slideIn();
			W3C.Legends.addClass('toggled');
		}
		
		W3C.TabLinks.each(function(link){
			link.href = (options) ? link.original + '+with_options' : link.original;
		});
	},
	
	displaySection: function(i, sudden){
		W3C.Sections.each(function(section, j){
			var block = section.getStyle('display') == 'block';
			if (j == i){
				if (block) return;
				W3C.Sections[j].setStyles({'opacity': sudden ? 1 : 0, 'display': 'block'});
				if (!sudden) W3C.SectionFx[j].start(1);
				if (W3C.WithOptions) W3C.OptionsFx[j].show().fireEvent('onComplete');
				else W3C.OptionsFx[j].hide();
			} else {
				if (!block) return;
				W3C.Sections[j].setStyles({'display': 'none', 'opacity': 0});
			}
		});
		
		W3C.Tabs.each(function(link, j){
			if (j == i) link.addClass('selected');
			else link.removeClass('selected');
		});
	},
	
	setHash: function(hash){
		if (window.webkit419){
			W3C.FakeForm = W3C.FakeForm || new Element('form', {'method': 'get'}).injectInside(document.body);
			W3C.FakeForm.setProperty('action', '#' + hash).submit();
		} else {
			window.location.hash = hash;
		}
	}
	
};

window.addEvent('domready', W3C.start);