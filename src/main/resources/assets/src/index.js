import angular from 'angular';


import 'bootstrap/dist/css/bootstrap.min.css';
import 'angular-ui-grid/ui-grid.min.css';
import 'leaflet/dist/leaflet.css';
import 'nvd3/build/nv.d3.min.css';
import 'leaflet.markercluster/dist/MarkerCluster.css';
import 'leaflet.markercluster/dist/MarkerCluster.Default.css';

// angular imports.

// We can't do this at the moment, it breaks the whole page.
//require('bootstrap');


require('jquery');
require('angular-ui');
require('angular-ui-bootstrap');
require('@uirouter/angularjs');
require('ui-leaflet');
require('leaflet.markercluster');
require('leaflet.timeline');
require('leaflet.heat');
require('leaflet.markercluster.layersupport');
require('leaflet.markercluster.placementstrategies');
require('angular-simple-logger');
require('angular-animate');
require('angular-spinners');
require('angular-nvd3');
require('angular-sanitize');
require('ng-csv');
require('moment');
require('round10').polyfill();


require('./ts/List');
require('./ts/Datum');
require('./ts/Key');
require('./ts/Span');
require('./ts/Spans');
require('./ts/Type');
require('./ts/Types');
require('./ts/Util');
require('./ts/DatumFactory');
 

import {initializeMicromacro} from './MicroMacroApp.js';
import {configureRoutes} from './Routes.js';
import {initializeServices} from './services';
import {initializeComponents} from './components';
import {initializeDirectives} from './directives';

const MicroMacroApp = initializeMicromacro();
console.log(MicroMacroApp);

configureRoutes(MicroMacroApp);
initializeServices(MicroMacroApp);
initializeComponents(MicroMacroApp);
initializeDirectives(MicroMacroApp);

