const filter = {
    templateUrl : 'html/filter.html',
    require : 'ngModel',
    bindings : {
        key : '<'
    },
    controller : function(Filters) {
        var $ctrl = this;

        $ctrl.$onInit = () => {
            $ctrl.filters = Filters.forType($ctrl.key);
        }

    }
};

export default filter;
