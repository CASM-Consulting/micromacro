MicroMacroApp.factory("Filters", function(Types) {

    var filters = {};

    filters[Types.SPANS.getKlass()] = ["present"];
    filters[Types.LABEL.getKlass()] = ["present", "equals"];
    filters[Types.STRING.getKlass()] = ["present", "equals", "regex"];
    filters[Types.BOOLEAN.getKlass()] = ["present", "equals"];
    filters[Types.LONG.getKlass()] = ["present", "equals", "range"];
    filters[Types.DOUBLE.getKlass()] = ["present", "equals", "range"];
    filters[Types.DATE.getKlass()] = ["present", "equals", "date_range"];
    filters[ypes.DATE2.getKlass()] = ["present", "equals", "date_range"];

    var forType = (type) => {
        if(!type) {
            return [];
        }
        if(typeof type == "string")  {
            return filters[type];
        } else {
            return filters[type.getKlass()];
        }
    }

    return {
        forType : forType
    }
});
