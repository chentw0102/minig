
app.filter('timeago', function(timeService) {
	
	return function(date) {
		if(date) {
			try {
				return timeService.humanReadableAbbr(date);
			} catch(e) {
				return "";
			}
		} else {
			return "";
		}
	}
});

app.filter('i18n', function(i18nService) {
	
	return function(text) {
		return i18nService.resolve(text);
	}
});

app.filter('subscribed', function() {
	
	return function(folder) {
		var filtered = [];
		
		angular.forEach(folder, function(folder) {
			if(folder.subscribed) {
				filtered.push(folder);
			}
		});
		
		return filtered;
	}
});

app.filter('displayName', function(i18nService) {
	
	return function(sender) {
		if(sender) {
			if(sender && sender.displayName) {
				return sender.displayName;
			} else {
				sender.email;
			}
		} else {
			return i18nService.resolve("undisclosed recipient");
		}
		
	}
});

app.filter('prettyFolderName', function() {

	return function(folder) {
	    var count = folder.id.split("/").length -2;
        var pretty = "";

        for(i=0;i<count;i++) {
            pretty = pretty + '\u00A0\u00A0'; //"&nbsp;&nbsp;";
        }

        return pretty + folder.name;
	}
});