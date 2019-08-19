import filter from './components/filter';
import literal from './components/literal';
import literals from './components/literals';
import mapConfig from './components/map-config';
import map from './components/map';
import maps from './components/maps';
import queryNotes from './components/query-notes';
import basicNote from './components/basic-note';
import queries from './components/queries';
import queryConfig from './components/query-config';
import queryResult from './components/query-result';
import spanText from './components/span-text';
import summary from './components/summary';
import workspace from './components/workspace';
import workspaces from './components/workspaces';

function initializeComponents(MicroMacroApp) {
    console.log("initializing components");

    MicroMacroApp.component("filter", filter);
    MicroMacroApp.component("literal", literal);
    MicroMacroApp.component("literals", literals);
    MicroMacroApp.component('mapConfig', mapConfig);
    MicroMacroApp.component('map', map);
    MicroMacroApp.component('maps', maps);
    MicroMacroApp.component('queryNotes', queryNotes);
    MicroMacroApp.component('basicNote', basicNote);
    MicroMacroApp.component('queries', queries);
    MicroMacroApp.component('queryConfig', queryConfig);
    MicroMacroApp.component('queryResult', queryResult);
    MicroMacroApp.component('spanText', spanText);
    MicroMacroApp.component('summary', summary);
    MicroMacroApp.component('workspace', workspace);
    MicroMacroApp.component('workspaces', workspaces);

    //MicroMacroApp.component('notes', {
    //    templateUrl : 'html/note.html',
    //    bindings : {
    //        note : '<',
    //        collapsed : '<'
    //    },
    //    controller: function($$scope, $stateParams) {
    //
    //        $scope.height = 200;
    //    }
    //});
}

export {initializeComponents};





