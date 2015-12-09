var year = 2016;

var countryOne = createDefaultCountry('France');
var countryTwo = createDefaultCountry('Russia');
var footbar = $('#page footer').html();

$(document).ready(function() {
	refreshRankings();
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
		ranking: -1,
		points: -1,
		toString: function() {
			return name + ' ranking : ' + this.ranking + ', points : ' + this.points;
		}
	};
}

function centerTitle(documentHeight) {
	var title = $('#page h1');
	title.css('margin-top', (documentHeight / 2) - (title.height() / 2));
}

function refreshTitle() {
	var title = $('#page h1');
	if(countryOne.ranking < countryTwo.ranking) {
		title.text('Yes');
		title.addClass('yes');
		$('#favicon').attr('href', 'assets/img/yes.png');
	}
	else {
		title.text('No');
		title.addClass('no');
		$('#favicon').attr('href', 'assets/img/no.png');
	}
	centerTitle($(this).height());
	$('#page footer').html(countryOne.toString() + '. ' + countryTwo.toString() + '. ' + footbar);
	console.log(countryOne.toString());
	console.log(countryTwo.toString());
	console.log('Is ' + countryOne.name + ' still ahead of ' + countryTwo.name + ' ? ' + title.text() + '.');
}