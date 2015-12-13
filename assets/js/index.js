stLight.options({
	publisher: "03ea2b5b-f960-4edf-b456-9565dbf2be0e",
	doNotHash: true,
	doNotCopy: true,
	hashAddressBar: false
});

(function(i,s,o,g,r,a,m){i['GoogleAnalyticsObject']=r;i[r]=i[r]||function(){
(i[r].q=i[r].q||[]).push(arguments)},i[r].l=1*new Date();a=s.createElement(o),
m=s.getElementsByTagName(o)[0];a.async=1;a.src=g;m.parentNode.insertBefore(a,m)
})(window,document,'script','//www.google-analytics.com/analytics.js','ga');

ga('create', 'UA-47485074-14', 'auto');
ga('send', 'pageview');

if(!String.format) {
	String.format = function(format) {
		var args = Array.prototype.slice.call(arguments, 1);
		return format.replace(/{(\d+)}/g, function(match, number) { 
			return typeof args[number] != 'undefined' ? args[number] : match;
		});
	};
}

var year = 2016;

var countryOne = createDefaultCountry('France');
var countryTwo = createDefaultCountry('Russia');
var footer = $('#page footer p');

$(document).ready(function() {
	refreshRankings();
	$('[data-localize]').localize('translation', {
		pathPrefix: './assets/translations',
		callback: function(data, defaultCallback) {
			$('[data-localize="description"]').attr('content', data.description);
			delete data.description;
			countryOne.displayedName = data.countryOne;
			delete data.countryOne;
			countryTwo.displayedName = data.countryTwo;
			delete data.countryTwo;
			countryOne.asString = data.countryToString;
			countryTwo.asString = data.countryToString;
			delete data.countryToString;
			$('[yes="Yes"]').attr('yes', data.yes);
			delete data.yes;
			$('[yes="No"]').attr('no', data.no);
			delete data.no;
			defaultCallback(data);
		}
	});
});

$(window).resize(function() {
	centerTitle($(this).height());
});

function refreshRankings() {
	$.getJSON('http://whateverorigin.org/get?url=' + encodeURIComponent('http://kassiesa.home.xs4all.nl/bert/uefa/data/method4/crank' + year + '.html') + '&callback=?', function(data) {
		var wrapper = $('<div/>');
		wrapper.html(data.contents);
		var currentRanking = 0;
		var ranking = wrapper.find('tr td');
		for(var index = 0; index < ranking.length; index++) {
			var country = $(ranking[index]);
			var attr = country.attr('align');
			if(typeof attr === typeof undefined || attr === false || attr !== 'left') {
				continue;
			}
			if(currentRanking == 0) {
				currentRanking++; // < 2015
				continue;
			}
			currentRanking++;
			var name = country.text();
			if(name == countryOne.name) {
				countryOne.ranking = currentRanking - 1;
				countryOne.points = $(ranking[index + 6]).text();
			}
			else if(name == countryTwo.name) {
				countryTwo.ranking = currentRanking - 1;
				countryTwo.points = $(ranking[index + 6]).text();
			}
			if(countryOne.ranking != -1 && countryTwo.ranking != -1) {
				break;
			}
		}
		var loader = $('#loader');
		loader.fadeOut(1000, function() {
			loader.remove();
		});
		refreshTitle();
	});
}

function createDefaultCountry(name) {
	return {
		name: name,
		displayedName: name,
		ranking: -1,
		points: -1,
		asString: '{0} ranking : {1}, points : {2}',
		toString: function() {
			return String.format(this.asString, this.displayedName, this.ranking, this.points);
		}
	};
}

function centerTitle(documentHeight) {
	var title = $('#page h1');
	title.css('margin-top', (documentHeight / 2) - (title.height() / 2));
}

function refreshTitle() {
	var link = $('#page h1 #link');
	link.colorbox({
		inline: true,
		href: '#dialog',
		height: '80%',
		width: '80%'
	});
	if(countryOne.ranking < countryTwo.ranking) {
		link.text(link.attr('yes'));
		$('#page h1').addClass('yes');
		$('#favicon').attr('href', 'assets/img/yes.png');
	}
	else {
		link.text(link.attr('no'));
		$('#page h1').addClass('no');
		$('#favicon').attr('href', 'assets/img/no.png');
	}
	centerTitle($(this).height());
	footer.html(countryOne.toString() + '. ' + countryTwo.toString() + '. ' + footer.html());
	console.log(countryOne.toString());
	console.log(countryTwo.toString());
	console.log($('title').text() + ' ' + link.text() + '.');
}