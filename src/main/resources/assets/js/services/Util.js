MicroMacroApp.factory("Server", function($http){

    return {
        get : function(url, options) {

            var error = options.error || function(){};
            var success = options.success || function(){};
            options.params = options.params || {};

            $http.get(url, options).then(function (data, status) {
                success(data.data);
            }, function (data, status) {
                error(data, status);
            });

        },
        post : function(url, data, options) {

        }

    }

});