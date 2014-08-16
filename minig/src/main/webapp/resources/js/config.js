var app = angular.module("minigApp", ['ngResource', 'ngRoute', 'LocalStorageModule', 'ngSanitize', 'minigTextAngular'])
.constant('API_HOME', 'api/1/')
.constant('DEFAULT_PAGE_SIZE', 20)
.constant('INITIAL_MAILBOX', 'INBOX'); //TODO: INBOX shouldn't be hardcoded

app.config(function($httpProvider, $routeProvider) {
	
    $httpProvider.interceptors.push(function($q, $window, $rootScope, i18nService) {
        return {
            'responseError': function(rejection) {
            	if(rejection.status === 401) {
            		$window.location.reload();
                }
            	if(rejection.status >= 500) {
            		if(typeof rejection.data === 'string') {
            			$rootScope.$broadcast('error', i18nService.resolve(rejection.data));
            		} else {
            			$rootScope.$broadcast('error', i18nService.resolve(rejection.data.message));
            		}
            	}

        		return $q.reject(rejection);
            }
        };
    });
    
    $httpProvider.interceptors.push(function($q) {
        return {
            'request': function(config) {            	
            	config.headers["X-Requested-With"] = "XMLHttpRequest";
                return config || $q.when(config);
            }
        };
    });
    
    $httpProvider.interceptors.push(function($q, $rootScope) {
        return {
            'request': function(config) {
                $rootScope.$broadcast('loading-started');
                return config || $q.when(config);
            },
            'response': function(response) {
                $rootScope.$broadcast('loading-complete');
                return response || $q.when(response);
            }
        };
    });

    $routeProvider
    .when('', {
        redirectTo : '/box'
    })
    .when('/box', {
        templateUrl: "box.jsp",
        controller: 'MailOverviewCtrl'
    })
    .when('/folder', {
        templateUrl: "folder.jsp",
        controller: 'FolderSettingsCtrl'
    })
    .when('/composer', {
        templateUrl: "composer.jsp",
        controller: 'ComposerCtrl',
        reloadOnSearch: false
    })
    .when('/message', {
        templateUrl: "message.jsp",
        controller: 'MessageCtrl'
    })
    .otherwise({redirectTo: '/box'});

});

angular.module('minigTextAngular', ['textAngular'])
.config(['$provide', function($provide) {
    $provide.decorator('taOptions', ['$delegate', function(taOptions){
        taOptions.toolbar = [
            ['bold', 'italics', 'underline', 'ul', 'ol'],
            ['justifyLeft','justifyCenter','justifyRight'],
            []
        ];

        return taOptions;
    }]);

}]);
